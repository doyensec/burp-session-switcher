package sessionswitcher.ui

import burp.api.montoya.http.message.HttpRequestResponse
import burp.api.montoya.http.message.requests.HttpRequest
import burp.api.montoya.ui.Selection
import burp.api.montoya.ui.editor.extension.EditorCreationContext
import burp.api.montoya.ui.editor.extension.EditorMode
import burp.api.montoya.ui.editor.extension.ExtensionProvidedHttpRequestEditor
import burp.api.montoya.ui.editor.extension.HttpRequestEditorProvider
import sessionswitcher.Logger
import sessionswitcher.SessionSwitcher
import sessionswitcher.sessions.Session
import sessionswitcher.ui.misc.*
import java.awt.*
import javax.swing.*
import javax.swing.event.DocumentEvent
import javax.swing.event.DocumentListener

class RequestEditorSwitcher private constructor(val plugin: SessionSwitcher, readOnly: Boolean) :
    ExtensionProvidedHttpRequestEditor {
    companion object {
        class Provider(private val plugin: SessionSwitcher) : HttpRequestEditorProvider {
            override fun provideHttpRequestEditor(creationContext: EditorCreationContext?): ExtensionProvidedHttpRequestEditor {
                return RequestEditorSwitcher(
                    plugin,
                    (creationContext?.editorMode() ?: EditorMode.DEFAULT) == EditorMode.READ_ONLY,
                )
            }
        }

        private var provider: Provider? = null
        fun getProvider(plugin: SessionSwitcher): Provider {
            if (provider == null) provider = Provider(plugin)
            return provider as Provider
        }

        val SESSION_NONE = Session("Current", "none")
    }

    // State-holding stuff
    private var editor = HighlightRequestEditor()

    private var _request: HttpRequest? = null
    private var httpRequest: HttpRequest?
        get() = this._request
        set(r) {
            this._request = r
            this.updateEditorFromRequest()
        }

    private var originalRequest: HttpRequest? = null
    private var originalRequestModified: Boolean = false
    private var isUpdatingUI: Boolean = false
    private var _selectedSession: Session? = null
    private var selectedSession: Session?
        get() = this._selectedSession
        set(s) {
            if (isUpdatingUI) return
            Logger.info("SELECTED SESSION SET")

            val original = this.originalRequest ?: HttpRequest.httpRequest()
            val request = original.withMethod(original.method())
            this._selectedSession = s
            this.editedLabel.text = ""
            if (s != null) {
                Logger.info("DIFF NULL")
                this.httpRequest = s.apply(request)
                this.originalRequestModified = true
                Logger.info(s.name)
            } else {
                Logger.info("NULL")
                this.httpRequest = this.originalRequest!!.withMethod(this.originalRequest!!.method())
                this.originalRequestModified = false
            }
            this.updateEditorFromRequest()

        }
    // END State-holding stuff

    // Session stuff

    private val contextMenu = EditorSendRequestFromPluginHandler(this)
    private fun updateEditorFromRequest() {
        // TODO: strip unwanted headers maybe?
        this.editor.setText(httpRequest.toString())
        this.editedLabel.text = ""
        this.saveSessionBtn.isEnabled = this.selectedSession == null
        this.deleteSessionBtn.isEnabled = this.selectedSession != null
    }

    private fun updateSessionsList() {
        this.isUpdatingUI = true
        this.sessionsComboBox.removeAllItems()
        this.sessionsComboBox.addItem(SESSION_NONE)
        this.plugin.sessions.getSessions().forEach { this.sessionsComboBox.addItem(it) }
        this.isUpdatingUI = false
    }

    private fun saveToSessionFromRequest() {
        val selected = (this.sessionsComboBox.selectedItem ?: return) as Session
        val request = this.httpRequest ?: return
        selected.loadFromRequestFiltered(request)
    }

    class EditorChangeListener(val callback: () -> Unit) : DocumentListener {
        override fun insertUpdate(e: DocumentEvent?) {}
        override fun removeUpdate(e: DocumentEvent?){}
        override fun changedUpdate(e: DocumentEvent?) = this.callback()
    }

    private fun deleteSelectedSession() {
        val isAlsoLinked = this.sessionsComboBox.selectedItem == this.selectedSession
        this.plugin.sessions.deleteSession(this.sessionsComboBox.selectedItem as Session)
        this.updateSessionsList()
        if (isAlsoLinked) {
            this.selectedSession = null
            this.sessionsComboBox.selectedIndex = 0
        } else {
            this.sessionsComboBox.selectedItem = this.selectedSession
        }
    }

    private fun saveSessionHandler() {
        if (this.selectedSession == null) {
            // New session
            val session = this.newSessionDialog() ?: return
            this.updateSessionsList()
            this.sessionsComboBox.selectedItem = session
            this.saveToSessionFromRequest()
        } else {
            // Save existing
            TODO()
        }
    }

    private fun selectedSessionChanged() {
        Logger.info("SELECTED SESSION CHANGED")
        val selected = (this.sessionsComboBox.selectedItem ?: return) as Session
        when (selected) {
            SESSION_NONE -> {
                Logger.info("NONE BRANCH")
                this.selectedSession = null
            }
            else -> {
                Logger.info("ELSE BRANCH")
                this.selectedSession = selected
            }
        }
    }

    private fun newSessionDialog(): Session? {
        var name: String?
        do {
            name = JOptionPane.showInputDialog(
                plugin.montoyaApi.userInterface().swingUtils().suiteFrame(),
                "Choose a name for the new Session. Valid characters: [A-Za-z0-9._-]",
                "New Session",
                JOptionPane.QUESTION_MESSAGE,
                null,
                null,
                "",
            ) as String?
            if (name == null) return null
        } while (!Session.isValidName(name!!))
        return this.plugin.sessions.createSession(name)
    }

    // END Session stuff

    private var component = BorderPanel(0)

    private val editedLabel = JLabel("").also {
        it.font = it.font.deriveFont(Font.BOLD)
    }
    private val sessionsComboBox = JComboBox<Session>().also {
        it.maximumSize = it.preferredSize
        it.minimumSize = Dimension(300, it.preferredSize.height)
        it.preferredSize = Dimension(300, it.preferredSize.height)
        it.toolTipText = "Select a session"
        it.addActionListener { this.selectedSessionChanged() }
    }
    private val saveSessionBtn = JButton("Save").also {
        it.isEnabled = true
        it.addActionListener {
            this.saveSessionHandler()
        }
    }
    private val deleteSessionBtn = JButton("Delete").also {
        it.isEnabled = false
        it.addActionListener {
            this.deleteSelectedSession()
        }
    }

    private fun editorChangeHandler() {
        this.editedLabel.text = "(Modified)"
        this.saveSessionBtn.isEnabled = true
    }

    // End UI Stuff

    init {
        val rootContainer = BoxPanel(BoxLayout.Y_AXIS)

        val sessionLabelPanel = FlowPanel(FlowLayout.LEFT).also {
            it.add(this.sessionsComboBox)
            it.add(editedLabel)
        }

        val buttonPanel = FlowPanel(FlowLayout.LEFT).also {
            it.add(this.saveSessionBtn)
            it.add(this.deleteSessionBtn)
        }

        rootContainer.also {
            it.add(FlowPanel(FlowLayout.LEFT).also { it.add(JLabel("Switch Session:")) })
            it.add(Box.createRigidArea(Dimension(0, 4)))
            it.add(sessionLabelPanel)
            it.add(Box.createRigidArea(Dimension(0, 10)))
            it.add(buttonPanel)
        }

        this.component.add(BorderLayout.PAGE_START, BorderPanel(10).also { it.add(rootContainer) })
        this.component.add(BorderLayout.CENTER, this.editor)

        val listener = EditorChangeListener { this.editorChangeHandler() }
        //val jt = this.editor.getTextAreaComponent()
        //jt.document.addDocumentListener(listener)

        // Add context menu handler
        //this.contextMenu.addRightClickHandler(this.editor.getTextAreaComponent())
        //this.contextMenu.addKeyboardShortcutHandler(this.editor.getTextAreaComponent())
    }

    override fun setRequestResponse(requestResponse: HttpRequestResponse) {
        this.originalRequestModified = false
        this.originalRequest = requestResponse.request()
        this.updateSessionsList()
        this.sessionsComboBox.selectedIndex = 0
        this.updateEditorFromRequest()
    }

    override fun isEnabledFor(requestResponse: HttpRequestResponse): Boolean = true

    override fun caption(): String = "Sessions"

    override fun uiComponent(): Component {
        return this.component
    }

    override fun selectedData(): Selection? {
        return null
    }

    override fun isModified(): Boolean = this.originalRequestModified

    override fun getRequest(): HttpRequest = this._request ?: HttpRequest.httpRequest()

    class EditorSendRequestFromPluginHandler(val editor: RequestEditorSwitcher) : SendFromPluginHandler(editor.plugin) {
        override fun getRequest(): HttpRequest {
            return editor.request
        }
    }
}
