package sessionswitcher.maintab

import PDControlScrollPane
import sessionswitcher.Logger
import sessionswitcher.SessionSwitcher
import sessionswitcher.settings.SettingsWindow
import sessionswitcher.ui.Label
import sessionswitcher.ui.UISection
import java.awt.BorderLayout
import java.awt.Dimension
import javax.swing.*

class MainSuiteTab(private val sessionSwitcher: SessionSwitcher): JPanel(BorderLayout()) {
    private val settingsWindow = SettingsWindow(sessionSwitcher.settings)

    private val mainPanel = JPanel().also {
        it.layout = BoxLayout(it, BoxLayout.Y_AXIS)
        it.border = BorderFactory.createEmptyBorder(5, 5, 0, 5)
    }
    init {
        // Title section
        // |- Title
        val topPanel = JPanel(BorderLayout()).also { it.border = BorderFactory.createEmptyBorder(5,5, 5, 5) }
        val title = Label("Session Switcher", bold = true, relativeSize = 8.0)
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
        val scrollPane = JScrollPane(padded)
        scrollPane.border = BorderFactory.createEmptyBorder()
        this.add(scrollPane)
        this.add(JLabel("Made with ❤ by Doyensec"), BorderLayout.PAGE_END)
    }

    private fun makeAutoRefreshRulesSection(): JPanel {
        val table = JTable(emptyArray(), arrayOf("Ruleset", "Session"))
        val rulesTable = PDControlScrollPane(table)
        rulesTable.preferredSize = Dimension(rulesTable.preferredSize.width, table.rowHeight*15)

        // |- Buttons
        val newButton = JButton("New")
        val editButton = JButton("Edit").also { it.isEnabled = false }
        val deleteButton = JButton("Delete").also { it.isEnabled = false }
        val duplicateButton = JButton("Duplicate").also { it.isEnabled = false }

        val buttonsPanel = JPanel().also { it ->
            it.layout = BoxLayout(it, BoxLayout.Y_AXIS)
            it.border = BorderFactory.createEmptyBorder(0, 0, 0, 10)
            it.add(JPanel(BorderLayout()).also{p-> p.add(newButton, BorderLayout.PAGE_START)})
            it.add(Box.createVerticalStrut(10))
            it.add(JPanel(BorderLayout()).also{p-> p.add(editButton, BorderLayout.PAGE_START)})
            it.add(Box.createVerticalStrut(10))
            it.add(JPanel(BorderLayout()).also{p-> p.add(deleteButton, BorderLayout.PAGE_START)})
            it.add(Box.createVerticalStrut(10))
            it.add(JPanel(BorderLayout()).also{p-> p.add(duplicateButton, BorderLayout.PAGE_START)})
        }

        val middlePanel = JPanel(BorderLayout()).also {
            it.add(buttonsPanel, BorderLayout.PAGE_START)
        }
        val outerPanel = JPanel(BorderLayout()).also {
            it.add(middlePanel, BorderLayout.LINE_START)
            it.add(rulesTable, BorderLayout.CENTER)
        }

        return UISection("Auto-Refresh Rulesets", null, JLabel("Work in progress"), JLabel("Come back later :)"), null, outerPanel)
    }
    private fun makeAutoInjectRulesSection(): JPanel {
        val table = JTable(emptyArray(), arrayOf("Ruleset", "Session"))
        val rulesTable = PDControlScrollPane(table)
        rulesTable.preferredSize = Dimension(rulesTable.preferredSize.width, table.rowHeight*15)
        return UISection("Auto-Inject Rulesets", null, JLabel("Work in progress"), JLabel("Come back later :)"), null, rulesTable)
    }

    fun focus() {
        Logger.debug("Focusing BurpSessions Main Tab")
        (this.parent as JTabbedPane).selectedComponent = this
    }

    /*
        private fun addSettingsTab() {
        val button = JButton("Settings")
        button.background = this.background
        button.isFocusable = false
        button.addActionListener { SwingUtilities.invokeLater { settingsWindow.isVisible = true } }
        val idx = this.tabbedPane.tabCount
        this.addTab("Settings", JPanel())
        this.tabbedPane.setTabComponentAt(idx, button)
        this.tabbedPane.setEnabledAt(idx, false)
    }
     */
}