package sessionswitcher.ui.maintab

import sessionswitcher.Logger
import sessionswitcher.SessionSwitcher
import sessionswitcher.rules.refresher.RefreshRule
import sessionswitcher.settings.SettingsWindow
import sessionswitcher.ui.Label
import sessionswitcher.ui.Table
import sessionswitcher.ui.UISection
import sessionswitcher.ui.maintab.tables.RefreshRuleTableModel
import sessionswitcher.ui.maintab.tables.SessionsTableModel
import java.awt.BorderLayout
import java.util.*
import javax.swing.*

class MainSuiteTab(private val sessionSwitcher: SessionSwitcher): JPanel(BorderLayout()) {
    private val settingsWindow = SettingsWindow(sessionSwitcher.settings)

    private val mainPanel = JPanel().also {
        it.layout = BoxLayout(it, BoxLayout.Y_AXIS)
        it.border = BorderFactory.createEmptyBorder(5, 5, 0, 5)
    }

    // Table models
    val sessionsTableModel = SessionsTableModel(sessionSwitcher.sessions)
    val refreshRuleTableModel = RefreshRuleTableModel(sessionSwitcher.refreshRules)

    init {
        // Title section
        // |- Title
        val topPanel = JPanel(BorderLayout()).also { it.border = BorderFactory.createEmptyBorder(5,5, 5, 5) }
        val title = Label("Sessions", bold = true, relativeSize = 12.0)
        topPanel.add(title, BorderLayout.LINE_START)
        // |- Settings button
        val settingsButton = JButton("Settings")
        settingsButton.addActionListener { SwingUtilities.invokeLater { settingsWindow.isVisible = true } }
        topPanel.add(settingsButton, BorderLayout.LINE_END)
        mainPanel.add(topPanel)
        mainPanel.add(JSeparator())

        // Sessions Table Section
        val savedSessionsSection = SavedSessionsSection(sessionSwitcher)
        mainPanel.add(savedSessionsSection.getComponent())
        mainPanel.add(JSeparator())

        // AutoRefresh Rules Section
        val autoRefreshRulesSection = makeAutoRefreshRulesSection()
        mainPanel.add(autoRefreshRulesSection)
        mainPanel.add(JSeparator())

        // AutoInject Rules Section
        val autoInjectRulesSection = makeAutoInjectRulesSection()
        mainPanel.add(autoInjectRulesSection)

        // Wrap everything in a cozy scrollPane
        val padded = JPanel(BorderLayout())
        padded.add(mainPanel, BorderLayout.PAGE_START)
        padded.add(JLabel("Made with ❤ by Doyensec"), BorderLayout.PAGE_END)
        val scrollPane = JScrollPane(padded)
        scrollPane.border = BorderFactory.createEmptyBorder()
        this.add(scrollPane)
    }

    // Refresh Rules
    fun newRefreshRule() {
        val ruleOptional = RefreshRuleWindow(Optional.empty<RefreshRule>()).showDialog()
        if (ruleOptional.isEmpty) return
        sessionSwitcher.refreshRules.add(ruleOptional.get())
        refreshRuleTableModel.fireTableDataChanged()
    }

    private fun makeAutoRefreshRulesSection(): JPanel {
        val table = Table(emptyArray()).also { it.model = refreshRuleTableModel }

        // |- Buttons
        val newButton = JButton("New")
        val editButton = JButton("Edit").also { it.isEnabled = false }
        val deleteButton = JButton("Delete").also { it.isEnabled = false }
        val duplicateButton = JButton("Duplicate").also { it.isEnabled = false }

        newButton.addActionListener { newRefreshRule() }

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
            it.add(table.withScrollPane(), BorderLayout.CENTER)
        }

        return UISection("AutoRefresh Rules", null, outerPanel)
    }
    private fun makeAutoInjectRulesSection(): JPanel {
        val table = Table(arrayOf("Rules", "Inject Session"))
        return UISection("AutoInject Rules", null, JLabel("Work in progress"), JLabel("Come back later :)"), null, table.withScrollPane())
    }

    fun focus() {
        Logger.debug("Focusing Sessions Main Tab")
        (this.parent as JTabbedPane).selectedComponent = this
    }
}