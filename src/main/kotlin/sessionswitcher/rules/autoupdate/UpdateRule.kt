package sessionswitcher.rules.autoupdate

import burp.api.montoya.http.message.requests.HttpRequest
import burp.api.montoya.persistence.PersistedObject
import burp.api.montoya.proxy.http.InterceptedRequest
import burp.api.montoya.proxy.http.InterceptedResponse
import sessionswitcher.rules.conditions.Condition
import sessionswitcher.rules.conditions.MatchInfo
import sessionswitcher.savestate.CanSaveData
import sessionswitcher.sessions.Session

class UpdateRule(val conditions: Array<Condition>, val session: Session, val config: UpdateConfig) : CanSaveData {
    companion object {
        private var currentId = 1;
        private fun generateId(): Int {
            return currentId++
        }
    }

    public val id = generateId()

    fun needsResponse(): Boolean {
        return config.updateSource == UpdateConfig.UPDATE_SOURCE.RESPONSE || conditions.any{ it.type.matchesOnResponse }
    }

    fun matchesRequest(httpRequest: InterceptedRequest): Boolean {
        if (needsResponse()) return false
        val m = MatchInfo()
        return conditions.all { it.matchesRequest(httpRequest, m) }
    }

    fun matchesResponse(httpResponse: InterceptedResponse): Boolean {
        val m = MatchInfo()
        return conditions.all {
            if (it.type.matchesOnResponse) {
                it.matchesResponse(httpResponse, m)
            } else {
                it.matchesRequest(httpResponse.request(), m)
            }

        }
    }

    fun updateIfRequestMatches(httpRequest: InterceptedRequest): Boolean {
        if (this.matchesRequest(httpRequest)) {
            when (config.updateSource) {
                UpdateConfig.UPDATE_SOURCE.REQUEST -> this.updateFromRequest(httpRequest)
                UpdateConfig.UPDATE_SOURCE.RESPONSE -> TODO("Not yet implemented")
            }
            return true
        }
        return false
    }

    fun updateIfResponseMatches(httpResponse: InterceptedResponse): Boolean {
        if (this.matchesResponse(httpResponse)) {
            when (config.updateSource) {
                UpdateConfig.UPDATE_SOURCE.REQUEST -> this.updateFromRequest(httpResponse.request())
                UpdateConfig.UPDATE_SOURCE.RESPONSE -> TODO("Not yet implemented")
            }
            return true
        }
        return false
    }

    private fun updateFromRequest(httpRequest: HttpRequest) {
        if (this.config.updateSource != UpdateConfig.UPDATE_SOURCE.REQUEST) throw Exception("updateFromRequest called on a rule that doesn't update from request")
        this.session.updateFromRequest(httpRequest, config.cookiesUpdateMode, config.headersUpdateMode)
        this.session.setLastUpdateReason(Session.LAST_UPDATE_TYPE.UPDATE_RULE, this.id)
    }

    public fun copy(): UpdateRule {
        return UpdateRule(conditions.map { it.copy() }.toTypedArray(), session, config.copy())
    }

    override val saveStateKey: String
        get() = "UpdateRule.$id"

    override fun getChildrenObjectsToSave(): Collection<CanSaveData>? = null

    override fun burpSerialize(): PersistedObject {
        TODO("Not yet implemented")
    }
}