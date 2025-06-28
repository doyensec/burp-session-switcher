package sessionswitcher.automode

import burp.api.montoya.http.message.requests.HttpRequest
import burp.api.montoya.http.message.responses.HttpResponse

data class RuleConfig(
    val isNegativeMatch: Boolean,
)

abstract class Rule {
    // Main function called during evaluation
    abstract fun matches(request: HttpRequest, response: HttpResponse?, matchInfo: MatchInfo): Boolean

    // For displaying in the UI
    abstract fun isNegativeMatch(): Boolean
    abstract fun matchOn(): String
    abstract fun needsResponse(): Boolean
    abstract fun getAvailableOperations(): List<String>
    abstract fun matchOperation(): String
    abstract fun canSetPattern(): Boolean
    abstract fun matchPattern(): String
    abstract fun validateConfiguration(): Pair<Boolean, String>

    // Prints the Rule in text format for the logs and such
    abstract fun describe(): String
}