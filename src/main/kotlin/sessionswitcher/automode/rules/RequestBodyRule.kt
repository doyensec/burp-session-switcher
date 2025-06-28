package sessionswitcher.automode.rules

import burp.api.montoya.http.message.requests.HttpRequest
import burp.api.montoya.http.message.responses.HttpResponse
import sessionswitcher.automode.MatchInfo

class RequestBodyRule(pattern: String, operator: OPERATORS, negative: Boolean = false) :
    StringMatchRule(pattern, operator, negative) {
    override fun matches(request: HttpRequest, response: HttpResponse?, matchInfo: MatchInfo): Boolean {
        val body = request.bodyToString()
        if (body.isNullOrBlank()) {
            return false
        }
        return this.stringMatches(body)
    }

    override fun matchOn(): String = "Request Body"

    override fun needsResponse(): Boolean = false
}