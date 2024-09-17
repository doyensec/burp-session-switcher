package sessionswitcher.sessions

import burp.api.montoya.http.message.requests.HttpRequest
import burp.api.montoya.persistence.PersistedObject
import sessionswitcher.Logger
import sessionswitcher.savestate.CanSaveData
import sessionswitcher.savestate.DeserializerFactory
import sessionswitcher.savestate.getChildObjectList
import sessionswitcher.savestate.setChildObjectList
import sessionswitcher.utils.headersMap
import sessionswitcher.utils.host
import sessionswitcher.utils.mergedHeaders
import sessionswitcher.utils.withUpsertedHeaders
import java.util.*


class Session(val name: String, private val id: String = UUID.randomUUID().toString()) : CanSaveData {
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

    class Deserializer(key: String) : DeserializerFactory<Session>(key) {
        override fun burpDeserialize(obj: PersistedObject) {
            val p = Session(obj.getString("name"), obj.getString("id"))
            p.host = obj.getString("host")
            p.cookies = Cookies.fromHeaderValue(obj.getString("cookies"))
            val headersLst = obj.getChildObjectList("headers")
            if (headersLst != null) {
                for (headerObj in headersLst) {
                    p.headers[headerObj.getString("k")] = headerObj.getString("v")
                }
            }
            this.deserialized = p
        }
    }

    private var host: String = ""

    /* Note: this map always hold lowercase header names to prevent issues
        with HTTP/2 requests. Burp should automatically fix the capitalization.
     */
    private val headers: MutableMap<String, String> = LinkedHashMap<String, String>()
    private var cookies = Cookies()

    override val saveStateKey: String
        get() = "Session.$id"

    override fun getChildrenObjectsToSave(): Collection<CanSaveData>? = null

    override fun burpSerialize(): PersistedObject {
        val obj = PersistedObject.persistedObject()
        obj.setString("name", name)
        obj.setString("id", id)
        obj.setString("host", host)
        val headersLst = ArrayList<PersistedObject>(this.headers.size)
        for ((k, v) in this.headers) {
            val headerObj = PersistedObject.persistedObject()
            headerObj.setString("k", k)
            headerObj.setString("v", v)
            headersLst.add(headerObj)
        }
        obj.setChildObjectList("headers", headersLst)
        obj.setString("cookies", this.cookies.toString())
        return obj
    }

    override fun toString(): String {
        return name
    }

    fun getHost(): String {
        return this.host
    }

    fun apply(r: HttpRequest, keepOtherCookies: Boolean = true): Triple<HttpRequest, Pair<List<String>, List<String>>, Pair<List<String>, List<String>>> {
        Logger.info("session.apply: " + this.headers)
        var (output, updatedHeaders, addedHeaders) = r.withUpsertedHeaders(this.headers)

        var updatedCookies = listOf<String>()
        var addedCookies = listOf<String>()
        if (!this.cookies.isEmpty()) {
            val reqCookies = Cookies.fromHttpRequest(r)

            val cookieDiffPair = if (keepOtherCookies) {
                reqCookies.update(this.cookies)
            } else {
                reqCookies.replace(this.cookies)
            }

            updatedCookies = cookieDiffPair.first
            addedCookies = cookieDiffPair.second
            output = output.withUpsertedHeaders(mapOf("cookie" to reqCookies.toString())).first
        }

        return Triple(output, Pair(updatedHeaders, addedHeaders), Pair(updatedCookies, addedCookies))
    }

    fun loadFromRequest(r: HttpRequest) {
        this.host = r.host()
        val reqHeaders = r.mergedHeaders()
        val custom = reqHeaders
            .filter { !EXCLUDED_HEADER_PREFIXES.any { h -> it.name().lowercase().startsWith(h) }  }
        Logger.debug("Saving headers into session: $custom")
        this.headers.clear()
        this.headers.putAll(custom.map { Pair(it.name().lowercase(), it.value()) })

        this.cookies = Cookies.fromHttpRequest(r)
        Logger.debug("Saving cookies into session: ${this.cookies}")
        this.saveToProjectFileAsync()
    }

    fun updateFromRequest(r: HttpRequest, onlyUpdateExistingHeaders: Boolean = true, onlyUpdateExistingCookies: Boolean = true) {
        Logger.debug("Updating session from request")
        // Update headers
        for (header in r.mergedHeaders()) {
            val headerName = header.name().lowercase()
            Logger.debug("Processing header $headerName")
            if (this.headers.containsKey(headerName)) {
                if (this.headers[headerName] != header.value()) {
                    // Update existing header if value is different
                    Logger.debug("Updating header in session ${this.name}: $headerName: ${this.headers[headerName]} -> ${header.value()}")
                    this.headers[headerName] = header.value()
                }
            } else if (!onlyUpdateExistingHeaders && !EXCLUDED_HEADER_PREFIXES.any { h -> headerName.startsWith(h) }) {
                // If new header, filter out common headers
                Logger.debug("Adding new header to session ${this.name}: $headerName: ${header.value()}")
                this.headers[headerName] = header.value()
            }
        }

        // Update cookies
        val newCookies = Cookies.fromHttpRequest(r)
        this.cookies.update(newCookies, onlyUpdateExistingCookies)
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
}
