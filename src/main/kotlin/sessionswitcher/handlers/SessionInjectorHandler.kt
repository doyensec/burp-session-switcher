package sessionswitcher.handlers

import burp.api.montoya.core.Annotations
import burp.api.montoya.core.HighlightColor
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
        const val HEADER_NAME = "X-SessionSwitcher-Inject"
    }
    override fun name(): String {
        return "Inject SessionSwitcher Session"
    }

    override fun performAction(actionData: SessionHandlingActionData): ActionResult {
        var request = actionData.request()
        val sessionName = request.getHeaderValue(HEADER_NAME)

        if (sessionName == null) {
            Logger.debug("Session injector header ${HEADER_NAME} not found in request")
            return ActionResult.actionResult(request)
        }

        request = request.withRemovedHeader(HEADER_NAME)
        val session = this.sessionSwitcher.sessions.getSession(sessionName.trim())

        if (session == null) {
            Logger.debug("Session with specified name not found: $sessionName")
            return ActionResult.actionResult(request)
        }

        // Prepare annotations
        val settings = this.sessionSwitcher.settings
        val highlightColor = HighlightColor.highlightColor(settings.injectorHighlightColor.get()) ?: HighlightColor.NONE
        var annotations = Annotations.annotations(highlightColor)
        if (settings.injectorAnnotateRequest.get()) {
           annotations = annotations.withNotes("SessionSwitcher: injected session $sessionName")
        }

        // Inject session
        Logger.debug("Updating session $sessionName")
        val newRequest = this.injectSession(session, request)
        return ActionResult.actionResult(newRequest, annotations)
    }

    private fun injectSession(session: Session, httpRequest: HttpRequest): HttpRequest {
        return session.apply(httpRequest).first
    }
}