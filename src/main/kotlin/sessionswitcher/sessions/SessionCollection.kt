package sessionswitcher.sessions

import burp.api.montoya.persistence.PersistedObject
import sessionswitcher.savestate.CanSaveAndLoadData
import sessionswitcher.savestate.CanSaveData
import sessionswitcher.savestate.getSaveStateKeys

class SessionCollection: CanSaveAndLoadData {
    private val sessions = LinkedHashMap<String, Session>()

    fun getSessionsId(): Collection<String> {
        return this.sessions.keys
    }

    fun getSessions(): Collection<Session> {
        return this.sessions.values
    }

    fun getSession(key: String): Session? {
        return this.sessions[key]
    }

    fun deleteSession(key: String) {
        if (this.sessions.containsKey(key)) {
            this.sessions[key]?.deleteFromProjectFileAsync()
            this.sessions.remove(key)
        }
    }

    fun createSession(name: String): Session {
        val s = Session(name)
        this.sessions[s.id] = s
        this.updateChildObjectAsync(s)
        return s
    }

    fun deleteSession(p: Session) {
        this.deleteSession(p.id)
    }

    override val saveStateKey: String
        get() = "SessionCollection"

    override fun getChildrenObjectsToSave(): Collection<CanSaveData> {
        return this.sessions.values
    }

    override fun burpSerialize(): PersistedObject {
        val obj = PersistedObject.persistedObject()
        obj.setStringList("SavedSessions", getSaveStateKeys(this.sessions.values))
        return obj
    }

    override fun burpDeserialize(obj: PersistedObject) {
        val sessionsList = obj.getStringList("SavedSessions") ?: return

        for (sessionId in sessionsList) {
            val p = Session.Deserializer(sessionId).get() ?: continue
            this.sessions[p.id] = p
        }
    }
}