package sessionswitcher.ui.tables

import sessionswitcher.rules.conditions.Condition
import java.util.*
import javax.swing.table.AbstractTableModel

class ConditionsTableModel(private val conditions: ArrayList<Condition>): AbstractTableModel(), ITableModel<Condition> {
    private val columnNames = arrayOf("Type", "Operation", "Pattern", "Negative")

    override fun getRowCount(): Int {
        return conditions.size
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
        val condition = conditions[rowIndex]
        return when (columnIndex) {
            0 -> condition.typeInstance.toString()
            1 -> condition.configuration.operation
            2 -> condition.configuration.pattern.orElse("")
            3 -> if (condition.configuration.negativeMatch) "Yes" else "No"
            else -> "N/A"
        }
    }

    override fun getAt(index: Int): Optional<Condition> {
        return try {
            Optional.of(this.conditions[index])
        } catch (e: IndexOutOfBoundsException) {
            Optional.empty()
        }
    }
    override fun refresh() = this.fireTableDataChanged()
}