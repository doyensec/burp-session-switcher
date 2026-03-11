package sessionswitcher.rules.conditions.type.types

import burp.api.montoya.http.message.requests.HttpRequest
import sessionswitcher.rules.conditions.ConditionConfig
import sessionswitcher.rules.conditions.MatchInfo
import sessionswitcher.rules.conditions.type.ConditionType

object InScopeConditionType : ConditionType(
    matchOn = "Scope",
    matchesOnResponse = false,
    availableOperations = listOf("Request is in scope"),
) {
    override fun matchesRequest(
        configuration: ConditionConfig,
        request: HttpRequest,
        matchInfo: MatchInfo,
    ): Boolean = request.isInScope xor configuration.negativeMatch

    override fun validateConfiguration(configuration: ConditionConfig): Pair<Boolean, String> = Pair(true, "")

    override fun describe(configuration: ConditionConfig): String =
        "Request is ${if (configuration.negativeMatch) "not in" else "in"} scope"
}
