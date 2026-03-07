package sessionswitcher.settings

import sessionswitcher.Logger
import sessionswitcher.ui.PDControlScrollPane
import java.awt.FlowLayout
import javax.swing.Box
import javax.swing.BoxLayout
import javax.swing.JCheckBox
import javax.swing.JComboBox
import javax.swing.JComponent
import javax.swing.JLabel
import javax.swing.JPanel
import javax.swing.JScrollPane
import javax.swing.JSpinner
import javax.swing.JTextArea
import javax.swing.JTextField
import javax.swing.SpinnerNumberModel
import javax.swing.event.DocumentEvent
import javax.swing.event.DocumentListener

abstract class SettingsItem<T>(
    val provider: SettingsProvider,
    val key: String,
    val description: String,
    val default: T
) {

    /* Used for determining where to read the setting from */
    enum class Scope {
        DEFAULT,            // Default value
        GLOBAL,             // Extension global setting
        PROJECT,            // Extension project setting
        EFFECTIVE_GLOBAL,   // GLOBAL, then DEFAULT
        EFFECTIVE,          // PROJECT, then GLOBAL, then DEFAULT
    }

    /* Used for determining where the setting is ultimately stored */
    enum class Store {
        GLOBAL,
        PROJECT
    }

    abstract fun getType(): String
    abstract fun getImpl(key: String, store: Store): T?
    abstract fun setImpl(key: String, value: T, store: Store)

    fun get(scope: Scope = Scope.EFFECTIVE): T {
        var output: T? = null
        var selectedScope: Scope = Scope.DEFAULT

        // With EFFECTIVE, Project takes precedence over Global
        if (scope == Scope.PROJECT || scope == Scope.EFFECTIVE) {
            output = this.getImpl(key, Store.PROJECT)
            selectedScope = Scope.PROJECT
        }
        if (output == null && (scope == Scope.GLOBAL || scope == Scope.EFFECTIVE || scope == Scope.EFFECTIVE_GLOBAL)) {
            output = this.getImpl(key, Store.GLOBAL)
            selectedScope = Scope.GLOBAL
        }
        if (output == null) {
            output = this.default
            selectedScope = Scope.DEFAULT
        }

        var logStr = "Search $key (scope $scope): "
        logStr += if (output != null) {
            "$output (from $selectedScope)"
        } else {
            "not found"
        }
        Logger.debug(logStr)
        return output!!
    }

    fun set(value: T, store: Store) {
        Logger.debug("Setting config value: $key=$value (store: $store)")
        this.setImpl(key, value, store)
    }

    fun clear(key: String, store: Store) {
        provider.deleteBoolean(key, store)
        provider.deleteInt(key, store)
        provider.deleteString(key, store)
    }
}

class BooleanSetting(provider: SettingsProvider, key: String, description: String, default: Boolean) :
    SettingsItem<Boolean>(provider, key, description, default) {
    override fun getType(): String = "Boolean"
    override fun getImpl(key: String, store: Store): Boolean? = this.provider.getBoolean(key, store)
    override fun setImpl(key: String, value: Boolean, store: Store) = this.provider.setBoolean(key, value, store)
    fun drawCheckbox(store: Store): JCheckBox {
        val scope = if (store == Store.GLOBAL) Scope.EFFECTIVE_GLOBAL else Scope.EFFECTIVE
        val checkbox = JCheckBox(this.description, this.get(scope))
        checkbox.addItemListener { this.set(checkbox.isSelected, store) }
        return checkbox
    }
}

class IntSetting(
    provider: SettingsProvider,
    key: String,
    description: String,
    default: Int,
    val min: Int? = null,
    val max: Int? = null
) : SettingsItem<Int>(provider, key, description, default) {
    override fun getType(): String = "Int"
    override fun getImpl(key: String, store: Store): Int? = this.provider.getInt(key, store)
    override fun setImpl(key: String, value: Int, store: Store) {
        if (min != null && value < min) {
            throw Exception("Tried to set a value ($value) less than the min ($min) for field $key")
        }
        if (max != null && value > max) {
            throw Exception("Tried to set a value ($value) more than the max ($max) for field $key")
        }
        this.provider.setInt(key, value, store)
    }

    fun drawSpinner(store: Store, withPanel: Boolean = false): JComponent {
        val scope = if (store == Store.GLOBAL) Scope.EFFECTIVE_GLOBAL else Scope.EFFECTIVE
        val spinner = JSpinner(SpinnerNumberModel(this.get(scope), this.min, this.max, 1))
        spinner.value = this.get(scope)
        spinner.addChangeListener { this.set(spinner.value as Int, store) }
        if (!withPanel) {
            return spinner
        }

        return JPanel().also {
            it.layout = FlowLayout(FlowLayout.LEFT, 5, 0)
            it.add(JLabel(description))
            it.add(spinner)
        }
    }
}

class EnumSetting<T : Enum<T>>(
    provider: SettingsProvider,
    key: String,
    description: String,
    private val enum: Class<T>,
    default: T
) : SettingsItem<T>(provider, key, description, default) {
    override fun getType(): String = "Enum"
    override fun getImpl(key: String, store: Store): T? {
        val strValue = this.provider.getString(key, store) ?: return null
        return java.lang.Enum.valueOf(enum, strValue)
    }

    override fun setImpl(key: String, value: T, store: Store) {
        this.provider.setString(key, value.name, store)
    }

    @Suppress("UNCHECKED_CAST")
    fun drawComboBox(store: Store, withPanel: Boolean = false): JComponent {
        val comboBox = JComboBox<T>(this.enum.enumConstants)

        val scope = if (store == Store.GLOBAL) Scope.EFFECTIVE_GLOBAL else Scope.EFFECTIVE
        comboBox.selectedItem = this.get(scope)
        comboBox.addItemListener { this.set(comboBox.selectedItem as T, store) }

        if (!withPanel) {
            return comboBox
        }

        return JPanel().also {
            it.layout = FlowLayout(FlowLayout.LEFT, 5, 0)
            it.add(JLabel(description))
            it.add(comboBox)
        }
    }
}

class StringSetting(
    provider: SettingsProvider,
    key: String,
    description: String,
    default: String,
    val allowedChoices: Array<String>? = null
) : SettingsItem<String>(provider, key, description, default) {
    override fun getType(): String = "String"
    override fun getImpl(key: String, store: Store): String? = this.provider.getString(key, store)
    override fun setImpl(key: String, value: String, store: Store) {
        if (allowedChoices != null && !allowedChoices.contains(value)) {
            throw Exception(
                "Tried to set a value ($value) which is not among the allowed choices: [${
                    allowedChoices.joinToString(
                        ","
                    )
                }] for field $key"
            )
        }
        this.provider.setString(key, value, store)
    }

    class SimpleDocumentListener(val callback: () -> Unit) : DocumentListener {
        override fun insertUpdate(e: DocumentEvent?) = this.callback()
        override fun removeUpdate(e: DocumentEvent?) = this.callback()
        override fun changedUpdate(e: DocumentEvent?) = this.callback()
    }

    fun drawComboBox(store: Store, withPanel: Boolean = false): JComponent {
        val scope = if (store == Store.GLOBAL) Scope.EFFECTIVE_GLOBAL else Scope.EFFECTIVE
        if (this.allowedChoices == null) {
            throw Exception("Can't draw JComboBox from setting with no allowedChoices: $key")
        }
        if (!allowedChoices.contains(default)) {
            throw Exception("Default value ($default) not in allowedChoices [${allowedChoices.joinToString(",")}] for field $key")
        }
        var currentValue = this.get(scope)
        if (!allowedChoices.contains(currentValue)) {
            currentValue = this.default
        }

        val comboBox = JComboBox<String>(this.allowedChoices)
        comboBox.selectedItem = currentValue
        comboBox.addItemListener { this.set(comboBox.selectedItem as String, store) }

        if (!withPanel) {
            return comboBox
        }

        return JPanel().also {
            it.layout = FlowLayout(FlowLayout.LEFT, 5, 0)
            it.add(JLabel(description))
            it.add(comboBox)
        }
    }

    fun drawTextField(store: Store, withPanel: Boolean = false, columns: Int = 20): JComponent {
        val scope = if (store == Store.GLOBAL) Scope.EFFECTIVE_GLOBAL else Scope.EFFECTIVE
        val textField = JTextField(columns)
        textField.text = this.get(scope)
        textField.document.addDocumentListener(SimpleDocumentListener { this.set(textField.text, store) })

        if (!withPanel) {
            return textField
        }

        return JPanel().also {
            it.layout = FlowLayout(FlowLayout.LEFT, 5, 0)
            it.add(JLabel(description))
            it.add(textField)
        }
    }

    fun drawTextArea(store: Store, withPanel: Boolean = false, rows: Int, columns: Int = 20): JComponent {
        val scope = if (store == Store.GLOBAL) Scope.EFFECTIVE_GLOBAL else Scope.EFFECTIVE
        val textArea = JTextArea(rows, columns).also {
            it.isEditable = true
            it.isOpaque = true
            it.lineWrap = true
            it.wrapStyleWord = true
            it.text = this.get(scope)
        }
        textArea.document.addDocumentListener(SimpleDocumentListener { this.set(textArea.text, store) })

        val scrollable = PDControlScrollPane(textArea)
        scrollable.verticalScrollBarPolicy = JScrollPane.VERTICAL_SCROLLBAR_ALWAYS
        scrollable.horizontalScrollBarPolicy = JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED

        if (!withPanel) {
            return scrollable
        }

        //val label = JPanel(BorderLayout()).also { it.add() }
        return JPanel().also {
            it.layout = BoxLayout(it, BoxLayout.Y_AXIS)
            it.add(JLabel(description))
            it.add(Box.createVerticalStrut(5))
            it.add(scrollable)
        }
    }
}