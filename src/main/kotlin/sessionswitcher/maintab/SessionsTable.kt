package sessionswitcher.maintab

import sessionswitcher.sessions.Session
import sessionswitcher.ui.Table
import java.awt.BorderLayout
import javax.swing.JPanel
import javax.swing.table.DefaultTableModel

class SessionsTable(): JPanel(BorderLayout()) {
    private var sessions: List<Session> = emptyList()
    private val table = Table(arrayOf("Name", "Host"))

    init {
        this.add(table.withScrollPane())
    }
    public fun update(sessions: List<Session>) {
        this.sessions = sessions
        this.refresh()
    }

    public fun clear() {
        this.sessions = emptyList()
        this.refresh()
    }

    public fun refresh() {
        this.table.clearSelection()
        val model = this.table.model as DefaultTableModel
        model.rowCount = 0
        for (session in this.sessions) {
            model.addRow(arrayOf(session.name, session.getHost()))
        }
    }

    public fun getSelectedRow(): Int = table.selectedRow

    public fun getSelectedSession(): Session? {
        val row = this.table.selectedRow
        if (row == -1) return null
        if (row >= this.sessions.size) return null

        return this.sessions[row]
    }

    public fun addSessionSelectionListener(listener: (Session?)->Unit) {
        this.table.selectionModel.addListSelectionListener {
            listener(getSelectedSession())
        }
    }
    public fun addRowSelectionListener(listener: (Int)->Unit) {
        this.table.selectionModel.addListSelectionListener {
            if (it.valueIsAdjusting) return@addListSelectionListener
            listener(table.selectedRow)
        }
    }
}