package sessionswitcher.rules.conditions.type.types

import burp.api.montoya.http.message.requests.HttpRequest
import com.google.gson.JsonParser
import sessionswitcher.Logger
import sessionswitcher.rules.conditions.ConditionConfig
import sessionswitcher.rules.conditions.MatchInfo
import sessionswitcher.rules.conditions.type.ConditionType
import sessionswitcher.rules.conditions.type.types.StringConditionType.OPERATORS
import sessionswitcher.sessions.Cookies
import kotlin.io.encoding.Base64

object JWTPayloadConditionType : ConditionType(
    matchOn = "JWT Claim",
    matchesOnResponse = false,
    availableOperations = listOf("JWT in Header", "JWT in Cookie"),
    extraFields = listOf(
        ConditionField.makeTextField("Header/Cookie Name"),
        ConditionField.makeTextField("Claim Name"),
        ConditionField.makeMultipleChoiceField("Match Type", StringConditionType.OPERATORS.entries.map {it.description}.toTypedArray()),
        ConditionField.makeTextField("Claim Value"))
) {
    override fun matchesRequest(configuration: ConditionConfig, request: HttpRequest, matchInfo: MatchInfo): Boolean {
        val jwt: String
        when (configuration.operation) {
            "JWT in Header" -> {
                val headerName =
                    configuration.extraFields["Header/Cookie Name"] ?: throw IllegalStateException("No header name specified!")
                val headerValue = request.headerValue(headerName) ?: return false
                val headerParts = headerValue.split(" ")
                val maybeJWT = headerParts.firstOrNull { it.startsWith("ey") }
                if (maybeJWT == null) {
                    Logger.debug("No JWT found in specified header: $headerName")
                    return false
                }
                jwt = maybeJWT.trim()
            }
            "JWT in Cookie" -> {
                val cookies = Cookies.fromHttpRequest(request)
                val cookieName =
                    configuration.extraFields["Header/Cookie Name"] ?: throw IllegalStateException("No cookie name specified!")
                val cookie = cookies.get(cookieName)?.trim() ?: return false
                if (!cookie.startsWith("ey")) {
                    Logger.debug("No JWT found in specified cookie: $cookieName")
                    return false
                }
                jwt = cookie
            }
            else -> {
                throw IllegalStateException("Invalid operation: ${configuration.operation}")
            }
        }
        val match = jwtMatches(jwt, configuration)
        if (match) {
            Logger.debug("JWT matches condition: ${describe(configuration)}")
        } else {
            Logger.debug("JWT does not match condition: ${describe(configuration)}")
        }
        return match
    }

    private fun jwtMatches(jwt: String, configuration: ConditionConfig): Boolean {
        try {
            // Parse JWT Payload
            val jwtPayload = jwt.split(".")[1]
            val jwtPayloadDecoded = Base64.UrlSafe.withPadding(Base64.PaddingOption.ABSENT_OPTIONAL).decode(jwtPayload).toString(Charsets.UTF_8)
            val jwtPayloadMap = JsonParser.parseString(jwtPayloadDecoded).asJsonObject

            // Get correct key
            val key = configuration.extraFields["Claim Name"]?: throw IllegalStateException("No claim name specified!")
            val value = jwtPayloadMap[key]
            if (value == null) {
                Logger.debug("Claim $key not found in JWT")
                return false
            }

            if (!value.isJsonPrimitive) {
                Logger.warning("Claim $key is not a primitive value, cannot compare.")
                return false
            }

            val strValue = value.asJsonPrimitive.asString

            val operationStr = configuration.extraFields["Match Type"]?: throw IllegalStateException("No match type specified!")
            val operation = StringConditionType.OPERATORS.fromDescription(operationStr)
            val pattern = configuration.extraFields["Claim Value"]?: throw IllegalStateException("No claim value specified!")
            val match = StringConditionType.stringMatches(pattern, operation, strValue, configuration.negativeMatch)
            Logger.debug("String matching: $pattern, $operation, $strValue, $match")
            return match
        } catch(e: Exception) {
            Logger.debug("Fail decoding JWT: ${e.message}")
            return false
        }
    }

    override fun validateConfiguration(configuration: ConditionConfig): Pair<Boolean, String> {
        val nonNullFields = listOf("Header/Cookie Name", "Claim Name", "Match Type", "Claim Value")
        for (field in nonNullFields) {
            val value = configuration.extraFields[field]
            if (value == null || value.isBlank()) {
                return Pair(true, "$field is empty!")
            }
        }

        if (!availableOperations.contains(configuration.operation)) {
            return Pair(false, "Unknown operation! Send bug report.")
        }

        val matchType = configuration.extraFields["Match Type"]?: throw IllegalStateException("No match type specified!")
        try {
            StringConditionType.OPERATORS.fromDescription(matchType)
        } catch (_: Exception) {
            return Pair(false, "Invalid match type!")
        }

        val pattern = configuration.extraFields["Claim Value"]?: throw IllegalStateException("No claim value specified!")
        if (matchType == OPERATORS.REGEX_MATCH.description) {
            try {
                Regex(pattern)
            } catch (e: Exception) {
                return Pair(false, e.message ?: "Invalid Regex")
            }
        }
        return Pair(true, "")
    }

    override fun describe(configuration: ConditionConfig): String {
        val headerOrCookie = configuration.extraFields["Header/Cookie Name"]?: throw IllegalStateException("No header/cookie name specified!")
        val claimName = configuration.extraFields["Claim Name"]?: throw IllegalStateException("No claim name specified!")
        val claimValue = configuration.extraFields["Claim Value"]?: throw IllegalStateException("No claim value specified!")
        val matchType = configuration.extraFields["Match Type"]?: throw IllegalStateException("No match type specified!")
        return "${configuration.operation} \"$headerOrCookie\" has claim \"$claimName\" that ${matchType.lowercase()} \"$claimValue\""
    }
}