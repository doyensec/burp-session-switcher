package sessionswitcher.rules.autoupdate

import burp.api.montoya.persistence.PersistedList
import burp.api.montoya.persistence.PersistedObject
import sessionswitcher.Logger
import sessionswitcher.SessionSwitcher
import sessionswitcher.savestate.CanSaveAndLoadData
import sessionswitcher.savestate.CanSaveData
import sessionswitcher.sessions.Session

class UpdateRulesCollection(
    private val sessionSwitcher: SessionSwitcher,
) : CanSaveAndLoadData {
    val updateRules = ArrayList<UpdateRule>()

    fun getRequestMatchingRules(): List<UpdateRule> = updateRules.filterNot { it.needsResponse() }

    fun getResponseMatchingRules(): List<UpdateRule> = updateRules.filter { it.needsResponse() }

    fun indexOf(rule: UpdateRule): Int = updateRules.indexOf(rule)

    fun getRuleWithId(id: Int): UpdateRule? = updateRules.find { it.ruleId == id }

    fun addRule(rule: UpdateRule) {
        updateRules.add(rule)
        this.updateChildObjectInProjectFileAsync(rule)
    }

    fun addRule(
        index: Int,
        rule: UpdateRule,
    ) {
        updateRules.add(index, rule)
        this.updateChildObjectInProjectFileAsync(rule)
    }

    fun deleteRule(id: Int) {
        val rule = getRuleWithId(id) ?: return
        deleteRule(rule)
    }

    fun deleteRule(rule: UpdateRule) {
        updateRules.remove(rule)
        this.deleteChildObjectFromProjectFileAsync(rule)
    }

    fun deleteRulesForSession(session: Session) {
        updateRules.filter { it.session == session }.forEach { deleteRule(it) }
    }

    fun deleteRulesForSession(session: String) {
        updateRules.filter { it.session.name == session }.forEach { deleteRule(it) }
    }

    fun duplicateRule(id: Int) {
        val rule = getRuleWithId(id) ?: return
        addRule(rule.copy())
    }

    override val saveStateKey: String
        get() = "UpdateRulesCollection"

    override fun getChildObjectsToSave(): Collection<CanSaveData> = this.updateRules

    override fun burpSerialize(obj: PersistedObject): PersistedObject {
        val rules = PersistedList.persistedStringList()
        for (rule in updateRules) {
            rules.add(rule.saveStateKey)
        }
        obj.setStringList("rules", rules)
        return obj
    }

    override fun burpDeserialize(obj: PersistedObject): Boolean {
        val rules = obj.getStringList("rules") ?: return true
        Logger.debug("Deserializing ${rules.size} rules")
        if (rules.isEmpty()) return true

        val deserializer = UpdateRule.Deserializer(sessionSwitcher)
        var atLeastOneLoadedSuccessfully = false
        for (ruleKey in rules) {
            try {
                val rule = deserializer.deserialize(ruleKey, obj) ?: continue
                this.updateRules.add(rule)
                atLeastOneLoadedSuccessfully = true
            } catch (_: Exception) {
                Logger.error("Failed deserializing rule: $ruleKey")
                continue
            }
        }
        return atLeastOneLoadedSuccessfully
    }
}
