package sessionswitcher.ui.maintab

import sessionswitcher.ui.UISection
import sessionswitcher.ui.tables.ITableModel
import sessionswitcher.ui.withScrollPane
import java.awt.BorderLayout
import java.util.Optional
import javax.swing.BorderFactory
import javax.swing.Box
import javax.swing.BoxLayout
import javax.swing.JButton
import javax.swing.JLabel
import javax.swing.JPanel
import javax.swing.JTable
import javax.swing.ListSelectionModel
import javax.swing.SwingUtilities
import javax.swing.event.ListSelectionEvent
import javax.swing.table.DefaultTableCellRenderer
import javax.swing.table.TableModel
import kotlin.math.min

@Suppress("UNCHECKED_CAST")
class TableSection<T>(
    val title: String,
    val description: String?,
    val tableModel: ITableModel<T>,
    val tableHeight: Int = 15,
    showNewButton: Boolean = true,
    showEditButton: Boolean = true,
    showDeleteButton: Boolean = true,
    showDuplicateButton: Boolean = true,
    showRefreshButton: Boolean = true,
    val otherButtons: Array<JButton> = emptyArray<JButton>(),
) {
    // Table model
    val table =
        JTable(tableModel as TableModel).also {
            it.setSelectionMode(ListSelectionModel.SINGLE_SELECTION)
            it.selectionModel.addListSelectionListener(this::tableSelectionListener)
            (it.tableHeader.defaultRenderer as DefaultTableCellRenderer).horizontalAlignment = JLabel.LEFT
        }

    // UI elements
    val mainPanel = JPanel(BorderLayout())

    // Buttons
    val newButton = JButton("New")
    val editButton =
        JButton("Edit").also {
            it.isEnabled = false
        }
    val deleteButton =
        JButton("Delete").also {
            it.isEnabled = false
        }
    val duplicateButton =
        JButton("Duplicate").also {
            it.isEnabled = false
        }
    val refreshButton =
        JButton("Refresh").also {
            it.addActionListener { this.refreshButtonCallback() }
        }

    // Callbacks
    fun refreshButtonCallback() {
        this.refreshTable()
    }

    fun refreshTable() {
        SwingUtilities.invokeLater {
            // Save current selected row
            var selectedRow = table.selectedRow

            // Update the table
            tableModel.refresh()

            // Restore selection
            selectedRow = min(selectedRow, table.rowCount - 1) // Do not select a row that does not exist anymore
            if (selectedRow != -1 && selectedRow < table.rowCount) {
                table.setRowSelectionInterval(selectedRow, selectedRow)
            }
        }
    }

    fun setNewButtonCallback(callback: () -> Unit) {
        this.newButton.addActionListener { callback() }
    }

    fun setEditButtonCallback(callback: () -> Unit) {
        this.editButton.addActionListener { callback() }
    }

    fun setDeleteButtonCallback(callback: () -> Unit) {
        this.deleteButton.addActionListener { callback() }
    }

    fun setDuplicateButtonCallback(callback: () -> Unit) {
        this.duplicateButton.addActionListener { callback() }
    }

    fun setRefreshButtonCallback(callback: () -> Unit) {
        this.refreshButton.addActionListener { callback() }
    }

    fun getSelected(): Optional<T> {
        val viewRow = table.selectedRow
        if (viewRow < 0) return Optional.empty<T>() as Optional<T>

        val modelRow = table.convertRowIndexToModel(viewRow)
        return tableModel.getAt(modelRow)
    }

    fun tableSelectionListener(e: ListSelectionEvent) {
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
        val buttonsPanel =
            JPanel().also {
                it.layout = BoxLayout(it, BoxLayout.Y_AXIS)
                it.border = BorderFactory.createEmptyBorder(0, 0, 0, 5)
                if (showNewButton) {
                    it.add(JPanel(BorderLayout()).also { p -> p.add(newButton, BorderLayout.PAGE_START) })
                    it.add(Box.createVerticalStrut(5))
                }
                if (showEditButton) {
                    it.add(JPanel(BorderLayout()).also { p -> p.add(editButton, BorderLayout.PAGE_START) })
                    it.add(Box.createVerticalStrut(5))
                }
                if (showDeleteButton) {
                    it.add(JPanel(BorderLayout()).also { p -> p.add(deleteButton, BorderLayout.PAGE_START) })
                    it.add(Box.createVerticalStrut(5))
                }
                if (showDuplicateButton) {
                    it.add(JPanel(BorderLayout()).also { p -> p.add(duplicateButton, BorderLayout.PAGE_START) })
                    it.add(Box.createVerticalStrut(5))
                }
                if (showRefreshButton) {
                    it.add(JPanel(BorderLayout()).also { p -> p.add(refreshButton, BorderLayout.PAGE_START) })
                    it.add(Box.createVerticalStrut(5))
                }
                for (button in otherButtons) {
                    it.add(JPanel(BorderLayout()).also { p -> p.add(button, BorderLayout.PAGE_START) })
                    it.add(Box.createVerticalStrut(5))
                }
            }

        if (showNewButton || showEditButton || showDeleteButton || showDuplicateButton || showRefreshButton || otherButtons.isNotEmpty()) {
            // Add buttons panel only if there are buttons to show
            val middlePanel =
                JPanel(BorderLayout()).also {
                    it.add(buttonsPanel, BorderLayout.PAGE_START)
                }
            mainPanel.add(middlePanel, BorderLayout.LINE_START)
        }

        mainPanel.add(table.withScrollPane(tableHeight), BorderLayout.CENTER)
    }

    fun getComponent(): JPanel = UISection(title, description, mainPanel)
}
