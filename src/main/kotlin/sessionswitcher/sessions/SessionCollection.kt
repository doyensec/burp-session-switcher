package sessionswitcher.sessions

import burp.api.montoya.persistence.PersistedObject
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import sessionswitcher.SessionSwitcher
import sessionswitcher.savestate.CanSaveAndLoadData
import sessionswitcher.savestate.CanSaveData
import sessionswitcher.savestate.getSaveStateKeys
import java.lang.ref.WeakReference

class SessionCollection(private val sessionSwitcher: SessionSwitcher) : CanSaveAndLoadData {
    companion object {
        val updateEventCoroutineScope = CoroutineScope(Dispatchers.Default)
        val updateMutex = Mutex()
    }

    private val sessions = LinkedHashMap<String, Session>()
    private val updateListeners = mutableListOf<WeakReference<SessionsListUpdateListener>>()

    val size get() = this.sessions.size

    fun getSessionNames(): Set<String> {
        return this.sessions.keys
    }

    fun hasSession(key: String): Boolean {
        return this.sessions.containsKey(key)
    }

    fun getSessions(suffix: String = ""): Collection<Session> {
        if (suffix.isNotBlank()) {
            return this.sessions.values.filter { it.getHost().endsWith(suffix) }
        }
        return this.sessions.values
    }

    fun getSession(key: String): Session? {
        return this.sessions[key]
    }

    fun deleteSession(key: String) {
        if (this.sessions.containsKey(key)) {
            val session = this.sessions[key] ?: return

            // Remove rules that reference this session
            this.sessionSwitcher.updateRulesCollection.deleteRulesForSession(session)

            // Delete session
            this.sessions.remove(key)
            this.deleteChildObjectAsync(session)
            fireUpdateEvent()
        }
    }

    fun createSession(name: String): Session {
        if (this.sessions.containsKey(name)) {
            throw Exception("Trying to create a session with a name that is already present in the collection: $name")
        }
        val s = Session(name)
        this.sessions[s.name] = s
        this.updateChildObjectAsync(s)
        fireUpdateEvent()
        return s
    }

    fun duplicateSession(name: String, newName: String): Session {
        if (this.hasSession(newName)) {
            throw Exception("Trying to duplicate a session with a name that is already present in the collection: $newName")
        }
        val oldSession = this.getSession(name) ?: throw Exception("Session to duplicate not found: $name")
        val newSession = Session(newName, oldSession)
        this.sessions[newName] = newSession
        this.updateChildObjectAsync(newSession)
        fireUpdateEvent()
        return newSession
    }

    fun deleteSession(p: Session) {
        this.deleteSession(p.name)
    }

    // Update Listeners
    fun registerUpdateListener(listener: SessionsListUpdateListener) {
        this.updateListeners.add(WeakReference(listener))
    }

    private fun fireUpdateEvent() {
        updateEventCoroutineScope.launch {
            updateMutex.withLock {
                val removeList = mutableListOf<WeakReference<SessionsListUpdateListener>>()
                for (ref in updateListeners) {
                    val listener = ref.get()
                    if (listener == null) {
                        removeList.add(ref)
                        continue
                    }
                    listener.onSessionsListUpdate()
                }
            }
        }
    }

    // Serialization stuff

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
            val p = Session.Deserializer.deserialize(sessionId) ?: continue
            this.sessions[p.name] = p
        }
    }
}