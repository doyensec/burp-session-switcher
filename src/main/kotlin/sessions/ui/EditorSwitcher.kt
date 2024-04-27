package sessions.ui

import burp.Burp
import burp.api.montoya.core.ByteArray
import burp.api.montoya.http.message.HttpRequestResponse
import burp.api.montoya.http.message.requests.HttpRequest
import burp.api.montoya.ui.Selection
import burp.api.montoya.ui.editor.EditorOptions
import burp.api.montoya.ui.editor.RawEditor
import burp.api.montoya.ui.editor.extension.EditorCreationContext
import burp.api.montoya.ui.editor.extension.EditorMode
import burp.api.montoya.ui.editor.extension.ExtensionProvidedHttpRequestEditor
import burp.api.montoya.ui.editor.extension.HttpRequestEditorProvider
import sessions.BurpSessions
import sessions.Logger
import sessions.Session
import sessions.utils.getTextAreaComponent
import java.awt.BorderLayout
import java.awt.Component
import java.awt.Dimension
import java.awt.Font
import javax.swing.*
import javax.swing.event.DocumentEvent
import javax.swing.event.DocumentListener

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
    private var editor: RawEditor

    private var _request: HttpRequest? = null
    private var httpRequest: HttpRequest?
        get() = this._request
        set(r) {
            this._request = r
            this.updateEditor()
        }

    private var originalRequest: HttpRequest? = null
    private var modified: Boolean = false
    private var isUpdating: Boolean = false
    private var _selectedSession: Session? = null
    private var selectedSession: Session?
        get() = this._selectedSession
        set(s) {
            if (isUpdating) return
            Logger.debug("SELECTED SESSION SET")

            val original = this.originalRequest ?: HttpRequest.httpRequest()
            val request = original.withMethod(original.method())
            this._selectedSession = s
            if (s != null) {
                Logger.debug("DIFF NULL")
                this.httpRequest = s.apply(request)
                this.sessionNameLabel.text = s.name
                this.modified = true
                Logger.debug(s.name)
            } else {
                Logger.debug("NULL")
                this.httpRequest = this.originalRequest!!.withMethod(this.originalRequest!!.method())
                this.sessionNameLabel.text = "None"
                this.modified = false
            }
            this.updateEditor()
            this.saveSessionBtn.isEnabled = false // Must be after editor update
        }
    // END State-holding stuff

    // Session stuff

    private fun updateEditor() {
        // TODO: strip unwanted headers maybe?
        this.editor.contents = httpRequest?.toByteArray() ?: ByteArray.byteArray("")
    }

    private fun updateSessionsList() {
        this.sessionsComboBox.removeAllItems()
        this.sessionsComboBox.addItem(SESSION_NONE)
        this.sessionsComboBox.toolTipText = "Select a session"
        this.plugin.getSessions().forEach { this.sessionsComboBox.addItem(it) }
    }

    private fun saveToSessionFromRequest() {
        val selected = (this.sessionsComboBox.selectedItem ?: return) as Session
        val request = this.httpRequest ?: return
        selected.loadFromRequestFiltered(request)
    }

    private fun saveToSessionFromEditor() {
        // TODO: implement
        this.saveSessionBtn.isEnabled = false
    }

    class EditorChangeListener(val callback: () -> Unit) : DocumentListener {
        override fun insertUpdate(e: DocumentEvent?) {}
        override fun removeUpdate(e: DocumentEvent?){}
        override fun changedUpdate(e: DocumentEvent?) = this.callback()
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
        val session = this.newSessionDialog() ?: return
        this.updateSessionsList()
        this.sessionsComboBox.selectedItem = session
        this.selectedSession = session
        this.deleteSessionBtn.isEnabled = true
        this.saveToSessionFromRequest()
    }

    private fun selectedSessionChanged() {
        Logger.debug("SELECTED SESSION CHANGED")
        val selected = (this.sessionsComboBox.selectedItem ?: return) as Session
        when (selected) {
            SESSION_NONE -> {
                Logger.debug("NONE BRANCH")

                this.selectedSession = null
                this.deleteSessionBtn.isEnabled = false
                this.saveSessionBtn.isEnabled = false
            }

            else -> {
                Logger.debug("ELSE BRANCH")
                this.selectedSession = selected
                this.deleteSessionBtn.isEnabled = true
            }
        }
    }

    private fun newSessionDialog(): Session? {
        var name: String?
        do {
            name = JOptionPane.showInputDialog(
                Burp.Montoya.userInterface().swingUtils().suiteFrame(),
                "Choose a name for the new Session. Valid characters: [A-Za-z0-9._-]",
                "New Session",
                JOptionPane.QUESTION_MESSAGE,
                null,
                null,
                "",
            ) as String?
            if (name == null) return null
        } while (!Session.isValidName(name!!))
        return this.plugin.createSession(name)
    }

    // END Session stuff

    private var component = BorderPanel(0)

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
        it.isEnabled = true
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
    private val saveSessionBtn = JButton("Save").also {
        it.isEnabled = false
        it.addActionListener {
            this.saveToSessionFromEditor()
        }
    }

    // End UI Stuff

    init {
        if (readOnly) {
            this.editor = Burp.Montoya.userInterface().createRawEditor(EditorOptions.READ_ONLY)
        } else {
            this.editor = Burp.Montoya.userInterface().createRawEditor()
        }

        val rootContainer = BoxPanel(BoxLayout.PAGE_AXIS)

        val sessionLabelPanel = BoxPanel(BoxLayout.LINE_AXIS).also {
            it.add(JLabel("Current Session:"))
            it.add(Box.createRigidArea(Dimension(5, 0)))
            it.add(sessionNameLabel)
        }
        rootContainer.add(BorderPanel(0, 5).also {
            it.add(sessionLabelPanel)
        })

        val buttonPanel = BoxPanel(BoxLayout.LINE_AXIS).also {
            it.add(this.sessionsComboBox)
            it.add(Box.createRigidArea(Dimension(10, 0)))
            it.add(this.newSessionBtn)
            it.add(Box.createRigidArea(Dimension(10, 0)))
            it.add(this.deleteSessionBtn)
            it.add(Box.createRigidArea(Dimension(10, 0)))
            it.add(this.saveSessionBtn)
            it.add(Box.createHorizontalGlue())
        }
        rootContainer.add(BorderPanel(10).also { it.add(buttonPanel) })

        this.component.add(BorderLayout.PAGE_START, BorderPanel(10).also { it.add(rootContainer) })
        this.component.add(BorderLayout.CENTER, this.editor.uiComponent())

        val listener = EditorChangeListener { this.saveSessionBtn.isEnabled = true }
        val jt = this.editor.getTextAreaComponent()
        jt.document.addDocumentListener(listener)
    }

    override fun setRequestResponse(requestResponse: HttpRequestResponse) {
        this.modified = false
        this.originalRequest = requestResponse.request()
        this.httpRequest = requestResponse.request().withMethod(requestResponse.request().method()) // Just a trick to ensure an object copy
        this.isUpdating = true
        val selectedItem = this.sessionsComboBox.selectedItem
        this.updateSessionsList()
        this.sessionsComboBox.selectedItem = selectedItem
        this.isUpdating = false
        this.updateEditor()
        this.saveSessionBtn.isEnabled = false
    }

    override fun isEnabledFor(requestResponse: HttpRequestResponse): Boolean = true

    override fun caption(): String = "Sessions"

    override fun uiComponent(): Component {
        return this.component
    }

    override fun selectedData(): Selection? {
        return null
    }

    override fun isModified(): Boolean = this.modified

    override fun getRequest(): HttpRequest? = this._request ?: HttpRequest.httpRequest()
}
