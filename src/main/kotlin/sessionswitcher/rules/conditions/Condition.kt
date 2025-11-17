package sessionswitcher.rules.conditions

import burp.api.montoya.proxy.ProxyHttpRequestResponse
import sessionswitcher.rules.conditions.types.*
import java.util.*

class Condition private constructor(public val type: ConditionType, public val configuration: ConditionConfiguration) {
    companion object {
        public fun make(type: ConditionType, configuration: ConditionConfiguration): Condition {
            val validationResult = type.validateConfiguration(configuration)
            if (!validationResult.first) {
                throw IllegalArgumentException("Invalid configuration for selected type: ${validationResult.second}")
            }
            return Condition(type, configuration)
        }

        public fun make(type: ConditionType, operation: String, pattern: Optional<String>, negativeMatch: Boolean): Condition {
            val configuration = ConditionConfiguration(operation, pattern, negativeMatch)
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
            ResponseBodyConditionType,
            ListenerPortConditionType
        )
    }

    // Main function called during evaluation
    fun matches(requestResponse: ProxyHttpRequestResponse, matchInfo: MatchInfo): Boolean {
        return this.type.matches(this.configuration, requestResponse, matchInfo)
    }

    // Prints the Rule in text format for the logs and such
    fun describe(): String {
        return this.type.describe(this.configuration)
    }
}