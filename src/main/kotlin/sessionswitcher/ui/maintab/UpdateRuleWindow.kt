package sessionswitcher.ui.maintab

import sessionswitcher.Logger
import sessionswitcher.SessionSwitcher
import sessionswitcher.rules.autoupdate.UpdateConfig
import sessionswitcher.rules.autoupdate.UpdateRule
import sessionswitcher.rules.conditions.Condition
import sessionswitcher.sessions.CookiesUpdateMode
import sessionswitcher.sessions.HeadersUpdateMode
import sessionswitcher.ui.ButtonPrimary
import sessionswitcher.ui.UISection
import sessionswitcher.ui.tables.ConditionsTableModel
import java.awt.Dimension
import java.awt.GridBagConstraints
import java.awt.GridBagLayout
import java.awt.Insets
import java.awt.event.ItemEvent
import java.util.*
import javax.swing.*
import kotlin.math.min

class UpdateRuleWindow(private val sessionSwitcher: SessionSwitcher, private val initialUpdateRule: Optional<UpdateRule>) :
    JDialog(sessionSwitcher.montoyaApi.userInterface().swingUtils().suiteFrame(), if (initialUpdateRule.isEmpty) "New Update Rule" else "Edit Update Rule", true) {

    // Flags
    var shouldSave = false

    // Conditions
    val conditions = ArrayList<Condition>()

    // UI elements
    val saveButton = ButtonPrimary("Save")
    val cancelButton = JButton("Cancel")
    val tableSection = TableSection("Conditions", "Conditions in this list are evaluated with a logical AND", ConditionsTableModel(conditions), showRefreshButton = false)

    val sessionSelector = JComboBox<String>()

    // Combo box valid options
    private val requestCookiesUpdateOptions = CookiesUpdateMode.entries.toTypedArray()
    private val responseCookiesUpdateOptions = CookiesUpdateMode.entries.filterNot { it == CookiesUpdateMode.MIRROR }.toTypedArray()
    private val defaultRequestCookieUpdateOption = CookiesUpdateMode.MIRROR
    private val defaultResponseCookieUpdateOption = CookiesUpdateMode.ADD_ALL
    private val headersUpdateOptions = HeadersUpdateMode.entries.toTypedArray()
    private val defaultHeadersUpdateOption = HeadersUpdateMode.UPDATE_EXISTING
    private val updateSourceOptions = UpdateConfig.UpdateSource.entries.filterNot { it == UpdateConfig.UpdateSource.RESPONSE }.toTypedArray() // Disable response parsing for now

    val updateSourceSelector = JComboBox<UpdateConfig.UpdateSource>(updateSourceOptions)
    val cookieModeSelector = JComboBox<CookiesUpdateMode>(requestCookiesUpdateOptions)
    val headersModeSelector = JComboBox<HeadersUpdateMode>(headersUpdateOptions)

    fun autoSize() {
        // Gets the size of the screen the Burp window is on (for multi-monitor setups)
        val screenSize = sessionSwitcher.montoyaApi.userInterface().swingUtils().suiteFrame().graphicsConfiguration.device.displayMode

        val reasonableHeight = min(this.preferredSize.height, screenSize.height - 50)
        //this.preferredSize = Dimension(reasonableWidth, reasonableHeight)

        // Set the maximum size of the frame to match its content
        this.maximumSize = Dimension(screenSize.width, reasonableHeight)

        // Set the minimum size to something reasonable as well
        //this.minimumSize = Dimension(this.minimumSize.width, 400)

        this.minimumSize = Dimension(680, 480)
        this.preferredSize = Dimension(1100, 550)

        // Pack the window to fit its content
        this.pack()
        this.setLocationRelativeTo(sessionSwitcher.montoyaApi.userInterface().swingUtils().suiteFrame())
    }

    private fun loadInitialRule() {
        if (this.initialUpdateRule.isPresent) {
            // Load conditions
            this.conditions.addAll(this.initialUpdateRule.get().conditions)

            // Set session
            val sessionName = this.initialUpdateRule.get().session.name
            val session = this.sessionSwitcher.sessions.getSession(sessionName)
            if (session == null) {
                Logger.warning("RefreshRule: Session with name $sessionName not found, cannot set initial session")
                return
            }
            this.sessionSelector.selectedItem = sessionName

            // Set update config
            val config = this.initialUpdateRule.get().config
            this.updateSourceSelector.selectedItem = config.updateSource
            this.cookieModeSelector.selectedItem = config.cookiesUpdateMode
            this.headersModeSelector.selectedItem = config.headersUpdateMode
        }
    }

    fun refreshConditionsTable() {
        this.tableSection.refreshTable()
    }

    fun makeRule(): UpdateRule {
        val sessionName = this.sessionSelector.selectedItem as String
        if (sessionName == "(No sessions)") {
            throw IllegalStateException("No sessions selected")
        }
        val session = sessionSwitcher.sessions.getSession(sessionName)
            ?: throw IllegalStateException("Session with name $sessionName not found")

        val config = UpdateConfig.make(
            updateSourceSelector.selectedItem as UpdateConfig.UpdateSource,
            cookieModeSelector.selectedItem as CookiesUpdateMode,
            headersModeSelector.selectedItem as HeadersUpdateMode,
        )

        return if (initialUpdateRule.isPresent) {
            UpdateRule(this.conditions.toTypedArray(), session, config, initialUpdateRule.get().ruleId)
        } else {
            UpdateRule(this.conditions.toTypedArray(), session, config)
        }
    }

    public fun showDialog(): Optional<UpdateRule> {
        this.isVisible = true
        return if (this.shouldSave) {
            Optional.of(makeRule())
        } else {
            Optional.empty()
        }
    }

    fun newButtonCallback() {
        val newCondition = ConditionEditWindow(this, Optional.empty<Condition>()).showDialog()
        if (!newCondition.isPresent) {
            return
        }
        this.conditions.add(newCondition.get())
        this.refreshConditionsTable()
        this.checkEnableSaveButton()
    }

    fun editButtonCallback() {
        val selectedCondition = this.tableSection.getSelected()
        if (selectedCondition.isEmpty) {
            Logger.warning("Delete button clicked but no table item selected, row: ${this.tableSection.table.selectedRow}")
            return
        }

        val newCondition = ConditionEditWindow(this, Optional.of(selectedCondition.get())).showDialog()
        if (!newCondition.isPresent) {
            return
        }

        val oldIndex = this.conditions.indexOf(selectedCondition.get())
        this.conditions.remove(selectedCondition.get())
        this.conditions.add(oldIndex, newCondition.get())
        this.refreshConditionsTable()
        this.checkEnableSaveButton()
    }

    fun deleteButtonCallback() {
        val selectedCondition = this.tableSection.getSelected()
        if (selectedCondition.isEmpty) {
            Logger.warning("Delete button clicked but no table item selected, row: ${this.tableSection.table.selectedRow}")
            return
        }
        this.conditions.remove(selectedCondition.get())
        this.refreshConditionsTable()
        this.checkEnableSaveButton()
    }

    fun duplicateButtonCallback() {
        val selectedCondition = this.tableSection.getSelected()
        if (selectedCondition.isEmpty) {
            Logger.warning("Duplicate button clicked but no table item selected, row: ${this.tableSection.table.selectedRow}")
            return
        }
        this.conditions.add(selectedCondition.get().copy())
        this.refreshConditionsTable()
        this.checkEnableSaveButton()
    }

    private fun updateOptionsSection(): JPanel {
        val controls = arrayOf(
            Pair("Session to update:", sessionSelector),
            Pair("Update from:", updateSourceSelector),
            Pair("Cookie update mode:", cookieModeSelector),
            Pair("Headers update mode:", headersModeSelector),
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
        return panel
    }

    init {
        // Set window properties
        this.isResizable = true
        this.isAutoRequestFocus = true

        saveButton.isEnabled = false

        tableSection.setNewButtonCallback(this::newButtonCallback)
        tableSection.setEditButtonCallback(this::editButtonCallback)
        tableSection.setDeleteButtonCallback(this::deleteButtonCallback)
        tableSection.setDuplicateButtonCallback(this::duplicateButtonCallback)

        // Update config
        var sessions = SessionSwitcher.getInstance().sessions.getSessionNames().toTypedArray()
        if (sessions.isEmpty()) {
            sessions = arrayOf("(No sessions)")
        }
        sessions.forEach { this.sessionSelector.addItem(it) }

        sessionSelector.addItemListener { this.checkEnableSaveButton() }
        tableSection.tableModel

        updateSourceSelector.addItemListener { it ->
            if (it.stateChange == ItemEvent.SELECTED) {
                val selectedItem = updateSourceSelector.selectedItem
                cookieModeSelector.removeAllItems()
                if (selectedItem == "Request") {
                    cookieModeSelector.removeAllItems()
                    requestCookiesUpdateOptions.forEach { item -> cookieModeSelector.addItem(item) }
                    cookieModeSelector.selectedItem = defaultRequestCookieUpdateOption
                    headersModeSelector.selectedItem = defaultHeadersUpdateOption
                    headersModeSelector.isEnabled = true
                } else {
                    responseCookiesUpdateOptions.forEach { item -> cookieModeSelector.addItem(item) }
                    cookieModeSelector.selectedItem = defaultResponseCookieUpdateOption
                    headersModeSelector.isEnabled = false
                }
            }
        }

        cookieModeSelector.selectedItem = defaultRequestCookieUpdateOption
        headersModeSelector.selectedItem = defaultHeadersUpdateOption

        val updateConfigSection = UISection("Update Options", null, updateOptionsSection())

        // Build the main window
        val panel = JPanel().also {
            it.border = BorderFactory.createEmptyBorder(5,5, 5, 5)
            it.layout = BoxLayout(it, BoxLayout.Y_AXIS)
            it.add(tableSection.getComponent())
            it.add(updateConfigSection)
            it.add(JPanel().also { p ->
                p.layout = BoxLayout(p, BoxLayout.X_AXIS)
                p.add(Box.createHorizontalGlue())
                p.add(saveButton)
                p.add(Box.createHorizontalStrut(5))
                p.add(cancelButton)
            })
        }

        // Set button listeners
        saveButton.addActionListener {
            this.shouldSave = true
            this.dispose()
        }
        cancelButton.addActionListener {
            this.shouldSave = false
            this.dispose()
        }

        val scrollable = JScrollPane(panel)
        scrollable.verticalScrollBarPolicy = JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED
        scrollable.horizontalScrollBarPolicy = JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED

        this.add(panel)
        this.autoSize()
        this.checkEnableSaveButton()

        loadInitialRule()
    }

    fun checkEnableSaveButton() {
        val enable = tableSection.table.rowCount > 0 && sessionSelector.selectedItem != "(No sessions)" && sessionSelector.selectedItem != ""
        saveButton.isEnabled = enable
    }
}
