package sessionswitcher.sessions

import burp.api.montoya.http.message.requests.HttpRequest
import burp.api.montoya.persistence.PersistedObject
import sessionswitcher.Logger
import sessionswitcher.savestate.CanSaveData
import sessionswitcher.savestate.DeserializerFactory
import sessionswitcher.savestate.getChildObjectList
import sessionswitcher.savestate.setChildObjectList
import sessionswitcher.utils.mergedHeaders
import sessionswitcher.utils.withUpsertedHeaders
import java.util.*


class Session(val name: String, val id: String = UUID.randomUUID().toString()) : CanSaveData {
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
            val headersLst = obj.getChildObjectList("headers")
            if (headersLst != null) {
                for (headerObj in headersLst) {
                    p.headers[headerObj.getString("k")] = headerObj.getString("v")
                }
            }
            this.deserialized = p
        }
    }

    private val headers: MutableMap<String, String> = LinkedHashMap<String, String>()
    private val cookies = Cookies()

    override val saveStateKey: String
        get() = "Session.$id"

    override fun getChildrenObjectsToSave(): Collection<CanSaveData>? = null

    override fun burpSerialize(): PersistedObject {
        val obj = PersistedObject.persistedObject()
        obj.setString("name", name)
        obj.setString("id", id)
        val headersLst = ArrayList<PersistedObject>(this.headers.size)
        for ((k, v) in this.headers) {
            val headerObj = PersistedObject.persistedObject()
            headerObj.setString("k", k)
            headerObj.setString("v", v)
            headersLst.add(headerObj)
        }
        obj.setChildObjectList("headers", headersLst)
        return obj
    }

    override fun toString(): String {
        return name
    }

    fun apply(r: HttpRequest): HttpRequest {
        Logger.info("session.apply: " + this.headers)
        return r.withUpsertedHeaders(this.headers)
        // TODO: apply cookies
    }

    fun loadFromRequest(r: HttpRequest) {
        val reqHeaders = r.mergedHeaders()
        val custom = reqHeaders
            .filter { !EXCLUDED_HEADER_PREFIXES.any { h -> it.name().lowercase().startsWith(h) }  }
        Logger.debug("Saving " +
                "headers into session: $custom")
        this.headers.clear()
        this.headers.putAll(custom.map { Pair(it.name(), it.value()) })

        this.cookies.update(Cookies.fromHttpRequest(r))
        this.saveToProjectFileAsync()
    }

    fun sessionMatches(r: HttpRequest) {
        // TODO: implement diff checking between Sessions
    }
}
