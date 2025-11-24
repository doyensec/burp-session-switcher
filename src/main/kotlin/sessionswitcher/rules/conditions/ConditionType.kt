package sessionswitcher.rules.conditions

import burp.api.montoya.proxy.ProxyHttpRequestResponse

abstract class ConditionType(val matchOn: String, val needsResponse: Boolean, val availableOperations: List<String>, val canSetPattern: Boolean) {
    // Main function called during evaluation
    abstract fun matches(configuration: ConditionConfig, requestResponse: ProxyHttpRequestResponse, matchInfo: MatchInfo): Boolean

    // Function called during rule creation to check if fields are ok
    abstract fun validateConfiguration(configuration: ConditionConfig): Pair<Boolean, String>

    // Prints the Rule in text human-readable format
    abstract fun describe(configuration: ConditionConfig): String

    override fun toString(): String = this.matchOn
}