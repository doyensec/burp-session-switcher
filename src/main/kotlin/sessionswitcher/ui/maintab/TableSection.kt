package sessionswitcher.ui.maintab

import sessionswitcher.ui.Table
import sessionswitcher.ui.UISection
import java.awt.BorderLayout
import javax.swing.*
import javax.swing.event.ListSelectionEvent
import javax.swing.table.AbstractTableModel

abstract class TableSection(public val title: String, public val description: String?, public val tableModel: AbstractTableModel) {
    // Table model
    val table = Table(emptyArray()).also {
        it.model = tableModel
        it.selectionModel.addListSelectionListener(this::tableSelectionListener)
    }

    // UI elements
    val mainPanel = JPanel(BorderLayout())

    // Buttons
    val newButton = JButton("New").also {
        it.addActionListener { newButtonCallback() }
    }
    val editButton = JButton("Edit").also {
        it.isEnabled = false
        it.addActionListener { editButtonCallback() }
    }
    val deleteButton = JButton("Delete").also {
        it.isEnabled = false
        it.addActionListener { deleteButtonCallback() }
    }
    val duplicateButton = JButton("Duplicate").also {
        it.isEnabled = false
        it.addActionListener { duplicateButtonCallback() }
    }
    val refreshButton = JButton("Refresh").also {
        it.addActionListener { refreshButtonCallback() }
    }

    // Callbacks
    abstract fun newButtonCallback()
    abstract fun deleteButtonCallback()
    abstract fun duplicateButtonCallback()
    abstract fun editButtonCallback()

    open fun refreshButtonCallback() {
        this.refreshTable()
    }

    public open fun refreshTable() {
        this.tableModel.fireTableDataChanged()
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
            it.add(JPanel(BorderLayout()).also{p-> p.add(newButton, BorderLayout.PAGE_START)})
            it.add(Box.createVerticalStrut(5))
            it.add(JPanel(BorderLayout()).also{p-> p.add(editButton, BorderLayout.PAGE_START)})
            it.add(Box.createVerticalStrut(5))
            it.add(JPanel(BorderLayout()).also{p-> p.add(deleteButton, BorderLayout.PAGE_START)})
            it.add(Box.createVerticalStrut(5))
            it.add(JPanel(BorderLayout()).also{p-> p.add(duplicateButton, BorderLayout.PAGE_START)})
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