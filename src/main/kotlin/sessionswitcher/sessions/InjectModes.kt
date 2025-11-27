package sessionswitcher.sessions

enum class CookiesInjectMode(val description: String) {
    MIRROR("Mirror session"),        // Replace the whole cookie set with the session's ones.
    ADD_ALL("Add all"),              // Update all the cookies and add new ones (but keep old cookies too unless expired) (Default for Set-Cookie responses?)
    UPDATE_EXISTING("Update existing"), // Only update the values of cookies already stored in the session
    NOOP("Do nothing");              // Do nothing

    override fun toString(): String {
        return this.description
    }
}