package sessionswitcher.rules.conditions.types

import burp.api.montoya.http.message.requests.HttpRequest
import sessionswitcher.rules.conditions.ConditionConfig
import sessionswitcher.rules.conditions.MatchInfo

object RequestBodyConditionType :
    StringConditionType(matchOn = "Request Body", matchesOnResponse = false) {
    override fun matchesRequest(configuration: ConditionConfig, request: HttpRequest, matchInfo: MatchInfo): Boolean {
        val body = request.bodyToString()
        if (body.isNullOrBlank()) {
            return false
        }
        return this.stringMatches(configuration, body)
    }
}