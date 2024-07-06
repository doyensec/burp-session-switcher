package sessionswitcher.savestate

import burp.api.montoya.persistence.PersistedObject
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import sessionswitcher.Logger
import sessionswitcher.SessionSwitcher

interface CanLoadData : BurpDeserializable {
    companion object {
        val coroutineScope = CoroutineScope(Dispatchers.IO)
    }

    val saveStateKey: String
    private val persistenceStore: PersistedObject
        get() {
            val persistence = SessionSwitcher.getApi().persistence().extensionData()
            assert(persistence != null)
            return persistence
        }

    fun dataPresentInProjectFile(): Boolean {
        val key = this.saveStateKey
        val obj = persistenceStore.getChildObject(key)
        return obj != null
    }

    fun loadFromProjectFile(): Boolean {
        val key = this.saveStateKey
        Logger.debug("[$key] Trying to load data from project file")
        val obj = persistenceStore.getChildObject(key)
        if (obj == null) {
            Logger.debug("[$key] No savestate with this key found in this project file")
            return false
        }
        try {
            Logger.debug("[$key] Found, deserializing...")
            this.burpDeserialize(obj)
        } catch (e: Exception) {
            Logger.error("[$key] Failed deserializing object's data")
            Logger.error(e.stackTraceToString())
            return false
        }
        Logger.info("[$key] Load from project completed")
        return true
    }

    fun loadFromProjectFileAsync() {
        coroutineScope.launch {
            this@CanLoadData.loadFromProjectFile()
        }
    }
}

interface CanSaveData : BurpSerializable {
    companion object {
        val coroutineScope = CoroutineScope(Dispatchers.IO)
    }

    private val persistenceStore: PersistedObject
        get() {
            val persistence = SessionSwitcher.getApi().persistence().extensionData()
            assert(persistence != null)
            return persistence
        }

    val saveStateKey: String
    fun saveToProjectFile(processChildren: Boolean = true): String? {
        val key = this.saveStateKey
        val obj: PersistedObject
        Logger.debug("[$key] Saving data to project file (with children: $processChildren)")
        try {
            // Processing children in a separate step allows to do partial updates
            // where for example a children is updated/created/deleted and saved independently of its parent.
            // Then we can invoke saveToProjectFile(false) to update the parent's children list but not
            // all the underlying children, saving some IO time
            if (processChildren) {
                Logger.debug("[$key] Processing children first...")
                this.saveChildrenObjectsToProjectFile()
            }
            Logger.debug("[$key] Serializing data")
            obj = this.burpSerialize()
        } catch (e: Exception) {
            Logger.error("[$key] Failed serializing the object's data")
            Logger.error(e.stackTraceToString())
            return null
        }
        Logger.info("[$key] Serialization completed successfully")
        persistenceStore.setChildObject(key, obj)
        return key
    }

    fun saveToProjectFileAsync(processChildren: Boolean = true) {
        coroutineScope.launch {
            this@CanSaveData.saveToProjectFile(processChildren)
        }
    }

    fun saveChildrenObjectsToProjectFile() {
        val children = this.getChildrenObjectsToSave() ?: return
        for (child in children) {
            child.saveToProjectFile()
        }
    }

    fun getChildrenObjectsToSave(): Collection<CanSaveData>?

    fun updateChildObject(obj: CanSaveData) {
        Logger.info("[${this.saveStateKey}] Updating child object: ${obj.saveStateKey}")
        obj.saveToProjectFile()
        this.saveToProjectFile(false)
    }

    fun updateChildObjectAsync(obj: CanSaveData) {
        coroutineScope.launch {
            this@CanSaveData.updateChildObject(obj)
        }
    }

    fun deleteFromProjectFile(deleteChildren: Boolean = true) {
        val key = this.saveStateKey
        persistenceStore.deleteChildObject(key)
        if (deleteChildren) {
            val children = this.getChildrenObjectsToSave() ?: return
            for (child in children) {
                child.deleteFromProjectFile(true)
            }
        }
    }

    fun deleteFromProjectFileAsync(deleteChildren: Boolean = true) {
        coroutineScope.launch {
            this@CanSaveData.deleteFromProjectFile(deleteChildren)
        }
    }

    fun deleteChildObject(obj: CanSaveData) {
        obj.deleteFromProjectFile()
        this.saveToProjectFile(false)
    }

    fun deleteChildObjectAsync(obj: CanSaveData) {
        coroutineScope.launch {
            this@CanSaveData.deleteChildObject(obj)
        }
    }
}

interface CanSaveAndLoadData : CanSaveData, CanLoadData

// This Factory-Deserializer class allows to create a Kotlin object from the deserialization
// of data from the project file, instead of creating the object first and then loading data into it
abstract class DeserializerFactory<T>(val key: String) : CanLoadData {
    protected var deserialized: T? = null
    fun get(): T? {
        this.loadFromProjectFile()
        return deserialized
    }

    override val saveStateKey: String
        get() = this.key
}
