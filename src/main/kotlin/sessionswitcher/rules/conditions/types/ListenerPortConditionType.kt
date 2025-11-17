package sessionswitcher.rules.conditions.types

import burp.api.montoya.proxy.ProxyHttpRequestResponse
import sessionswitcher.rules.conditions.ConditionConfiguration
import sessionswitcher.rules.conditions.MatchInfo

object ListenerPortConditionType :
    StringConditionType(matchOn = "Listener Port", needsResponse = false) {
    override fun matches(configuration: ConditionConfiguration, requestResponse: ProxyHttpRequestResponse, matchInfo: MatchInfo): Boolean {
        return this.stringMatches(configuration, requestResponse.listenerPort().toString())
    }
}