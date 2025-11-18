package sessionswitcher.ui.maintab.tables

import sessionswitcher.rules.refresher.RefreshRule
import javax.swing.table.AbstractTableModel

class RefreshRuleTableModel(private val rules: ArrayList<RefreshRule>): AbstractTableModel() {
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

    public fun getAt(index: Int): RefreshRule = this.rules[index]
}