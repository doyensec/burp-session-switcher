package sessionswitcher.rules.conditions

import burp.api.montoya.http.message.requests.HttpRequest
import burp.api.montoya.http.message.responses.HttpResponse
import burp.api.montoya.persistence.PersistedObject
import sessionswitcher.rules.conditions.types.*
import sessionswitcher.savestate.CanSaveData
import sessionswitcher.savestate.DeserializerFactory
import java.util.*

class Condition private constructor(public val typeInstance: ConditionTypeInstance, public val configuration: ConditionConfig, private val saveStateId: UUID = UUID.randomUUID()) : CanSaveData {
    companion object {
        public fun make(type: ConditionType, configuration: ConditionConfig): Condition {
            val validationResult = type.instance.validateConfiguration(configuration)
            if (!validationResult.first) {
                throw IllegalArgumentException("Invalid configuration for selected type: ${validationResult.second}")
            }
            return Condition(type.instance, configuration)
        }

        public fun make(type: ConditionType, operation: String, pattern: Optional<String>, negativeMatch: Boolean): Condition {
            val configuration = ConditionConfig(operation, pattern, negativeMatch)
            return this.make(type, configuration)
        }

        val Deserializer = object: DeserializerFactory<Condition>() {
            override fun deserializeObject(obj: PersistedObject): Condition {
                val id = UUID.fromString(obj.getString("id"))
                val type = ConditionType.valueOf(obj.getString("type"))
                val conditionConfigKey = obj.getString("configuration")
                val configuration = ConditionConfig.Deserializer.deserialize(conditionConfigKey)
                    ?: throw Exception("Cannot deserialize ConditionConfig: $conditionConfigKey")

                return Condition(type.instance, configuration, id)
            }
        }
    }

    public enum class ConditionType(val instance: ConditionTypeInstance) {
        IN_SCOPE(InScopeConditionType),
        DOMAIN_NAME(DomainNameConditionType),
        URL(UrlConditionType),
        PROTOCOL(ProtocolConditionType),
        METHOD(MethodConditionType),
        REQUEST_HEADER(RequestHeaderConditionType),
        REQUEST_COOKIE(RequestCookieConditionType),
        REQUEST_BODY(RequestBodyConditionType),
        PATH(PathConditionType),
        QUERY_PARAM(QueryStringConditionType),
        FILE_EXTENSION(FileExtensionConditionType),
        RESPONSE_HEADER(ResponseHeaderConditionType),
        STATUS_CODE(StatusCodeConditionType),
        RESPONSE_BODY(ResponseBodyConditionType);

        companion object {
            fun fromInstance(type: ConditionTypeInstance): ConditionType {
                return entries.find { it.instance == type } ?: throw IllegalArgumentException("Unknown condition type: $type")
            }
            val instances = entries.map { it.instance }.toTypedArray()
        }
    }

    // Main function called during evaluation
    fun matchesRequest(request: HttpRequest, matchInfo: MatchInfo): Boolean {
        return this.typeInstance.matchesRequest(this.configuration, request, matchInfo)
    }

    fun matchesResponse(response: HttpResponse, matchInfo: MatchInfo): Boolean {
        return this.typeInstance.matchesResponse(this.configuration, response, matchInfo)
    }

    // Prints the Rule in text format for the logs and such
    fun describe(): String {
        return this.typeInstance.describe(this.configuration)
    }

    public fun copy(): Condition {
        return Condition(this.typeInstance, this.configuration.copy())
    }

    override val saveStateKey: String
        get() = "UpdateRule.Condition.$saveStateId"

    override fun getChildrenObjectsToSave(): Collection<CanSaveData> {
        return arrayListOf(configuration)
    }

    override fun burpSerialize(): PersistedObject {
        val obj = PersistedObject.persistedObject()
        val type = ConditionType.fromInstance(typeInstance)
        val configuration = this.configuration.saveStateKey

        obj.setString("id", saveStateId.toString())
        obj.setString("type", type.name)
        obj.setString("configuration", configuration)
        return obj
    }
}