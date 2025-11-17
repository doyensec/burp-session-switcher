package sessionswitcher.rules.conditions.types

import burp.api.montoya.proxy.ProxyHttpRequestResponse
import sessionswitcher.rules.conditions.ConditionConfiguration
import sessionswitcher.rules.conditions.MatchInfo

object FileExtensionConditionType:
    StringConditionType(matchOn = "File Extension", needsResponse = false) {
    override fun matches(configuration: ConditionConfiguration, requestResponse: ProxyHttpRequestResponse, matchInfo: MatchInfo): Boolean {
        val path = requestResponse.request().pathWithoutQuery()
        val fileName = path.substringAfterLast("/")
        val fileExtension = fileName.substringAfterLast(".", "")
        return this.stringMatches(configuration, fileExtension)
    }
}