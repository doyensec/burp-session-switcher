package sessionswitcher.settings

import sessionswitcher.Logger
import sessionswitcher.ui.PDControlScrollPane
import java.awt.FlowLayout
import javax.swing.*
import javax.swing.event.DocumentEvent
import javax.swing.event.DocumentListener

abstract class SettingsItem<T>(val provider: SettingsProvider, val key: String, val description: String, val default: T) {
    enum class Scope {
        DEFAULT,
        GLOBAL,
        PROJECT,
        EFFECTIVE_GLOBAL, // always match except project
        EFFECTIVE, // always match
    }
    enum class Store {
        GLOBAL,
        PROJECT
    }
    abstract fun getType(): String
    abstract fun _get(key: String, store: Store): T?
    abstract fun _set(key: String, value: T, store: Store)

    public fun get(scope: Scope = Scope.EFFECTIVE): T {
        var output: T? = null
        var scopeLog: Scope = Scope.DEFAULT

        if (scope == Scope.PROJECT || scope == Scope.EFFECTIVE) {
            output = this._get(key, Store.PROJECT)
            scopeLog = Scope.PROJECT
        }
        if (output == null && (scope == Scope.GLOBAL || scope == Scope.EFFECTIVE || scope == Scope.EFFECTIVE_GLOBAL)) {
            output = this._get(key, Store.GLOBAL)
            scopeLog = Scope.GLOBAL
        }
        if (output == null) {
            output = this.default
            scopeLog = Scope.DEFAULT
        }

        var logStr = "Search $key (scope $scope): "
        logStr += if (output != null) {
            "$output (from $scopeLog)"
        } else {
            "not found"
        }
        Logger.debug(logStr)
        return output!!
    }

    fun set(value: T, store: Store = Store.PROJECT) {
        Logger.debug("Setting config value: $key=$value (store: $store)")
        this._set(key, value, store)
    }

    fun clear(key: String, store: Store = Store.PROJECT) {
        provider.deleteBoolean(key, store)
        provider.deleteInt(key, store)
        provider.deleteString(key, store)
    }
}

class BooleanSetting(provider: SettingsProvider, key: String, description: String, default: Boolean): SettingsItem<Boolean>(provider, key, description, default) {
    override fun getType(): String = "Boolean"
    override fun _get(key: String, store: Store): Boolean? = this.provider.getBoolean(key)
    override fun _set(key: String, value: Boolean, store: Store) = this.provider.setBoolean(key, value)
    fun drawCheckbox(): JCheckBox {
        val checkbox = JCheckBox(this.description, this.get(Scope.EFFECTIVE_GLOBAL))
        checkbox.addItemListener{ this.set(checkbox.isSelected, Store.GLOBAL) }
        return checkbox
    }
}

class IntSetting(provider: SettingsProvider, key: String, description: String, default: Int, val min: Int? = null, val max: Int? = null): SettingsItem<Int>(provider, key, description, default) {
    override fun getType(): String = "Int"
    override fun _get(key: String, store: Store): Int? = this.provider.getInt(key)
    override fun _set(key: String, value: Int, store: Store) {
        if (min != null && value < min) {
            throw Exception("Tried to set a value ($value) less than the min ($min) for field $key")
        }
        if (max != null && value > max) {
            throw Exception("Tried to set a value ($value) more than the max ($max) for field $key")
        }
        this.provider.setInt(key, value)
    }
    fun drawSpinner(withPanel: Boolean = false): JComponent {
        val spinner = JSpinner(SpinnerNumberModel(this.get(Scope.EFFECTIVE_GLOBAL), this.min, this.max, 1))
        spinner.value = this.get(Scope.EFFECTIVE_GLOBAL)
        spinner.addChangeListener{ this.set(spinner.value as Int, Store.GLOBAL) }
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

class EnumSetting<T: Enum<T>>(provider: SettingsProvider, key: String, description: String, private val enum: Class<T>, default: T): SettingsItem<T>(provider, key, description, default) {
    override fun getType(): String = "Enum"
    override fun _get(key: String, store: Store): T? {
        val strValue = this.provider.getString(key)?: return null
        return java.lang.Enum.valueOf(enum, strValue)
    }
    override fun _set(key: String, value: T, store: Store) {
        this.provider.setString(key, value.name)
    }

    @Suppress("UNCHECKED_CAST")
    fun drawComboBox(withPanel: Boolean = false): JComponent {
        val comboBox = JComboBox<T>(this.enum.enumConstants)
        comboBox.selectedItem = this.get(Scope.EFFECTIVE_GLOBAL)
        comboBox.addItemListener { this.set(comboBox.selectedItem as T, Store.GLOBAL) }

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

class StringSetting(provider: SettingsProvider, key: String, description: String, default: String, val allowedChoices: Array<String>? = null): SettingsItem<String>(provider, key, description, default) {
    override fun getType(): String = "String"
    override fun _get(key: String, store: Store): String? = this.provider.getString(key)
    override fun _set(key: String, value: String, store: Store) {
        if (allowedChoices != null && !allowedChoices.contains(value)) {
            throw Exception("Tried to set a value ($value) which is not among the allowed choices: [${allowedChoices.joinToString(",")}] for field $key")
        }
        this.provider.setString(key, value)
    }

    class SimpleDocumentListener(val callback: () -> Unit) : DocumentListener {
        override fun insertUpdate(e: DocumentEvent?) = this.callback()
        override fun removeUpdate(e: DocumentEvent?) = this.callback()
        override fun changedUpdate(e: DocumentEvent?) = this.callback()
    }

    fun drawComboBox(withPanel: Boolean = false): JComponent {
        if (this.allowedChoices == null) {
            throw Exception("Can't draw JComboBox from setting with no allowedChoices: $key")
        }
        if (!allowedChoices.contains(default)) {
            throw Exception("Default value ($default) not in allowedChoices [${allowedChoices.joinToString(",")}] for field $key")
        }
        var currentValue = this.get(Scope.EFFECTIVE_GLOBAL)
        if (!allowedChoices.contains(currentValue)) {
            currentValue = this.default
        }

        val comboBox = JComboBox<String>(this.allowedChoices)
        comboBox.selectedItem = currentValue
        comboBox.addItemListener { this.set(comboBox.selectedItem as String, Store.GLOBAL) }

        if (!withPanel) {
            return comboBox
        }

        return JPanel().also {
            it.layout = FlowLayout(FlowLayout.LEFT, 5, 0)
            it.add(JLabel(description))
            it.add(comboBox)
        }
    }

    fun drawTextField(withPanel: Boolean = false, columns: Int = 20): JComponent {
        val textField = JTextField(columns)
        textField.text = this.get(Scope.EFFECTIVE_GLOBAL)
        textField.document.addDocumentListener(SimpleDocumentListener { this.set(textField.text, Store.GLOBAL) })

        if (!withPanel) {
            return textField
        }

        return JPanel().also {
            it.layout = FlowLayout(FlowLayout.LEFT, 5, 0)
            it.add(JLabel(description))
            it.add(textField)
        }
    }

    fun drawTextArea(withPanel: Boolean = false, rows: Int, columns: Int = 20): JComponent {
        val textArea = JTextArea(rows, columns).also {
            it.isEditable = true
            it.isOpaque = true
            it.lineWrap = true
            it.wrapStyleWord = true
            it.text = this.get(Scope.EFFECTIVE_GLOBAL)
        }
        textArea.document.addDocumentListener(SimpleDocumentListener { this.set(textArea.text, Store.GLOBAL) })

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