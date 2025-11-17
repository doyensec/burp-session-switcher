package sessionswitcher.rules.refresher

import burp.api.montoya.proxy.ProxyHttpRequestResponse
import sessionswitcher.rules.conditions.MatchInfo
import sessionswitcher.sessions.Session
import java.util.*

abstract class RefreshAction(protected var session: Session) {
    enum class REFRESH_SOURCE {
        REQUEST,
        RESPONSE
    }

    enum class COOKIE_REFRESH_MODE {
        REPLACE_ALL,         // Replace the whole cookie set with the request's ones. Useful when updating from **Requests** to match the browser's cookies. (Default for requests)
        REFRESH_ALL,         // Update all the cookies and add new ones (but keep old cookies too unless expired) (Default for Set-Cookie responses?)
        REFRESH_EXISTING,    // Only update the values of cookies already stored in the session
        REFRESH_SOME,        // Update a specific list of cookies specified in a different param
        NO_REFRESH,          // Do nothing
    }

    enum class HEADER_REFRESH_MODE {
        REFRESH_EXISTING,    // Only update the values of headers already stored in the session
        REFRESH_SOME,        // Update a specific list of headers specified in a different param
        NO_REFRESH,          // Do nothing
    }

    /*
    Turn an enum name into a human-readable string
     */
    private fun humanify(name: String): String {
        return name.split(" ").joinToString(" ") { it.replaceFirstChar { c -> if (c.isLowerCase()) c.titlecase(Locale.getDefault()) else c.toString() } }
    }

    public lateinit var refreshSource: REFRESH_SOURCE
        private set

    public lateinit var cookieRefreshMode: COOKIE_REFRESH_MODE
        private set

    public var cookiesToRefresh: Set<String> = emptySet()
        private set

    public lateinit var headerRefreshMode: HEADER_REFRESH_MODE
        private set

    public var headersToRefresh: Set<String> = emptySet()
        private set


    fun refreshFrom(httpRequestResponse: ProxyHttpRequestResponse, matchInfo: MatchInfo) {
        // TODO: DELETE EXPIRED COOKIES WHEN UPDATING FROM RESPONSE
        TODO()
    }
    public fun describe(): String {
        return "Action: ${humanify(cookieRefreshMode.name)}"
    }
}