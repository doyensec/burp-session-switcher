package sessionswitcher.ui.tables

import sessionswitcher.rules.autoupdate.UpdateRule
import java.util.Optional
import javax.swing.table.AbstractTableModel

class UpdateRuleTableModel(private val rules: ArrayList<UpdateRule>) : AbstractTableModel(), ITableModel<UpdateRule> {
    private val columnNames = arrayOf("ID", "Conditions", "Session", "Enabled", "Color")

    override fun getRowCount(): Int {
        return rules.size
    }

    override fun getColumnCount(): Int {
        return columnNames.size
    }

    override fun getColumnName(column: Int): String {
        return columnNames[column]
    }

    override fun getColumnClass(columnIndex: Int): Class<*> {
        return when(columnIndex) {
            3 -> Boolean::class.javaObjectType
            else -> String::class.java
        }
    }

    override fun getValueAt(rowIndex: Int, columnIndex: Int): Any {
        val rule = rules[rowIndex]
        val conditionsDescription = rule.conditions.joinToString(", ") { it.describe() }
        return when (columnIndex) {
            0 -> rule.ruleId.toString()
            1 -> conditionsDescription
            2 -> rule.session.name
            3 -> rule.isEnabled
            4 -> rule.config.highlightColor
            else -> "N/A"
        }
    }

    override fun isCellEditable(rowIndex: Int, columnIndex: Int): Boolean {
        return columnIndex == 3
    }

    override fun setValueAt(aValue: Any?, rowIndex: Int, columnIndex: Int) {
        when (columnIndex) {
            3 -> {
                val newValue = aValue as Boolean
                rules[rowIndex].setEnabled(newValue)
                fireTableCellUpdated(rowIndex, columnIndex)
            }
            else -> throw IllegalArgumentException("Invalid column index: $columnIndex")
        }
    }

    override fun getAt(index: Int): Optional<UpdateRule> {
        return try {
            Optional.of(this.rules[index])
        } catch (e: IndexOutOfBoundsException) {
            Optional.empty()
        }
    }

    override fun refresh() = this.fireTableDataChanged()
}