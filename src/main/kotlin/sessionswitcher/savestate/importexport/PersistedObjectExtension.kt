package sessionswitcher.savestate.importexport

import burp.api.montoya.persistence.PersistedList
import burp.api.montoya.persistence.PersistedObject
import com.google.gson.JsonArray
import com.google.gson.JsonObject

fun PersistedObject.toJsonObject(): JsonObject {
    val json = JsonObject()

    // Primitives
    this.stringKeys().forEach { key -> json.addProperty(key, this.getString(key)) }
    this.integerKeys().forEach { key -> json.addProperty(key, this.getInteger(key)) }
    this.booleanKeys().forEach { key -> json.addProperty(key, this.getBoolean(key)) }
    this.longKeys().forEach { key -> json.addProperty(key, this.getLong(key)) }
    this.shortKeys().forEach { key -> json.addProperty(key, this.getShort(key)) }

    // Lists
    this.stringListKeys().forEach { key -> json.add(key, this.getStringList(key).toJsonArray()) }
    this.integerListKeys().forEach { key -> json.add(key, this.getIntegerList(key).toJsonArray()) }
    this.booleanListKeys().forEach { key -> json.add(key, this.getBooleanList(key).toJsonArray()) }
    this.longListKeys().forEach { key -> json.add(key, this.getLongList(key).toJsonArray()) }
    this.shortListKeys().forEach { key -> json.add(key, this.getShortList(key).toJsonArray()) }

    // Objects
    this.childObjectKeys().forEach { key -> json.add(key, this.getChildObject(key).toJsonObject()) }

    return json
}

inline fun <reified T> PersistedList<T>.toJsonArray(): JsonArray {
    val array = JsonArray(this.size)
    when (T::class) {
        String::class -> this.forEach { array.add(it as String) }
        Int::class -> this.forEach { array.add(it as Int) }
        Boolean::class -> this.forEach { array.add(it as Boolean) }
        Long::class -> this.forEach { array.add(it as Long) }
        Short::class -> this.forEach { array.add(it as Short) }
        else -> throw Exception("Not implemented")
    }
    return array
}
