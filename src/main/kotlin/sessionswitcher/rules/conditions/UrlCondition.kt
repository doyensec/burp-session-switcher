package sessionswitcher.rules.conditions

import burp.api.montoya.proxy.ProxyHttpRequestResponse
import sessionswitcher.rules.MatchInfo

class UrlCondition(pattern: String, operator: OPERATORS, negative: Boolean = false) :
    StringCondition(matchOn = "URL", needsResponse = false, pattern, operator, negative) {
    override fun matches(requestResponse: ProxyHttpRequestResponse, matchInfo: MatchInfo): Boolean {
        val url = requestResponse.request().url()
        return this.stringMatches(url)
    }
}