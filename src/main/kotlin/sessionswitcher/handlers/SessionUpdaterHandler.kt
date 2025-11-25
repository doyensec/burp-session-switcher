package sessionswitcher.handlers

import burp.api.montoya.http.message.requests.HttpRequest
import burp.api.montoya.http.sessions.ActionResult
import burp.api.montoya.http.sessions.SessionHandlingAction
import burp.api.montoya.http.sessions.SessionHandlingActionData
import sessionswitcher.Logger
import sessionswitcher.SessionSwitcher
import sessionswitcher.sessions.Session
import sessionswitcher.utils.getHeaderValue

class SessionUpdaterHandler(private val sessionSwitcher: SessionSwitcher):  SessionHandlingAction {
    companion object {
        const val HEADER_NAME = "X-SessionSwitcher-Update"
    }
    override fun name(): String {
        return "Update SessionSwitcher Session"
    }

    override fun performAction(actionData: SessionHandlingActionData): ActionResult {
        var request = actionData.request()
        val sessionName = request.getHeaderValue(HEADER_NAME)

        if (sessionName == null) {
            Logger.debug("Session update header $HEADER_NAME not found in request")
            return ActionResult.actionResult(request)
        }

        request = request.withRemovedHeader(HEADER_NAME)
        val session = this.sessionSwitcher.sessions.getSession(sessionName.trim())

        if (session == null) {
            Logger.debug("Session with specified name not found: $sessionName")
            return ActionResult.actionResult(request)
        }

        Logger.debug("Updating session $sessionName")
        this.updateSession(session, request)

        return ActionResult.actionResult(request)
    }

    private fun updateSession(session: Session, httpRequest: HttpRequest) {
        val settings = this.sessionSwitcher.settings
        session.updateFromRequest(
            httpRequest,
            settings.cookiesUpdateMode.get(),
            settings.headersUpdateMode.get(),
        )
        // TODO: trigger session list update?
    }
}