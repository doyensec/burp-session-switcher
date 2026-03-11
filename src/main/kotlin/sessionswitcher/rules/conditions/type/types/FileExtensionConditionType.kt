package sessionswitcher.rules.conditions.type.types

import burp.api.montoya.http.message.requests.HttpRequest
import sessionswitcher.rules.conditions.ConditionConfig
import sessionswitcher.rules.conditions.MatchInfo

object FileExtensionConditionType :
    StringConditionType(matchOn = "File Extension", matchesOnResponse = false) {
    override fun matchesRequest(
        configuration: ConditionConfig,
        request: HttpRequest,
        matchInfo: MatchInfo,
    ): Boolean {
        val path = request.pathWithoutQuery()
        val fileName = path.substringAfterLast("/")
        val fileExtension = fileName.substringAfterLast(".", "")
        return this.stringMatches(configuration, fileExtension)
    }
}
