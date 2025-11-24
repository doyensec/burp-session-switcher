package sessionswitcher.rules.conditions.types

import burp.api.montoya.proxy.ProxyHttpRequestResponse
import sessionswitcher.rules.conditions.ConditionConfig
import sessionswitcher.rules.conditions.MatchInfo
import sessionswitcher.utils.host

object DomainNameConditionType:
    StringConditionType(matchOn = "Domain Name", needsResponse = false) {
    override fun matches(configuration: ConditionConfig, requestResponse: ProxyHttpRequestResponse, matchInfo: MatchInfo): Boolean {
        val domainName = requestResponse.request().host()
        return this.stringMatches(configuration, domainName)
    }
}