package sessionswitcher.rules.conditions.type.types

import burp.api.montoya.http.message.requests.HttpRequest
import sessionswitcher.rules.conditions.ConditionConfig
import sessionswitcher.rules.conditions.MatchInfo

object PathConditionType :
    StringConditionType(matchOn = "Path", matchesOnResponse = false) {
    override fun matchesRequest(configuration: ConditionConfig, request: HttpRequest, matchInfo: MatchInfo): Boolean {
        val url = request.pathWithoutQuery()
        return this.stringMatches(configuration, url)
    }
}