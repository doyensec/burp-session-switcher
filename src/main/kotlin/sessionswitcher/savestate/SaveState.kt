package sessionswitcher.savestate

import burp.api.montoya.persistence.PersistedObject
import kotlinx.coroutines.*
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import sessionswitcher.Logger
import sessionswitcher.SessionSwitcher

val saveStateMutex = Mutex()

interface CanLoadData : BurpDeserializable {
    companion object {
        private val coroutineScope = CoroutineScope(Dispatchers.IO)
    }

    val saveStateKey: String
    private val persistenceStore: PersistedObject
        get() {
            val persistence = SessionSwitcher.getApi().persistence().extensionData()
            assert(persistence != null)
            return persistence
        }

    suspend fun loadFromProjectFile(): Boolean {
        val key = saveStateKey
        Logger.debug("[$key] Trying to load data from project file")
        val obj: PersistedObject?
        saveStateMutex.withLock {
            obj = persistenceStore.getChildObject(key)
        }
        if (obj == null) {
            Logger.warning("[$key] No savestate with this key found in this project file")
            return false
        }
        try {
            Logger.debug("[$key] Found, deserializing...")
            this@CanLoadData.burpDeserialize(obj)
        } catch (e: Exception) {
            Logger.error("[$key] Failed deserializing object's data")
            Logger.error(e.stackTraceToString())
            return false
        }
        Logger.info("[$key] Load from project completed")
        return true
    }

}

interface CanSaveData : BurpSerializable {
    companion object {
        private val coroutineScope = CoroutineScope(Dispatchers.IO)
        private val jobs = mutableListOf<Job>()
        suspend fun joinAll() = jobs.joinAll()
    }

    private val persistenceStore: PersistedObject
        get() {
            val persistence = SessionSwitcher.getApi().persistence().extensionData()
            assert(persistence != null)
            return persistence
        }

    val saveStateKey: String
    suspend fun saveToProjectFile(processChildren: Boolean = true): String? {
        val key = saveStateKey
        val obj: PersistedObject
        Logger.debug("[$key] Saving data to project file (with children: $processChildren)")
        try {
            // Processing children in a separate step allows to do partial updates
            // where for example a children is updated/created/deleted and saved independently of its parent.
            // Then we can invoke saveToProjectFile(false) to update the parent's children list but not
            // all the underlying children, saving some IO time
            if (processChildren) {
                Logger.debug("[$key] Processing children first...")
                saveChildrenObjectsToProjectFile()
            }
            Logger.debug("[$key] Serializing data")
            obj = burpSerialize()
        } catch (e: Exception) {
            Logger.error("[$key] Failed serializing the object's data")
            Logger.error(e.stackTraceToString())
            return null
        }
        Logger.info("[$key] Serialization completed successfully")
        saveStateMutex.withLock {
            persistenceStore.setChildObject(key, obj)
        }
        return key
    }

    fun saveToProjectFileAsync(processChildren: Boolean = true) {
        jobs.add(coroutineScope.launch {
            this@CanSaveData.saveToProjectFile(processChildren)
        })
    }

    suspend fun saveChildrenObjectsToProjectFile() {
        val children = this.getChildrenObjectsToSave() ?: return
        for (child in children) {
            child.saveToProjectFile()
        }
    }

    fun getChildrenObjectsToSave(): Collection<CanSaveData>?

    suspend fun updateChildObject(obj: CanSaveData) {
        Logger.info("[${this.saveStateKey}] Updating child object: ${obj.saveStateKey}")
        obj.saveToProjectFile()
        this.saveToProjectFile(false)
    }

    fun updateChildObjectAsync(obj: CanSaveData) {
        jobs.add(coroutineScope.launch {
            this@CanSaveData.updateChildObject(obj)
        })
    }

    suspend fun deleteFromProjectFile(deleteChildren: Boolean = true) {
        val key = saveStateKey
        saveStateMutex.withLock {
            persistenceStore.deleteChildObject(key)
        }
        if (deleteChildren) {
            val children = getChildrenObjectsToSave() ?: return
            for (child in children) {
                child.deleteFromProjectFile(true)
            }
        }
    }

    fun deleteFromProjectFileAsync(deleteChildren: Boolean = true) {
        jobs.add(coroutineScope.launch {
            this@CanSaveData.deleteFromProjectFile(deleteChildren)
        })
    }

    suspend fun deleteChildObject(obj: CanSaveData) {
        obj.deleteFromProjectFile()
        this.saveToProjectFile(false)
    }

    fun deleteChildObjectAsync(obj: CanSaveData) {
        jobs.add(coroutineScope.launch {
            this@CanSaveData.deleteChildObject(obj)
        })
    }
}

interface CanSaveAndLoadData : CanSaveData, CanLoadData

// This Factory-Deserializer class allows to create a Kotlin object from the deserialization
// of data from the project file, instead of creating the object first and then loading data into it
abstract class DeserializerFactory<T> {
    fun deserialize(id: String): T? {
        val deserializer = object : CanLoadData {
            var deserialized: T? = null
            override val saveStateKey: String
                get() = id

            override fun burpDeserialize(obj: PersistedObject) {
                this.deserialized = deserializeObject(obj)
            }

            fun deserialize(): T? = runBlocking {
                loadFromProjectFile()
                return@runBlocking deserialized
            }
        }
        return deserializer.deserialize()
    }

    protected abstract fun deserializeObject(obj: PersistedObject): T?
}
