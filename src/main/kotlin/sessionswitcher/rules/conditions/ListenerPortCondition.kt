package sessionswitcher.rules.conditions

import burp.api.montoya.proxy.ProxyHttpRequestResponse

class ListenerPortCondition(pattern: String, operator: OPERATORS, negative: Boolean = false) :
    StringCondition(matchOn = "Listener Port", needsResponse = false, pattern, operator, negative) {
    override fun matches(requestResponse: ProxyHttpRequestResponse, matchInfo: MatchInfo): Boolean {
        return this.stringMatches(requestResponse.listenerPort().toString())
    }
}