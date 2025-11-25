package sessionswitcher.rules.conditions.types

import burp.api.montoya.http.message.requests.HttpRequest
import sessionswitcher.rules.conditions.ConditionConfig
import sessionswitcher.rules.conditions.ConditionType
import sessionswitcher.rules.conditions.MatchInfo

object InScopeConditionType: ConditionType(matchOn = "Scope", matchesOnResponse = false, availableOperations = listOf("Request is in scope"), canSetPattern = false) {
    override fun matchesRequest(configuration: ConditionConfig, request: HttpRequest, matchInfo: MatchInfo): Boolean {
        return request.isInScope xor configuration.negativeMatch
    }
    override fun  validateConfiguration(configuration: ConditionConfig): Pair<Boolean, String> = Pair(true, "")

    override fun describe(configuration: ConditionConfig): String {
        return "Request is ${if (configuration.negativeMatch) "not in" else "in"} scope"
    }
}