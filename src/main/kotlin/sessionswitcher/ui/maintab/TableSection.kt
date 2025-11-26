package sessionswitcher.ui.maintab

import sessionswitcher.ui.Table
import sessionswitcher.ui.UISection
import sessionswitcher.ui.tables.ITableModel
import java.awt.BorderLayout
import java.util.*
import javax.swing.*
import javax.swing.event.ListSelectionEvent
import javax.swing.table.TableModel
import kotlin.math.min

@Suppress("UNCHECKED_CAST")
class TableSection<T>(public val title: String, public val description: String?, public val tableModel: ITableModel<T>, showNewButton: Boolean = true, showEditButton: Boolean = true, showDeleteButton: Boolean = true, showDuplicateButton: Boolean = true, showRefreshButton: Boolean = true, showDeleteButtonIfSelected: Boolean = true, showDuplicateButtonIfSelected: Boolean = true, showRefreshButtonIfSelected: Boolean = true) {
    // Table model
    val table = Table(emptyArray()).also {
        it.model = tableModel as TableModel?
        it.selectionModel.addListSelectionListener(this::tableSelectionListener)
    }

    // UI elements
    val mainPanel = JPanel(BorderLayout())

    // Buttons
    val newButton = JButton("New")
    val editButton = JButton("Edit").also {
        it.isEnabled = false
    }
    val deleteButton = JButton("Delete").also {
        it.isEnabled = false
    }
    val duplicateButton = JButton("Duplicate").also {
        it.isEnabled = false
    }
    val refreshButton = JButton("Refresh").also {
        it.addActionListener { this.refreshButtonCallback() }
    }

    // Callbacks
    fun refreshButtonCallback() {
        this.refreshTable()
    }

    public fun refreshTable() {
        // Save current selected row
        var selectedRow = this.table.selectedRow

        // Update
        this.tableModel.refresh()

        // Restore selection
        selectedRow = min(selectedRow, this.table.rowCount - 1) // Do not select a row that does not exist anymore
        if (selectedRow != -1 && selectedRow < this.table.rowCount) {
            this.table.setRowSelectionInterval(selectedRow, selectedRow)
        }
    }

    public fun setNewButtonCallback(callback: () -> Unit) {
        this.newButton.addActionListener { callback() }
    }

    public fun setEditButtonCallback(callback: () -> Unit) {
        this.editButton.addActionListener { callback() }
    }

    public fun setDeleteButtonCallback(callback: () -> Unit) {
        this.deleteButton.addActionListener { callback() }
    }

    public fun setDuplicateButtonCallback(callback: () -> Unit) {
        this.duplicateButton.addActionListener { callback() }
    }

    public fun setRefreshButtonCallback(callback: () -> Unit) {
        this.refreshButton.addActionListener { callback() }
    }

    public fun getSelected(): Optional<T> {
        val row = this.table.selectedRow
        return tableModel.getAt(row)
    }

    open fun tableSelectionListener(e: ListSelectionEvent) {
        if (e.valueIsAdjusting) return
        if (table.selectedRow == -1) {
            editButton.isEnabled = false
            deleteButton.isEnabled = false
            duplicateButton.isEnabled = false
        } else {
            editButton.isEnabled = true
            deleteButton.isEnabled = true
            duplicateButton.isEnabled = true
        }
    }

    init {
        val buttonsPanel = JPanel().also { it ->
            it.layout = BoxLayout(it, BoxLayout.Y_AXIS)
            it.border = BorderFactory.createEmptyBorder(0, 0, 0, 5)
            if (showNewButton) {
                it.add(JPanel(BorderLayout()).also{p-> p.add(newButton, BorderLayout.PAGE_START)})
                it.add(Box.createVerticalStrut(5))
            }
            if (showEditButton) {
                it.add(JPanel(BorderLayout()).also{p-> p.add(editButton, BorderLayout.PAGE_START)})
                it.add(Box.createVerticalStrut(5))
            }
            if (showDeleteButton) {
                it.add(JPanel(BorderLayout()).also{p-> p.add(deleteButton, BorderLayout.PAGE_START)})
                it.add(Box.createVerticalStrut(5))
            }
            if (showDuplicateButton) {
                it.add(JPanel(BorderLayout()).also{p-> p.add(duplicateButton, BorderLayout.PAGE_START)})
                it.add(Box.createVerticalStrut(5))
            }
            if (showRefreshButton) {
                it.add(JPanel(BorderLayout()).also{p-> p.add(refreshButton, BorderLayout.PAGE_START)})
                it.add(Box.createVerticalStrut(5))
            }
        }

        val middlePanel = JPanel(BorderLayout()).also {
            it.add(buttonsPanel, BorderLayout.PAGE_START)
        }
        mainPanel.add(middlePanel, BorderLayout.LINE_START)
        mainPanel.add(table.withScrollPane(), BorderLayout.CENTER)
    }

    public fun getComponent(): JPanel {
        return UISection(title, description, mainPanel)
    }
}