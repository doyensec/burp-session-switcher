package sessionswitcher.rules.conditions

import burp.api.montoya.proxy.ProxyHttpRequestResponse

class ResponseHeaderCondition(pattern: String, operator: OPERATORS, val negative: Boolean = false) :
    StringCondition(matchOn = "Response Header", needsResponse = true, pattern, operator, false) {
    override fun matches(requestResponse: ProxyHttpRequestResponse, matchInfo: MatchInfo): Boolean {
        if (!requestResponse.hasResponse()) return false
        val headers = requestResponse.originalResponse().headers().map { it.toString() }
        return if (negative) {
            headers.none { this.stringMatches(it) }
        } else {
            headers.any { this.stringMatches(it) }
        }
    }
}