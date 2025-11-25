package sessionswitcher.rules.conditions.types

import burp.api.montoya.http.message.requests.HttpRequest
import sessionswitcher.rules.conditions.ConditionConfig
import sessionswitcher.rules.conditions.ConditionTypeInstance
import sessionswitcher.rules.conditions.MatchInfo

object ProtocolConditionType:
    ConditionTypeInstance(matchOn = "Protocol", matchesOnResponse = false, availableOperations = listOf("HTTP", "HTTPS"), canSetPattern = false)
 {
    override fun matchesRequest(configuration: ConditionConfig, request: HttpRequest, matchInfo: MatchInfo): Boolean {
        return (request.httpService().secure() && configuration.operation == "HTTPS") || (!request.httpService().secure() && configuration.operation == "HTTP")
    }

    override fun validateConfiguration(configuration: ConditionConfig): Pair<Boolean, String> = Pair(true, "")

    override fun describe(configuration: ConditionConfig): String = "Request protocol is ${configuration.operation}"
}