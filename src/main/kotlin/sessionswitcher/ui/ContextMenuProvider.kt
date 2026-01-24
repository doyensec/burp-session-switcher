package sessionswitcher.ui

import burp.api.montoya.http.message.requests.HttpRequest
import burp.api.montoya.ui.contextmenu.AuditIssueContextMenuEvent
import burp.api.montoya.ui.contextmenu.ContextMenuEvent
import burp.api.montoya.ui.contextmenu.ContextMenuItemsProvider
import sessionswitcher.SessionSwitcher
import java.awt.Component

/*
    This class provides the Context Menu that is opened when the user Right-Clicks inside the request editor
    The autoupdate have associated Keyboard Shortcuts so that standard Burp shortcuts can be used from the editor
 */
class ContextMenuProvider(private val sessionSwitcher: SessionSwitcher) : ContextMenuItemsProvider {

    private val saveNewSessionAction = MenuAction("New session from this request", null) {
        SaveSessionDialog(this.sessionSwitcher).newSessionDialog(this.request!!)
    }

    private val updateSessionAction = MenuAction("Update session from this request", null) {
        SaveSessionDialog(this.sessionSwitcher).updateSessionDialog(this.request!!)
    }

    private val menuItems = listOf(
        this.saveNewSessionAction.asJMenuItem(),
        this.updateSessionAction.asJMenuItem()
    )

    private var request: HttpRequest? = null
    private fun requestFromContext(event: ContextMenuEvent): HttpRequest? {
        val invocationType = event.invocationType()?: return null
        if (invocationType.containsHttpRequestResponses()) {
            val requestResponses = event.selectedRequestResponses()
            if (requestResponses.size != 1) return null
            return requestResponses[0].request()
        } else if (invocationType.containsHttpMessage()) {
            val msg = event.messageEditorRequestResponse().orElse(null) ?: return null
            return msg.requestResponse().request()
        }
        return null
    }

    override fun provideMenuItems(event: ContextMenuEvent): List<Component> {
        this.request = this.requestFromContext(event) ?: return emptyList()
        return menuItems
    }

    override fun provideMenuItems(event: AuditIssueContextMenuEvent): List<Component> {
        val issues = event.selectedIssues()
        if (issues.size != 1) return emptyList()
        val requestResponses = issues[0].requestResponses()
        if (requestResponses.isEmpty()) return emptyList()
        this.request = requestResponses[0].request()
        return menuItems
    }
}
