package sessionswitcher.rules.autoupdate

import burp.api.montoya.persistence.PersistedObject
import sessionswitcher.SessionSwitcher
import sessionswitcher.savestate.CanSaveAndLoadData
import sessionswitcher.savestate.CanSaveData

class UpdateRulesCollection(private val sessionSwitcher: SessionSwitcher): CanSaveAndLoadData {
    val updateRules = ArrayList<UpdateRule>()

    fun getRules(): ArrayList<UpdateRule> {
        return updateRules
    }

    fun getRuleWithId(id: Int): UpdateRule? {
        return updateRules.find { it.id == id }
    }

    fun addRule(rule: UpdateRule) {
        updateRules.add(rule)
    }

    fun deleteRule(id: Int) {
        val rule = getRuleWithId(id) ?: return
        deleteRule(rule)
    }

    fun deleteRule(rule: UpdateRule) {
        updateRules.remove(rule)
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
        TODO("Not yet implemented")
    }

    override fun burpDeserialize(obj: PersistedObject) {
        TODO("Not yet implemented")
    }
}