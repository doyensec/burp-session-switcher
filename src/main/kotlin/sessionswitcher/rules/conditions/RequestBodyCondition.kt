package sessionswitcher.rules.conditions

import burp.api.montoya.http.message.requests.HttpRequest
import burp.api.montoya.http.message.responses.HttpResponse
import sessionswitcher.rules.MatchInfo

class RequestBodyCondition(pattern: String, operator: OPERATORS, negative: Boolean = false) :
    StringCondition(matchOn = "Request Body", needsResponse = false, pattern, operator, negative) {
    override fun matches(request: HttpRequest, response: HttpResponse?, matchInfo: MatchInfo): Boolean {
        val body = request.bodyToString()
        if (body.isNullOrBlank()) {
            return false
        }
        return this.stringMatches(body)
    }
}