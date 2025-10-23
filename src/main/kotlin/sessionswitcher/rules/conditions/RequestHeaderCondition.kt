package sessionswitcher.rules.conditions

import burp.api.montoya.proxy.ProxyHttpRequestResponse

class RequestHeaderCondition(pattern: String, operator: OPERATORS, val negative: Boolean = false) :
    StringCondition(matchOn = "Request Header", needsResponse = false, pattern, operator, false) {
    override fun matches(requestResponse: ProxyHttpRequestResponse, matchInfo: MatchInfo): Boolean {
        val headers = requestResponse.request().headers().map { it.toString() }
        return if (negative) {
            headers.none { this.stringMatches(it) }
        } else {
            headers.any { this.stringMatches(it) }
        }
    }
}