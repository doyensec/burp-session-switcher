package sessionswitcher.rules.conditions

import burp.api.montoya.persistence.PersistedObject
import sessionswitcher.Logger
import sessionswitcher.savestate.CanSaveData
import sessionswitcher.savestate.DeserializerFactory
import java.util.*

class ConditionConfig private constructor(val operation: String, val pattern: Optional<String>, val negativeMatch: Boolean, private val saveStateId: UUID): CanSaveData {
    constructor(operation: String, pattern: Optional<String>,negativeMatch: Boolean) : this(operation, pattern, negativeMatch, UUID.randomUUID())

    companion object {
        val Deserializer = object: DeserializerFactory<ConditionConfig>() {
            override fun deserializeObject(obj: PersistedObject): ConditionConfig {
                Logger.debug("Deserializing ConditionConfig: $obj")
                val id = UUID.fromString(obj.getString("id"))
                val operation = obj.getString("operation")
                val patternSet = obj.getBoolean("patternSet")
                val pattern = if (patternSet) Optional.of(obj.getString("pattern")) else Optional.empty()
                val negativeMatch = obj.getBoolean("negativeMatch")
                return ConditionConfig(operation, pattern, negativeMatch, id)
            }
        }
    }

    override val saveStateKey: String = "UpdateRule.Condition.Config.${saveStateId}"

    override fun getChildrenObjectsToSave(): Collection<CanSaveData>? = null

    override fun burpSerialize(): PersistedObject {
        val obj = PersistedObject.persistedObject()
        obj.setString("id", saveStateId.toString())
        obj.setString("operation", operation)
        obj.setBoolean("patternSet", pattern.isPresent)
        obj.setString("pattern", pattern.orElse(""))
        obj.setBoolean("negativeMatch", negativeMatch)
        return obj
    }

    fun copy(): ConditionConfig {
        return ConditionConfig(operation, pattern, negativeMatch)
    }
}