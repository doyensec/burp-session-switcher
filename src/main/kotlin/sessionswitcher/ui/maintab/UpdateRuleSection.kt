package sessionswitcher.ui.maintab

import sessionswitcher.Logger
import sessionswitcher.SessionSwitcher
import sessionswitcher.rules.autoupdate.UpdateRule
import sessionswitcher.ui.maintab.tables.UpdateRuleTableModel
import java.util.*
import javax.swing.JComponent

object UpdateRuleSection {
    // Table model
    private lateinit var sessionSwitcher: SessionSwitcher
    private lateinit var tableSection: TableSection<UpdateRule>

    public fun make(sessionSwitcher: SessionSwitcher): JComponent {
        this.sessionSwitcher = sessionSwitcher
        this.tableSection = TableSection("Auto Update Rules", "Automatically update sessions from matching requests and responses",  UpdateRuleTableModel(sessionSwitcher.updateRulesCollection.updateRules))
        tableSection.refreshTable()
        tableSection.setNewButtonCallback(this::newButtonCallback)
        tableSection.setEditButtonCallback(this::editButtonCallback)
        tableSection.setDeleteButtonCallback(this::deleteButtonCallback)
        tableSection.setDuplicateButtonCallback(this::duplicateButtonCallback)
        tableSection.table.columnModel.getColumn(0).maxWidth = 30 // For ID column
        return tableSection.getComponent()
    }

    private fun deleteButtonCallback() {
        val item = tableSection.getSelected()
        if (item.isEmpty) {
            Logger.warning("Delete button clicked but no table item selected, row: ${tableSection.table.selectedRow}")
        }
        this.sessionSwitcher.updateRulesCollection.deleteRule(item.get())
        tableSection.refreshTable()
    }

    private fun duplicateButtonCallback() {
        val item = tableSection.getSelected()
        if (item.isEmpty) {
            Logger.warning("Duplicate button clicked but no table item selected, row: ${tableSection.table.selectedRow}")
        }
        this.sessionSwitcher.updateRulesCollection.addRule(item.get().copy())
        tableSection.refreshTable()
    }

    private fun editButtonCallback() {
        val oldRule = tableSection.getSelected()
        if (oldRule.isEmpty) {
            Logger.warning("Delete button clicked but no table item selected, row: ${tableSection.table.selectedRow}")
        }
        val newRule = UpdateRuleWindow(sessionSwitcher, oldRule).showDialog()
        if (newRule.isEmpty) return
        val oldIndex = sessionSwitcher.updateRulesCollection.indexOf(oldRule.get())
        sessionSwitcher.updateRulesCollection.deleteRule(oldRule.get())
        sessionSwitcher.updateRulesCollection.addRule(oldIndex, newRule.get())
        tableSection.refreshTable()
    }

    private fun newButtonCallback() {
        val ruleOptional = UpdateRuleWindow(sessionSwitcher, Optional.empty<UpdateRule>()).showDialog()
        if (ruleOptional.isEmpty) return
        sessionSwitcher.updateRulesCollection.addRule(ruleOptional.get())
        tableSection.refreshTable()
    }
}