package sessionswitcher.rules.conditions.types

import burp.api.montoya.proxy.ProxyHttpRequestResponse
import sessionswitcher.rules.conditions.ConditionConfiguration
import sessionswitcher.rules.conditions.MatchInfo

object QueryStringConditionType :
    StringConditionType(matchOn = "Request Parameter", needsResponse = false) {
    override fun matches(
        configuration: ConditionConfiguration,
        requestResponse: ProxyHttpRequestResponse,
        matchInfo: MatchInfo
    ): Boolean {
        val queryString = requestResponse.request().query()
        val parameters = queryString.split("&")
        return if (configuration.negativeMatch) {
            parameters.none { this.stringMatches(configuration, it) }
        } else {
            parameters.any { this.stringMatches(configuration, it) }
        }
    }

    override fun describe(configuration: ConditionConfiguration): String {
        return "${if (configuration.negativeMatch) "No" else "Any"} ${this.matchOn} ${configuration.operation.lowercase()} \"${configuration.pattern.get()}\""
    }
}