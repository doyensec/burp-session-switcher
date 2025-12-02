package sessionswitcher.rules.conditions.type.types

import burp.api.montoya.http.message.responses.HttpResponse
import sessionswitcher.rules.conditions.ConditionConfig
import sessionswitcher.rules.conditions.MatchInfo

object StatusCodeConditionType :
    StringConditionType(matchOn = "Response Status Code", matchesOnResponse = true) {
    override fun matchesResponse(
        configuration: ConditionConfig,
        response: HttpResponse,
        matchInfo: MatchInfo
    ): Boolean {
        return this.stringMatches(configuration, response.statusCode().toString())
    }
}