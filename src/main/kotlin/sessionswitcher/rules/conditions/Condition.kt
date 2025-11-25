package sessionswitcher.rules.conditions

import burp.api.montoya.http.message.requests.HttpRequest
import burp.api.montoya.http.message.responses.HttpResponse
import burp.api.montoya.persistence.PersistedObject
import sessionswitcher.rules.conditions.types.*
import sessionswitcher.savestate.CanSaveData
import java.util.*

class Condition private constructor(public val type: ConditionType, public val configuration: ConditionConfig) : CanSaveData {
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

        public enum class ConditionTypes(val type: ConditionType) {
            IN_SCOPE(InScopeConditionType),
            DOMAIN_NAME(DomainNameConditionType),
            URL(UrlConditionType),
            PROTOCOL(ProtocolConditionType),
            METHOD(MethodConditionType),
            REQUEST_HEADER(RequestHeaderConditionType),
            REQUEST_BODY(RequestBodyConditionType),
            QUERY_PARAM(QueryStringConditionType),
            FILE_EXTENSION(FileExtensionConditionType),
            RESPONSE_HEADER(ResponseHeaderConditionType),
            STATUS_CODE(StatusCodeConditionType),
            RESPONSE_BODY(ResponseBodyConditionType);
        }
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

    override val saveStateKey: String
        get() = TODO("Not yet implemented")

    override fun getChildrenObjectsToSave(): Collection<CanSaveData>? {
        TODO("Not yet implemented")
    }

    override fun burpSerialize(): PersistedObject {
        TODO("Not yet implemented")
    }
}