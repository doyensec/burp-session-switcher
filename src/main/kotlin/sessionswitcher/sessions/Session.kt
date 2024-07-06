package sessionswitcher.sessions

import burp.api.montoya.http.message.requests.HttpRequest
import burp.api.montoya.persistence.PersistedObject
import sessionswitcher.Logger
import sessionswitcher.savestate.CanSaveData
import sessionswitcher.savestate.DeserializerFactory
import sessionswitcher.savestate.getChildObjectList
import sessionswitcher.savestate.setChildObjectList
import sessionswitcher.utils.headersMap
import sessionswitcher.utils.withUpsertedHeaders
import java.util.*


class Session(val name: String, val id: String = UUID.randomUUID().toString()) : CanSaveData {
    companion object {
        val EXCLUDED_HEADER_PREFIXES = setOf<String>(
            // Keep these lowercase
            ":", // HTTP2 headers
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
                    p.customHeaders[headerObj.getString("k")] = headerObj.getString("v")
                }
            }
            this.deserialized = p
        }
    }

    val customHeaders: MutableMap<String, String> = LinkedHashMap<String, String>()

    private fun overwrite(headers: Map<String, String>) {
        this.customHeaders.clear()
        this.customHeaders.putAll(headers)
        this.saveToProjectFileAsync()
    }

    override val saveStateKey: String
        get() = "Session.$id"

    override fun getChildrenObjectsToSave(): Collection<CanSaveData>? = null

    override fun burpSerialize(): PersistedObject {
        val obj = PersistedObject.persistedObject()
        obj.setString("name", name)
        obj.setString("id", id)
        val headersLst = ArrayList<PersistedObject>(this.customHeaders.size)
        for ((k, v) in this.customHeaders) {
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
        Logger.info("session.apply: " + this.customHeaders)
        return r.withUpsertedHeaders(this.customHeaders)
    }

    fun loadFromRequestFiltered(r: HttpRequest) {
        val reqHeaders = r.headersMap()
        val custom = reqHeaders
            .filter { (rh, _) -> !EXCLUDED_HEADER_PREFIXES.any { h -> rh.lowercase().startsWith(h) }  } // This could be replaced with a blocklist instead
        Logger.debug("Saving headers into session: $custom")
        this.overwrite(custom)
    }

    fun loadFromRequestAll(r: HttpRequest) {
        val reqHeaders = r.headersMap().filter { (rh, _) -> !rh.startsWith(":") }
        this.overwrite(reqHeaders)
    }
}
