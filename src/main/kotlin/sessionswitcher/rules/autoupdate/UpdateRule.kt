package sessionswitcher.rules.autoupdate

import burp.api.montoya.core.HighlightColor
import burp.api.montoya.http.message.requests.HttpRequest
import burp.api.montoya.persistence.PersistedList
import burp.api.montoya.persistence.PersistedObject
import burp.api.montoya.proxy.http.InterceptedRequest
import burp.api.montoya.proxy.http.InterceptedResponse
import sessionswitcher.SessionSwitcher
import sessionswitcher.rules.conditions.Condition
import sessionswitcher.rules.conditions.MatchInfo
import sessionswitcher.savestate.CanSaveData
import sessionswitcher.savestate.DeserializerFactory
import sessionswitcher.sessions.Session
import java.util.UUID
import kotlin.math.max

class UpdateRule private constructor(
    val conditions: Array<Condition>,
    val session: Session,
    val config: UpdateConfig,
    val ruleId: Int,
    private val saveStateId: UUID,
) : CanSaveData {
    companion object {
        private var currentId = 1

        private fun generateId(): Int = currentId++
    }

    constructor(
        conditions: Array<Condition>,
        session: Session,
        config: UpdateConfig,
        ruleId: Int = generateId(),
    ) : this(conditions, session, config, ruleId, UUID.randomUUID())

    init {
        // Update the stored ID counter in case the ID was set externally
        currentId = max(currentId, ruleId + 1)
    }

    var isEnabled: Boolean = true
        private set

    fun setEnabled(enabled: Boolean) {
        this.isEnabled = enabled
        SessionSwitcher.getInstance().updateRulesCollection.updateChildObjectInProjectFileAsync(this)
    }

    fun needsResponse(): Boolean =
        config.updateSource == UpdateConfig.UpdateSource.RESPONSE ||
            conditions.any {
                it.typeInstance.matchesOnResponse
            }

    fun matchesRequest(httpRequest: InterceptedRequest): Boolean {
        if (!isEnabled) return false
        if (needsResponse()) return false
        val m = MatchInfo()
        return conditions.all { it.matchesRequest(httpRequest, m) }
    }

    fun matchesResponse(httpResponse: InterceptedResponse): Boolean {
        if (!isEnabled) return false
        val m = MatchInfo()
        return conditions.all {
            if (it.typeInstance.matchesOnResponse) {
                it.matchesResponse(httpResponse, m)
            } else {
                it.matchesRequest(httpResponse.request(), m)
            }
        }
    }

    fun updateIfRequestMatches(httpRequest: InterceptedRequest): Boolean {
        if (this.matchesRequest(httpRequest)) {
            when (config.updateSource) {
                UpdateConfig.UpdateSource.REQUEST -> this.updateFromRequest(httpRequest)
                UpdateConfig.UpdateSource.RESPONSE -> TODO("Not yet implemented")
            }
            if (config.highlightColor != HighlightColor.NONE) httpRequest.annotations().setHighlightColor(config.highlightColor)
            return true
        }
        return false
    }

    fun updateIfResponseMatches(httpResponse: InterceptedResponse): Boolean {
        if (this.matchesResponse(httpResponse)) {
            when (config.updateSource) {
                UpdateConfig.UpdateSource.REQUEST -> this.updateFromRequest(httpResponse.request())
                UpdateConfig.UpdateSource.RESPONSE -> TODO("Not yet implemented")
            }
            if (config.highlightColor != HighlightColor.NONE) httpResponse.annotations().setHighlightColor(config.highlightColor)
            return true
        }
        return false
    }

    private fun updateFromRequest(httpRequest: HttpRequest) {
        if (this.config.updateSource !=
            UpdateConfig.UpdateSource.REQUEST
        ) {
            throw Exception("updateFromRequest called on a rule that doesn't update from request")
        }
        this.session.updateFromRequest(httpRequest, config.cookiesUpdateMode, config.headersUpdateMode)
        this.session.setLastUpdateReason(Session.LastUpdateType.UPDATE_RULE, this.saveStateKey)
    }

    fun copy(): UpdateRule = UpdateRule(conditions.map { it.copy() }.toTypedArray(), session, config.copy())

    override val saveStateKey: String
        get() = "UpdateRule.$saveStateId"

    override fun getChildObjectsToSave(): Collection<CanSaveData> = arrayListOf(*conditions, config)

    override fun burpSerialize(obj: PersistedObject): PersistedObject {
        val saveStateId = this.saveStateId.toString()
        obj.setString("id", saveStateId)

        val session = session.name
        obj.setString("session", session)

        val updateConfig = config.saveStateKey
        obj.setString("config", updateConfig)

        obj.setBoolean("enabled", isEnabled)

        val conditionsList = PersistedList.persistedStringList()
        conditionsList.addAll(conditions.map { it.saveStateKey })
        obj.setStringList("conditions", conditionsList)

        return obj
    }

    class Deserializer(
        val sessionSwitcher: SessionSwitcher,
    ) : DeserializerFactory<UpdateRule>() {
        override fun deserializeObject(obj: PersistedObject): UpdateRule {
            val saveStateId = UUID.fromString(obj.getString("id"))

            val sessionName = obj.getString("session")
            val session =
                sessionSwitcher.sessions.getSession(sessionName)
                    ?: throw Exception("Cannot find session with name $sessionName")

            val configKey = obj.getString("config")
            val config =
                UpdateConfig.Deserializer.deserialize(configKey, obj)
                    ?: throw Exception("Cannot deserialize UpdateConfig: $configKey")

            val enabled = obj.getBoolean("enabled") ?: true

            val conditionsList = obj.getStringList("conditions")

            val conditions: ArrayList<Condition> = ArrayList<Condition>()
            conditionsList.forEach { Condition.Deserializer.deserialize(it, obj)?.let { e -> conditions.add(e) } }

            val rule = UpdateRule(conditions.toTypedArray(), session, config, generateId(), saveStateId)
            rule.isEnabled = enabled
            return rule
        }
    }
}
