package sessionswitcher.sessions

import burp.api.montoya.persistence.PersistedObject
import sessionswitcher.savestate.CanSaveAndLoadData
import sessionswitcher.savestate.CanSaveData
import sessionswitcher.savestate.getSaveStateKeys

class SessionCollection: CanSaveAndLoadData {
    private val sessions = LinkedHashMap<String, Session>()

    fun getSessionNames(): Collection<String> {
        return this.sessions.keys
    }

    fun hasSession(key: String): Boolean {
        return this.sessions.containsKey(key)
    }

    fun getSessions(suffix: String = ""): Collection<Session> {
        return this.sessions.values.filter { it.getHost().endsWith(suffix) }
    }

    fun getSession(key: String): Session? {
        return this.sessions[key]
    }

    fun deleteSession(key: String) {
        if (this.sessions.containsKey(key)) {
            this.sessions.remove(key)
            this.sessions[key]?.deleteFromProjectFileAsync()
        }
    }

    fun createSession(name: String): Session {
        if (this.sessions.containsKey(name)) {
            throw Exception("Trying to create a session with a name that is already present in the collection: $name")
        }
        val s = Session(name)
        this.sessions[s.name] = s
        this.updateChildObjectAsync(s)
        return s
    }

    fun duplicateSession(name: String): Session {
        // Find unused name
        val oldSession = this.getSession(name)
            ?: throw Exception("Session to duplicate not found: $name")
        val newNameBase = oldSession.name + " Copy"
        var newName = newNameBase
        var copyNr = 1
        while (this.hasSession(newName)) {
            copyNr++
            newName = "$newNameBase $copyNr"
        }
        val newSession = Session(newName, oldSession)
        this.sessions[newName] = newSession
        this.updateChildObjectAsync(newSession)
        return newSession
    }

    fun deleteSession(p: Session) {
        this.deleteSession(p.name)
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
            this.sessions[p.name] = p
        }
    }
}