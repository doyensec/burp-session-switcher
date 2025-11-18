package sessionswitcher.rules.autoupdate

import java.util.*

class UpdateConfig private constructor(val updateSource: UPDATE_SOURCE, val cookiesUpdateMode: COOKIES_UPDATE_MODE, val headersUpdateMode: HEADERS_UPDATE_MODE, val cookiesToUpdate: Set<String> = emptySet(), val headersToUpdate: Set<String> = emptySet(), ) {
    companion object {
        public fun make(updateSource: UPDATE_SOURCE, cookiesUpdateMode: COOKIES_UPDATE_MODE, headersUpdateMode: HEADERS_UPDATE_MODE, cookiesToUpdate: Set<String> = emptySet(), headersToUpdate: Set<String> = emptySet()): UpdateConfig {
            if (updateSource == UPDATE_SOURCE.RESPONSE && cookiesUpdateMode == COOKIES_UPDATE_MODE.REPLACE_ALL) {
                throw IllegalArgumentException("Cannot use REPLACE_ALL cookie update mode when updating from a response")
            }
            return UpdateConfig(updateSource, cookiesUpdateMode, headersUpdateMode, cookiesToUpdate, headersToUpdate)
        }

        public fun make(updateSource: String, cookiesUpdateMode: String, headersUpdateMode: String, cookiesToUpdate: Set<String> = emptySet(), headersToUpdate: Set<String> = emptySet()): UpdateConfig {
            return this.make(UPDATE_SOURCE.valueOf(updateSource.uppercase()), COOKIES_UPDATE_MODE.valueOf(cookiesUpdateMode.uppercase()), HEADERS_UPDATE_MODE.valueOf(headersUpdateMode.uppercase()), cookiesToUpdate, headersToUpdate)
        }
    }

    /*
    Allowed combinations:
    # UPDATE_SOURCE: REQUEST -> ALL MODES
    # UPDATE_SOURCE: RESPONSE -> COOKIE: (UPDATE_ALL, UPDATE_EXISTING, UPDATE_SOME, NO_UPDATE) HEADER: ALL
     */

    enum class UPDATE_SOURCE(val description: String) {
        REQUEST("Request"),
        RESPONSE("Response");

        override fun toString(): String {
            return this.description
        }
    }

    enum class COOKIES_UPDATE_MODE(val description: String) {
        REPLACE_ALL("Replace all"),         // Replace the whole cookie set with the request's ones. Useful when updating from **Requests** to match the browser's cookies. (Default for requests)
        UPDATE_ALL("Update all"),           // Update all the cookies and add new ones (but keep old cookies too unless expired) (Default for Set-Cookie responses?)
        UPDATE_EXISTING("Update existing"), // Only update the values of cookies already stored in the session
        //UPDATE_SOME("Update some"),         // Update a specific list of cookies specified in a different param
        NO_UPDATE("Don't update");          // Do nothing

        override fun toString(): String {
            return this.description
        }
    }

    enum class HEADERS_UPDATE_MODE(val description: String) {
        UPDATE_EXISTING("Update existing"),   // Only update the values of headers already stored in the session
        //UPDATE_SOME("Update some"),           // Update a specific list of headers specified in a different param
        NO_UPDATE("Don't update");            // Do nothing

        override fun toString(): String {
            return this.description
        }
    }

    public fun describe(name: String): String {
        return name.split(" ").joinToString(" ") { it.replaceFirstChar { c -> if (c.isLowerCase()) c.titlecase(Locale.getDefault()) else c.toString() } }
    }

    // Copy constructor
    public fun copy(): UpdateConfig{
        return UpdateConfig(this.updateSource, this.cookiesUpdateMode, this.headersUpdateMode, this.cookiesToUpdate.toSet(), this.headersToUpdate.toSet())
    }
}