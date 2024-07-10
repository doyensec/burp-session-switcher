package sessionswitcher.proxy

import burp.api.montoya.core.HighlightColor
import burp.api.montoya.http.message.requests.HttpRequest
import burp.api.montoya.proxy.http.InterceptedRequest
import burp.api.montoya.proxy.http.ProxyRequestHandler
import burp.api.montoya.proxy.http.ProxyRequestReceivedAction
import burp.api.montoya.proxy.http.ProxyRequestToBeSentAction
import sessionswitcher.Logger
import sessionswitcher.SessionSwitcher
import sessionswitcher.sessions.Session
import sessionswitcher.utils.getHeaderValue
import sessionswitcher.utils.withUpsertedHeader

class Injector(private val plugin: SessionSwitcher): ProxyRequestHandler {
    companion object {
        const val INTERCEPT_HEADER = "X-Burp-Session"

        private fun injectSessionDataInRequest(request: HttpRequest, session: Session): HttpRequest {
            Logger.debug("Injecting session ${session.name} in request to ${request.url()}")

            var newReq = request
            for ((key, value) in session.headers) {
                newReq = newReq.withUpsertedHeader(key, value)
            }
            return newReq
        }
    }
    private val color: HighlightColor = HighlightColor.highlightColor(plugin.settings.proxyHighlightInjectedColor.get())


    // Part of ProxyRequestHandler interface, executed first on every request coming through Burp's Proxy tool
    override fun handleRequestReceived(request: InterceptedRequest): ProxyRequestReceivedAction {
        return ProxyRequestReceivedAction.continueWith(request)
    }

    // Part of ProxyRequestHandler interface, leave it alone
    override fun handleRequestToBeSent(interceptedRequest: InterceptedRequest): ProxyRequestToBeSentAction {
        // Has our proprietary `X-Burp-Session` header holding the session identifier
        if (interceptedRequest.getHeaderValue(INTERCEPT_HEADER) != null) {
            Logger.debug("Request with Session header intercepted")

            val sessionName = interceptedRequest.getHeaderValue(INTERCEPT_HEADER)
            if (sessionName.isNullOrBlank()) {
                return ProxyRequestToBeSentAction.continueWith(interceptedRequest)
            }
            val session = this.plugin.sessions.getSession(sessionName)
            if (session == null) {
                Logger.error("Requested session not found")
                return ProxyRequestToBeSentAction.continueWith(interceptedRequest)
            }
            var request: HttpRequest = interceptedRequest
            request = injectSessionDataInRequest(request, session)
            request = request.withRemovedHeader(INTERCEPT_HEADER)
            highlight(interceptedRequest, session.name)

            return ProxyRequestToBeSentAction.continueWith(request)
        }

        // Otherwise just forward request
        return ProxyRequestToBeSentAction.continueWith(interceptedRequest)
    }

    private fun highlight(req: InterceptedRequest, session: String) {
        req.annotations().setHighlightColor(this.color)
        req.annotations().setNotes("Injected session: $session")
    }
}