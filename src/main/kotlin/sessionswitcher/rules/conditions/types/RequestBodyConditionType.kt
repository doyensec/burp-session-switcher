package sessionswitcher.rules.conditions.types

import burp.api.montoya.proxy.ProxyHttpRequestResponse
import sessionswitcher.rules.conditions.ConditionConfig
import sessionswitcher.rules.conditions.MatchInfo

object RequestBodyConditionType :
    StringConditionType(matchOn = "Request Body", needsResponse = false) {
    override fun matches(configuration: ConditionConfig, requestResponse: ProxyHttpRequestResponse, matchInfo: MatchInfo): Boolean {
        val body = requestResponse.request().bodyToString()
        if (body.isNullOrBlank()) {
            return false
        }
        return this.stringMatches(configuration, body)
    }
}