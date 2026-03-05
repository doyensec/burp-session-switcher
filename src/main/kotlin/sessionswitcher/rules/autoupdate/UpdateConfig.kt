package sessionswitcher.rules.autoupdate

import burp.api.montoya.core.HighlightColor
import burp.api.montoya.persistence.PersistedList
import burp.api.montoya.persistence.PersistedObject
import sessionswitcher.savestate.CanSaveData
import sessionswitcher.savestate.DeserializerFactory
import sessionswitcher.sessions.CookiesUpdateMode
import sessionswitcher.sessions.HeadersUpdateMode
import java.util.Locale
import java.util.UUID

class UpdateConfig private constructor(
    val updateSource: UpdateSource,
    val cookiesUpdateMode: CookiesUpdateMode,
    val headersUpdateMode: HeadersUpdateMode,
    val cookiesToUpdate: Set<String> = emptySet(),
    val headersToUpdate: Set<String> = emptySet(),
    val highlightColor: HighlightColor = HighlightColor.NONE,
    private val saveStateId: UUID = UUID.randomUUID()
) :
    CanSaveData {
    companion object {
        fun make(
            updateSource: UpdateSource,
            cookiesUpdateMode: CookiesUpdateMode,
            headersUpdateMode: HeadersUpdateMode,
            cookiesToUpdate: Set<String> = emptySet(),
            headersToUpdate: Set<String> = emptySet(),
            highlightColor: HighlightColor = HighlightColor.NONE
        ): UpdateConfig {
            if (updateSource == UpdateSource.RESPONSE && cookiesUpdateMode == CookiesUpdateMode.MIRROR) {
                throw IllegalArgumentException("Cannot use MIRROR cookie update mode when updating from a response")
            }
            return UpdateConfig(updateSource, cookiesUpdateMode, headersUpdateMode, cookiesToUpdate, headersToUpdate, highlightColor)
        }

        fun make(
            updateSource: String,
            cookiesUpdateMode: String,
            headersUpdateMode: String,
            cookiesToUpdate: Set<String> = emptySet(),
            headersToUpdate: Set<String> = emptySet(),
            highlightColor: String = HighlightColor.NONE.name
        ): UpdateConfig {
            return this.make(
                UpdateSource.valueOf(updateSource.uppercase()),
                CookiesUpdateMode.valueOf(cookiesUpdateMode.uppercase()),
                HeadersUpdateMode.valueOf(headersUpdateMode.uppercase()),
                cookiesToUpdate,
                headersToUpdate,
                HighlightColor.valueOf(highlightColor)
            )
        }

        val Deserializer = object : DeserializerFactory<UpdateConfig>() {
            override fun deserializeObject(obj: PersistedObject): UpdateConfig {
                val id = UUID.fromString(obj.getString("id"))
                val updateSource = UpdateSource.valueOf(obj.getString("update_source"))
                val cookiesUpdateMode = CookiesUpdateMode.valueOf(obj.getString("cookies_update_mode"))
                val headersUpdateMode = HeadersUpdateMode.valueOf(obj.getString("headers_update_mode"))
                val cookiesToUpdate = obj.getStringList("cookies_to_update")
                val headersToUpdate = obj.getStringList("headers_to_update")
                val highlightColor = HighlightColor.valueOf(obj.getString("highlight_color") ?: HighlightColor.NONE.name)

                return UpdateConfig(
                    updateSource,
                    cookiesUpdateMode,
                    headersUpdateMode,
                    cookiesToUpdate.toSet(),
                    headersToUpdate.toSet(),
                    highlightColor,
                    id
                )
            }
        }
    }

    /*
    Allowed combinations:
    # UPDATE_SOURCE: REQUEST -> ALL MODES
    # UPDATE_SOURCE: RESPONSE -> COOKIE: (UPDATE_ALL, UPDATE_EXISTING, UPDATE_SOME, NO_UPDATE) HEADER: ALL
     */

    enum class UpdateSource(val description: String) {
        REQUEST("Request"),
        RESPONSE("Response");

        override fun toString(): String {
            return this.description
        }
    }

    fun describe(name: String): String {
        return name.split(" ")
            .joinToString(" ") { it.replaceFirstChar { c -> if (c.isLowerCase()) c.titlecase(Locale.getDefault()) else c.toString() } }
    }

    // Copy constructor
    fun copy(): UpdateConfig {
        return UpdateConfig(
            this.updateSource,
            this.cookiesUpdateMode,
            this.headersUpdateMode,
            this.cookiesToUpdate.toSet(),
            this.headersToUpdate.toSet(),
            this.highlightColor
        )
    }

    override val saveStateKey: String
        get() = "UpdateRule.Config.$saveStateId"

    override fun getChildrenObjectsToSave(): Collection<CanSaveData>? = null

    override fun burpSerialize(): PersistedObject {
        val obj = PersistedObject.persistedObject()

        obj.setString("id", saveStateId.toString())

        obj.setString("update_source", updateSource.name)
        obj.setString("cookies_update_mode", cookiesUpdateMode.name)
        obj.setString("headers_update_mode", headersUpdateMode.name)
        obj.setString("highlight_color", highlightColor.name)

        val cookiesToUpdateLst = PersistedList.persistedStringList()
        cookiesToUpdateLst.addAll(cookiesToUpdate)
        obj.setStringList("cookies_to_update", cookiesToUpdateLst)

        val headersToUpdateLst = PersistedList.persistedStringList()
        headersToUpdateLst.addAll(headersToUpdate)
        obj.setStringList("headers_to_update", headersToUpdateLst)

        return obj
    }
}