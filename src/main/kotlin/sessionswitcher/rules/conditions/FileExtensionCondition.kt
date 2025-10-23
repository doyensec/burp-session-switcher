package sessionswitcher.rules.conditions

import burp.api.montoya.proxy.ProxyHttpRequestResponse

class FileExtensionCondition(pattern: String, operator: OPERATORS, negative: Boolean = false) :
    StringCondition(matchOn = "File Extension", needsResponse = false, pattern, operator, negative) {
    override fun matches(requestResponse: ProxyHttpRequestResponse, matchInfo: MatchInfo): Boolean {
        val path = requestResponse.request().pathWithoutQuery()
        val fileName = path.substringAfterLast("/")
        val fileExtension = fileName.substringAfterLast(".", "")
        return this.stringMatches(fileExtension)
    }
}