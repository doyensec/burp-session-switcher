package sessionswitcher.rules.conditions.types

import burp.api.montoya.http.message.responses.HttpResponse
import sessionswitcher.rules.conditions.ConditionConfig
import sessionswitcher.rules.conditions.MatchInfo

object ResponseBodyConditionType :
    StringConditionType(matchOn = "Response Body", matchesOnResponse = true) {
    override fun matchesResponse(
        configuration: ConditionConfig,
        response: HttpResponse,
        matchInfo: MatchInfo
    ): Boolean {
        val body = response.bodyToString()
        if (body.isNullOrBlank()) {
            return false
        }
        return this.stringMatches(configuration, body)
    }
}