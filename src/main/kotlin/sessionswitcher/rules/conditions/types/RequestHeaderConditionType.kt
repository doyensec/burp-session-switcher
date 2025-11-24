package sessionswitcher.rules.conditions.types

import burp.api.montoya.proxy.ProxyHttpRequestResponse
import sessionswitcher.rules.conditions.ConditionConfiguration
import sessionswitcher.rules.conditions.MatchInfo

object RequestHeaderConditionType :
    StringConditionType(matchOn = "Request Header", needsResponse = false) {
    override fun matches(configuration: ConditionConfiguration, requestResponse: ProxyHttpRequestResponse, matchInfo: MatchInfo): Boolean {
        val headers = requestResponse.request().headers().map { it.toString() }
        return if (configuration.negativeMatch) {
            headers.none { this.stringMatches(configuration, it) }
        } else {
            headers.any { this.stringMatches(configuration, it) }
        }
    }

    override fun describe(configuration: ConditionConfiguration): String {
        return "${if (configuration.negativeMatch) "No" else "Any"} ${this.matchOn} ${configuration.operation.lowercase()} \"${configuration.pattern.get()}\""
    }
}