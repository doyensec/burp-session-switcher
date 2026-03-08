package sessionswitcher.requesteditor

import burp.api.montoya.http.message.HttpRequestResponse
import burp.api.montoya.http.message.requests.HttpRequest
import burp.api.montoya.ui.Selection
import burp.api.montoya.ui.editor.extension.EditorCreationContext
import burp.api.montoya.ui.editor.extension.EditorMode
import burp.api.montoya.ui.editor.extension.ExtensionProvidedHttpRequestEditor
import burp.api.montoya.ui.editor.extension.HttpRequestEditorProvider
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import sessionswitcher.Logger
import sessionswitcher.SessionSwitcher
import sessionswitcher.sessions.Session
import sessionswitcher.sessions.SessionsListUpdateListener
import sessionswitcher.settings.Settings
import sessionswitcher.settings.SettingsItem
import sessionswitcher.ui.ConfirmationDialog
import sessionswitcher.ui.SaveSessionDialog
import sessionswitcher.ui.maintab.SessionEditWindow
import sessionswitcher.utils.host
import sessionswitcher.utils.topDomain
import java.awt.BorderLayout
import java.awt.Component
import java.awt.Dimension
import java.awt.FlowLayout
import java.awt.Font
import java.util.Optional
import javax.swing.BorderFactory
import javax.swing.Box
import javax.swing.BoxLayout
import javax.swing.JButton
import javax.swing.JComboBox
import javax.swing.JLabel
import javax.swing.JPanel

class RequestEditor private constructor(
    val sessionSwitcher: SessionSwitcher,
    val readOnly: Boolean,
) : ExtensionProvidedHttpRequestEditor,
    SessionsListUpdateListener {
    companion object {
        class Provider(
            private val plugin: SessionSwitcher,
        ) : HttpRequestEditorProvider {
            override fun provideHttpRequestEditor(creationContext: EditorCreationContext): ExtensionProvidedHttpRequestEditor =
                RequestEditor(plugin, creationContext.editorMode() == EditorMode.READ_ONLY)
        }

        private var provider: Provider? = null

        fun getProvider(plugin: SessionSwitcher): Provider {
            if (provider == null) provider = Provider(plugin)
            return provider as Provider
        }

        val SESSION_NONE = Session("No change") // Placeholder for no session selected
    }

    // State-holding stuff
    private var editor = DiffHighlightRequestEditor()
    private val settings = SessionSwitcher.getInstance().settings

    private var _request: HttpRequest? = null
    private var httpRequest: HttpRequest?
        get() = this._request
        set(r) {
            val selectedSession = this.selectedSession
            this._request = r
            this.editedLabel.text = ""
            this.deleteSessionBtn.isEnabled = selectedSession != null
            this.editSessionBtn.isEnabled = selectedSession != null
            this.newOrOverwriteBtn.text = if (selectedSession == null) "New" else "Overwrite"
        }

    private val editorUpdateMutex = Mutex()
    private val comboBoxUpdateMutex = Mutex()
    private var originalRequest: HttpRequest? = null
    private var originalRequestModified: Boolean = false
    private var isUpdatingUI: Boolean = false
    private var selectedSession: Session? = null

    private fun setSelectedSession(s: Session?) =
        runBlocking {
            if (isUpdatingUI) return@runBlocking

            val original = originalRequest ?: HttpRequest.httpRequest()
            val request = original.withMethod(original.method())
            selectedSession = s
            editedLabel.text = ""
            if (s != null) {
                Logger.info("Session is ${s.name}")
                val (req, headersDiffInfo, cookiesDiffInfo) = s.apply(request, settings.cookiesInjectMode.get())
                httpRequest = req
                editorUpdateMutex.withLock {
                    editor.setRequest(req, headersDiffInfo, cookiesDiffInfo)
                }
                originalRequestModified = true
            } else {
                Logger.debug("Session is NULL")
                httpRequest = originalRequest?.withMethod(originalRequest!!.method()) ?: return@runBlocking
                editorUpdateMutex.withLock {
                    editor.setRequest(httpRequest!!)
                }
                originalRequestModified = false
            }
        }

    // END State-holding stuff

    // Session stuff

    private val contextMenu = ContextMenu(this)

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

    /*
    Updates the sessions listed in the combo box selector.
    Tries to restore the old selection if the old selected session still exists
    and it still matches the current request
     */
    private suspend fun updateSessionsList() {
        comboBoxUpdateMutex.withLock {
            val oldSession = selectedSession

            val request = originalRequest
            val hostFilter: String =
                if (request == null) {
                    // If request is null, do not filter
                    ""
                } else if (settings.filterSessionMode.get() == Settings.FilterSessionMode.BY_SUBDOMAIN) {
                    // Filter by subdomain (entire host)
                    request.host()
                } else if (settings.filterSessionMode.get() == Settings.FilterSessionMode.BY_DOMAIN) {
                    // Filter by main domain
                    request.topDomain()
                } else {
                    // No filter
                    ""
                }
            isUpdatingUI = true
            sessionsComboBox.removeAllItems()
            sessionsComboBox.addItem(SESSION_NONE)
            sessionSwitcher.sessions.getSessions(hostFilter).forEach { sessionsComboBox.addItem(it) }
            isUpdatingUI = false
            val oldSessionRestored = tryRestoreOldSession(oldSession)
            if (!oldSessionRestored) {
                sessionsComboBox.selectedIndex = 0
            }
        }
    }

    private fun editSelectedSession() {
        SessionEditWindow(sessionSwitcher, Optional.of(this.selectedSession as Session)).showDialog()
    }

    private fun deleteSelectedSession() {
        this.sessionSwitcher.sessions.deleteSession(this.sessionsComboBox.selectedItem as Session)
        runBlocking {
            updateSessionsList()
        }
    }

    private fun newOrUpdateBtnHandler() {
        val session = this.selectedSession
        if (session == null) {
            // "New" mode
            this.newSessionHandler()
        } else {
            // Ask confirmation dialog if needed
            val doNotAskOverwrite = this.sessionSwitcher.settings.editorDoNotAskOverwriteConfirmation
            if (!doNotAskOverwrite.get()) {
                val dialog =
                    ConfirmationDialog(
                        sessionSwitcher,
                        "Do you want to overwrite the selected session with the data from the original request?",
                        "Confirm Overwrite",
                    )
                dialog.show()
                if (!dialog.getAnswer()) {
                    // User canceled
                    return
                } else if (dialog.shouldSavePreference()) {
                    doNotAskOverwrite.set(true, SettingsItem.Store.GLOBAL)
                }
            }
            // "Update from original" mode
            this.updateSessionFromOriginalRequest()
        }
    }

    private fun updateSessionFromOriginalRequest() {
        val req = this.originalRequest ?: return
        val session = this.selectedSession ?: return

        val settings = this.sessionSwitcher.settings
        session.updateFromRequest(
            req,
            settings.cookiesUpdateMode.get(),
            settings.headersUpdateMode.get(),
        )

        // Apply the new session and refresh the list for good measure
        this.setSelectedSession(session)
        runBlocking {
            updateSessionsList()
        }
    }

    private fun newSessionHandler() {
        // New session
        val request = this.originalRequest ?: return
        val session = SaveSessionDialog(this.sessionSwitcher).newSessionDialog(request) ?: return
        runBlocking {
            updateSessionsList()
        }
        this.sessionsComboBox.selectedItem = session
    }

    private fun selectedSessionChanged() {
        Logger.verbose("Selected session changed to ${this.sessionsComboBox.selectedItem}")
        var selected: Session? = (this.sessionsComboBox.selectedItem ?: return) as Session
        if (selected == SESSION_NONE) {
            selected = null
        }
        if (readOnly) {
            // In read-only mode, we don't want to change the displayed request,
            // just the saved request for a possible "update" operation,
            // so we skip the update logic and set the value directly
            this.selectedSession = selected
            // Let's re-set the same request to trigger the UI update
            this.httpRequest = this.httpRequest
        } else {
            this.setSelectedSession(selected)
        }
    }

    // END Session stuff

    private var component = JPanel(BorderLayout())
    private val editedLabel =
        JLabel("").also {
            it.font = it.font.deriveFont(Font.BOLD)
        }
    private val sessionsComboBox =
        JComboBox<Session>().also {
            it.maximumSize = it.preferredSize
            it.minimumSize = Dimension(300, it.preferredSize.height)
            it.preferredSize = Dimension(300, it.preferredSize.height)
            it.toolTipText = "Select a session"
            it.addActionListener { this.selectedSessionChanged() }
        }
    private val newOrOverwriteBtn =
        JButton("New").also {
            it.isEnabled = true
            it.addActionListener {
                this.newOrUpdateBtnHandler()
            }
        }
    private val editSessionBtn =
        JButton("Edit").also {
            it.isEnabled = false
            it.addActionListener {
                this.editSelectedSession()
            }
        }
    private val deleteSessionBtn =
        JButton("Delete").also {
            it.isEnabled = false
            it.addActionListener {
                this.deleteSelectedSession()
            }
        }

    // End UI Stuff

    init {
        val sessionLabelPanel =
            JPanel().also {
                it.layout = FlowLayout(FlowLayout.LEFT, 5, 0)
                it.add(this.sessionsComboBox)
                it.add(editedLabel)
            }

        val buttonPanel =
            JPanel().also {
                it.layout = FlowLayout(FlowLayout.LEFT, 5, 0)
                it.add(this.newOrOverwriteBtn)
                it.add(this.editSessionBtn)
                it.add(this.deleteSessionBtn)
            }

        val switchSessionLabelPanel =
            JPanel().also {
                it.layout = FlowLayout(FlowLayout.LEFT, 5, 0)
                val switcherLabelText = if (this.readOnly) "Update Session:" else "Switch Session:"
                it.add(JLabel(switcherLabelText))
            }

        val rootContainer =
            JPanel().also {
                it.layout = BoxLayout(it, BoxLayout.Y_AXIS)
                it.add(switchSessionLabelPanel)
                it.add(Box.createRigidArea(Dimension(0, 4)))
                it.add(sessionLabelPanel)
                it.add(Box.createRigidArea(Dimension(0, 10)))
                it.add(buttonPanel)
            }

        val borderPanel =
            JPanel(BorderLayout()).also {
                it.border = BorderFactory.createEmptyBorder(10, 10, 10, 10)
                it.add(rootContainer)
            }

        this.component.add(BorderLayout.PAGE_START, borderPanel)
        this.component.add(BorderLayout.CENTER, this.editor)

        // Add context menu handler
        this.contextMenu.addRightClickHandler(this.editor.textPane)

        // Register for session list updates
        this.sessionSwitcher.sessions.registerUpdateListener(this)
    }

    override fun setRequestResponse(requestResponse: HttpRequestResponse?) {
        if (requestResponse == null) return
        this.originalRequestModified = false
        this.originalRequest = requestResponse.request()
        this.httpRequest = this.originalRequest
        this.editor.setRequest(this.httpRequest!!)
        runBlocking {
            updateSessionsList()
        }
    }

    override fun isEnabledFor(requestResponse: HttpRequestResponse): Boolean = true

    override fun caption(): String = "Sessions"

    override fun uiComponent(): Component = this.component

    override fun selectedData(): Selection? = null

    override fun isModified(): Boolean = this.originalRequestModified && !this.readOnly

    override fun getRequest(): HttpRequest = this.httpRequest!!

    override suspend fun onSessionsListUpdate() {
        try {
            this.updateSessionsList()
        } catch (_: Exception) {
            Logger.warning("Exception caught while updating sessions list")
        }
    }
}
