package sessionswitcher.rules.autoupdate

import sessionswitcher.sessions.CookiesUpdateMode
import sessionswitcher.sessions.HeadersUpdateMode
import java.util.*

class UpdateConfig private constructor(val updateSource: UPDATE_SOURCE, val cookiesUpdateMode: CookiesUpdateMode, val headersUpdateMode: HeadersUpdateMode, val cookiesToUpdate: Set<String> = emptySet(), val headersToUpdate: Set<String> = emptySet(), ) {
    companion object {
        public fun make(updateSource: UPDATE_SOURCE, cookiesUpdateMode: CookiesUpdateMode, headersUpdateMode: HeadersUpdateMode, cookiesToUpdate: Set<String> = emptySet(), headersToUpdate: Set<String> = emptySet()): UpdateConfig {
            if (updateSource == UPDATE_SOURCE.RESPONSE && cookiesUpdateMode == CookiesUpdateMode.MIRROR) {
                throw IllegalArgumentException("Cannot use MIRROR cookie update mode when updating from a response")
            }
            return UpdateConfig(updateSource, cookiesUpdateMode, headersUpdateMode, cookiesToUpdate, headersToUpdate)
        }

        public fun make(updateSource: String, cookiesUpdateMode: String, headersUpdateMode: String, cookiesToUpdate: Set<String> = emptySet(), headersToUpdate: Set<String> = emptySet()): UpdateConfig {
            return this.make(UPDATE_SOURCE.valueOf(updateSource.uppercase()), CookiesUpdateMode.valueOf(cookiesUpdateMode.uppercase()), HeadersUpdateMode.valueOf(headersUpdateMode.uppercase()), cookiesToUpdate, headersToUpdate)
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

    public fun describe(name: String): String {
        return name.split(" ").joinToString(" ") { it.replaceFirstChar { c -> if (c.isLowerCase()) c.titlecase(Locale.getDefault()) else c.toString() } }
    }

    // Copy constructor
    public fun copy(): UpdateConfig{
        return UpdateConfig(this.updateSource, this.cookiesUpdateMode, this.headersUpdateMode, this.cookiesToUpdate.toSet(), this.headersToUpdate.toSet())
    }
}