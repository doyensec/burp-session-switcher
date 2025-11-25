package sessionswitcher.sessions

enum class CookiesUpdateMode(val description: String) {
    MIRROR("Mirror Request"),            // Replace the whole cookie set with the request's ones. Useful when updating from **Requests** to match the browser's cookies. (Default for requests)
    ADD_ALL("Add all"),                // Update all the cookies and add new ones (but keep old cookies too unless expired) (Default for Set-Cookie responses?)
    UPDATE_EXISTING("Update existing"),  // Only update the values of cookies already stored in the session
    NOOP("Do nothing");                  // Do nothing

    override fun toString(): String {
        return this.description
    }
}

enum class HeadersUpdateMode(val description: String) {
    MIRROR("Mirror Request"),            // Replace all the headers with the request's ones.
    ADD_ALL("Add all"),                   // Update all the cookies and add new ones
    UPDATE_EXISTING("Update existing"),   // Only update the values of headers already stored in the session
    NOOP("Do nothing");                   // Do nothing

    override fun toString(): String {
        return this.description
    }
}