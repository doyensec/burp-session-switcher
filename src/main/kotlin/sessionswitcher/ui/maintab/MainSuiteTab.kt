package sessionswitcher.ui.maintab

import sessionswitcher.Logger
import sessionswitcher.SessionSwitcher
import sessionswitcher.settings.SettingsWindow
import sessionswitcher.ui.Label
import java.awt.BorderLayout
import javax.swing.*

class MainSuiteTab(sessionSwitcher: SessionSwitcher) : JPanel(BorderLayout()) {
    private val settingsWindow = SettingsWindow(sessionSwitcher.settings)

    private val mainPanel = JPanel().also {
        it.layout = BoxLayout(it, BoxLayout.Y_AXIS)
        it.border = BorderFactory.createEmptyBorder(5, 5, 0, 5)
    }

    init {
        // Title section
        // |- Title
        val topPanel = JPanel(BorderLayout()).also { it.border = BorderFactory.createEmptyBorder(5, 5, 5, 5) }
        val titlePanel = JPanel().also {
            it.layout = BoxLayout(it, BoxLayout.X_AXIS)
        }

        val title = Label("Session Switcher", bold = true, relativeSize = 12.0)
        val logo = ImageIcon(MainSuiteTab::class.java.getResource("/logo.png")).image.getScaledInstance(
            32,
            32,
            java.awt.Image.SCALE_SMOOTH
        )
        titlePanel.add(JLabel(ImageIcon(logo)))
        titlePanel.add(Box.createHorizontalStrut(10))
        titlePanel.add(title)

        topPanel.add(titlePanel, BorderLayout.LINE_START)

        // Settings button
        val theme = sessionSwitcher.montoyaApi.userInterface().currentTheme()
        val fullImage = ImageIcon(MainSuiteTab::class.java.getResource("/icons/${theme.name.lowercase()}/settings.png"))
        val scaled = fullImage.image.getScaledInstance(24, 24, java.awt.Image.SCALE_SMOOTH)
        val icon = ImageIcon(scaled)
        val settingsButton = JButton(icon)
        settingsButton.addActionListener { SwingUtilities.invokeLater { settingsWindow.isVisible = true } }
        val settingsButtonPanel = JPanel().also { it.add(settingsButton) }
        topPanel.add(settingsButtonPanel, BorderLayout.LINE_END)
        mainPanel.add(topPanel)

        // Sessions Table Section
        val savedSessionsSection = SavedSessionsSection.make(sessionSwitcher)
        mainPanel.add(savedSessionsSection)
        mainPanel.add(JSeparator())

        // AutoUpdate Rules Section
        val autoUpdateRulesSection = UpdateRuleSection.make(sessionSwitcher)
        mainPanel.add(autoUpdateRulesSection)
        //mainPanel.add(JSeparator())

        // AutoInject Rules Section
        // val autoInjectRulesSection = makeAutoInjectRulesSection()
        // mainPanel.add(autoInjectRulesSection)

        // Wrap everything in a cozy scrollPane
        val padded = JPanel(BorderLayout())
        padded.add(mainPanel, BorderLayout.PAGE_START)
        padded.add(JLabel("Made with ❤ by Doyensec"), BorderLayout.PAGE_END)
        val scrollPane = JScrollPane(padded)
        scrollPane.border = BorderFactory.createEmptyBorder()
        this.add(scrollPane)
    }

    fun focus() {
        Logger.debug("Focusing Sessions Main Tab")
        (this.parent as JTabbedPane).selectedComponent = this
    }
}