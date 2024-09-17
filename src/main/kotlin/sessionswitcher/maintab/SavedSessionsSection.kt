package sessionswitcher.maintab

import sessionswitcher.SessionSwitcher
import sessionswitcher.ui.UISection
import java.awt.BorderLayout
import javax.swing.JButton
import javax.swing.JPanel

class SavedSessionsSection(private val sessionSwitcher: SessionSwitcher) {
    private val component: JPanel
    private val table = SessionsListComponent()

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
        table.update(sessionSwitcher.sessions.getSessions().toList())
    }
    init {
        // Sessions Table Section
        // |- Table
        this.refreshTable()
        // |- Buttons

        val innerSessionTableButtonsPanel = JPanel().also {
            it.add(newSessionButton)
            it.add(editSessionButton)
            it.add(deleteSessionButton)
            it.add(duplicateSessionButton)
            it.add(refreshSessionsButton)
        }
        val sessionTableButtonPanel = JPanel(BorderLayout()).also { it.add(innerSessionTableButtonsPanel, BorderLayout.LINE_START) }
        table.addRowSelectionListener { index: Int->
            if (index == -1) {
                editSessionButton.isEnabled = false
                deleteSessionButton.isEnabled = false
                duplicateSessionButton.isEnabled = false
            } else {
                editSessionButton.isEnabled = true
                deleteSessionButton.isEnabled = true
                duplicateSessionButton.isEnabled = true
            }
        }

        // Add to session
        this.component = UISection("Saved Sessions", null, table, sessionTableButtonPanel)
    }

    private fun deleteButtonCallback() {
        val session = this.table.getSelectedSession() ?: return
        this.sessionSwitcher.sessions.deleteSession(session)
        this.refreshTable()
    }

    private fun duplicateButtonCallback() {
        val session = this.table.getSelectedSession() ?: return
        this.sessionSwitcher.sessions.duplicateSession(session.name)
        this.refreshTable()
    }
}