package sessionswitcher.rules.conditions

import burp.api.montoya.http.message.requests.HttpRequest
import burp.api.montoya.http.message.responses.HttpResponse
import sessionswitcher.rules.conditions.types.*
import java.util.*

class Condition private constructor(public val type: ConditionType, public val configuration: ConditionConfig) {
    companion object {
        public fun make(type: ConditionType, configuration: ConditionConfig): Condition {
            val validationResult = type.validateConfiguration(configuration)
            if (!validationResult.first) {
                throw IllegalArgumentException("Invalid configuration for selected type: ${validationResult.second}")
            }
            return Condition(type, configuration)
        }

        public fun make(type: ConditionType, operation: String, pattern: Optional<String>, negativeMatch: Boolean): Condition {
            val configuration = ConditionConfig(operation, pattern, negativeMatch)
            return this.make(type, configuration)
        }

        public val AVAILABLE_TYPES = arrayOf(
            InScopeConditionType,
            DomainNameConditionType,
            UrlConditionType,
            ProtocolConditionType,
            MethodConditionType,
            RequestHeaderConditionType,
            RequestBodyConditionType,
            QueryStringConditionType,
            FileExtensionConditionType,
            ResponseHeaderConditionType,
            StatusCodeConditionType,
            ResponseBodyConditionType
        )
    }

    // Main function called during evaluation
    fun matchesRequest(request: HttpRequest, matchInfo: MatchInfo): Boolean {
        return this.type.matchesRequest(this.configuration, request, matchInfo)
    }

    fun matchesResponse(response: HttpResponse, matchInfo: MatchInfo): Boolean {
        return this.type.matchesResponse(this.configuration, response, matchInfo)
    }

    // Prints the Rule in text format for the logs and such
    fun describe(): String {
        return this.type.describe(this.configuration)
    }

    public fun copy(): Condition {
        return Condition(this.type, this.configuration.copy())
    }
}