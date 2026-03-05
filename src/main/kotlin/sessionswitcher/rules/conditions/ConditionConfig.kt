package sessionswitcher.rules.conditions

import burp.api.montoya.persistence.PersistedList
import burp.api.montoya.persistence.PersistedObject
import sessionswitcher.Logger
import sessionswitcher.savestate.CanSaveData
import sessionswitcher.savestate.DeserializerFactory
import java.util.UUID

class ConditionConfig private constructor(
    val operation: String,
    val negativeMatch: Boolean,
    val extraFields: Map<String, String>,
    private val saveStateId: UUID
) : CanSaveData {
    constructor(operation: String, negativeMatch: Boolean, extraFields: Map<String, String>) : this(
        operation,
        negativeMatch,
        extraFields.toMap(),
        UUID.randomUUID()
    )

    companion object {
        val Deserializer = object : DeserializerFactory<ConditionConfig>() {
            override fun deserializeObject(obj: PersistedObject, store: PersistedObject): ConditionConfig {
                val id = UUID.fromString(obj.getString("id"))
                Logger.debug("Deserializing ConditionConfig: $id")
                val operation = obj.getString("operation")
                val negativeMatch = obj.getBoolean("negativeMatch")
                val extraFieldsKeys = obj.getStringList("extraFieldsKeys")
                val extraFields = HashMap<String, String>()
                for (key in extraFieldsKeys) {
                    val value = obj.getString("extraField.$key") ?: continue
                    extraFields[key] = value
                }
                return ConditionConfig(operation, negativeMatch, extraFields.toMap(), id)
            }
        }
    }

    override val saveStateKey: String = "UpdateRule.Condition.Config.${saveStateId}"

    override fun getChildrenObjectsToSave(): Collection<CanSaveData>? = null

    override fun burpSerialize(): PersistedObject {
        val obj = PersistedObject.persistedObject()
        obj.setString("id", saveStateId.toString())
        obj.setString("operation", operation)
        obj.setBoolean("negativeMatch", negativeMatch)

        val extraFieldsKeys = PersistedList.persistedStringList()
        extraFieldsKeys.addAll(extraFields.keys)
        obj.setStringList("extraFieldsKeys", extraFieldsKeys)
        extraFields.forEach { (k, v) -> obj.setString("extraField.$k", v) }

        return obj
    }

    fun copy(): ConditionConfig {
        return ConditionConfig(operation, negativeMatch, extraFields.toMap())
    }
}