package sessionswitcher.rules.autoupdate

import java.util.*

class UpdateConfig private constructor(val updateSource: UPDATE_SOURCE, val cookieUpdateMode: COOKIE_UPDATE_MODE, val headerUpdateMode: HEADER_UPDATE_MODE, val cookiesToUpdate: Set<String> = emptySet(), val headersToUpdate: Set<String> = emptySet(), ) {
    companion object {
        public fun make(updateSource: UPDATE_SOURCE, cookieUpdateMode: COOKIE_UPDATE_MODE, headerUpdateMode: HEADER_UPDATE_MODE, cookiesToUpdate: Set<String> = emptySet(), headersToUpdate: Set<String> = emptySet()): UpdateConfig {
            return UpdateConfig(updateSource, cookieUpdateMode, headerUpdateMode, cookiesToUpdate, headersToUpdate)
        }
    }

    enum class UPDATE_SOURCE(val description: String) {
        REQUEST("Request"),
        RESPONSE("Response")
    }

    enum class COOKIE_UPDATE_MODE(val description: String) {
        REPLACE_ALL("Replace all"),         // Replace the whole cookie set with the request's ones. Useful when updating from **Requests** to match the browser's cookies. (Default for requests)
        UPDATE_ALL("Update all"),          // Update all the cookies and add new ones (but keep old cookies too unless expired) (Default for Set-Cookie responses?)
        UPDATE_EXISTING("Update existing"),    // Only update the values of cookies already stored in the session
        UPDATE_SOME("Update some"),        // Update a specific list of cookies specified in a different param
        NO_UPDATE("Don't update"),          // Do nothing
    }

    enum class HEADER_UPDATE_MODE(val description: String) {
        UPDATE_EXISTING("Update existing"),    // Only update the values of headers already stored in the session
        UPDATE_SOME("Update some"),        // Update a specific list of headers specified in a different param
        NO_UPDATE("Don't update"),          // Do nothing
    }

    public fun describe(name: String): String {
        return name.split(" ").joinToString(" ") { it.replaceFirstChar { c -> if (c.isLowerCase()) c.titlecase(Locale.getDefault()) else c.toString() } }
    }

    // Copy constructor
    public fun copy(): UpdateConfig{
        return UpdateConfig(this.updateSource, this.cookieUpdateMode, this.headerUpdateMode, this.cookiesToUpdate.toSet(), this.headersToUpdate.toSet())
    }
}