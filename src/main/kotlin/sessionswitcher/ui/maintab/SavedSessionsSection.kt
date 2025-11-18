package sessionswitcher.ui.maintab

import sessionswitcher.SessionSwitcher
import sessionswitcher.sessions.Session
import sessionswitcher.ui.Table
import sessionswitcher.ui.UISection
import sessionswitcher.ui.maintab.tables.SessionsTableModel
import java.awt.BorderLayout
import javax.swing.*

class SavedSessionsSection(private val sessionSwitcher: SessionSwitcher) {
    private val component: JPanel
    val sessionsTableModel = SessionsTableModel(sessionSwitcher.sessions)
    private val table = Table(emptyArray()).also { it.model = sessionsTableModel }

    private val newSessionButton = JButton("New")
    private val editSessionButton = JButton("Edit").also { it.isEnabled = false }
    private val deleteSessionButton = JButton("Delete").also {
        it.isEnabled = false
        it.addActionListener { this.deleteButtonCallback() }
    }
    private val duplicateSessionButton = JButton("Duplicate").also {
        it.isEnabled = false
        it.addActionListener { this.duplicateButtonCallback() }
    }
    private val refreshSessionsButton = JButton("Refresh").also {
        it.addActionListener { this.refreshTable() }
    }
    public fun getComponent(): JPanel {
        return this.component
    }

    public fun refreshTable() {
        sessionsTableModel.fireTableDataChanged()
    }
    init {
        this.refreshTable()
        table.selectionModel.addListSelectionListener { evt ->
            if (evt.valueIsAdjusting) return@addListSelectionListener
            if (table.selectedRow == -1) {
                editSessionButton.isEnabled = false
                deleteSessionButton.isEnabled = false
                duplicateSessionButton.isEnabled = false
            } else {
                editSessionButton.isEnabled = true
                deleteSessionButton.isEnabled = true
                duplicateSessionButton.isEnabled = true
            }
        }

        // Button Panel
        val buttonsPanel = JPanel().also { it ->
            it.layout = BoxLayout(it, BoxLayout.Y_AXIS)
            it.border = BorderFactory.createEmptyBorder(0, 0, 0, 5)
            it.add(JPanel(BorderLayout()).also{p-> p.add(newSessionButton, BorderLayout.PAGE_START)})
            it.add(Box.createVerticalStrut(5))
            it.add(JPanel(BorderLayout()).also{p-> p.add(editSessionButton, BorderLayout.PAGE_START)})
            it.add(Box.createVerticalStrut(5))
            it.add(JPanel(BorderLayout()).also{p-> p.add(deleteSessionButton, BorderLayout.PAGE_START)})
            it.add(Box.createVerticalStrut(5))
            it.add(JPanel(BorderLayout()).also{p-> p.add(duplicateSessionButton, BorderLayout.PAGE_START)})
            it.add(Box.createVerticalStrut(5))
            it.add(JPanel(BorderLayout()).also{p-> p.add(refreshSessionsButton, BorderLayout.PAGE_START)})
        }

        val leftPanel = JPanel(BorderLayout()).also {
            it.add(buttonsPanel, BorderLayout.PAGE_START)
        }
        val outerPanel = JPanel(BorderLayout()).also {
            it.add(leftPanel, BorderLayout.LINE_START)
            it.add(table.withScrollPane(), BorderLayout.CENTER)
        }

        // Add to session
        this.component = UISection("Saved Sessions", null, outerPanel)
    }

    private fun getSelectedSession(): Session? {
        val row = this.table.selectedRow
        if (row == -1) return null
        if (row >= sessionSwitcher.sessions.size) return null

        return this.sessionsTableModel.getAt(row)
    }

    private fun deleteButtonCallback() {
        val session = getSelectedSession()?: return
        this.sessionSwitcher.sessions.deleteSession(session)
        this.refreshTable()
    }

    private fun duplicateButtonCallback() {
        val session = getSelectedSession()?: return
        this.sessionSwitcher.sessions.duplicateSession(session.name)
        this.refreshTable()
    }
}