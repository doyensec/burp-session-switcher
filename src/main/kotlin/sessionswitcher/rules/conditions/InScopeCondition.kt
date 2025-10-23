package sessionswitcher.rules.conditions

import burp.api.montoya.proxy.ProxyHttpRequestResponse

class InScopeCondition(public val negative: Boolean): Condition(
    Properties(matchOn = "Scope", needsResponse = false, availableOperations = listOf("Request is in scope"), canSetPattern = false),
    Configuration(operation = "Request is in scope", negativeMatch = negative)
) {
    override fun matches(requestResponse: ProxyHttpRequestResponse, matchInfo: MatchInfo): Boolean {
        return requestResponse.request().isInScope xor negative
    }

    override fun validateConfiguration(): Pair<Boolean, String> = Pair(true, "")

    override fun describe(): String = "Rule: Request is in scope"
}