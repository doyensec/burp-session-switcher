package sessionswitcher.ui.maintab.tables

import sessionswitcher.sessions.Session
import sessionswitcher.sessions.SessionCollection
import javax.swing.table.AbstractTableModel

class SessionsTableModel(private val sessionCollection: SessionCollection): AbstractTableModel() {
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

    public fun getAt(index: Int): Session = this.sessions[index]
}