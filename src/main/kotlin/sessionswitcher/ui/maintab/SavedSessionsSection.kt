package sessionswitcher.ui.maintab

import sessionswitcher.Logger
import sessionswitcher.SessionSwitcher
import sessionswitcher.sessions.Session
import sessionswitcher.ui.maintab.tables.SessionsTableModel
import javax.swing.JComponent

object SavedSessionsSection
{
    private lateinit var tableSection: TableSection<Session>
    private lateinit var sessionSwitcher: SessionSwitcher

    public fun make(sessionSwitcher: SessionSwitcher): JComponent {
        this.sessionSwitcher = sessionSwitcher
        this.tableSection = TableSection("Sessions", "Saved sessions for this project",
            SessionsTableModel(sessionSwitcher.sessions)
        )
        tableSection.refreshTable()
        tableSection.setNewButtonCallback(this::newButtonCallback)
        tableSection.setEditButtonCallback(this::editButtonCallback)
        tableSection.setDeleteButtonCallback(this::deleteButtonCallback)
        tableSection.setDuplicateButtonCallback(this::duplicateButtonCallback)
        return tableSection.getComponent()
    }

    private fun newButtonCallback() {
        TODO("Not yet implemented")
    }

    private fun deleteButtonCallback() {
        val session = tableSection.getSelected()
        if (session.isEmpty) {
            Logger.warning("Delete button clicked but no session selected, row: ${tableSection.table.selectedRow}")
        }
        this.sessionSwitcher.sessions.deleteSession(session.get())
        tableSection.refreshTable()
    }

    private fun duplicateButtonCallback() {
        val session = tableSection.getSelected()
        if (session.isEmpty) {
            Logger.warning("Delete button clicked but no session selected, row: ${tableSection.table.selectedRow}")
        }
        this.sessionSwitcher.sessions.duplicateSession(session.get().name)
        tableSection.refreshTable()
    }

    private fun editButtonCallback() {
        TODO("Not yet implemented")
    }
}