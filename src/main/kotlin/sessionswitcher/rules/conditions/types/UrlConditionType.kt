package sessionswitcher.rules.conditions.types

import burp.api.montoya.proxy.ProxyHttpRequestResponse
import sessionswitcher.rules.conditions.ConditionConfig
import sessionswitcher.rules.conditions.MatchInfo

object UrlConditionType :
    StringConditionType(matchOn = "URL", needsResponse = false) {
    override fun matches(configuration: ConditionConfig, requestResponse: ProxyHttpRequestResponse, matchInfo: MatchInfo): Boolean {
        val url = requestResponse.request().url()
        return this.stringMatches(configuration, url)
    }
}