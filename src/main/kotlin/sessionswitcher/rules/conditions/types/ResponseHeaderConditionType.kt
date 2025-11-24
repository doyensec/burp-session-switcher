package sessionswitcher.rules.conditions.types

import burp.api.montoya.proxy.ProxyHttpRequestResponse
import sessionswitcher.rules.conditions.ConditionConfig
import sessionswitcher.rules.conditions.MatchInfo

object ResponseHeaderConditionType :
    StringConditionType(matchOn = "Response Header", needsResponse = true) {
    override fun matches(configuration: ConditionConfig, requestResponse: ProxyHttpRequestResponse, matchInfo: MatchInfo): Boolean {
        if (!requestResponse.hasResponse()) return false
        val headers = requestResponse.originalResponse().headers().map { it.toString() }
        return if (configuration.negativeMatch) {
            headers.none { this.stringMatches(configuration, it) }
        } else {
            headers.any { this.stringMatches(configuration, it) }
        }
    }

    override fun describe(configuration: ConditionConfig): String {
        return "${if (configuration.negativeMatch) "No" else "Any"} ${this.matchOn} ${configuration.operation.lowercase()} \"${configuration.pattern.get()}\""
    }
}