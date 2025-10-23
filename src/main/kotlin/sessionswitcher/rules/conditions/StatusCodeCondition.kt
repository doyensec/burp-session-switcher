package sessionswitcher.rules.conditions

import burp.api.montoya.proxy.ProxyHttpRequestResponse
import sessionswitcher.rules.MatchInfo

class StatusCodeCondition(pattern: String, operator: OPERATORS, negative: Boolean = false) :
    StringCondition(matchOn = "Response Status Code", needsResponse = true, pattern, operator, negative) {
    override fun matches(requestResponse: ProxyHttpRequestResponse, matchInfo: MatchInfo): Boolean {
        return this.stringMatches(requestResponse.originalResponse().statusCode().toString())
    }
}