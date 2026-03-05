package sessionswitcher.savestate

import burp.api.montoya.persistence.PersistedObject
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.joinAll
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
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

    suspend fun loadFromSavedData(persistenceStore: PersistedObject): Boolean {
        val key = saveStateKey
        Logger.debug("[$key] Trying to load data from project file")
        val obj: PersistedObject?
        saveStateMutex.withLock {
            obj = persistenceStore.getChildObject(key)
        }
        if (obj == null) {
            Logger.warning("[$key] No savestate with this key found in data store")
            return false
        }
        try {
            Logger.debug("[$key] Found, deserializing...")
            val loadedSuccessfully = this@CanLoadData.burpDeserialize(obj)
            if (loadedSuccessfully) {
                Logger.verbose("[$key] Object loaded successfully")
                return true
            } else {
                Logger.warning("[$key] Failed object data deserialization, data may be corrupted")
                return false
            }
        } catch (e: Exception) {
            Logger.error("[$key] Exception deserializing object's data")
            Logger.printStackTrace(e)
            return false
        }
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
            Logger.printStackTrace(e)
            return null
        }
        Logger.verbose("[$key] Serialization completed successfully")
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

// This Factory-Deserializer class allows creating a Kotlin object from the deserialization
// of data from the project file, instead of creating the object first and then loading data into it
abstract class DeserializerFactory<T> {
    fun deserialize(id: String, store: PersistedObject): T? {
        val wrapper = object : CanLoadData {
            var deserialized: T? = null
            override val saveStateKey: String
                get() = id

            override fun burpDeserialize(obj: PersistedObject): Boolean {
                this.deserialized = deserializeObject(obj)
                return true
            }

            fun deserialize(): T? = runBlocking {
                val deserializationSuccess = loadFromSavedData(store)
                if (!deserializationSuccess) {
                    Logger.warning("[$id] Failed to deserialize data from project file")
                    throw IllegalStateException("Failed to deserialize data from project file")
                }
                return@runBlocking deserialized
            }
        }
        return wrapper.deserialize()
    }

    protected abstract fun deserializeObject(obj: PersistedObject): T?
}
