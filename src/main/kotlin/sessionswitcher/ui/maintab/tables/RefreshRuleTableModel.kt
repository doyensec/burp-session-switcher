package sessionswitcher.ui.maintab.tables

import sessionswitcher.rules.refresher.RefreshRule
import java.util.*
import javax.swing.table.AbstractTableModel

class RefreshRuleTableModel(private val rules: ArrayList<RefreshRule>): AbstractTableModel(), ITableModel<RefreshRule> {
    private val columnNames = arrayOf("Conditions", "Session")

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
            0 -> conditionsDescription
            1 -> rule.session.name
            else -> "N/A"
        }
    }

    override fun getAt(index: Int): Optional<RefreshRule> {
        return try {
            Optional.of(this.rules[index])
        } catch (e: IndexOutOfBoundsException) {
            Optional.empty()
        }
    }    override fun refresh() = this.fireTableDataChanged()
}