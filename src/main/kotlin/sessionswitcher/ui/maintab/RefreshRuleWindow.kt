package sessionswitcher.ui.maintab

import sessionswitcher.Logger
import sessionswitcher.SessionSwitcher
import sessionswitcher.rules.conditions.Condition
import sessionswitcher.rules.refresher.RefreshConfig
import sessionswitcher.rules.refresher.RefreshRule
import sessionswitcher.ui.ButtonPrimary
import sessionswitcher.ui.ComboBox
import sessionswitcher.ui.UISection
import sessionswitcher.ui.maintab.tables.ConditionsTableModel
import java.awt.Dimension
import java.awt.event.ItemEvent
import java.util.*
import javax.swing.*
import kotlin.math.min

class RefreshRuleWindow(private val sessionSwitcher: SessionSwitcher, private val initialRefreshRule: Optional<RefreshRule>) :
    JDialog(sessionSwitcher.montoyaApi.userInterface().swingUtils().suiteFrame(), if (initialRefreshRule.isEmpty) "New Refresh Rule" else "Edit Refresh Rule", true) {

    // Flags
    var cancelPressed = false

    // Conditions
    val conditions = ArrayList<Condition>()

    // UI elements
    val saveButton = ButtonPrimary("Save")
    val cancelButton = JButton("Cancel")
    val tableSection = TableSection("Conditions", "Conditions in this list are evaluated with a logical AND", ConditionsTableModel(conditions), showRefreshButton = false)

    val sessionSelector = ComboBox("Session to refresh")

    val refreshSourceSelector = ComboBox("Refresh data from", "Request", "Response")
    val REQUEST_COOKIES_REFRESH_OPTIONS = arrayOf("Replace All", "Refresh All", "Refresh Existing", "Don't refresh")
    val RESPONSE_COOKIES_REFRESH_OPTIONS = arrayOf("Refresh All", "Refresh Existing", "Don't refresh")
    val cookieRefreshMode = ComboBox("Cookie refresh mode", *REQUEST_COOKIES_REFRESH_OPTIONS)
    val headerRefreshMode = ComboBox("Header refresh mode", "Refresh Existing", "Don't refresh")

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

    fun makeRule(): RefreshRule {
        val sessionName = this.sessionSelector.getSelectedItem()
        if (sessionName == "(No sessions)") {
            throw IllegalStateException("No sessions selected")
        }
        val session = sessionSwitcher.sessions.getSession(sessionName)
            ?: throw IllegalStateException("Session with name $sessionName not found")

        return RefreshRule(this.conditions.toTypedArray(), session, RefreshConfig()) // TODO: Add refresh config
    }

    public fun showDialog(): Optional<RefreshRule> {
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
        TODO("Not yet implemented")
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

        // Refresh config
        var sessions = SessionSwitcher.getInstance().sessions.getSessionNames().toTypedArray()
        if (sessions.isEmpty()) {
            sessions = arrayOf("(No sessions)")
        }
        sessions.forEach { this.sessionSelector.component.addItem(it) }

        sessionSelector.addItemListener { this.checkEnableSaveButton() }
        tableSection.tableModel

        refreshSourceSelector.addItemListener { it ->
            if (it.stateChange == ItemEvent.SELECTED) {
                val selectedItem = refreshSourceSelector.getSelectedItem()
                cookieRefreshMode.component.removeAllItems()
                if (selectedItem == "Request") {
                    cookieRefreshMode.component.removeAllItems()
                    REQUEST_COOKIES_REFRESH_OPTIONS.forEach { item -> cookieRefreshMode.component.addItem(item) }
                } else {
                    RESPONSE_COOKIES_REFRESH_OPTIONS.forEach { item -> cookieRefreshMode.component.addItem(item) }
                }
            }
        }

        val outerPanel = JPanel().also {
            it.layout = BoxLayout(it, BoxLayout.Y_AXIS)
            it.add(sessionSelector)
            it.add(Box.createVerticalStrut(5))
            it.add(refreshSourceSelector)
            it.add(Box.createVerticalStrut(5))
            it.add(cookieRefreshMode)
            it.add(Box.createVerticalStrut(5))
            it.add(headerRefreshMode)

        }
        val refreshActionSection = UISection("Refresh Options", null, outerPanel)

        // Build the main window
        val panel = JPanel().also {
            it.border = BorderFactory.createEmptyBorder(5,5, 5, 5)
            it.layout = BoxLayout(it, BoxLayout.Y_AXIS)
            it.add(tableSection.getComponent())
            it.add(refreshActionSection)
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
