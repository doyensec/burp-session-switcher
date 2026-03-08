package sessionswitcher.ui.tables

import sessionswitcher.rules.conditions.Condition
import java.util.Optional
import javax.swing.table.AbstractTableModel

class ConditionsTableModel(
    private val conditions: ArrayList<Condition>,
) : AbstractTableModel(),
    ITableModel<Condition> {
    private val columnNames = arrayOf("Type", "Operation", "Pattern", "Negative")

    override fun getRowCount(): Int = conditions.size

    override fun getColumnCount(): Int = columnNames.size

    override fun getColumnName(column: Int): String = columnNames[column]

    override fun getColumnClass(columnIndex: Int): Class<*> = String::class.java

    override fun getValueAt(
        rowIndex: Int,
        columnIndex: Int,
    ): Any? {
        val condition = conditions[rowIndex]
        return when (columnIndex) {
            0 -> condition.typeInstance.toString()
            1 -> condition.configuration.operation
            2 -> condition.configuration.extraFields["Pattern"] ?: ""
            3 -> if (condition.configuration.negativeMatch) "Yes" else "No"
            else -> "N/A"
        }
    }

    override fun getAt(index: Int): Optional<Condition> =
        try {
            Optional.of(this.conditions[index])
        } catch (e: IndexOutOfBoundsException) {
            Optional.empty()
        }

    override fun refresh() = this.fireTableDataChanged()
}
