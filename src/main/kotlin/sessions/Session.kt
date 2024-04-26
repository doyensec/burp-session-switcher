package sessions

import burp.api.montoya.http.message.requests.HttpRequest
import burp.api.montoya.persistence.PersistedObject
import sessions.savestate.DeserializerFactory
import sessions.savestate.SavesDataToProject
import sessions.savestate.getChildObjectList
import sessions.savestate.setChildObjectList
import sessions.utils.withUpsertedHeaders
import java.util.*


class Session(val name: String, val id: String = UUID.randomUUID().toString()) : SavesDataToProject {
    companion object {
        val EXCLUDED_HEADERS = setOf<String>(
            // Keep these lowercase
            "connection",
            "host",
            "content-type",
            "content-length",
            "content-encoding",
            "accept",
            "accept-language",
            "accept-encoding",
            "cache-control",
            "origin"
        )
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

    fun overwrite(headers: Map<String, String>) {
        this.customHeaders.clear()
        this.customHeaders.putAll(headers)
        this.saveToProjectFileAsync()
    }

    override val saveStateKey: String
        get() = "Session.$id"

    override fun getChildrenObjectsToSave(): Collection<SavesDataToProject>? = null

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
        return r.withUpsertedHeaders(this.customHeaders)
    }

    fun loadFromRequest(r: HttpRequest) {
        val reqHeaders = r.headers().associate { header -> header.name() to header.value() }
        val defaultHeaders = HttpRequest.httpRequest().withDefaultHeaders().headers()
            .associate { header -> header.name().lowercase() to header.value() }
        // Keep the header from the request if it's not a default one OR the value is different from the default
        val custom =
            reqHeaders
                .filter { (!defaultHeaders.containsKey(it.key.lowercase())) || (defaultHeaders.containsKey(it.key.lowercase()) && defaultHeaders[it.key] != it.value) }
                .filter { !EXCLUDED_HEADERS.contains(it.key.lowercase())  }
        this.overwrite(custom)
    }
}
