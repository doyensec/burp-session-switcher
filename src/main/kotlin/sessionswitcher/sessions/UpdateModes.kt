package sessionswitcher.sessions

enum class CookiesUpdateMode(
    val description: String,
) {
    // Replace the whole cookie set with the request's ones. Useful when updating from **Requests** to match the browser's cookies. (Default for requests)
    MIRROR("Mirror request"),

    // Update all the cookies and add new ones (but keep old cookies too unless expired) (Default for Set-Cookie responses?)
    ADD_ALL("Add all"),

    // Only update the values of cookies already stored in the session
    UPDATE_EXISTING("Update existing"),

    // Do nothing
    NOOP("Do nothing"), // Do nothing
    ;

    override fun toString(): String = this.description
}

enum class HeadersUpdateMode(
    val description: String,
) {
    // Replace all the headers with the request's ones.
    MIRROR("Mirror request"),

    // Update all the cookies and add new ones
    ADD_ALL("Add all"),

    // Only update the values of headers already stored in the session
    UPDATE_EXISTING("Update existing"),

    // Do nothing
    NOOP("Do nothing"),
    ;

    override fun toString(): String = this.description
}
