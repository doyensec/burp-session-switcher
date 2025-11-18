package sessionswitcher.ui.maintab

import sessionswitcher.Logger
import sessionswitcher.SessionSwitcher
import sessionswitcher.rules.refresher.RefreshRule
import sessionswitcher.ui.maintab.tables.RefreshRuleTableModel
import java.util.*
import javax.swing.JComponent

object RefreshRuleSection {
    // Table model
    private lateinit var sessionSwitcher: SessionSwitcher
    private lateinit var tableSection: TableSection<RefreshRule>

    public fun make(sessionSwitcher: SessionSwitcher): JComponent {
        this.sessionSwitcher = sessionSwitcher
        this.tableSection = TableSection("Auto Update Rules", "Automatically update sessions from matching requests and responses",  RefreshRuleTableModel(sessionSwitcher.refreshRules))
        tableSection.refreshTable()
        tableSection.setNewButtonCallback(this::newButtonCallback)
        tableSection.setEditButtonCallback(this::editButtonCallback)
        tableSection.setDeleteButtonCallback(this::deleteButtonCallback)
        tableSection.setDuplicateButtonCallback(this::duplicateButtonCallback)
        return tableSection.getComponent()
    }

    private fun deleteButtonCallback() {
        val item = tableSection.getSelected()
        if (item.isEmpty) {
            Logger.warning("Delete button clicked but no table item selected, row: ${tableSection.table.selectedRow}")
        }
        this.sessionSwitcher.refreshRules.remove(item.get())
        tableSection.refreshTable()
    }

    private fun duplicateButtonCallback() {
        TODO("Not yet implemented")
    }

    private fun editButtonCallback() {
        val oldRule = tableSection.getSelected()
        if (oldRule.isEmpty) {
            Logger.warning("Delete button clicked but no table item selected, row: ${tableSection.table.selectedRow}")
        }
        val newRule = RefreshRuleWindow(sessionSwitcher, oldRule).showDialog()
        if (newRule.isEmpty) return
        val oldIndex = sessionSwitcher.refreshRules.indexOf(oldRule.get())
        sessionSwitcher.refreshRules.remove(oldRule.get())
        sessionSwitcher.refreshRules.add(oldIndex, newRule.get())
        tableSection.refreshTable()
    }

    private fun newButtonCallback() {
        val ruleOptional = RefreshRuleWindow(sessionSwitcher, Optional.empty<RefreshRule>()).showDialog()
        if (ruleOptional.isEmpty) return
        sessionSwitcher.refreshRules.add(ruleOptional.get())
        tableSection.refreshTable()
    }
}