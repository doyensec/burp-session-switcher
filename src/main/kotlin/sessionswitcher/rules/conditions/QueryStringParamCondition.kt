package sessionswitcher.rules.conditions

import burp.api.montoya.proxy.ProxyHttpRequestResponse
import sessionswitcher.rules.MatchInfo

class QueryStringParamCondition(pattern: String, operator: OPERATORS, val negative: Boolean = false) :
    StringCondition(matchOn = "Request Parameter", needsResponse = false, pattern, operator, false) {
    override fun matches(requestResponse: ProxyHttpRequestResponse, matchInfo: MatchInfo): Boolean {
        val queryString = requestResponse.request().query()
        val parameters = queryString.split("&")
        return if (negative) {
            parameters.none { this.stringMatches(it) }
        } else {
            parameters.any { this.stringMatches(it) }
        }
    }
}