package sessionswitcher.rules.conditions

import burp.api.montoya.persistence.PersistedObject
import sessionswitcher.savestate.CanLoadData
import sessionswitcher.savestate.CanSaveData
import sessionswitcher.savestate.DeserializerFactory
import java.util.*

data class ConditionConfig(public val operation: String, public val pattern: Optional<String>, public val negativeMatch: Boolean): CanSaveData {

    object Deserializer: DeserializerFactory<ConditionConfig>() {
        override val saveStateKey: String
            get() = TODO("Not yet implemented")

        override fun burpDeserialize(obj: PersistedObject) {
            val operation = obj.getString("operation")
            val patternSet = obj.getBoolean("patternSet")
            val pattern = if (patternSet) Optional.of(obj.getString("pattern")) else Optional.empty()
            val negativeMatch = obj.getBoolean("negativeMatch")
            this.des
        }

    }

    override val saveStateKey: String
        get() = "UpdateRule.ConditionConfig.${UUID.randomUUID()}"

    override fun getChildrenObjectsToSave(): Collection<CanSaveData>? = null

    override fun burpSerialize(): PersistedObject {
        val obj = PersistedObject.persistedObject()
        obj.setString("operation", operation)
        obj.setBoolean("patternSet", pattern.isPresent)
        obj.setString("pattern", pattern.orElse(""))
        obj.setBoolean("negativeMatch", negativeMatch)
        return obj
    }
}