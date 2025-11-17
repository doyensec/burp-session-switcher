package sessionswitcher.rules.conditions.types

import burp.api.montoya.proxy.ProxyHttpRequestResponse
import sessionswitcher.rules.conditions.ConditionConfiguration
import sessionswitcher.rules.conditions.ConditionType
import sessionswitcher.rules.conditions.MatchInfo

object InScopeConditionType: ConditionType(matchOn = "Scope", needsResponse = false, availableOperations = listOf("Request is in scope"), canSetPattern = false) {
    override fun matches(configuration: ConditionConfiguration, requestResponse: ProxyHttpRequestResponse, matchInfo: MatchInfo): Boolean {
        return requestResponse.request().isInScope xor configuration.negativeMatch
    }
    override fun  validateConfiguration(configuration: ConditionConfiguration): Pair<Boolean, String> = Pair(true, "")

    override fun describe(configuration: ConditionConfiguration): String = "Request is in scope"
}