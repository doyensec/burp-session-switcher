package sessionswitcher.sessions

import burp.api.montoya.http.message.requests.HttpRequest
import burp.api.montoya.persistence.PersistedObject
import sessionswitcher.Logger
import sessionswitcher.SessionSwitcher
import sessionswitcher.savestate.CanSaveData
import sessionswitcher.savestate.DeserializerFactory
import sessionswitcher.savestate.getChildObjectList
import sessionswitcher.savestate.setChildObjectList
import sessionswitcher.utils.*
import java.time.Instant
import java.util.*


class Session private constructor(val name: String, private val id: String) : CanSaveData {
    companion object {
        val EXCLUDED_HEADER_PREFIXES = setOf<String>(
            // Keep these lowercase
            ":", // HTTP2 headers
            "cookie", // Handled separately
            "connection",
            "sec-",
            "priority",
            "accept",
            "cache",
            "content",
            "host",
            "user-agent",
            "referer",
            "upgrade",
            "if-",
            "access-",
            "date",
            "expect",
            "forwarded",
            "http2",
            "max-forwards",
            "pragma",
            "proxy-",
            "range",
            "te",
            "trailer",
            "transfer-",
            "via",
            "warning"
        )

        fun isValidName(name: String): Boolean {
            return name.isNotEmpty() && !name.contains(Regex("[^A-Za-z0-9._-]"))
        }
    }

    constructor(name: String) : this(name, UUID.randomUUID().toString())

    // Duplication constructor
    constructor(name: String, old: Session) : this(name) {
        this.host = old.host
        this.headers.putAll(old.headers)
        this.cookies = Cookies.duplicate(old.cookies)
    }

    private var host: String = ""

    /* Note: this map always holds lowercase header names to prevent issues
        with HTTP/2 requests. Burp should automatically fix the capitalization.
     */
    private val headers: MutableMap<String, String> = LinkedHashMap<String, String>()
    private var cookies = Cookies()

    /*
    Some metadata about the last time this session was updated
     */
    public enum class LAST_UPDATE_TYPE(val description: String) {
        CREATION("Creation"),
        MANUAL_EDIT("Manual (Edit Menu)"),
        MANUAL_REQUEST("Manual (From Request)"),
        UPDATE_RULE("Update rule");

        override fun toString(): String = description
    }

    public var lastUpdatedAt: Instant = Instant.now()
        private set
    public var lastUpdatedBy: LAST_UPDATE_TYPE = LAST_UPDATE_TYPE.CREATION
        private set
    public var lastUpdatedRuleId: Int? = null
        private set

    public fun setLastUpdateReason(reason: LAST_UPDATE_TYPE, ruleId: Int? = null) {
        if (reason != LAST_UPDATE_TYPE.UPDATE_RULE && ruleId != null) throw IllegalArgumentException("Cannot set rule ID for reason other than UPDATE_RULE")
        this.lastUpdatedBy = reason
        this.lastUpdatedRuleId = ruleId
    }

    override fun toString(): String {
        return name
    }

    fun getHost(): String {
        return this.host
    }

    fun apply(r: HttpRequest): Triple<HttpRequest, Pair<List<String>, List<String>>, Pair<List<String>, List<String>>> {
        return apply(r, SessionSwitcher.getInstance().settings.cookiesInjectMode.get())
    }

    fun apply(r: HttpRequest, cookiesInjectMode: CookiesInjectMode): Triple<HttpRequest, Pair<List<String>, List<String>>, Pair<List<String>, List<String>>> {
        Logger.debug("session.apply: " + this.headers)
        var (output, updatedHeaders, addedHeaders) = r.withHeaders(this.headers)

        var updatedCookies = listOf<String>()
        var addedCookies = listOf<String>()
        Logger.debug("Cookies in this Session: " + this.cookies)
        if (!this.cookies.isEmpty()) {
            val reqCookies = Cookies.fromHttpRequest(r)
            Logger.debug("Cookies in the Request: $reqCookies")

            Logger.debug("Cookies Inject Mode: $cookiesInjectMode")
            val cookieDiffPair = when (cookiesInjectMode) {
                CookiesInjectMode.MIRROR -> reqCookies.replace(this.cookies)
                CookiesInjectMode.ADD_ALL -> reqCookies.update(this.cookies, onlyUpdateExisting = false)
                CookiesInjectMode.UPDATE_EXISTING -> reqCookies.update(this.cookies, onlyUpdateExisting = true)
                CookiesInjectMode.NOOP -> Pair(listOf(), listOf())
            }

            Logger.debug("Cookies in the output: $reqCookies")

            updatedCookies = cookieDiffPair.first
            addedCookies = cookieDiffPair.second
            output = output.withCookies(reqCookies)
        }

        return Triple(output, Pair(updatedHeaders, addedHeaders), Pair(updatedCookies, addedCookies))
    }

    /*
    Loads all the info from a request into this session. Discards any previous data.
     */
    fun loadFromRequest(r: HttpRequest) {
        this.host = r.host()
        this.updateFromRequest(r, CookiesUpdateMode.MIRROR, HeadersUpdateMode.MIRROR)
        this.saveToProjectFileAsync()
    }

    /*
    Updates stored info from a request.
     */

    private fun updateHeaders(r: HttpRequest, headersUpdateMode: HeadersUpdateMode) {
        if (headersUpdateMode == HeadersUpdateMode.NOOP) return

        if (headersUpdateMode == HeadersUpdateMode.MIRROR || headersUpdateMode == HeadersUpdateMode.ADD_ALL) {
            // If it's MIRROR mode, just clear all the saved headers
            if (headersUpdateMode == HeadersUpdateMode.MIRROR) this.headers.clear()

            // Filter useless headers
            val filtered = r.mergedHeaders()
                .filter { !EXCLUDED_HEADER_PREFIXES.any { h -> it.name().lowercase().startsWith(h) }  }

            // Add the filtered headers
            this.headers.putAll(filtered.map { Pair(it.name().lowercase(), it.value()) })
        } else if (headersUpdateMode == HeadersUpdateMode.UPDATE_EXISTING) {
            for (header in r.mergedHeaders()) {
                val headerName = header.name().lowercase()
                Logger.debug("Processing header $headerName")
                if (this.headers.containsKey(headerName)) {
                    if (this.headers[headerName] != header.value()) {
                        // Update existing header if value is different
                        Logger.debug("Updating header in session ${this.name}: $headerName: ${this.headers[headerName]} -> ${header.value()}")
                        this.headers[headerName] = header.value()
                    }
                }
            }
        }
    }

    private fun updateCookies(r: HttpRequest, cookiesUpdateMode: CookiesUpdateMode) {
        when (cookiesUpdateMode) {
            CookiesUpdateMode.MIRROR -> {
                this.cookies = Cookies.fromHttpRequest(r)
            }
            CookiesUpdateMode.ADD_ALL -> {
                this.cookies.update(Cookies.fromHttpRequest(r), onlyUpdateExisting = false)
            }
            CookiesUpdateMode.UPDATE_EXISTING -> {
                this.cookies.update(Cookies.fromHttpRequest(r), onlyUpdateExisting = true)
            }
            CookiesUpdateMode.NOOP -> return
        }
    }

    fun updateFromRequest(r: HttpRequest, cookiesUpdateMode: CookiesUpdateMode, headersUpdateMode: HeadersUpdateMode) {
        Logger.debug("Updating session from request")
        this.updateHeaders(r, headersUpdateMode)
        this.updateCookies(r, cookiesUpdateMode)

        this.lastUpdatedAt = Instant.now()
        this.lastUpdatedBy = LAST_UPDATE_TYPE.MANUAL_REQUEST

        this.saveToProjectFileAsync()
    }

    /*
    Check if all headers & cookies contained in this session are already applied to a request
     */
    fun matchesRequest(httpRequest: HttpRequest): Boolean {
        // Check headers
        for (header in this.headers) {
            if (httpRequest.headersMap()[header.key] != header.value) {
                return false
            }
        }

        // Check cookies
        val otherCookies = Cookies.fromHttpRequest(httpRequest)
        return otherCookies.contains(this.cookies)
    }

    /*
    Project saving stuff
     */

    class Deserializer(key: String) : DeserializerFactory<Session>(key) {
        override fun burpDeserialize(obj: PersistedObject) {
            // Basic data
            val p = Session(obj.getString("name"), obj.getString("id"))
            p.host = obj.getString("host")

            // Cookies
            p.cookies = Cookies.fromHeaderValue(obj.getString("cookies"))

            // Headers
            val headersLst = obj.getChildObjectList("headers")
            if (headersLst != null) {
                for (headerObj in headersLst) {
                    p.headers[headerObj.getString("k")] = headerObj.getString("v")
                }
            }

            // Last update info
            if (obj.getLong("lastUpdatedAt") != null) {
                p.lastUpdatedAt = Instant.ofEpochSecond(obj.getLong("lastUpdatedAt"))
                p.lastUpdatedBy = LAST_UPDATE_TYPE.entries[obj.getInteger("lastUpdatedFrom")]
                val lastUpdatedRuleId = obj.getInteger("lastUpdatedRuleId")
                if (lastUpdatedRuleId != null && lastUpdatedRuleId != -1) {
                    p.lastUpdatedRuleId = lastUpdatedRuleId
                } else {
                    p.lastUpdatedRuleId = null
                }
            }

            this.deserialized = p
        }
    }

    override val saveStateKey: String
        get() = "Session.$id"

    override fun getChildrenObjectsToSave(): Collection<CanSaveData>? = null

    override fun burpSerialize(): PersistedObject {
        val obj = PersistedObject.persistedObject()

        // Basic data
        obj.setString("name", name)
        obj.setString("id", id)
        obj.setString("host", host)

        // Headers
        val headersLst = ArrayList<PersistedObject>(this.headers.size)
        for ((k, v) in this.headers) {
            val headerObj = PersistedObject.persistedObject()
            headerObj.setString("k", k)
            headerObj.setString("v", v)
            headersLst.add(headerObj)
        }
        obj.setChildObjectList("headers", headersLst)

        // Cookies
        obj.setString("cookies", this.cookies.toString())

        // Last update info
        obj.setLong("lastUpdatedAt", lastUpdatedAt.epochSecond)
        obj.setInteger("lastUpdatedFrom", lastUpdatedBy.ordinal)
        obj.setInteger("lastUpdatedRuleId", lastUpdatedRuleId ?: -1)
        return obj
    }
}
