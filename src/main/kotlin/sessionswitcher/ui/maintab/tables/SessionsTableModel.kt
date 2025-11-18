package sessionswitcher.ui.maintab.tables

import sessionswitcher.sessions.Session
import sessionswitcher.sessions.SessionCollection
import java.util.*
import javax.swing.table.AbstractTableModel

class SessionsTableModel(private val sessionCollection: SessionCollection): AbstractTableModel(), ITableModel<Session> {
    private val columnNames = arrayOf("Name", "Host")
    private val sessions: ArrayList<Session> get() {
        return sessionCollection.getSessions().toTypedArray().toCollection(ArrayList())
    }

    override fun getRowCount(): Int {
        return sessions.size
    }

    override fun getColumnCount(): Int {
        return columnNames.size
    }

    override fun getColumnName(column: Int): String {
        return columnNames[column]
    }

    override fun getColumnClass(columnIndex: Int): Class<*> {
        return String::class.java
    }

    override fun getValueAt(rowIndex: Int, columnIndex: Int): Any? {
        val session = sessions[rowIndex]
        return when (columnIndex) {
            0 -> session.name
            1 -> session.getHost()
            else -> "N/A"
        }
    }

    override fun getAt(index: Int): Optional<Session> {
        return try {
            Optional.of(this.sessions[index])
        } catch (e: IndexOutOfBoundsException) {
            Optional.empty()
        }
    }    override fun refresh() = this.fireTableDataChanged()
}