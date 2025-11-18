package sessionswitcher.ui.maintab

import sessionswitcher.Logger
import sessionswitcher.SessionSwitcher
import sessionswitcher.rules.refresher.RefreshRule
import sessionswitcher.ui.Table
import sessionswitcher.ui.UISection
import sessionswitcher.ui.maintab.tables.RefreshRuleTableModel
import java.awt.BorderLayout
import java.util.*
import javax.swing.*

class RefreshRuleSection(private val sessionSwitcher: SessionSwitcher) {
    // Table model
    val refreshRuleTableModel = RefreshRuleTableModel(sessionSwitcher.refreshRules)
    val table = Table(emptyArray()).also { it.model = refreshRuleTableModel }

    // UI elements
    val mainPanel = JPanel(BorderLayout())

    // Buttons
    val newButton = JButton("New").also {
        it.addActionListener { newRefreshRule() }
    }
    val editButton = JButton("Edit").also {
        it.isEnabled = false
        it.addActionListener { editButtonCallback() }
    }
    val deleteButton = JButton("Delete").also {
        it.isEnabled = false
        it.addActionListener { deleteButtonCallback() }
    }
    val duplicateButton = JButton("Duplicate").also {
        it.isEnabled = false
        it.addActionListener { duplicateButtonCallback() }
    }

    // Logic

    private fun getSelectedItem(): Optional<RefreshRule> {
        val row = this.table.selectedRow
        if (row == -1) return Optional.empty<RefreshRule>()
        if (row >= sessionSwitcher.sessions.size) return Optional.empty<RefreshRule>()

        return Optional.of(this.refreshRuleTableModel.getAt(row))
    }

    private fun deleteButtonCallback() {
        val item = getSelectedItem()
        if (item.isEmpty) {
            Logger.warning("Delete button clicked but no table item selected, row: ${table.selectedRow}")
        }
        this.sessionSwitcher.refreshRules.remove(item.get())
        refreshRuleTableModel.fireTableDataChanged()
    }

    private fun duplicateButtonCallback() {
        TODO("Not yet implemented")
    }

    private fun editButtonCallback() {
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

    fun newRefreshRule() {
        val ruleOptional = RefreshRuleWindow(Optional.empty<RefreshRule>()).showDialog()
        if (ruleOptional.isEmpty) return
        sessionSwitcher.refreshRules.add(ruleOptional.get())
        refreshRuleTableModel.fireTableDataChanged()
    }

    init {
        val buttonsPanel = JPanel().also { it ->
            it.layout = BoxLayout(it, BoxLayout.Y_AXIS)
            it.border = BorderFactory.createEmptyBorder(0, 0, 0, 5)
            it.add(JPanel(BorderLayout()).also{p-> p.add(newButton, BorderLayout.PAGE_START)})
            it.add(Box.createVerticalStrut(5))
            it.add(JPanel(BorderLayout()).also{p-> p.add(editButton, BorderLayout.PAGE_START)})
            it.add(Box.createVerticalStrut(5))
            it.add(JPanel(BorderLayout()).also{p-> p.add(deleteButton, BorderLayout.PAGE_START)})
            it.add(Box.createVerticalStrut(5))
            it.add(JPanel(BorderLayout()).also{p-> p.add(duplicateButton, BorderLayout.PAGE_START)})
        }

        val middlePanel = JPanel(BorderLayout()).also {
            it.add(buttonsPanel, BorderLayout.PAGE_START)
        }
        mainPanel.add(middlePanel, BorderLayout.LINE_START)
        mainPanel.add(table.withScrollPane(), BorderLayout.CENTER)
    }

    public fun getComponent(): JPanel {
        return UISection("Auto Update Rules", "Automatically update sessions from matching requests and responses", mainPanel)
    }
}