package sessionswitcher.rules.conditions.type.types

import burp.api.montoya.http.message.requests.HttpRequest
import sessionswitcher.rules.conditions.ConditionConfig
import sessionswitcher.rules.conditions.MatchInfo

object UrlConditionType :
    StringConditionType(matchOn = "URL", matchesOnResponse = false) {
    override fun matchesRequest(
        configuration: ConditionConfig,
        request: HttpRequest,
        matchInfo: MatchInfo,
    ): Boolean {
        val url = request.url()
        return this.stringMatches(configuration, url)
    }
}
