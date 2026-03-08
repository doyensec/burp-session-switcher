package sessionswitcher.savestate.importexport

import burp.api.montoya.persistence.PersistedObject
import com.google.gson.JsonParser
import sessionswitcher.Logger
import sessionswitcher.SessionSwitcher

object JSONImportExport {
    suspend fun importFromJson(json: String): Boolean {
        val jsonObject = JsonParser.parseString(json).asJsonObject
        // Load the data
        val sessionSwitcher = SessionSwitcher.getInstance()
        val success = sessionSwitcher.tryDeserializeData(JsonPersistedObject(jsonObject))
        Logger.verbose("Triggering save of new data on project file")

        // Save in project file
        val extensionStore = SessionSwitcher.getApi().persistence().extensionData()
        sessionSwitcher.sessions.saveToDataStore(extensionStore, true)
        sessionSwitcher.updateRulesCollection.saveToDataStore(extensionStore, true)

        // Import settings
        if (!jsonObject.has("Settings") || !jsonObject.get("Settings").isJsonObject) return success
        Logger.verbose("Importing settings...")
        val settingsObject = extensionStore.getChildObject("Settings") ?: PersistedObject.persistedObject()
        for (key in jsonObject.getAsJsonObject("Settings").keySet()) {
            val value = jsonObject.getAsJsonObject("Settings").get(key)
            if (!value.isJsonPrimitive) continue
            val primitive = value.asJsonPrimitive
            if (primitive.isString) {
                settingsObject.setString(key, primitive.asString)
            } else if (primitive.isNumber) {
                settingsObject.setInteger(key, primitive.asInt)
            } else if (primitive.isBoolean) {
                settingsObject.setBoolean(key, primitive.asBoolean)
            }
        }
        extensionStore.setChildObject("Settings", settingsObject)
        return success
    }

    fun exportToJson(): String =
        SessionSwitcher
            .getApi()
            .persistence()
            .extensionData()
            .toJsonObject()
            .toString()
}
