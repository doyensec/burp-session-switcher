package sessionswitcher.rules.conditions.type.types

import burp.api.montoya.http.message.requests.HttpRequest
import sessionswitcher.rules.conditions.ConditionConfig
import sessionswitcher.rules.conditions.MatchInfo
import sessionswitcher.sessions.Cookies

object RequestCookieConditionType :
    StringConditionType(matchOn = "Request Cookie", matchesOnResponse = false) {
    override fun matchesRequest(configuration: ConditionConfig, request: HttpRequest, matchInfo: MatchInfo): Boolean {
        val cookies = Cookies.fromHttpRequest(request)
        val cookiePairs = cookies.getPairs().map { "${it.first}=${it.second}" }
        return if (configuration.negativeMatch) {
            cookiePairs.none { this.stringMatches(configuration, it, false) }
        } else {
            cookiePairs.any { this.stringMatches(configuration, it, false) }
        }
    }

    override fun describe(configuration: ConditionConfig): String {
        return "${if (configuration.negativeMatch) "No" else "Any"} ${this.matchOn} ${configuration.operation.lowercase()} \"${configuration.extraFields["Pattern"]}\""
    }
}