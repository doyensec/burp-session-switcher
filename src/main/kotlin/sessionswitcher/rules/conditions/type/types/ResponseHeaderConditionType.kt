package sessionswitcher.rules.conditions.type.types

import burp.api.montoya.http.message.responses.HttpResponse
import sessionswitcher.rules.conditions.ConditionConfig
import sessionswitcher.rules.conditions.MatchInfo

object ResponseHeaderConditionType :
    StringConditionType(matchOn = "Response Header", matchesOnResponse = true) {
    override fun matchesResponse(
        configuration: ConditionConfig,
        response: HttpResponse,
        matchInfo: MatchInfo
    ): Boolean {
        val headers = response.headers().map { it.toString() }
        return if (configuration.negativeMatch) {
            headers.none { this.stringMatches(configuration, it, false) }
        } else {
            headers.any { this.stringMatches(configuration, it, false) }
        }
    }

    override fun describe(configuration: ConditionConfig): String {
        return "${if (configuration.negativeMatch) "No" else "Any"} ${this.matchOn} ${configuration.operation.lowercase()} \"${configuration.extraFields["Pattern"]}\""
    }
}