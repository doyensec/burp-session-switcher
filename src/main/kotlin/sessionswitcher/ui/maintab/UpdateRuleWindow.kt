package sessionswitcher.ui.maintab

import sessionswitcher.Logger
import sessionswitcher.SessionSwitcher
import sessionswitcher.rules.autoupdate.UpdateConfig
import sessionswitcher.rules.autoupdate.UpdateRule
import sessionswitcher.rules.conditions.Condition
import sessionswitcher.ui.ButtonPrimary
import sessionswitcher.ui.ComboBox
import sessionswitcher.ui.UISection
import sessionswitcher.ui.maintab.tables.ConditionsTableModel
import java.awt.Dimension
import java.awt.event.ItemEvent
import java.util.*
import javax.swing.*
import kotlin.math.min

class UpdateRuleWindow(private val sessionSwitcher: SessionSwitcher, private val initialUpdateRule: Optional<UpdateRule>) :
    JDialog(sessionSwitcher.montoyaApi.userInterface().swingUtils().suiteFrame(), if (initialUpdateRule.isEmpty) "New Update Rule" else "Edit Update Rule", true) {

    // Flags
    var cancelPressed = false

    // Conditions
    val conditions = ArrayList<Condition>()

    // UI elements
    val saveButton = ButtonPrimary("Save")
    val cancelButton = JButton("Cancel")
    val tableSection = TableSection("Conditions", "Conditions in this list are evaluated with a logical AND", ConditionsTableModel(conditions), showRefreshButton = false)

    val sessionSelector = ComboBox("Session to update")

    val updateourceSelector = ComboBox("Update data from", "Request", "Response")
    val REQUEST_COOKIES_UPDATE_OPTIONS = arrayOf("Replace All", "Update All", "Update Existing", "Don't update")
    val RESPONSE_COOKIES_UPDATE_OPTIONS = arrayOf("Update All", "Update Existing", "Don't update")
    val cookieUpdateMode = ComboBox("Cookie update mode", *REQUEST_COOKIES_UPDATE_OPTIONS)
    val headerUpdateMode = ComboBox("Header update mode", "Update Existing", "Don't update")

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

    fun refreshConditionsTable() {
        this.tableSection.refreshTable()
    }

    fun makeRule(): UpdateRule {
        val sessionName = this.sessionSelector.getSelectedItem()
        if (sessionName == "(No sessions)") {
            throw IllegalStateException("No sessions selected")
        }
        val session = sessionSwitcher.sessions.getSession(sessionName)
            ?: throw IllegalStateException("Session with name $sessionName not found")

        return UpdateRule(this.conditions.toTypedArray(), session, UpdateConfig.make(UpdateConfig.UPDATE_SOURCE.RESPONSE, UpdateConfig.COOKIE_UPDATE_MODE.NO_UPDATE, UpdateConfig.HEADER_UPDATE_MODE.NO_UPDATE)) // TODO: Add update config
    }

    public fun showDialog(): Optional<UpdateRule> {
        this.isVisible = true
        return if (this.cancelPressed) {
            Optional.empty()
        } else {
            Optional.of(makeRule())
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
        TODO()
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
        sessions.forEach { this.sessionSelector.component.addItem(it) }

        sessionSelector.addItemListener { this.checkEnableSaveButton() }
        tableSection.tableModel

        updateourceSelector.addItemListener { it ->
            if (it.stateChange == ItemEvent.SELECTED) {
                val selectedItem = updateourceSelector.getSelectedItem()
                cookieUpdateMode.component.removeAllItems()
                if (selectedItem == "Request") {
                    cookieUpdateMode.component.removeAllItems()
                    REQUEST_COOKIES_UPDATE_OPTIONS.forEach { item -> cookieUpdateMode.component.addItem(item) }
                } else {
                    RESPONSE_COOKIES_UPDATE_OPTIONS.forEach { item -> cookieUpdateMode.component.addItem(item) }
                }
            }
        }

        val outerPanel = JPanel().also {
            it.layout = BoxLayout(it, BoxLayout.Y_AXIS)
            it.add(sessionSelector)
            it.add(Box.createVerticalStrut(5))
            it.add(updateourceSelector)
            it.add(Box.createVerticalStrut(5))
            it.add(cookieUpdateMode)
            it.add(Box.createVerticalStrut(5))
            it.add(headerUpdateMode)

        }
        val updateConfigSection = UISection("Update Options", null, outerPanel)

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
            this.cancelPressed = false
            this.dispose()
        }
        cancelButton.addActionListener {
            this.cancelPressed = true
            this.dispose()
        }

        val scrollable = JScrollPane(panel)
        scrollable.verticalScrollBarPolicy = JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED
        scrollable.horizontalScrollBarPolicy = JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED

        this.add(panel)
        this.autoSize()
        this.checkEnableSaveButton()
    }

    fun checkEnableSaveButton() {
        val enable = tableSection.table.rowCount > 0 && sessionSelector.getSelectedItem() != "(No sessions)" && sessionSelector.getSelectedItem() != ""
        saveButton.isEnabled = enable
    }
}
