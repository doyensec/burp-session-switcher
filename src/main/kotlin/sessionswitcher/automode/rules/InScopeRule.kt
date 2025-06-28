package sessionswitcher.automode.rules

import burp.api.montoya.http.message.requests.HttpRequest
import burp.api.montoya.http.message.responses.HttpResponse
import sessionswitcher.automode.MatchInfo
import sessionswitcher.automode.Rule

class InScopeRule(public val negative: Boolean): Rule() {
    override fun matches(request: HttpRequest, response: HttpResponse?, matchInfo: MatchInfo): Boolean {
        return request.isInScope xor negative
    }

    override fun isNegativeMatch(): Boolean {
        return negative
    }

    override fun matchOn(): String = "Scope"

    override fun needsResponse(): Boolean = false

    override fun getAvailableOperations(): List<String> = listOf("Request is in scope")

    override fun matchOperation(): String = "Request is in scope"

    override fun canSetPattern(): Boolean = false

    override fun matchPattern(): String {
        throw UnsupportedOperationException()
    }

    override fun validateConfiguration(): Pair<Boolean, String> = Pair(true, "")

    override fun describe(): String = "Rule: Request is in scope"
}