package sessionswitcher.rules.conditions.types

import burp.api.montoya.http.message.requests.HttpRequest
import sessionswitcher.rules.conditions.ConditionConfig
import sessionswitcher.rules.conditions.MatchInfo

object QueryStringConditionType :
    StringConditionType(matchOn = "Request Parameter", matchesOnResponse = false) {
    override fun matchesRequest(
        configuration: ConditionConfig,
        request: HttpRequest,
        matchInfo: MatchInfo
    ): Boolean {
        request.parameters()
        val queryString = request.query()
        val parameters = queryString.split("&")
        return if (configuration.negativeMatch) {
            parameters.none { this.stringMatches(configuration, it, false) }
        } else {
            parameters.any { this.stringMatches(configuration, it, false) }
        }
    }

    override fun describe(configuration: ConditionConfig): String {
        return "${if (configuration.negativeMatch) "No" else "Any"} ${this.matchOn} ${configuration.operation.lowercase()} \"${configuration.pattern.get()}\""
    }
}