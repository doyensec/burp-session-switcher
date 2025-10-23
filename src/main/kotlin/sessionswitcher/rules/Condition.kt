package sessionswitcher.rules

import burp.api.montoya.proxy.ProxyHttpRequestResponse
import java.util.*

abstract class Condition(public val properties: Properties, public val configuration: Configuration) {
    class Properties(matchOn: String, needsResponse: Boolean, availableOperations: List<String>, canSetPattern: Boolean) {
        public val matchOn: String = matchOn
        public val needsResponse: Boolean = needsResponse
        public val availableOperations: List<String> = availableOperations
        public val canSetPattern: Boolean = canSetPattern
    }

    class Configuration(operation: String, negativeMatch: Boolean = false, pattern: Optional<String> = Optional.empty()) {
        public var operation: String = operation
        public var negativeMatch: Boolean = negativeMatch
        public var pattern: Optional<String> = pattern
    }

    // Main function called during evaluation
    abstract fun matches(requestResponse: ProxyHttpRequestResponse, matchInfo: MatchInfo): Boolean

    // Function called during rule creation to check if fields are ok
    abstract fun validateConfiguration(): Pair<Boolean, String>

    // Prints the Rule in text format for the logs and such
    abstract fun describe(): String
}