package sessionswitcher.rules.conditions.type.types

import burp.api.montoya.http.message.requests.HttpRequest
import sessionswitcher.rules.conditions.ConditionConfig
import sessionswitcher.rules.conditions.MatchInfo
import sessionswitcher.utils.host

object DomainNameConditionType :
    StringConditionType(matchOn = "Domain Name", matchesOnResponse = false) {
    override fun matchesRequest(configuration: ConditionConfig, request: HttpRequest, matchInfo: MatchInfo): Boolean {
        val domainName = request.host()
        return this.stringMatches(configuration, domainName)
    }
}