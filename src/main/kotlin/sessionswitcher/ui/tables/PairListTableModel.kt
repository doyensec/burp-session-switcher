package sessionswitcher.ui.tables

import sessionswitcher.Logger
import java.util.*
import javax.swing.table.AbstractTableModel

class PairListTableModel(private val list: MutableList<Pair<String, String>>): AbstractTableModel(), ITableModel<Pair<String, String>> {
    private val columnNames = arrayOf("Name", "Value", "Delete")
    private val editListeners = mutableListOf<(Int, Int) -> Unit>()

    override fun getRowCount(): Int {
        // Add one for the "new" row
        return list.size + 1
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
        if (rowIndex == list.size) return ""
        return when (columnIndex) {
            0 -> list[rowIndex].first
            1 -> list[rowIndex].second
            2 -> "delete"
            else -> "N/A"
        }
    }

    override fun setValueAt(aValue: Any?, rowIndex: Int, columnIndex: Int) {
        val newValue = aValue as String
        if (newValue.isBlank()) return
        if (rowIndex == list.size && columnIndex == 0) {
            // New pair
            list.add(Pair(newValue, ""))
        } else if (rowIndex < list.size) {
            when (columnIndex) {
                0 -> {
                    list[rowIndex] = Pair(newValue, list[rowIndex].second)
                }
                1 -> {
                    list[rowIndex] = Pair(list[rowIndex].first, newValue)
                }
                2 -> {
                    // Do nothing
                }
                else -> {
                    Logger.error("Trying to set value at invalid column: $columnIndex")
                }
            }
        }
        invokeListeners(rowIndex, columnIndex)
    }

    override fun isCellEditable(rowIndex: Int, columnIndex: Int): Boolean {
        return true
    }

    override fun getAt(index: Int): Optional<Pair<String, String>> {
        return try {
            Optional.of(this.list[index])
        } catch (e: IndexOutOfBoundsException) {
            Optional.empty()
        }
    }
    override fun refresh() = this.fireTableDataChanged()

    public fun addEditListener(listener: (Int, Int) -> Unit) {
        this.editListeners.add(listener)
    }

    private fun invokeListeners(row: Int, column: Int) {
        this.editListeners.forEach { it(row, column) }
    }
}