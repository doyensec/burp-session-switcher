package sessionswitcher.rules.conditions

import burp.api.montoya.persistence.PersistedObject
import sessionswitcher.savestate.CanSaveData
import sessionswitcher.savestate.DeserializerFactory
import java.util.*

data class ConditionConfig(public val operation: String, public val pattern: Optional<String>, public val negativeMatch: Boolean, private val saveStateId: UUID): CanSaveData {
    constructor(operation: String, pattern: Optional<String>,negativeMatch: Boolean) : this(operation, pattern, negativeMatch, UUID.randomUUID())

    companion object {
        val Deserializer = object: DeserializerFactory<ConditionConfig>() {
            override fun deserializeObject(obj: PersistedObject): ConditionConfig {
                val id = UUID.fromString(obj.getString("id"))
                val operation = obj.getString("operation")
                val patternSet = obj.getBoolean("patternSet")
                val pattern = if (patternSet) Optional.of(obj.getString("pattern")) else Optional.empty()
                val negativeMatch = obj.getBoolean("negativeMatch")
                return ConditionConfig(operation, pattern, negativeMatch, id)
            }
        }
    }


    override val saveStateKey: String
        get() = "UpdateRule.Condition.ConditionConfig.${UUID.randomUUID()}"

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
}