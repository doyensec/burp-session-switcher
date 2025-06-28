package sessionswitcher.automode.rules

import burp.api.montoya.http.message.requests.HttpRequest
import burp.api.montoya.http.message.responses.HttpResponse
import sessionswitcher.automode.MatchInfo
import sessionswitcher.utils.host

class DomainNameRule(pattern: String, operator: OPERATORS, negative: Boolean = false) :
    StringMatchRule(pattern, operator, negative) {
    override fun matches(request: HttpRequest, response: HttpResponse?, matchInfo: MatchInfo): Boolean {
        val domainName = request.host()
        return this.stringMatches(domainName)
    }

    override fun matchOn(): String = "Domain Name"

    override fun needsResponse(): Boolean = false
}