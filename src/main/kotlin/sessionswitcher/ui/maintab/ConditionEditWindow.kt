package sessionswitcher.ui.maintab

import sessionswitcher.SessionSwitcher
import sessionswitcher.rules.conditions.Condition
import sessionswitcher.rules.conditions.Condition.ConditionType
import sessionswitcher.rules.conditions.ConditionConfig
import sessionswitcher.rules.conditions.ConditionTypeInstance
import sessionswitcher.ui.ButtonPrimary
import sessionswitcher.ui.UISection
import java.awt.*
import java.util.*
import javax.swing.*
import javax.swing.event.DocumentEvent
import javax.swing.event.DocumentListener

class ConditionEditWindow(owner: Dialog, private val initialCondition: Optional<Condition>) :
    JDialog(owner, if (initialCondition.isEmpty) "New Condition" else "Edit Condition", true) {

    // Flags
    var shouldSave = false

    // Buttons
    val saveButton = ButtonPrimary("OK")
    val cancelButton = JButton("Cancel")

    // Condition selection
    val conditionTypesSelector = JComboBox<ConditionTypeInstance>(Condition.ConditionType.instances)
    val operationSelector = JComboBox<String>()
    val patternTextBox =  JTextField().also {
        it.isEnabled = false
        it.font = SessionSwitcher.getApi().userInterface().currentEditorFont()
    }
    val negativeMatchCheckBox = JCheckBox("Negative match")
    val validationMessageLabel = JLabel("")

    val selectedConditionType: ConditionTypeInstance get() {
        return conditionTypesSelector.selectedItem as ConditionTypeInstance
    }

    val configuration: ConditionConfig get() {
        val operation = operationSelector.selectedItem as String
        val pattern = if (patternTextBox.isEnabled) Optional.of(patternTextBox.text) else Optional.empty<String>()
        val negativeMatch = this.negativeMatchCheckBox.isSelected
        return ConditionConfig(operation, pattern, negativeMatch)
    }

    fun autoSize() {
        // Pack the window to fit its content
        this.minimumSize = Dimension(500, 310)
        this.preferredSize = this.minimumSize
        this.pack()
        this.setLocationRelativeTo(SessionSwitcher.getApi().userInterface().swingUtils().suiteFrame())
    }

    public fun showDialog(): Optional<Condition> {
        this.isVisible = true
        return if (this.shouldSave) {
            Optional.of(Condition.make(ConditionType.fromInstance(this.selectedConditionType), this.configuration))
        } else {
            Optional.empty()
        }
    }

    private fun loadInitialCondition() {
        if (this.initialCondition.isPresent) {
            this.conditionTypesSelector.selectedItem = this.initialCondition.get().typeInstance
            this.operationSelector.selectedItem = this.initialCondition.get().configuration.operation
            this.patternTextBox.text = this.initialCondition.get().configuration.pattern.orElse("")
            this.negativeMatchCheckBox.isSelected = this.initialCondition.get().configuration.negativeMatch
        }
    }

    init {
        // Set UI properties
        this.isResizable = true
        this.isAutoRequestFocus = true
        this.modalityType = Dialog.ModalityType.APPLICATION_MODAL

        saveButton.isEnabled = false

        val controls = arrayOf(
            Pair("Type", conditionTypesSelector),
            Pair("Operation", operationSelector),
            Pair("Pattern", patternTextBox),
            Pair("Negative match", negativeMatchCheckBox),
            Pair("Validation", validationMessageLabel)
            )

        val panel = JPanel(GridBagLayout())
        val c = GridBagConstraints()
        c.insets = Insets(5, 5, 5, 5)

        var index = 0
        for ((text, control) in controls) {
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
            panel.add(control, c)

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

        val section = UISection("Condition Details", "Specify the match condition", panel)

        this.add(section)
        this.autoSize()

        // Setup action listeners
        saveButton.addActionListener {
            this.shouldSave = true
            this.dispose()
        }
        cancelButton.addActionListener {
            this.shouldSave = false
            this.dispose()
        }
        conditionTypesSelector.addActionListener {
            // Populate operations
            this.operationSelector.removeAllItems()
            this.selectedConditionType.availableOperations.forEach { this.operationSelector.addItem(it) }
            this.operationSelector.selectedIndex = 0

            // Reset pattern
            this.patternTextBox.isEnabled = this.selectedConditionType.canSetPattern
            this.patternTextBox.text = ""

            // Validate configuration
            this.validateConfiguration()
        }
        this.operationSelector.addActionListener { this.validateConfiguration() }
        this.patternTextBox.document.addDocumentListener(object : DocumentListener {
            override fun insertUpdate(e: DocumentEvent) = validateConfiguration()
            override fun removeUpdate(e: DocumentEvent) = validateConfiguration()
            override fun changedUpdate(e: DocumentEvent) = validateConfiguration()
        })

        // Populate operations for the first condition type
        this.selectedConditionType.availableOperations.forEach { this.operationSelector.addItem(it) }

        // Load initial condition if present
        this.loadInitialCondition()
    }

    fun validateConfiguration() {
        SwingUtilities.invokeLater {
            val configurationValidation = this.selectedConditionType.validateConfiguration(this.configuration)
            this.validationMessageLabel.text = configurationValidation.second.ifBlank { "OK" }
            saveButton.isEnabled = configurationValidation.first
        }
    }
}
