package sessionswitcher.ui.maintab

import sessionswitcher.SessionSwitcher
import sessionswitcher.rules.conditions.Condition
import sessionswitcher.rules.refresher.RefreshConfig
import sessionswitcher.rules.refresher.RefreshRule
import sessionswitcher.ui.ButtonPrimary
import sessionswitcher.ui.ComboBox
import sessionswitcher.ui.Table
import sessionswitcher.ui.UISection
import sessionswitcher.ui.maintab.tables.ConditionsTableModel
import java.awt.BorderLayout
import java.awt.Dimension
import java.awt.event.ItemEvent
import java.util.*
import javax.swing.*
import kotlin.math.min

class RefreshRuleWindow(private val initialRefreshRule: Optional<RefreshRule>) :
    JDialog(SessionSwitcher.getApi().userInterface().swingUtils().suiteFrame(), if (initialRefreshRule.isEmpty) "New Refresh Rule" else "Edit Refresh Rule", true) {

    // Flags
    var cancelPressed = false

    // Conditions
    val conditions = ArrayList<Condition>()
    val tableModel = ConditionsTableModel(conditions)

    // UI elements
    val saveButton = ButtonPrimary("Save")
    val cancelButton = JButton("Cancel")

    val conditionsTable = Table(emptyArray()).also { it.model = tableModel }

    val sessionSelector = ComboBox("Session to refresh")

    val refreshSourceSelector = ComboBox("Refresh data from", "Request", "Response")
    val REQUEST_COOKIES_REFRESH_OPTIONS = arrayOf("Replace All", "Refresh All", "Refresh Existing", "Don't refresh")
    val RESPONSE_COOKIES_REFRESH_OPTIONS = arrayOf("Refresh All", "Refresh Existing", "Don't refresh")
    val cookieRefreshMode = ComboBox("Cookie refresh mode", *REQUEST_COOKIES_REFRESH_OPTIONS)
    val headerRefreshMode = ComboBox("Header refresh mode", "Refresh Existing", "Don't refresh")

    fun autoSize() {
        // Gets the size of the screen the Burp window is on (for multi-monitor setups)
        val screenSize = SessionSwitcher.getApi().userInterface().swingUtils().suiteFrame().graphicsConfiguration.device.displayMode

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
        this.setLocationRelativeTo(SessionSwitcher.getApi().userInterface().swingUtils().suiteFrame())
    }

    fun newCondition() {
        val newCondition = ConditionEditWindow(this, Optional.empty<Condition>()).showDialog()
        if (!newCondition.isPresent) {
            return
        }
        this.conditions.add(newCondition.get())
        this.refreshConditionsTable()
    }

    fun refreshConditionsTable() {
        tableModel.fireTableDataChanged()
    }

    fun makeRule(): RefreshRule {
        val sessionName = this.sessionSelector.getSelectedItem()
        if (sessionName == "(No sessions)") {
            throw IllegalStateException("No sessions selected")
        }
        val session = SessionSwitcher.getInstance().sessions.getSession(sessionName)
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

    init {
        // Set window properties
        this.isResizable = true
        this.isAutoRequestFocus = true

        saveButton.isEnabled = false

        val conditionSection = makeConditionsSection()

        // Refresh options
        var sessions = SessionSwitcher.getInstance().sessions.getSessionNames().toTypedArray()
        if (sessions.isEmpty()) {
            sessions = arrayOf("(No sessions)")
        }
        sessions.forEach { this.sessionSelector.component.addItem(it) }

        sessionSelector.addItemListener { this.checkEnableSaveButton() }
        conditionsTable.model.addTableModelListener { this.checkEnableSaveButton() }

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
            it.add(conditionSection)
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

    private fun makeConditionsSection(): JPanel {
        // |- Buttons
        val newButton = JButton("New")
        val editButton = JButton("Edit").also { it.isEnabled = false }
        val deleteButton = JButton("Delete").also { it.isEnabled = false }
        val duplicateButton = JButton("Duplicate").also { it.isEnabled = false }

        // Set button listeners
        newButton.addActionListener { newCondition() }

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
        val outerPanel = JPanel(BorderLayout()).also {
            it.add(middlePanel, BorderLayout.LINE_START)
            it.add(conditionsTable.withScrollPane(), BorderLayout.CENTER)
        }

        return UISection("Conditions", "Conditions in this list are evaluated with a logical AND", outerPanel)
    }

    fun checkEnableSaveButton() {
        val enable = conditionsTable.model.rowCount > 0 && sessionSelector.getSelectedItem() != "(No sessions)" && sessionSelector.getSelectedItem() != ""
        saveButton.isEnabled = enable
    }
}
