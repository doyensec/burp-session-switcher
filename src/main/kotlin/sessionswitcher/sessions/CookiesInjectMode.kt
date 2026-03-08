package sessionswitcher.sessions

enum class CookiesInjectMode(
    val description: String,
) {
    // Replace the whole cookie set with the session's ones.
    MIRROR("Mirror session"),

    // Update all the cookies and add new ones (but keep old cookies too unless expired) (Default for Set-Cookie responses?)
    ADD_ALL("Add all"),

    // Only update the values of cookies already stored in the session
    UPDATE_EXISTING("Update existing"),

    // Do nothing
    NOOP("Do nothing"),
    ;

    override fun toString(): String = this.description
}
