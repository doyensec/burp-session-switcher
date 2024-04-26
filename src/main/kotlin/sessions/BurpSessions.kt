package sessions

import burp.Burp
import burp.BurpExtender
import burp.BurpIcons
import burp.api.montoya.persistence.PersistedObject
import kotlinx.coroutines.runBlocking
import sessions.savestate.SavesAndLoadData
import sessions.savestate.SavesDataToProject
import sessions.savestate.getSaveStateKeys
import sessions.ui.EditorSwitcher
import sessions.ui.ImgButton
import sessions.ui.SettingsWindow
import sessions.ui.TabbedPane
import java.awt.Component
import javax.swing.JPanel
import javax.swing.JTabbedPane
import javax.swing.SwingUtilities

class BurpSessions : TabbedPane(), SavesAndLoadData {

    private val config = Config.getInstance()
    private val sessions = LinkedHashMap<String, Session>()

    init {
        Burp.Montoya.logging().raiseInfoEvent("BurpSession ${BurpExtender.version} Started")

        // Cleanup from previous versions
        // FIXME: Remove this once this is exposed through Settings UI
        config.delete("logging.level", Config.Scope.PROJECT)
        config.delete("codegen.depth", Config.Scope.PROJECT)
        config.delete("codegen.pad", Config.Scope.PROJECT)
        config.delete("ScannerPanel", Config.Scope.GLOBAL)

        val logLevel = config.getString("logging.level") ?: "DEBUG"
        Logger.setLevel(logLevel)


        // Register GraphQL Payload Editor
        Burp.Montoya.userInterface().registerHttpRequestEditorProvider(EditorSwitcher.getProvider(this))


        //this.addTab("Scanner", this.scanner)
        //this.addSettingsTab()

        // Register the extension main tab
        //Burp.Montoya.userInterface().registerSuiteTab("Sessions", this)

        // Register context menu handler
        //Burp.Montoya.userInterface().registerContextMenuItemsProvider(SendToInqlHandler(this))

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
        val button = ImgButton("Settings", BurpIcons.CONFIG)
        button.background = this.background
        button.isFocusable = false
        button.addActionListener { SwingUtilities.invokeLater { SettingsWindow.getInstance().isVisible = true } }
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
        obj.setStringList("sessions", getSaveStateKeys(this.sessions.values))
        return obj
    }

    override fun burpDeserialize(obj: PersistedObject) {
        val sessionsList = obj.getStringList("sessions")
        if (sessionsList != null) {
            for (sessionId in sessionsList) {
                val p = Session.Deserializer(sessionId).get() ?: continue
                this.sessions[p.id] = p
            }
        }

        //this.scanner.loadFromProjectFile()
    }
}
