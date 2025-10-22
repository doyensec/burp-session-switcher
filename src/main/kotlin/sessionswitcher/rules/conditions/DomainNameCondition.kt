package sessionswitcher.rules.conditions

import burp.api.montoya.http.message.requests.HttpRequest
import burp.api.montoya.http.message.responses.HttpResponse
import sessionswitcher.rules.MatchInfo
import sessionswitcher.utils.host

class DomainNameCondition(pattern: String, operator: OPERATORS, negative: Boolean = false) :
    StringCondition(matchOn = "Domain Name", needsResponse = false, pattern, operator, negative) {
    override fun matches(request: HttpRequest, response: HttpResponse?, matchInfo: MatchInfo): Boolean {
        val domainName = request.host()
        return this.stringMatches(domainName)
    }
}