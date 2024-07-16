package sessionswitcher.handlers

import burp.api.montoya.core.Annotations
import burp.api.montoya.http.message.requests.HttpRequest
import burp.api.montoya.http.sessions.ActionResult
import burp.api.montoya.http.sessions.SessionHandlingAction
import burp.api.montoya.http.sessions.SessionHandlingActionData
import sessionswitcher.Logger
import sessionswitcher.SessionSwitcher
import sessionswitcher.sessions.Session
import sessionswitcher.utils.getHeaderValue

class SessionInjectorHandler(private val sessionSwitcher: SessionSwitcher): SessionHandlingAction {
    companion object {
        final val HEADER_NAME = "X-SessionSwitcher-Inject"
    }
    override fun name(): String {
        return "Inject SessionSwitcher Session"
    }

    override fun performAction(actionData: SessionHandlingActionData): ActionResult {
        val request = actionData.request()
        val sessionName = request.getHeaderValue(SessionUpdaterHandler.HEADER_NAME)

        if (sessionName == null) {
            Logger.debug("Session injector header ${SessionUpdaterHandler.HEADER_NAME} not found in request")
            return ActionResult.actionResult(request)
        }

        val session = this.sessionSwitcher.sessions.getSession(sessionName.trim())

        if (session == null) {
            Logger.debug("Session with specified name not found: $sessionName")
            return ActionResult.actionResult(request)
        }

        Logger.debug("Updating session $sessionName")
        val newRequest = this.injectSession(session, request)
        return ActionResult.actionResult(newRequest, Annotations.annotations("SessionSwitcher: injected session $sessionName"))
    }

    private fun injectSession(session: Session, httpRequest: HttpRequest): HttpRequest {
        return session.apply(httpRequest).first
    }
}