package sessionswitcher.ui.maintab

import sessionswitcher.Logger
import sessionswitcher.SessionSwitcher
import sessionswitcher.rules.autoupdate.UpdateRule
import sessionswitcher.ui.tables.UpdateRuleTableModel
import java.util.*
import javax.swing.JButton
import javax.swing.JComponent
import javax.swing.event.ListSelectionEvent

object UpdateRuleSection {
    // Table model
    private lateinit var sessionSwitcher: SessionSwitcher
    private lateinit var tableSection: TableSection<UpdateRule>

    private fun deleteButtonCallback() {
        val item = tableSection.getSelected()
        if (item.isEmpty) {
            Logger.warning("Delete button clicked but no table item selected, row: ${tableSection.table.selectedRow}")
            return
        }
        this.sessionSwitcher.updateRulesCollection.deleteRule(item.get())
        tableSection.refreshTable()
    }

    private fun duplicateButtonCallback() {
        val item = tableSection.getSelected()
        if (item.isEmpty) {
            Logger.warning("Duplicate button clicked but no table item selected, row: ${tableSection.table.selectedRow}")
            return
        }
        this.sessionSwitcher.updateRulesCollection.addRule(item.get().copy())
        tableSection.refreshTable()
    }

    private fun editButtonCallback() {
        val oldRule = tableSection.getSelected()
        if (oldRule.isEmpty) {
            Logger.warning("Delete button clicked but no table item selected, row: ${tableSection.table.selectedRow}")
            return
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

    // Up and Down buttons
    val upButton = JButton("Up").also {
        it.isEnabled = false
        it.addActionListener { upButtonCallback() }
    }
    val downButton = JButton("Down").also {
        it.isEnabled = false
        it.addActionListener { downButtonCallback() }
    }

    private fun selectionListener(e: ListSelectionEvent) {
        if (e.valueIsAdjusting) return
        val row = tableSection.table.selectedRow
        when (row) {
            -1 -> {
                upButton.isEnabled = false
                downButton.isEnabled = false
            }

            0 -> {
                upButton.isEnabled = false
                downButton.isEnabled = true
            }

            tableSection.table.rowCount - 1 -> {
                upButton.isEnabled = true
                downButton.isEnabled = false
            }

            else -> {
                upButton.isEnabled = true
                downButton.isEnabled = true
            }
        }
    }

    private fun upButtonCallback() {
        val item = tableSection.getSelected()
        if (item.isEmpty) {
            Logger.warning("Up button clicked but no table item selected, row: ${tableSection.table.selectedRow}")
            return
        }
        val rules = sessionSwitcher.updateRulesCollection.updateRules
        val index = rules.indexOf(item.get())
        if (index == 0) return
        val itemBefore = rules[index - 1]
        rules[index - 1] = item.get()
        rules[index] = itemBefore
        tableSection.refreshTable()
        sessionSwitcher.updateRulesCollection.saveToProjectFileAsync(false)
    }

    private fun downButtonCallback() {
        val item = tableSection.getSelected()
        if (item.isEmpty) {
            Logger.warning("Down button clicked but no table item selected, row: ${tableSection.table.selectedRow}")
            return
        }
        val rules = sessionSwitcher.updateRulesCollection.updateRules
        val index = rules.indexOf(item.get())
        if (index == rules.size - 1) return
        val itemAfter = rules[index + 1]
        rules[index + 1] = item.get()
        rules[index] = itemAfter
        tableSection.refreshTable()
        sessionSwitcher.updateRulesCollection.saveToProjectFileAsync(false)
    }

    fun make(sessionSwitcher: SessionSwitcher): JComponent {
        this.sessionSwitcher = sessionSwitcher
        this.tableSection = TableSection(
            "Auto Update Rules",
            "Automatically update sessions from matching requests and responses",
            UpdateRuleTableModel(sessionSwitcher.updateRulesCollection.updateRules),
            otherButtons = arrayOf(upButton, downButton),
            tableHeight = 15
        )
        tableSection.refreshTable()
        tableSection.setNewButtonCallback(this::newButtonCallback)
        tableSection.setEditButtonCallback(this::editButtonCallback)
        tableSection.setDeleteButtonCallback(this::deleteButtonCallback)
        tableSection.setDuplicateButtonCallback(this::duplicateButtonCallback)
        tableSection.table.columnModel.getColumn(0).maxWidth = 30 // For ID column
        tableSection.table.selectionModel.addListSelectionListener { this.selectionListener(it) }
        return tableSection.getComponent()
    }
}