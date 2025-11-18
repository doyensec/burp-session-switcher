package sessionswitcher.ui.maintab

import sessionswitcher.Logger
import sessionswitcher.SessionSwitcher
import sessionswitcher.sessions.Session
import sessionswitcher.ui.maintab.tables.SessionsTableModel
import java.util.*

class SavedSessionsSection(private val sessionSwitcher: SessionSwitcher) :
    TableSection("Sessions", "Saved sessions for this project", SessionsTableModel(sessionSwitcher.sessions))
{

    init {
        this.refreshTable()
    }

    private fun getSelectedSession(): Optional<Session> {
        val row = this.table.selectedRow
        if (row == -1) return Optional.empty<Session>()
        if (row >= sessionSwitcher.sessions.size) return Optional.empty<Session>()

        return Optional.of((this.tableModel as SessionsTableModel).getAt(row))
    }

    override fun newButtonCallback() {
        TODO("Not yet implemented")
    }

    override fun deleteButtonCallback() {
        val session = getSelectedSession()
        if (session.isEmpty) {
            Logger.warning("Delete button clicked but no session selected, row: ${table.selectedRow}")
        }
        this.sessionSwitcher.sessions.deleteSession(session.get())
        this.refreshTable()
    }

    override fun duplicateButtonCallback() {
        val session = getSelectedSession()
        if (session.isEmpty) {
            Logger.warning("Delete button clicked but no session selected, row: ${table.selectedRow}")
        }
        this.sessionSwitcher.sessions.duplicateSession(session.get().name)
        this.refreshTable()
    }

    override fun editButtonCallback() {
        TODO("Not yet implemented")
    }
}