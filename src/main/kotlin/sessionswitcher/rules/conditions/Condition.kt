package sessionswitcher.rules.conditions

import burp.api.montoya.http.message.requests.HttpRequest
import burp.api.montoya.http.message.responses.HttpResponse
import burp.api.montoya.persistence.PersistedObject
import sessionswitcher.rules.conditions.type.types.DomainNameConditionType
import sessionswitcher.rules.conditions.type.types.FileExtensionConditionType
import sessionswitcher.rules.conditions.type.types.InScopeConditionType
import sessionswitcher.rules.conditions.type.types.JWTPayloadConditionType
import sessionswitcher.rules.conditions.type.types.MethodConditionType
import sessionswitcher.rules.conditions.type.types.PathConditionType
import sessionswitcher.rules.conditions.type.types.ProtocolConditionType
import sessionswitcher.rules.conditions.type.types.QueryStringConditionType
import sessionswitcher.rules.conditions.type.types.RequestBodyConditionType
import sessionswitcher.rules.conditions.type.types.RequestCookieConditionType
import sessionswitcher.rules.conditions.type.types.RequestHeaderConditionType
import sessionswitcher.rules.conditions.type.types.ResponseBodyConditionType
import sessionswitcher.rules.conditions.type.types.ResponseHeaderConditionType
import sessionswitcher.rules.conditions.type.types.StatusCodeConditionType
import sessionswitcher.rules.conditions.type.types.UrlConditionType
import sessionswitcher.rules.conditions.type.types.UserAgentConditionType
import sessionswitcher.savestate.CanSaveData
import sessionswitcher.savestate.DeserializerFactory
import java.util.UUID

class Condition private constructor(
    val typeInstance: sessionswitcher.rules.conditions.type.ConditionType,
    val configuration: ConditionConfig,
    private val saveStateId: UUID = UUID.randomUUID(),
) : CanSaveData {
    companion object {
        fun make(
            type: ConditionTypeEnum,
            configuration: ConditionConfig,
        ): Condition {
            val validationResult = type.instance.validateConfiguration(configuration)
            if (!validationResult.first) {
                throw IllegalArgumentException("Invalid configuration for selected type: ${validationResult.second}")
            }
            return Condition(type.instance, configuration)
        }

        fun make(
            type: ConditionTypeEnum,
            operation: String,
            negativeMatch: Boolean,
            extraFields: Map<String, String>,
        ): Condition {
            val configuration = ConditionConfig(operation, negativeMatch, extraFields)
            return this.make(type, configuration)
        }

        val Deserializer =
            object : DeserializerFactory<Condition>() {
                override fun deserializeObject(obj: PersistedObject): Condition {
                    val id = UUID.fromString(obj.getString("id"))
                    val type = ConditionTypeEnum.valueOf(obj.getString("type"))
                    val conditionConfigKey = obj.getString("configuration")
                    val configuration =
                        ConditionConfig.Deserializer.deserialize(conditionConfigKey, obj)
                            ?: throw Exception("Cannot deserialize ConditionConfig: $conditionConfigKey")

                    return Condition(type.instance, configuration, id)
                }
            }
    }

    enum class ConditionTypeEnum(
        val instance: sessionswitcher.rules.conditions.type.ConditionType,
    ) {
        IN_SCOPE(InScopeConditionType),
        DOMAIN_NAME(DomainNameConditionType),
        URL(UrlConditionType),
        PROTOCOL(ProtocolConditionType),
        METHOD(MethodConditionType),
        REQUEST_HEADER(RequestHeaderConditionType),
        REQUEST_COOKIE(RequestCookieConditionType),
        REQUEST_BODY(RequestBodyConditionType),
        USER_AGENT(UserAgentConditionType),
        PATH(PathConditionType),
        QUERY_PARAM(QueryStringConditionType),
        FILE_EXTENSION(FileExtensionConditionType),
        JWT_PAYLOAD(JWTPayloadConditionType),
        RESPONSE_HEADER(ResponseHeaderConditionType),
        STATUS_CODE(StatusCodeConditionType),
        RESPONSE_BODY(ResponseBodyConditionType),
        ;

        companion object {
            fun fromInstance(type: sessionswitcher.rules.conditions.type.ConditionType): ConditionTypeEnum =
                entries.find { it.instance == type }
                    ?: throw IllegalArgumentException("Unknown condition type: $type")

            val instances = entries.map { it.instance }.toTypedArray()
        }
    }

    // Main function called during evaluation
    fun matchesRequest(
        request: HttpRequest,
        matchInfo: MatchInfo,
    ): Boolean = this.typeInstance.matchesRequest(this.configuration, request, matchInfo)

    fun matchesResponse(
        response: HttpResponse,
        matchInfo: MatchInfo,
    ): Boolean = this.typeInstance.matchesResponse(this.configuration, response, matchInfo)

    // Prints the Rule in text format for the logs and such
    fun describe(): String = this.typeInstance.describe(this.configuration)

    fun copy(): Condition = Condition(this.typeInstance, this.configuration.copy())

    override val saveStateKey: String
        get() = "UpdateRule.Condition.$saveStateId"

    override fun getChildObjectsToSave(): Collection<CanSaveData> = arrayListOf(configuration)

    override fun burpSerialize(obj: PersistedObject): PersistedObject {
        val type = ConditionTypeEnum.fromInstance(typeInstance)
        val configuration = this.configuration.saveStateKey

        obj.setString("id", saveStateId.toString())
        obj.setString("type", type.name)
        obj.setString("configuration", configuration)
        return obj
    }
}
