package sessionswitcher.rules.autoupdate

import burp.api.montoya.proxy.http.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.channels.Channel.Factory.UNLIMITED
import kotlinx.coroutines.launch
import sessionswitcher.Logger
import sessionswitcher.SessionSwitcher

class AutoUpdateProxyListener(private val sessionSwitcher: SessionSwitcher) : ProxyRequestHandler,
    ProxyResponseHandler {
    private val supervisor = SupervisorJob()
    private val coroutineScope = CoroutineScope(supervisor + Dispatchers.Default)
    private val tasks = Channel<suspend () -> Unit>(UNLIMITED)
    val stopAtFirstMatch = sessionSwitcher.settings.stopAtFirstUpdateRule.get()

    init {
        coroutineScope.launch {
            for (task in tasks) {
                try {
                    task()
                } catch (t: Throwable) {
                    Logger.error("Error in worker: ${t.message}")
                }
            }
        }
        Logger.debug("Auto update proxy listener initialized")
    }


    override fun handleRequestReceived(interceptedRequest: InterceptedRequest): ProxyRequestReceivedAction {
        coroutineScope.launch {
            tasks.send {
                for (rule in sessionSwitcher.updateRulesCollection.getRequestMatchingRules()) {
                    if (rule.updateIfRequestMatches(interceptedRequest)) {
                        Logger.info("Request ${interceptedRequest.messageId()} with URL ${interceptedRequest.url()} matched rule ${rule.ruleId}")
                        if (stopAtFirstMatch) return@send
                    }
                }
            }
        }
        return ProxyRequestReceivedAction.continueWith(interceptedRequest)
    }

    override fun handleRequestToBeSent(interceptedRequest: InterceptedRequest): ProxyRequestToBeSentAction {
        return ProxyRequestToBeSentAction.continueWith(interceptedRequest)
    }

    override fun handleResponseReceived(interceptedResponse: InterceptedResponse): ProxyResponseReceivedAction {
        coroutineScope.launch {
            tasks.send {
                for (rule in sessionSwitcher.updateRulesCollection.getResponseMatchingRules()) {
                    if (rule.updateIfResponseMatches(interceptedResponse)) {
                        Logger.info(
                            "Response ${interceptedResponse.messageId()} with URL ${
                                interceptedResponse.request().url()
                            } matched rule ${rule.ruleId}"
                        )
                        if (stopAtFirstMatch) return@send
                    }
                }
            }
        }
        return ProxyResponseReceivedAction.continueWith(interceptedResponse)
    }

    override fun handleResponseToBeSent(interceptedResponse: InterceptedResponse): ProxyResponseToBeSentAction {
        return ProxyResponseToBeSentAction.continueWith(interceptedResponse)
    }

    fun stop() {
        try {
            supervisor.cancel()
        } catch (_: Exception) {
            // Ignore
        }
    }
}