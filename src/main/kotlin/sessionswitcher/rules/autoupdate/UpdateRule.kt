package sessionswitcher.rules.autoupdate

import burp.api.montoya.proxy.ProxyHttpRequestResponse
import sessionswitcher.rules.conditions.Condition
import sessionswitcher.rules.conditions.MatchInfo
import sessionswitcher.sessions.Session

class UpdateRule(val conditions: Array<Condition>, val session: Session, val config: UpdateConfig) {
    companion object {
        private var currentId = 1;
        private fun generateId(): Int {
            return currentId++
        }
    }

    public val id = generateId()

    fun matches(httpRequestResponse: ProxyHttpRequestResponse): Boolean {
        TODO()
    }

    fun updateIfMatch(httpRequestResponse: ProxyHttpRequestResponse) {
        // TODO: DELETE EXPIRED COOKIES WHEN UPDATING FROM RESPONSE
        TODO()
    }

    fun update(httpRequestResponse: ProxyHttpRequestResponse, matchInfo: MatchInfo) {
        // TODO: DELETE EXPIRED COOKIES WHEN UPDATING FROM RESPONSE
        TODO()
    }

    public fun copy(): UpdateRule {
        return UpdateRule(conditions.map { it.copy() }.toTypedArray(), session, config.copy())
    }
}