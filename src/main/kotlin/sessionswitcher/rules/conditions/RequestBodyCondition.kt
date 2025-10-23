package sessionswitcher.rules.conditions

import burp.api.montoya.proxy.ProxyHttpRequestResponse
import sessionswitcher.rules.MatchInfo

class RequestBodyCondition(pattern: String, operator: OPERATORS, negative: Boolean = false) :
    StringCondition(matchOn = "Request Body", needsResponse = false, pattern, operator, negative) {
    override fun matches(requestResponse: ProxyHttpRequestResponse, matchInfo: MatchInfo): Boolean {
        val body = requestResponse.request().bodyToString()
        if (body.isNullOrBlank()) {
            return false
        }
        return this.stringMatches(body)
    }
}