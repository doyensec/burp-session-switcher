package sessionswitcher.ui

import burp.api.montoya.http.message.HttpRequestResponse
import burp.api.montoya.http.message.requests.HttpRequest
import burp.api.montoya.ui.Selection
import burp.api.montoya.ui.editor.extension.EditorCreationContext
import burp.api.montoya.ui.editor.extension.ExtensionProvidedHttpRequestEditor
import burp.api.montoya.ui.editor.extension.HttpRequestEditorProvider
import sessionswitcher.Logger
import sessionswitcher.SessionSwitcher
import sessionswitcher.sessions.Session
import sessionswitcher.ui.editor.DiffHighlightRequestEditor
import sessionswitcher.ui.misc.BorderPanel
import sessionswitcher.ui.misc.BoxPanel
import sessionswitcher.ui.misc.FlowPanel
import sessionswitcher.ui.misc.SendFromPluginHandler
import sessionswitcher.utils.host
import java.awt.*
import javax.swing.*

class RequestEditorSwitcher private constructor(val plugin: SessionSwitcher) :
    ExtensionProvidedHttpRequestEditor {
    companion object {
        class Provider(private val plugin: SessionSwitcher) : HttpRequestEditorProvider {
            override fun provideHttpRequestEditor(creationContext: EditorCreationContext?): ExtensionProvidedHttpRequestEditor {
                return RequestEditorSwitcher(plugin)
            }
        }

        private var provider: Provider? = null
        fun getProvider(plugin: SessionSwitcher): Provider {
            if (provider == null) provider = Provider(plugin)
            return provider as Provider
        }

        val SESSION_NONE = Session("No change", "none")
    }

    // State-holding stuff
    private var editor = DiffHighlightRequestEditor()

    private var _request: HttpRequest? = null
    private var httpRequest: HttpRequest?
        get() = this._request
        set(r) {
            this._request = r
            this.editedLabel.text = ""
            this.deleteSessionBtn.isEnabled = this.selectedSession != null
            this.editSessionBtn.isEnabled = this.selectedSession != null
        }

    private var originalRequest: HttpRequest? = null
    private var originalRequestModified: Boolean = false
    private var isUpdatingUI: Boolean = false
    private var _selectedSession: Session? = null
    private var selectedSession: Session?
        get() = this._selectedSession
        set(s) {
            if (isUpdatingUI) return
            Logger.info("Selected session set")

            val original = this.originalRequest ?: HttpRequest.httpRequest()
            val request = original.withMethod(original.method())
            this._selectedSession = s
            this.editedLabel.text = ""
            if (s != null) {
                Logger.info("not null")
                val (req, headersDiffInfo, cookiesDiffinfo) = s.apply(request)
                this.httpRequest = req
                this.editor.setRequest(req, headersDiffInfo, cookiesDiffinfo)
                this.originalRequestModified = true
                Logger.info(s.name)
            } else {
                Logger.info("NULL")
                this.httpRequest = this.originalRequest!!.withMethod(this.originalRequest!!.method())
                this.editor.setRequest(this.httpRequest!!)
                this.originalRequestModified = false
            }
        }
    // END State-holding stuff

    // Session stuff

    private val contextMenu = EditorSendRequestFromPluginHandler(this)

    private fun tryRestoreOldSession(old: Session?): Boolean {
        if (old == null) return false
        val req = this.httpRequest ?: return false

        // See if old session is still in the combo box
        var found = false
        for (i in 0..<this.sessionsComboBox.itemCount) {
            if (this.sessionsComboBox.getItemAt(i) == old) {
                found = true
                break
            }
        }
        if (!found) return false

        // See if the old session still matches the request
        if (!old.matchesRequest(req)) {
            return false
        }

        // Ok, select it in the combo box
        this.sessionsComboBox.selectedItem = old
        return true
    }
    private fun updateSessionsList() {
        this.isUpdatingUI = true
        val oldSession = this.selectedSession
        this.sessionsComboBox.removeAllItems()
        this.sessionsComboBox.addItem(SESSION_NONE)

        val settings = SessionSwitcher.getInstance().settings
        val request = this.originalRequest
        val hostFilter: String = if (request == null) {
            // If request is null, do not filter
            ""
        } else if (settings.filterSessionBySubdomain.get()) {
            // Filter by subdomain (entire host)
            request.host()
        } else if (settings.filterSessionByDomain.get()) {
            // Filter by main domain
            request.host().split('.').takeLast(2).joinToString(".")
        } else {
            // No filter
            ""
        }

        this.plugin.sessions.getSessions(hostFilter).forEach { this.sessionsComboBox.addItem(it) }
        val oldSessionRestored = this.tryRestoreOldSession(oldSession)
        this.isUpdatingUI = false
        if (!oldSessionRestored) {
            this.sessionsComboBox.selectedIndex = 0
        }
    }

    private fun editSelectedSession() {
        TODO()
    }

    private fun deleteSelectedSession() {
        this.plugin.sessions.deleteSession(this.sessionsComboBox.selectedItem as Session)
        this.updateSessionsList()
    }

    private fun newSessionHandler() {
        // New session
        val session = this.newSession() ?: return
        this.updateSessionsList()
        val request = this.originalRequest ?: return
        session.loadFromRequest(request)
        this.sessionsComboBox.selectedItem = session
    }

    private fun selectedSessionChanged() {
        Logger.info("Selected session changed")
        val selected = (this.sessionsComboBox.selectedItem ?: return) as Session
        when (selected) {
            SESSION_NONE -> {
                Logger.info("Selected session null")
                this.selectedSession = null
            }
            else -> {
                Logger.info("Selected session NOT null")
                this.selectedSession = selected
            }
        }
    }

    private fun newSession(): Session? {
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
    private val newSessionBtn = JButton("New").also {
        it.isEnabled = true
        it.addActionListener {
            this.newSessionHandler()
        }
    }
    private val editSessionBtn = JButton("Edit").also {
        it.isEnabled = false
        it.addActionListener {
            this.editSelectedSession()
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
        this.newSessionBtn.isEnabled = true
    }

    // End UI Stuff

    init {
        val rootContainer = BoxPanel(BoxLayout.Y_AXIS)

        val sessionLabelPanel = FlowPanel(FlowLayout.LEFT).also {
            it.add(this.sessionsComboBox)
            it.add(editedLabel)
        }

        val buttonPanel = FlowPanel(FlowLayout.LEFT).also {
            it.add(this.newSessionBtn)
            it.add(this.editSessionBtn)
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

        // Add context menu handler
        //this.contextMenu.addRightClickHandler(this.editor.getTextAreaComponent())
        //this.contextMenu.addKeyboardShortcutHandler(this.editor.getTextAreaComponent())
    }

    override fun setRequestResponse(requestResponse: HttpRequestResponse) {
        this.originalRequestModified = false
        this.originalRequest = requestResponse.request()
        this.httpRequest = this.originalRequest
        this.editor.setRequest(this.httpRequest!!)
        this.updateSessionsList()
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
