package sessionswitcher.rules.conditions

import burp.api.montoya.http.message.requests.HttpRequest
import burp.api.montoya.http.message.responses.HttpResponse
import sessionswitcher.rules.MatchInfo

class UrlCondition(pattern: String, operator: OPERATORS, negative: Boolean = false) :
    StringCondition(pattern, operator, negative) {
    override fun matches(request: HttpRequest, response: HttpResponse?, matchInfo: MatchInfo): Boolean {
        val url = request.url()
        return this.stringMatches(url)
    }

    override fun matchOn(): String = "URL"

    override fun needsResponse(): Boolean = false
}