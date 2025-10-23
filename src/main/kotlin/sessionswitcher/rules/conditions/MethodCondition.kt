package sessionswitcher.rules.conditions

import burp.api.montoya.proxy.ProxyHttpRequestResponse
import sessionswitcher.rules.MatchInfo

class MethodCondition(pattern: String, operator: OPERATORS, negative: Boolean = false) :
    StringCondition(matchOn = "Request Method", needsResponse = false, pattern, operator, negative) {
    override fun matches(requestResponse: ProxyHttpRequestResponse, matchInfo: MatchInfo): Boolean {
        return this.stringMatches(requestResponse.request().method())
    }
}