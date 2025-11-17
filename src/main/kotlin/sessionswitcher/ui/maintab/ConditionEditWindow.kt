package sessionswitcher.ui.maintab

import sessionswitcher.SessionSwitcher
import sessionswitcher.rules.conditions.Condition
import sessionswitcher.ui.ButtonPrimary
import sessionswitcher.ui.UISection
import sessionswitcher.ui.Window
import java.awt.Dimension
import java.awt.GridBagConstraints
import java.awt.GridBagLayout
import java.awt.Insets
import java.util.*
import javax.swing.JButton
import javax.swing.JComboBox
import javax.swing.JLabel
import javax.swing.JPanel

class ConditionEditWindow(private val condition: Optional<Condition>) :
    Window(if (condition.isEmpty) "New Condition" else "Edit Condition") {

    val saveButton = ButtonPrimary("OK")
    val cancelButton = JButton("Cancel")

    override fun autoSize() {
        // Pack the window to fit its content
        this.preferredSize = Dimension(500, 280)
        this.pack()
        this.setLocationRelativeTo(SessionSwitcher.getApi().userInterface().swingUtils().suiteFrame())
    }

    init {
        // Set window properties
        this.isResizable = true
        this.isAutoRequestFocus = true

        saveButton.isEnabled = false


        val combo1 = JComboBox(arrayOf("1", "2", "3", "4", "5", "6", "7", "8", "9", "10"))
        val combo2 = JComboBox(arrayOf("1", "2", "3", "4", "5", "6", "7", "8", "9", "10"))
        val combo3 = JComboBox(arrayOf("1", "2", "3", "4", "5", "6", "7", "8", "9", "10"))
        val combo4 = JComboBox(arrayOf("1", "2", "3", "4", "5", "6", "7", "8", "9", "10"))

        val controls = arrayOf(
            Pair("Operator", combo1),
            Pair("Match type", combo2),
            Pair("Match relationship", combo3),
            Pair("Match condition", combo4)
            )

        val panel = JPanel(GridBagLayout())
        val c = GridBagConstraints()
        c.insets = Insets(5, 5, 5, 5)

        //c.anchor = GridBagConstraints.LINE_START

        var index = 0
        for ((text, combo) in controls) {
            c.gridy = index
            // Add label first
            c.anchor = GridBagConstraints.LINE_START
            c.fill = GridBagConstraints.NONE
            c.ipadx = 0
            c.ipady = 0
            c.gridx = 0
            c.weightx = 0.0
            c.gridwidth = 1
            val label = JLabel(text)
            panel.add(label, c)

            // Add combo box
            c.anchor = GridBagConstraints.LINE_END
            c.fill = GridBagConstraints.HORIZONTAL
            c.gridx = 1
            c.ipadx = 100
            c.weightx = 1.0
            c.gridwidth = 2
            panel.add(combo, c)

            // Next row
            index++
        }

        // Add save and cancel buttons
        c.insets = Insets(10, 5, 0, 5)
        c.anchor = GridBagConstraints.LINE_END
        c.fill = GridBagConstraints.NONE
        c.gridy = index
        c.gridx = 2
        c.ipadx = 0
        c.weightx = 0.0
        c.gridwidth = 1
        val buttonPanel = JPanel().also {
            it.add(saveButton)
            it.add(cancelButton)
        }
        panel.add(buttonPanel, c)

        // Set button listeners
        cancelButton.addActionListener { this.dispose() }

        val section = UISection("Condition Details", "Specify the match condition", panel)

        this.add(section)
        this.autoSize()
        this.checkEnableSaveButton()
    }

    fun checkEnableSaveButton() {
        //val enable = conditionsTable.model.rowCount > 0 && sessionSelector.getSelectedItem() != "(No sessions)" && sessionSelector.getSelectedItem() != ""
        saveButton.isEnabled = false
    }
}
