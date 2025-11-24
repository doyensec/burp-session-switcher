package sessionswitcher.rules.conditions.types

import burp.api.montoya.proxy.ProxyHttpRequestResponse
import sessionswitcher.rules.conditions.ConditionConfig
import sessionswitcher.rules.conditions.MatchInfo

object ResponseBodyConditionType :
    StringConditionType(matchOn = "Response Body", needsResponse = true) {
    override fun matches(configuration: ConditionConfig, requestResponse: ProxyHttpRequestResponse, matchInfo: MatchInfo): Boolean {
        if (!requestResponse.hasResponse()) return false
        val body = requestResponse.originalResponse().bodyToString()
        if (body.isNullOrBlank()) {
            return false
        }
        return this.stringMatches(configuration, body)
    }
}