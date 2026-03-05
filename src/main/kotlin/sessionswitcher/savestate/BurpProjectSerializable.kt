package sessionswitcher.savestate

import burp.api.montoya.persistence.PersistedObject

interface BurpSerializable {
    fun burpSerialize(obj: PersistedObject): PersistedObject
}

interface BurpDeserializable {
    fun burpDeserialize(obj: PersistedObject): Boolean
}

