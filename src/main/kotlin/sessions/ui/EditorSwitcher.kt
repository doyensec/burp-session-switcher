package sessions.ui

import burp.Burp
import burp.api.montoya.http.message.HttpRequestResponse
import burp.api.montoya.http.message.requests.HttpRequest
import burp.api.montoya.ui.Selection
import burp.api.montoya.ui.editor.EditorOptions
import burp.api.montoya.ui.editor.HttpRequestEditor
import burp.api.montoya.ui.editor.extension.EditorCreationContext
import burp.api.montoya.ui.editor.extension.EditorMode
import burp.api.montoya.ui.editor.extension.ExtensionProvidedHttpRequestEditor
import burp.api.montoya.ui.editor.extension.HttpRequestEditorProvider
import sessions.BurpSessions
import sessions.Session
import java.awt.BorderLayout
import java.awt.Component
import java.awt.Dimension
import java.awt.Font
import javax.swing.*

class EditorSwitcher private constructor(val plugin: BurpSessions, readOnly: Boolean) :
    ExtensionProvidedHttpRequestEditor {
    companion object {
        class Provider(private val plugin: BurpSessions) : HttpRequestEditorProvider {
            override fun provideHttpRequestEditor(creationContext: EditorCreationContext?): ExtensionProvidedHttpRequestEditor {
                return EditorSwitcher(
                    plugin,
                    (creationContext?.editorMode() ?: EditorMode.DEFAULT) == EditorMode.READ_ONLY,
                )
            }
        }

        private var provider: Provider? = null
        fun getProvider(plugin: BurpSessions): Provider {
            if (provider == null) provider = Provider(plugin)
            return provider as Provider
        }

        val SESSION_NONE = Session("Original (no change)", "none")
    }

    // State-holding stuff
    private var editor: HttpRequestEditor

    private var _request: HttpRequest? = null
    private var request: HttpRequest?
        get() = this._request
        set(r) {
            this._request = r
            this.updateEditor()
        }

    private var originalRequest: HttpRequest? = null
    private var modified: Boolean = false
    private var _selectedSession: Session? = null
    private var selectedSession: Session?
        get() = this._selectedSession
        set(s) {
            val request = this.request ?: HttpRequest.httpRequest()
            this._selectedSession = s
            if (s != null) {
                this.request = s.apply(request)
                this.sessionNameLabel.text = s.name
            } else {
                this.request = this.originalRequest!!.withMethod(this.originalRequest!!.method())
                this.sessionNameLabel.text = "None"
            }
            this.updateEditor()
        }
    // END State-holding stuff

    // Session stuff

    private fun updateEditor() {
        // TODO: strip unwanted headers maybe?
        this.editor.request = request
    }

    private fun updateSessionsList() {
        this.sessionsComboBox.removeAllItems()
        this.sessionsComboBox.addItem(SESSION_NONE)
        this.sessionsComboBox.toolTipText = "Select a session"
        this.plugin.getSessions().forEach { this.sessionsComboBox.addItem(it) }
    }

    private fun saveSelectedSession() {
        val selected = (this.sessionsComboBox.selectedItem ?: return) as Session
        val request = this.request ?: return
        selected.loadFromRequest(request)
    }

    private fun deleteSelectedSession() {
        val isAlsoLinked = this.sessionsComboBox.selectedItem == this.selectedSession
        this.plugin.deleteSession(this.sessionsComboBox.selectedItem as Session)
        this.updateSessionsList()
        if (isAlsoLinked) {
            this.selectedSession = null
            this.sessionsComboBox.selectedIndex = 0
        } else {
            this.sessionsComboBox.selectedItem = this.selectedSession
        }
    }

    private fun newSession() {
        val session = this.newSessionDialog()
        if (session == null) {
            if (this.selectedSession == null) {
                this.sessionsComboBox.selectedIndex = 0
            } else {
                this.sessionsComboBox.selectedItem = this.selectedSession
            }
            return
        }
        this.updateSessionsList()
        this.sessionsComboBox.selectedItem = session
        this.newSessionBtn.isEnabled = true
        this.deleteSessionBtn.isEnabled = true
        this.saveSelectedSession()
    }

    private fun selectedSessionChanged() {
        val selected = (this.sessionsComboBox.selectedItem ?: return) as Session
        when (selected) {
            SESSION_NONE -> {
                this.selectedSession = null
                this.newSessionBtn.isEnabled = true
                this.deleteSessionBtn.isEnabled = false
            }

            else -> {
                this.newSessionBtn.isEnabled = true
                this.deleteSessionBtn.isEnabled = true
            }
        }
    }

    private fun newSessionDialog(): Session? {
        var name: String?
        do {
            name = JOptionPane.showInputDialog(
                Burp.Montoya.userInterface().swingUtils().suiteFrame(),
                "Choose a name for the new Session",
                "New Session",
                JOptionPane.QUESTION_MESSAGE,
                null,
                null,
                "",
            ) as String?
            if (name == null) return null
        } while (name == "")
        return this.plugin.createSession(name!!)
    }

    // END Session stuff

    private var component = BorderPanel(10)

    private val sessionNameLabel = JLabel("None").also {
        it.font = it.font.deriveFont(Font.BOLD)
    }
    private val sessionsComboBox = JComboBox<Session>().also {
        it.maximumSize = it.preferredSize
        it.minimumSize = Dimension(300, it.preferredSize.height)
        it.preferredSize = Dimension(300, it.preferredSize.height)
        it.addActionListener { this.selectedSessionChanged() }
    }
    private val newSessionBtn = JButton("New").also {
        it.isEnabled = false
        it.addActionListener {
            this.newSession()
        }
    }
    private val deleteSessionBtn = JButton("Delete").also {
        it.isEnabled = false
        it.addActionListener {
            this.deleteSelectedSession()
        }
    }

    // End UI Stuff

    init {
        if (readOnly) {
            this.editor = Burp.Montoya.userInterface().createHttpRequestEditor(EditorOptions.READ_ONLY)
        } else {
            this.editor = Burp.Montoya.userInterface().createHttpRequestEditor()
        }
        this.component
        val rootContainer = BoxPanel(BoxLayout.PAGE_AXIS)

        val sessionLabelPanel = BoxPanel(BoxLayout.LINE_AXIS).also {
            it.add(JLabel("Current Session:"))
            it.add(Box.createRigidArea(Dimension(5, 0)))
        }
        rootContainer.add(BorderPanel(0, 5).also { it.add(sessionLabelPanel) })

        val buttonPanel = BoxPanel(BoxLayout.LINE_AXIS).also {
            it.add(this.sessionsComboBox)
            it.add(Box.createRigidArea(Dimension(10, 0)))
            it.add(this.newSessionBtn)
            it.add(Box.createRigidArea(Dimension(10, 0)))
            it.add(this.deleteSessionBtn)
            it.add(Box.createHorizontalGlue())
        }
        rootContainer.add(BorderPanel(10).also { it.add(buttonPanel) })

        this.component.add(BorderLayout.PAGE_START, rootContainer)
        this.component.add(BorderLayout.CENTER, this.editor.uiComponent())


        // Add context menu handler
        /*
        this.contextMenu.addRightClickHandler(this.queryEditor)
        this.contextMenu.addKeyboardShortcutHandler(this.queryEditor)
        */
    }

    override fun setRequestResponse(requestResponse: HttpRequestResponse) {
        this.modified = false
        this.originalRequest = requestResponse.request()
        this.request = requestResponse.request().withMethod(requestResponse.request().method()) // Just a trick to ensure an object copy
    }

    override fun isEnabledFor(requestResponse: HttpRequestResponse): Boolean = true

    override fun caption(): String {
        return if (this.selectedSession == null) {
            "Sessions"
        } else {
            "Session: " + this.selectedSession!!.name
        }
    }

    override fun uiComponent(): Component {
        return this.component
    }

    override fun selectedData(): Selection? {
        return null
    }

    override fun isModified(): Boolean = this.modified

    override fun getRequest(): HttpRequest = this.request!!
}
