package sessionswitcher.sessions

/*
This is meant for request editors to track when the Session **Collection** changes.
i.e. a Session is added or removed. This way, editors can update themselves.
 */
interface SessionsListUpdateListener {
    suspend fun onSessionsListUpdate()
}
