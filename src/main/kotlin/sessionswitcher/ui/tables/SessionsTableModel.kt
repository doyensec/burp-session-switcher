package sessionswitcher.ui.tables

import sessionswitcher.sessions.Session
import sessionswitcher.sessions.SessionCollection
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Optional
import javax.swing.table.AbstractTableModel

class SessionsTableModel(private val sessionCollection: SessionCollection) : AbstractTableModel(),
    ITableModel<Session> {
    private val columnNames = arrayOf("Name", "Host", "Last Updated At", "Last Updated By")
    private val sessions: ArrayList<Session>
        get() {
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

    override fun getValueAt(rowIndex: Int, columnIndex: Int): Any {
        val session: Session
        try {
            session = sessions[rowIndex]
        } catch (_: IndexOutOfBoundsException) {
            return ""
        }
        return when (columnIndex) {
            0 -> session.name
            1 -> session.getHost()
            2 -> formatDate(session.lastUpdatedAt)
            3 -> formatLastUpdatedBy(session)
            else -> "N/A"
        }
    }

    private fun formatDate(instant: Instant): String {
        val dateTime = instant.atZone(ZoneId.systemDefault()).toLocalDateTime()
        val formatter = if (dateTime.toLocalDate().equals(LocalDate.now())) {
            // Same day, only care about time
            DateTimeFormatter.ofPattern("HH:mm:ss")
        } else if (dateTime.toLocalDate().year == LocalDate.now().year) {
            // Same year, only care about month and day
            DateTimeFormatter.ofPattern("dd MMM HH:mm:ss")
        } else {
            // Different year, show full date
            DateTimeFormatter.ofPattern("dd MMM yyyy HH:mm:ss")
        }
        return dateTime.format(formatter)
    }

    private fun formatLastUpdatedBy(session: Session): String {
        if (session.lastUpdatedBy == Session.LastUpdateType.UPDATE_RULE) {
            if (session.lastUpdatedRuleId == null) return "Rule"
            return "Rule ${session.lastUpdatedRuleId}"
        }
        return session.lastUpdatedBy.toString()
    }

    override fun getAt(index: Int): Optional<Session> {
        return try {
            if (index < 0 || index >= this.sessions.size) {
                return Optional.empty()
            }
            Optional.of(this.sessions[index])
        } catch (e: IndexOutOfBoundsException) {
            Optional.empty()
        }
    }

    override fun refresh() = this.fireTableDataChanged()
}