package sessionswitcher.rules.conditions.types

import burp.api.montoya.proxy.ProxyHttpRequestResponse
import sessionswitcher.rules.conditions.ConditionConfiguration
import sessionswitcher.rules.conditions.ConditionType
import sessionswitcher.rules.conditions.MatchInfo

object ProtocolConditionType:
    ConditionType(matchOn = "Protocol", needsResponse = false, availableOperations = listOf("HTTP", "HTTPS"), canSetPattern = false)
 {
    override fun matches(configuration: ConditionConfiguration, requestResponse: ProxyHttpRequestResponse, matchInfo: MatchInfo): Boolean {
        return (requestResponse.httpService().secure() && configuration.operation == "HTTPS") || (!requestResponse.httpService().secure() && configuration.operation == "HTTP")
    }

    override fun validateConfiguration(configuration: ConditionConfiguration): Pair<Boolean, String> = Pair(true, "")

    override fun describe(configuration: ConditionConfiguration): String = "Request protocol is ${configuration.operation}"
}