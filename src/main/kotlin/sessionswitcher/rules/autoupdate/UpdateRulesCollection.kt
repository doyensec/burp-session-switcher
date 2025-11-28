package sessionswitcher.rules.autoupdate

import burp.api.montoya.persistence.PersistedList
import burp.api.montoya.persistence.PersistedObject
import sessionswitcher.Logger
import sessionswitcher.SessionSwitcher
import sessionswitcher.savestate.CanSaveAndLoadData
import sessionswitcher.savestate.CanSaveData
import sessionswitcher.sessions.Session

class UpdateRulesCollection(private val sessionSwitcher: SessionSwitcher): CanSaveAndLoadData {
    val updateRules = ArrayList<UpdateRule>()

    fun getRules(): ArrayList<UpdateRule> {
        return updateRules
    }

    fun getRequestMatchingRules(): List<UpdateRule> {
        return updateRules.filterNot { it.needsResponse() }
    }
    fun getResponseMatchingRules(): List<UpdateRule> {
        return updateRules.filter { it.needsResponse() }
    }

    fun indexOf(rule: UpdateRule): Int {
        return updateRules.indexOf(rule)
    }

    fun getRuleWithId(id: Int): UpdateRule? {
        return updateRules.find { it.ruleId == id }
    }

    fun addRule(rule: UpdateRule) {
        updateRules.add(rule)
        this.updateChildObjectAsync(rule)
    }

    fun addRule(index: Int, rule: UpdateRule) {
        updateRules.add(index, rule)
        this.updateChildObjectAsync(rule)
    }

    fun deleteRule(id: Int) {
        val rule = getRuleWithId(id) ?: return
        deleteRule(rule)
    }

    fun deleteRule(rule: UpdateRule) {
        updateRules.remove(rule)
        this.deleteChildObjectAsync(rule)
    }

    fun deleteRulesForSession(session: Session) {
        updateRules.filter { it.session == session }.forEach { deleteRule(it) }
    }

    fun deleteRulesForSession(session: String) {
        updateRules.filter { it.session.name == session }.forEach { deleteRule(it) }
    }

    fun duplicateRule(id: Int) {
        val rule = getRuleWithId(id)?: return
        addRule(rule.copy())
    }

    override val saveStateKey: String
        get() = "UpdateRulesCollection"

    override fun getChildrenObjectsToSave(): Collection<CanSaveData>? {
        return this.updateRules
    }

    override fun burpSerialize(): PersistedObject {
        val obj = PersistedObject.persistedObject()
        val rules = PersistedList.persistedStringList()
        for (rule in updateRules) {
            rules.add(rule.saveStateKey)
        }
        obj.setStringList("rules", rules)
        return obj
    }

    override fun burpDeserialize(obj: PersistedObject) {
        val rules = obj.getStringList("rules") ?: return
        Logger.debug("Deserializing ${rules.size} rules")
        val deserializer = UpdateRule.Deserializer(sessionSwitcher)
        for (ruleKey in rules) {
            try {
                val rule = deserializer.deserialize(ruleKey) ?: continue
                this.updateRules.add(rule)
            } catch (e: Exception) {
                Logger.error("Failed deserializing rule: $ruleKey")
                continue
            }
        }
    }
}