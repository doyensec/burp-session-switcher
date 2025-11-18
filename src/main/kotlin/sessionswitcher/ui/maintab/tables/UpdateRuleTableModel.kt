package sessionswitcher.ui.maintab.tables

import sessionswitcher.rules.autoupdate.UpdateRule
import java.util.*
import javax.swing.table.AbstractTableModel

class UpdateRuleTableModel(private val rules: ArrayList<UpdateRule>): AbstractTableModel(), ITableModel<UpdateRule> {
    private val columnNames = arrayOf("ID", "Conditions", "Session")

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
        return String::class.java
    }

    override fun getValueAt(rowIndex: Int, columnIndex: Int): Any? {
        val rule = rules[rowIndex]
        val conditionsDescription = rule.conditions.joinToString(", ") { it.describe() }
        return when (columnIndex) {
            0 -> rule.id.toString()
            1 -> conditionsDescription
            2 -> rule.session.name
            else -> "N/A"
        }
    }

    override fun getAt(index: Int): Optional<UpdateRule> {
        return try {
            Optional.of(this.rules[index])
        } catch (e: IndexOutOfBoundsException) {
            Optional.empty()
        }
    }    override fun refresh() = this.fireTableDataChanged()
}