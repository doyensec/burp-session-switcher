package sessionswitcher.ui.maintab

import sessionswitcher.Logger
import sessionswitcher.SessionSwitcher
import sessionswitcher.sessions.Session
import sessionswitcher.sessions.SessionsListUpdateListener
import sessionswitcher.ui.SaveSessionDialog
import sessionswitcher.ui.SessionEditWindow
import sessionswitcher.ui.tables.SessionsTableModel
import java.util.*
import javax.swing.JComponent

object SavedSessionsSection : SessionsListUpdateListener {
    private lateinit var tableSection: TableSection<Session>
    private lateinit var sessionSwitcher: SessionSwitcher

    fun make(sessionSwitcher: SessionSwitcher): JComponent {
        this.sessionSwitcher = sessionSwitcher
        this.tableSection = TableSection(
            "Sessions", "Saved sessions for this project",
            SessionsTableModel(sessionSwitcher.sessions),
            tableHeight = 20
        )
        tableSection.refreshTable()
        tableSection.setNewButtonCallback(this::newButtonCallback)
        tableSection.setEditButtonCallback(this::editButtonCallback)
        tableSection.setDeleteButtonCallback(this::deleteButtonCallback)
        tableSection.setDuplicateButtonCallback(this::duplicateButtonCallback)
        tableSection.table.autoCreateRowSorter = true
        return tableSection.getComponent()
    }

    private fun newButtonCallback() {
        val session = SessionEditWindow(sessionSwitcher, Optional.empty<Session>()).showDialog()
        if (session.isPresent) {
            tableSection.refreshTable()
        }
    }

    private fun deleteButtonCallback() {
        val session = tableSection.getSelected()
        if (session.isEmpty) {
            Logger.warning("Delete button clicked but no session selected, row: ${tableSection.table.selectedRow}")
            return
        }
        this.sessionSwitcher.sessions.deleteSession(session.get())
        tableSection.refreshTable()
    }

    private fun duplicateButtonCallback() {
        val session = tableSection.getSelected()
        if (session.isEmpty) {
            Logger.warning("Delete button clicked but no session selected, row: ${tableSection.table.selectedRow}")
            return
        }
        SaveSessionDialog(sessionSwitcher).duplicateSessionDialog(session.get())
        tableSection.refreshTable()
    }

    private fun editButtonCallback() {
        val selectedSession = tableSection.getSelected()
        if (selectedSession.isEmpty) {
            Logger.warning("Delete button clicked but no session selected, row: ${tableSection.table.selectedRow}")
            return
        }
        val session = SessionEditWindow(sessionSwitcher, Optional.of(selectedSession.get())).showDialog()
        if (session.isPresent) {
            tableSection.refreshTable()
        }
    }

    override suspend fun onSessionsListUpdate() {
        this.tableSection.refreshTable()
    }
}