package sessionswitcher.ui.maintab

import sessionswitcher.Logger
import sessionswitcher.SessionSwitcher
import sessionswitcher.settings.SettingsWindow
import sessionswitcher.ui.Label
import sessionswitcher.ui.Table
import sessionswitcher.ui.UISection
import java.awt.BorderLayout
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
        val title = Label("Sessions", bold = true, relativeSize = 12.0)
        topPanel.add(title, BorderLayout.LINE_START)
        // |- Settings button
        val settingsButton = JButton("Settings")
        settingsButton.addActionListener { SwingUtilities.invokeLater { settingsWindow.isVisible = true } }
        topPanel.add(settingsButton, BorderLayout.LINE_END)
        mainPanel.add(topPanel)
        mainPanel.add(JSeparator())

        // Sessions Table Section
        val savedSessionsSection = SavedSessionsSection.make(sessionSwitcher)
        mainPanel.add(savedSessionsSection)
        mainPanel.add(JSeparator())

        // AutoRefresh Rules Section
        val autoRefreshRulesSection = RefreshRuleSection.make(sessionSwitcher)
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

    private fun makeAutoInjectRulesSection(): JPanel {
        val table = Table(arrayOf("Rules", "Inject Session"))
        return UISection("AutoInject Rules", null, JLabel("Work in progress"), JLabel("Come back later :)"), null, table.withScrollPane())
    }

    fun focus() {
        Logger.debug("Focusing Sessions Main Tab")
        (this.parent as JTabbedPane).selectedComponent = this
    }
}