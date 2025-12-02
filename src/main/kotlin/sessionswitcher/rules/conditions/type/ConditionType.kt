package sessionswitcher.rules.conditions.type

import burp.api.montoya.http.message.requests.HttpRequest
import burp.api.montoya.http.message.responses.HttpResponse
import sessionswitcher.rules.conditions.ConditionConfig
import sessionswitcher.rules.conditions.MatchInfo
import sessionswitcher.rules.conditions.type.types.ConditionField

abstract class ConditionType(
    val matchOn: String,
    val matchesOnResponse: Boolean,
    val availableOperations: List<String>,
    val extraFields: List<ConditionField> = listOf(),
) {
    // Main functions called during evaluation

    open fun matchesRequest(configuration: ConditionConfig, request: HttpRequest, matchInfo: MatchInfo): Boolean {
        return false
    }

    open fun matchesResponse(configuration: ConditionConfig, response: HttpResponse, matchInfo: MatchInfo): Boolean {
        return false
    }

    // Function called during rule creation to check if fields are ok
    abstract fun validateConfiguration(configuration: ConditionConfig): Pair<Boolean, String>

    // Prints the Rule in text human-readable format
    abstract fun describe(configuration: ConditionConfig): String

    override fun toString(): String = this.matchOn
}