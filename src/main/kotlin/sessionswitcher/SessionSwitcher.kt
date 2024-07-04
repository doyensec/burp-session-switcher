package sessionswitcher

import burp.api.montoya.MontoyaApi
import burp.api.montoya.persistence.PersistedObject
import kotlinx.coroutines.runBlocking
import sessionswitcher.savestate.SavesAndLoadData
import sessionswitcher.savestate.SavesDataToProject
import sessionswitcher.savestate.getSaveStateKeys
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

public class SessionSwitcher(
    val montoyaApi: MontoyaApi,
    val settingsProvider: SettingsProvider = BurpSettingsProvider(montoyaApi) // Allow to override this to use this as a library
) : TabbedPane(), SavesAndLoadData {

    public val settings = Settings(this.settingsProvider)
    private val sessions = LinkedHashMap<String, Session>()

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
        if (!this.dataPresentInProjectFile()) {
            this.saveToProjectFile(false) // initialize main object
        } else {
            this.loadFromProjectFileAsync()
        }

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

    fun getSessionsId(): Collection<String> {
        return this.sessions.keys
    }

    fun getSessions(): Collection<Session> {
        return this.sessions.values
    }

    fun getSession(key: String): Session? {
        return this.sessions[key]
    }

    fun deleteSession(key: String) {
        if (this.sessions.containsKey(key)) {
            this.sessions[key]?.deleteFromProjectFileAsync()
            this.sessions.remove(key)
        }
    }

    fun createSession(name: String): Session {
        val s = Session(name)
        this.sessions[s.id] = s
        this.updateChildObjectAsync(s)
        return s
    }

    fun deleteSession(p: Session) {
        this.deleteSession(p.id)
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

    override val saveStateKey: String
        get() = "Sessions_Main"

    override fun getChildrenObjectsToSave(): Collection<SavesDataToProject> {
        val lst: MutableList<SavesDataToProject> = this.sessions.values.toMutableList()
        //lst.add(this.scanner)
        return lst
    }

    override fun burpSerialize(): PersistedObject {
        val obj = PersistedObject.persistedObject()
        obj.setStringList("sessionswitcher", getSaveStateKeys(this.sessions.values))
        return obj
    }

    override fun burpDeserialize(obj: PersistedObject) {
        val sessionsList = obj.getStringList("sessionswitcher")
        if (sessionsList != null) {
            for (sessionId in sessionsList) {
                val p = Session.Deserializer(sessionId).get() ?: continue
                this.sessions[p.id] = p
            }
        }

        //this.scanner.loadFromProjectFile()
    }
}
