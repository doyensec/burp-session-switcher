package sessionswitcher.savestate

import burp.api.montoya.persistence.PersistedObject

interface BurpSerializable {
    fun burpSerialize(): PersistedObject
}

interface BurpDeserializable {
    fun burpDeserialize(obj: PersistedObject)
}

