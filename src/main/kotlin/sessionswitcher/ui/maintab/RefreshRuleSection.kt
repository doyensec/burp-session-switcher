package sessionswitcher.ui.maintab

import sessionswitcher.Logger
import sessionswitcher.SessionSwitcher
import sessionswitcher.rules.refresher.RefreshRule
import sessionswitcher.ui.maintab.tables.RefreshRuleTableModel
import java.util.*

class RefreshRuleSection(private val sessionSwitcher: SessionSwitcher):
    TableSection("Auto Update Rules", "Automatically update sessions from matching requests and responses",  RefreshRuleTableModel(sessionSwitcher.refreshRules)) {
    // Table model
    val refreshRuleTableModel = RefreshRuleTableModel(sessionSwitcher.refreshRules)

    private fun getSelectedItem(): Optional<RefreshRule> {
        val row = this.table.selectedRow
        if (row == -1) return Optional.empty<RefreshRule>()
        if (row >= sessionSwitcher.sessions.size) return Optional.empty<RefreshRule>()

        return Optional.of((this.tableModel as RefreshRuleTableModel).getAt(row))
    }

    override fun deleteButtonCallback() {
        val item = getSelectedItem()
        if (item.isEmpty) {
            Logger.warning("Delete button clicked but no table item selected, row: ${table.selectedRow}")
        }
        this.sessionSwitcher.refreshRules.remove(item.get())
        refreshRuleTableModel.fireTableDataChanged()
    }

    override fun duplicateButtonCallback() {
        TODO("Not yet implemented")
    }

    override fun editButtonCallback() {
        val oldRule = getSelectedItem()
        if (oldRule.isEmpty) {
            Logger.warning("Delete button clicked but no table item selected, row: ${table.selectedRow}")
        }
        val newRule = RefreshRuleWindow(oldRule).showDialog()
        if (newRule.isEmpty) return
        val oldIndex = sessionSwitcher.refreshRules.indexOf(oldRule.get())
        sessionSwitcher.refreshRules.remove(oldRule.get())
        sessionSwitcher.refreshRules.add(oldIndex, newRule.get())
        refreshRuleTableModel.fireTableDataChanged()
    }

    override fun newButtonCallback() {
        val ruleOptional = RefreshRuleWindow(Optional.empty<RefreshRule>()).showDialog()
        if (ruleOptional.isEmpty) return
        sessionSwitcher.refreshRules.add(ruleOptional.get())
        refreshRuleTableModel.fireTableDataChanged()
    }
}