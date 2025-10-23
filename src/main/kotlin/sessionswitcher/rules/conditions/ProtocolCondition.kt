package sessionswitcher.rules.conditions

import burp.api.montoya.proxy.ProxyHttpRequestResponse
import sessionswitcher.rules.Condition
import sessionswitcher.rules.MatchInfo

class ProtocolCondition(public val operation: String, public val negative: Boolean): Condition(
    Properties(matchOn = "Protocol", needsResponse = false, availableOperations = listOf("HTTP", "HTTPS"), canSetPattern = false),
    Configuration(operation = operation, negativeMatch = negative)
) {
    override fun matches(requestResponse: ProxyHttpRequestResponse, matchInfo: MatchInfo): Boolean {
        return requestResponse.httpService().secure() && operation == "HTTPS" || !requestResponse.httpService().secure() && operation == "HTTP"
    }

    override fun validateConfiguration(): Pair<Boolean, String> = Pair(true, "")

    override fun describe(): String = "Rule: Request protocol is $operation"
}