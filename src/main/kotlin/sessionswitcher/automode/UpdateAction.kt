package sessionswitcher.automode

import burp.api.montoya.http.message.requests.HttpRequest
import burp.api.montoya.http.message.responses.HttpResponse
import sessionswitcher.sessions.Session
import java.util.*

abstract class UpdateAction(protected var session: Session) {
    enum class UPDATE_SOURCE {
        REQUEST,
        RESPONSE
    }

    enum class COOKIE_UPDATE_MODE {
        REPLACE_ALL,        // Replace the whole cookie set with the request's ones. Useful when updating from **Requests** to match the browser's cookies. (Default for requests)
        UPDATE_ALL,         // Update all the cookies and add new ones (but keep old cookies too unless expired) (Default for Set-Cookie responses?)
        UPDATE_EXISTING,    // Only update the values of cookies already stored in the session
        UPDATE_SOME,        // Update a specific list of cookies specified in a different param
        NO_UPDATE,          // Do nothing
    }

    enum class HEADER_UPDATE_MODE {
        UPDATE_EXISTING,    // Only update the values of headers already stored in the session
        UPDATE_SOME,        // Update a specific list of headers specified in a different param
        NO_UPDATE,          // Do nothing
    }

    /*
    Turn an enum name into a human-readable string
     */
    private fun humanify(name: String): String {
        return name.split(" ").joinToString(" ") { it.replaceFirstChar { c -> if (c.isLowerCase()) c.titlecase(Locale.getDefault()) else c.toString() } }
    }

    public lateinit var updateSource: UPDATE_SOURCE
        private set

    public lateinit var cookieUpdateMode: COOKIE_UPDATE_MODE
        private set

    public var cookiesToUpdate: Set<String> = emptySet()
        private set

    public lateinit var headerUpdateMode: HEADER_UPDATE_MODE
        private set

    public var headersToUpdate: Set<String> = emptySet()
        private set


    fun updateFrom(request: HttpRequest, response: HttpResponse?, matchInfo: MatchInfo) {
        // TODO: DELETE EXPIRED COOKIES WHEN UPDATING FROM RESPONSE
        TODO()
    }
    public fun describe(): String {
        return "Action: ${humanify(cookieUpdateMode.name)}"
    }


}