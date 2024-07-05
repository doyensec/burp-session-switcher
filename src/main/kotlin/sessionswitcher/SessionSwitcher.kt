package sessionswitcher

import burp.api.montoya.MontoyaApi
import kotlinx.coroutines.runBlocking
import sessionswitcher.sessions.SessionCollection
import sessionswitcher.settings.BurpSettingsProvider
import sessionswitcher.settings.Settings
import sessionswitcher.settings.SettingsProvider
import sessionswitcher.settings.SettingsWindow
import sessionswitcher.ui.EditorSwitcher
import sessionswitcher.ui.TabbedPane
import java.awt.Component
import javax.swing.JButton
import javax.swing.JPanel
import javax.swing.JTabbedPane
import javax.swing.SwingUtilities

public class SessionSwitcher private constructor(
    val montoyaApi: MontoyaApi,
    val settingsProvider: SettingsProvider
) : TabbedPane() {

    // Singleton pattern to ensure the extension is not initialized more than once
    companion object {
        private lateinit var montoyaApi: MontoyaApi
        public fun getApi(): MontoyaApi {
            if (!this::montoyaApi.isInitialized) {
                throw Exception("Montoya API not initialized yet.")
            }
            return this.montoyaApi
        }
        public fun getInstance(): SessionSwitcher {
            if (!this::instance.isInitialized) {
                throw Exception("SessionSwitcher not initialized yet.")
            }
            return this.instance
        }
        private lateinit var instance: SessionSwitcher
        public fun init(
            montoyaApi: MontoyaApi,
            settingsProvider: SettingsProvider = BurpSettingsProvider(montoyaApi) // Allow to override this to use this as a library
        ): SessionSwitcher {
            if (this::instance.isInitialized) {
                throw Exception("SessionSwitcher instance already initialized.")
            }
            this.montoyaApi = montoyaApi
            this.instance = SessionSwitcher(montoyaApi, settingsProvider)
            return this.instance
        }
    }

    final val sessions = SessionCollection()
    public val settings = Settings(this.settingsProvider)

    // Windows
    private val settingsWindow = SettingsWindow(settings)

    init {
        montoyaApi.logging().raiseInfoEvent("Session Switcher v${SessionSwitcherExtension.VERSION} Started")

        val logLevel = settings.loggingLevel.get()
        Logger.setLevel(logLevel)

        // Register GraphQL Payload Editor
        if (settings.displayRequestEditor.get()) {
            montoyaApi.userInterface().registerHttpRequestEditorProvider(EditorSwitcher.getProvider(this))
        }

        // Register the extension main tab
        if (settings.displayExtensionMainTab.get()) {
            //Burp.Montoya.userInterface().registerSuiteTab("Sessions", this)
        }

        // Register context menu handler
        if (settings.registerContextMenu.get()) {
            //Burp.Montoya.userInterface().registerContextMenuItemsProvider(SendToInqlHandler(this))
        }

        // Reload data from the project file
        this.sessions.loadFromProjectFile()

        // Initialize ExternalToolsService to make it ready to spawn the webserver and register the interceptor when they are needed
        /*
        if (this.config.getBoolean("integrations.webserver.lazy") == false) {
            ExternalToolsService.startIfOff()
        }
        */
    }

    fun unload() = runBlocking {
        //TODO: stop proxy interceptors
    }

    fun focus() {
        Logger.debug("Focusing BurpSessions")
        (this.parent as JTabbedPane).selectedComponent = this
    }

    fun focusTab(tab: Component) {
        (this.parent as JTabbedPane).selectedComponent = this
        this.tabbedPane.selectedComponent = tab
    }

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
}
