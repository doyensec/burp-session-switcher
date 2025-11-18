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
        this.tableSection = TableSection("Auto Update Rules", "Automatically update sessions from matching requests and responses",  UpdateRuleTableModel(sessionSwitcher.updateRules))
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
        this.sessionSwitcher.updateRules.remove(item.get())
        tableSection.refreshTable()
    }

    private fun duplicateButtonCallback() {
        val item = tableSection.getSelected()
        if (item.isEmpty) {
            Logger.warning("Duplicate button clicked but no table item selected, row: ${tableSection.table.selectedRow}")
        }
        this.sessionSwitcher.updateRules.add(item.get().copy())
        tableSection.refreshTable()
    }

    private fun editButtonCallback() {
        val oldRule = tableSection.getSelected()
        if (oldRule.isEmpty) {
            Logger.warning("Delete button clicked but no table item selected, row: ${tableSection.table.selectedRow}")
        }
        val newRule = UpdateRuleWindow(sessionSwitcher, oldRule).showDialog()
        if (newRule.isEmpty) return
        val oldIndex = sessionSwitcher.updateRules.indexOf(oldRule.get())
        sessionSwitcher.updateRules.remove(oldRule.get())
        sessionSwitcher.updateRules.add(oldIndex, newRule.get())
        tableSection.refreshTable()
    }

    private fun newButtonCallback() {
        val ruleOptional = UpdateRuleWindow(sessionSwitcher, Optional.empty<UpdateRule>()).showDialog()
        if (ruleOptional.isEmpty) return
        sessionSwitcher.updateRules.add(ruleOptional.get())
        tableSection.refreshTable()
    }
}