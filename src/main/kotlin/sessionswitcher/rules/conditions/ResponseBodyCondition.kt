package sessionswitcher.rules.conditions

import burp.api.montoya.proxy.ProxyHttpRequestResponse

class ResponseBodyCondition(pattern: String, operator: OPERATORS, negative: Boolean = false) :
    StringCondition(matchOn = "Response Body", needsResponse = true, pattern, operator, negative) {
    override fun matches(requestResponse: ProxyHttpRequestResponse, matchInfo: MatchInfo): Boolean {
        if (!requestResponse.hasResponse()) return false
        val body = requestResponse.originalResponse().bodyToString()
        if (body.isNullOrBlank()) {
            return false
        }
        return this.stringMatches(body)
    }
}