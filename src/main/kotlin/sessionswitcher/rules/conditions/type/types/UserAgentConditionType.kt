package sessionswitcher.rules.conditions.type.types

import burp.api.montoya.http.message.requests.HttpRequest
import sessionswitcher.Logger
import sessionswitcher.rules.conditions.ConditionConfig
import sessionswitcher.rules.conditions.MatchInfo

object UserAgentConditionType :
    StringConditionType(matchOn = "User Agent", matchesOnResponse = false) {
    override fun matchesRequest(
        configuration: ConditionConfig,
        request: HttpRequest,
        matchInfo: MatchInfo,
    ): Boolean {
        val userAgent = request.header("User-Agent")
        if (userAgent == null) {
            Logger.debug("User-Agent header not found in request")
            return false
        }
        return this.stringMatches(configuration, userAgent.value())
    }
}
