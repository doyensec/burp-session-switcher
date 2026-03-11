package sessionswitcher.ui.maintab

import sessionswitcher.SessionSwitcher
import sessionswitcher.rules.conditions.Condition
import sessionswitcher.rules.conditions.Condition.ConditionTypeEnum
import sessionswitcher.rules.conditions.ConditionConfig
import sessionswitcher.rules.conditions.type.types.ConditionField
import sessionswitcher.ui.ButtonPrimary
import sessionswitcher.ui.UISection
import java.awt.Dialog
import java.awt.Dimension
import java.awt.GridBagConstraints
import java.awt.GridBagLayout
import java.awt.Insets
import java.util.Optional
import javax.swing.JButton
import javax.swing.JCheckBox
import javax.swing.JComboBox
import javax.swing.JComponent
import javax.swing.JDialog
import javax.swing.JLabel
import javax.swing.JPanel
import javax.swing.JTextField
import javax.swing.SwingUtilities
import javax.swing.event.DocumentEvent
import javax.swing.event.DocumentListener

class ConditionEditWindow(
    owner: Dialog,
    private val initialCondition: Optional<Condition>,
) : JDialog(owner, if (initialCondition.isEmpty) "New Condition" else "Edit Condition", true) {
    // Flags
    var shouldSave = false

    // Buttons
    val saveButton = ButtonPrimary("OK")
    val cancelButton = JButton("Cancel")

    // Condition selection
    val conditionTypesSelector = JComboBox(ConditionTypeEnum.instances)
    val operationSelector = JComboBox<String>()
    val extraFields = LinkedHashMap<String, JComponent>()
    val negativeMatchCheckBox = JCheckBox("Negative match")
    val validationMessageLabel = JLabel("")

    val selectedConditionType: sessionswitcher.rules.conditions.type.ConditionType
        get() {
            return conditionTypesSelector.selectedItem as sessionswitcher.rules.conditions.type.ConditionType
        }

    val configuration: ConditionConfig
        get() {
            val operation = operationSelector.selectedItem as String
            val negativeMatch = this.negativeMatchCheckBox.isSelected

            val extraFieldsMap = HashMap<String, String>()
            for ((name, field) in this.extraFields) {
                val value =
                    when (field) {
                        is JTextField -> {
                            field.text
                        }

                        is JComboBox<*> -> {
                            field.selectedItem as String
                        }

                        is JCheckBox -> {
                            field.isSelected.toString()
                        }

                        else -> {
                            throw IllegalStateException("Unexpected field type: ${field.javaClass}")
                        }
                    }
                extraFieldsMap[name] = value
            }

            return ConditionConfig(operation, negativeMatch, extraFieldsMap)
        }

    fun autoSize() {
        // Pack the window to fit its content
        this.minimumSize = Dimension(500, this.minimumSize.height)
        // this.preferredSize = this.minimumSize
        this.pack()
        this.setLocationRelativeTo(
            SessionSwitcher
                .getApi()
                .userInterface()
                .swingUtils()
                .suiteFrame(),
        )
    }

    fun showDialog(): Optional<Condition> {
        this.isVisible = true
        return if (this.shouldSave) {
            Optional.of(Condition.make(ConditionTypeEnum.fromInstance(this.selectedConditionType), this.configuration))
        } else {
            Optional.empty()
        }
    }

    private fun loadInitialCondition() {
        if (this.initialCondition.isPresent) {
            val condition = this.initialCondition.get()
            val configuration = condition.configuration
            this.conditionTypesSelector.selectedItem = condition.typeInstance
            this.operationSelector.selectedItem = configuration.operation
            this.negativeMatchCheckBox.isSelected = configuration.negativeMatch
            for (fieldDefinition in condition.typeInstance.extraFields) {
                val value =
                    configuration.extraFields[fieldDefinition.name]
                        ?: throw IllegalStateException("Missing extra field value for ${fieldDefinition.name}")
                val component =
                    this.extraFields[fieldDefinition.name]
                        ?: throw IllegalStateException("Missing extra field component for ${fieldDefinition.name}")
                when (component) {
                    is JTextField -> {
                        component.text = value
                    }

                    is JComboBox<*> -> {
                        component.selectedItem = value
                    }

                    is JCheckBox -> {
                        component.isSelected = value.toBoolean()
                    }
                }
            }
        }
    }

    private fun generateExtraFieldsComponents() {
        this.extraFields.clear()
        for (field in this.selectedConditionType.extraFields) {
            val component =
                when (field.type) {
                    ConditionField.FieldType.TEXT -> {
                        val defaultText = field.defaultText ?: ""
                        JTextField(defaultText).also {
                            it.document.addDocumentListener(
                                object : DocumentListener {
                                    override fun insertUpdate(e: DocumentEvent) = validateConfiguration()

                                    override fun removeUpdate(e: DocumentEvent) = validateConfiguration()

                                    override fun changedUpdate(e: DocumentEvent) = validateConfiguration()
                                },
                            )
                        }
                    }

                    ConditionField.FieldType.MULTIPLE_CHOICE -> {
                        val defaultChoice = field.defaultChoice
                        val comboBox = JComboBox(field.choices)
                        if (defaultChoice != null) {
                            comboBox.selectedItem = defaultChoice
                        }
                        comboBox.addActionListener { validateConfiguration() }
                        comboBox
                    }

                    ConditionField.FieldType.BOOLEAN -> {
                        JCheckBox().also {
                            it.isSelected = field.defaultBoolean
                            it.addActionListener { validateConfiguration() }
                        }
                    }
                }
            this.extraFields[field.name] = component
        }
    }

    val controlsPanel = JPanel(GridBagLayout())

    private fun redrawControls() {
        controlsPanel.removeAll()

        val controls =
            arrayOf(
                Pair("Type", conditionTypesSelector),
                Pair("Operation", operationSelector),
                *this.extraFields.map { (name, component) -> Pair(name, component) }.toTypedArray(),
                Pair("Negative match", negativeMatchCheckBox),
                Pair("Validation", validationMessageLabel),
            )

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
            controlsPanel.add(label, c)

            // Add combo box
            c.anchor = GridBagConstraints.LINE_END
            c.fill = GridBagConstraints.HORIZONTAL
            c.gridx = 1
            c.ipadx = 100
            c.weightx = 1.0
            c.gridwidth = 2
            controlsPanel.add(control, c)

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
        val buttonPanel =
            JPanel().also {
                it.add(saveButton)
                it.add(cancelButton)
            }
        controlsPanel.add(buttonPanel, c)
    }

    init {
        // Set UI properties
        this.isResizable = true
        this.isAutoRequestFocus = true
        this.modalityType = ModalityType.APPLICATION_MODAL

        saveButton.isEnabled = false
        generateExtraFieldsComponents()
        redrawControls()

        val section = UISection("Condition Details", "Specify the match condition", controlsPanel)

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

            this.generateExtraFieldsComponents()
            this.redrawControls()
            this.autoSize()

            // Validate configuration
            this.validateConfiguration()
        }
        this.operationSelector.addActionListener { this.validateConfiguration() }

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
