package sessionswitcher.savestate.importexport

import burp.api.montoya.core.ByteArray
import burp.api.montoya.http.message.HttpRequestResponse
import burp.api.montoya.http.message.requests.HttpRequest
import burp.api.montoya.http.message.responses.HttpResponse
import burp.api.montoya.persistence.PersistedList
import burp.api.montoya.persistence.PersistedObject
import com.google.gson.JsonObject

/* For deserializing from JSON only, write methods not implemented */
class JsonPersistedObject(val jsonObject: JsonObject): PersistedObject {
    override fun getChildObject(key: String): PersistedObject? {
        val child = jsonObject.get(key) ?: return null
        if (!child.isJsonObject) return null
        return JsonPersistedObject(child.asJsonObject)
    }

    override fun childObjectKeys(): Set<String?> {
        return jsonObject.keySet().filter { jsonObject.get(it).isJsonObject }.toSet()
    }

    override fun getString(key: String): String? {
        val elem = jsonObject.get(key) ?: return null
        if (!elem.isJsonPrimitive || !elem.asJsonPrimitive.isString) return null
        return elem.asString
    }

    override fun stringKeys(): Set<String?>? {
        return jsonObject.keySet().filter { jsonObject.get(it).isJsonPrimitive && jsonObject.getAsJsonPrimitive(it).isString }.toSet()
    }

    override fun getBoolean(key: String): Boolean? {
        val elem = jsonObject.get(key) ?: return null
        if (!elem.isJsonPrimitive || !elem.asJsonPrimitive.isBoolean) return null
        return elem.asBoolean
    }

    override fun booleanKeys(): Set<String?>? {
        return jsonObject.keySet().filter { jsonObject.get(it).isJsonPrimitive && jsonObject.getAsJsonPrimitive(it).isBoolean }.toSet()
    }

    override fun getShort(key: String): Short? {
        val elem = jsonObject.get(key) ?: return null
        if (!elem.isJsonPrimitive || !elem.asJsonPrimitive.isNumber) return null
        return elem.asShort
    }

    override fun shortKeys(): Set<String?> {
        return jsonObject.keySet().filter { jsonObject.get(it).isJsonPrimitive && jsonObject.getAsJsonPrimitive(it).isNumber }.toSet()
    }

    override fun getInteger(key: String): Int? {
        val elem = jsonObject.get(key) ?: return null
        if (!elem.isJsonPrimitive || !elem.asJsonPrimitive.isNumber) return null
        return elem.asInt
    }

    override fun integerKeys(): Set<String?>? {
        return jsonObject.keySet().filter { jsonObject.get(it).isJsonPrimitive && jsonObject.getAsJsonPrimitive(it).isNumber }.toSet()
    }

    override fun getLong(key: String): Long? {
        val elem = jsonObject.get(key) ?: return null
        if (!elem.isJsonPrimitive || !elem.asJsonPrimitive.isNumber) return null
        return elem.asLong
    }

    override fun longKeys(): Set<String?> {
        return jsonObject.keySet().filter { jsonObject.get(it).isJsonPrimitive && jsonObject.getAsJsonPrimitive(it).isNumber }.toSet()
    }

    override fun getStringList(key: String): PersistedList<String?>? {
        val array = jsonObject.get(key) ?: return null
        if (!array.isJsonArray) return null
        val list = PersistedList.persistedStringList()
        for (elem in array.asJsonArray) {
            if (!elem.isJsonPrimitive || !elem.asJsonPrimitive.isString) continue
            list.add(elem.asString)
        }
        return list
    }

    override fun stringListKeys(): Set<String?>? {
        return jsonObject.keySet().filter { jsonObject.get(it).isJsonArray && jsonObject.getAsJsonArray(it).size() > 0 && jsonObject.getAsJsonArray(it)[0].isJsonPrimitive && jsonObject.getAsJsonArray(it)[0].asJsonPrimitive.isString }.toSet()
    }

    /* Non implemented methods */

    override fun getByte(key: String): Byte? = throw NotImplementedError("Not implemented")

    override fun byteKeys(): Set<String?>? = throw NotImplementedError("Not implemented")

    override fun getBooleanList(key: String): PersistedList<Boolean?>? = throw NotImplementedError("Not implemented")

    override fun booleanListKeys(): Set<String?>? = throw NotImplementedError("Not implemented")

    override fun getShortList(key: String): PersistedList<Short?>? = throw NotImplementedError("Not implemented")

    override fun shortListKeys(): Set<String?>? = throw NotImplementedError("Not implemented")

    override fun getIntegerList(key: String): PersistedList<Int?>? = throw NotImplementedError("Not implemented")

    override fun integerListKeys(): Set<String?>? = throw NotImplementedError("Not implemented")

    override fun getLongList(key: String): PersistedList<Long?>? = throw NotImplementedError("Not implemented")

    override fun longListKeys(): Set<String?>?  = throw NotImplementedError("Not implemented")

    override fun setChildObject(key: String, childObject: PersistedObject?) = throw UnsupportedOperationException("Writing to JSON Object is not supported.")

    override fun deleteChildObject(key: String) = throw UnsupportedOperationException("Writing to JSON Object is not supported.")

    override fun setString(key: String, value: String?) = throw UnsupportedOperationException("Writing to JSON Object is not supported.")

    override fun deleteString(key: String) = throw UnsupportedOperationException("Writing to JSON Object is not supported.")

    override fun setBoolean(key: String, value: Boolean) = throw UnsupportedOperationException("Writing to JSON Object is not supported.")

    override fun deleteBoolean(key: String) = throw UnsupportedOperationException("Writing to JSON Object is not supported.")

    override fun setByte(key: String, value: Byte) = throw UnsupportedOperationException("Writing to JSON Object is not supported.")

    override fun deleteByte(key: String) = throw UnsupportedOperationException("Writing to JSON Object is not supported.")
    
    override fun setShort(key: String, value: Short) = throw UnsupportedOperationException("Writing to JSON Object is not supported.")

    override fun deleteShort(key: String) = throw UnsupportedOperationException("Writing to JSON Object is not supported.")

    override fun setInteger(key: String, value: Int) = throw UnsupportedOperationException("Writing to JSON Object is not supported.")

    override fun deleteInteger(key: String) = throw UnsupportedOperationException("Writing to JSON Object is not supported.")

    override fun setLong(key: String, value: Long) = throw UnsupportedOperationException("Writing to JSON Object is not supported.")

    override fun deleteLong(key: String) = throw UnsupportedOperationException("Writing to JSON Object is not supported.")

    override fun getByteArray(key: String): ByteArray? = throw NotImplementedError("Not implemented")

    override fun setByteArray(key: String, value: ByteArray?) = throw UnsupportedOperationException("Writing to JSON Object is not supported.")

    override fun deleteByteArray(key: String) = throw UnsupportedOperationException("Writing to JSON Object is not supported.")

    override fun byteArrayKeys(): Set<String?>? = throw NotImplementedError("Not implemented")

    override fun getHttpRequest(key: String): HttpRequest? = throw NotImplementedError("Not implemented")

    override fun setHttpRequest(key: String, value: HttpRequest?) = throw UnsupportedOperationException("Writing to JSON Object is not supported.")

    override fun deleteHttpRequest(key: String) = throw UnsupportedOperationException("Writing to JSON Object is not supported.")

    override fun httpRequestKeys(): Set<String?>? = throw NotImplementedError("Not implemented")

    override fun getHttpRequestList(key: String): PersistedList<HttpRequest?>? = throw NotImplementedError("Not implemented")

    override fun setHttpRequestList(
        key: String,
        value: PersistedList<HttpRequest?>?
    ) = throw UnsupportedOperationException("Writing to JSON Object is not supported.")

    override fun deleteHttpRequestList(key: String) = throw UnsupportedOperationException("Writing to JSON Object is not supported.")

    override fun httpRequestListKeys(): Set<String?>? = throw NotImplementedError("Not implemented")

    override fun getHttpResponse(key: String): HttpResponse? = throw NotImplementedError("Not implemented")

    override fun setHttpResponse(key: String, value: HttpResponse?) = throw UnsupportedOperationException("Writing to JSON Object is not supported.")

    override fun deleteHttpResponse(key: String) = throw UnsupportedOperationException("Writing to JSON Object is not supported.")

    override fun httpResponseKeys(): Set<String?>? = throw NotImplementedError("Not implemented")

    override fun getHttpResponseList(key: String): PersistedList<HttpResponse?>? = throw NotImplementedError("Not implemented")

    override fun setHttpResponseList(
        key: String,
        value: PersistedList<HttpResponse?>?
    ) = throw UnsupportedOperationException("Writing to JSON Object is not supported.")

    override fun deleteHttpResponseList(key: String) = throw UnsupportedOperationException("Writing to JSON Object is not supported.")

    override fun httpResponseListKeys(): Set<String?>? = throw NotImplementedError("Not implemented")

    override fun getHttpRequestResponse(key: String): HttpRequestResponse? = throw NotImplementedError("Not implemented")

    override fun setHttpRequestResponse(
        key: String,
        value: HttpRequestResponse?
    ) = throw UnsupportedOperationException("Writing to JSON Object is not supported.")

    override fun deleteHttpRequestResponse(key: String) = throw UnsupportedOperationException("Writing to JSON Object is not supported.")

    override fun httpRequestResponseKeys(): Set<String?>? = throw NotImplementedError("Not implemented")

    override fun getHttpRequestResponseList(key: String): PersistedList<HttpRequestResponse?>? = throw NotImplementedError("Not implemented")

    override fun setHttpRequestResponseList(
        key: String,
        value: PersistedList<HttpRequestResponse?>?
    ) = throw UnsupportedOperationException("Writing to JSON Object is not supported.")

    override fun deleteHttpRequestResponseList(key: String) = throw UnsupportedOperationException("Writing to JSON Object is not supported.")

    override fun httpRequestResponseListKeys(): Set<String?>? = throw NotImplementedError("Not implemented")

    override fun setBooleanList(
        key: String,
        value: PersistedList<Boolean?>?
    ) = throw UnsupportedOperationException("Writing to JSON Object is not supported.")

    override fun deleteBooleanList(key: String) = throw UnsupportedOperationException("Writing to JSON Object is not supported.")
    

    override fun setShortList(key: String, value: PersistedList<Short?>?) = throw UnsupportedOperationException("Writing to JSON Object is not supported.")

    override fun deleteShortList(key: String) = throw UnsupportedOperationException("Writing to JSON Object is not supported.")
    

    override fun setIntegerList(key: String, value: PersistedList<Int?>?) = throw UnsupportedOperationException("Writing to JSON Object is not supported.")

    override fun deleteIntegerList(key: String) = throw UnsupportedOperationException("Writing to JSON Object is not supported.")


    override fun setLongList(key: String, value: PersistedList<Long?>?) = throw UnsupportedOperationException("Writing to JSON Object is not supported.")

    override fun deleteLongList(key: String) = throw UnsupportedOperationException("Writing to JSON Object is not supported.")
    
    override fun setStringList(
        key: String,
        value: PersistedList<String?>?
    ) = throw UnsupportedOperationException("Writing to JSON Object is not supported.")

    override fun deleteStringList(key: String) = throw UnsupportedOperationException("Writing to JSON Object is not supported.")

    override fun getByteArrayList(key: String): PersistedList<ByteArray?>? = throw NotImplementedError("Not implemented")

    override fun setByteArrayList(
        key: String,
        value: PersistedList<ByteArray?>?
    ) = throw UnsupportedOperationException("Writing to JSON Object is not supported.")

    override fun deleteByteArrayList(key: String) = throw UnsupportedOperationException("Writing to JSON Object is not supported.")

    override fun byteArrayListKeys(): Set<String?>? = throw NotImplementedError("Not implemented")
}