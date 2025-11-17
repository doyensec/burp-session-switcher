package sessionswitcher.rules.conditions.types

import burp.api.montoya.proxy.ProxyHttpRequestResponse
import sessionswitcher.rules.conditions.ConditionConfiguration
import sessionswitcher.rules.conditions.MatchInfo

object ResponseHeaderConditionType :
    StringConditionType(matchOn = "Response Header", needsResponse = true) {
    override fun matches(configuration: ConditionConfiguration, requestResponse: ProxyHttpRequestResponse, matchInfo: MatchInfo): Boolean {
        if (!requestResponse.hasResponse()) return false
        val headers = requestResponse.originalResponse().headers().map { it.toString() }
        return if (configuration.negativeMatch) {
            headers.none { this.stringMatches(configuration, it) }
        } else {
            headers.any { this.stringMatches(configuration, it) }
        }
    }
}