package sessionswitcher.rules.conditions

import burp.api.montoya.http.message.requests.HttpRequest
import burp.api.montoya.http.message.responses.HttpResponse
import sessionswitcher.rules.Condition
import sessionswitcher.rules.MatchInfo

class InScopeCondition(public val negative: Boolean): Condition(
    Properties(matchOn = "Scope", needsResponse = false, availableOperations = listOf("Request is in scope"), canSetPattern = false),
    Configuration(operation = "Request is in scope", negativeMatch = negative)
) {
    override fun matches(request: HttpRequest, response: HttpResponse?, matchInfo: MatchInfo): Boolean {
        return request.isInScope xor negative
    }

    override fun validateConfiguration(): Pair<Boolean, String> = Pair(true, "")

    override fun describe(): String = "Rule: Request is in scope"
}