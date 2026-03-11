package sessionswitcher.rules.conditions.type.types

import burp.api.montoya.http.message.requests.HttpRequest
import sessionswitcher.rules.conditions.ConditionConfig
import sessionswitcher.rules.conditions.MatchInfo

object RequestHeaderConditionType :
    StringConditionType(matchOn = "Request Header", matchesOnResponse = false) {
    override fun matchesRequest(
        configuration: ConditionConfig,
        request: HttpRequest,
        matchInfo: MatchInfo,
    ): Boolean {
        val headers = request.headers().map { it.toString() }
        return if (configuration.negativeMatch) {
            headers.none { this.stringMatches(configuration, it, false) }
        } else {
            headers.any { this.stringMatches(configuration, it, false) }
        }
    }

    override fun describe(configuration: ConditionConfig): String =
        "${if (configuration.negativeMatch) "No" else "Any"} ${this.matchOn} ${configuration.operation.lowercase()} \"${configuration.extraFields["Pattern"]}\""
}
