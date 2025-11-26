package sessionswitcher.sessions

import burp.api.montoya.http.message.requests.HttpRequest
import sessionswitcher.utils.headersMap

class Cookies() {
    private val cookies = LinkedHashMap<String, String>()
    companion object {
        fun fromHeaderValue(headerValue: String): Cookies {
            val cookieValueString = headerValue.trim()
            val output = Cookies()
            val pairs = cookieValueString
                    .split(';') // Separate different key=value pairs
                    .map { it.trim().split('=', limit = 2) } // Split pair in key and value
                    .filter { it.size == 2 } // Just a sanity check
                    .map { Pair(it[0], it[1]) } // Make it a Pair
            for (pair in pairs) {
                output.set(pair.first, pair.second)
            }
            return output
        }

        fun fromHttpRequest(httpRequest: HttpRequest): Cookies {
            val cookieHeader = httpRequest.headersMap()["cookie"]
            if (cookieHeader != null) {
                return fromHeaderValue(cookieHeader)
            }
            return Cookies()
        }

        fun duplicate(input: Cookies): Cookies {
            val output = Cookies()
            output.setPairs(input.getPairs())
            return output
        }
    }

    override fun toString(): String {
        return this.cookies.entries.joinToString("; ") { "${it.key}=${it.value}" }
    }

    fun isEmpty(): Boolean = this.cookies.isEmpty()

    fun get(key: String): String? = this.cookies[key]

    fun getPairs(): List<Pair<String, String>> {
       return this.cookies.entries.map { Pair(it.key, it.value) }
    }

    public fun setPairs(pairs: Collection<Pair<String, String>>) {
        this.cookies.putAll(pairs)
    }

    public fun clear() {
        this.cookies.clear()
    }

    /*
    Returns true if the value was updated, false if it was added
     */
    fun set(key: String, value: String): Boolean {
        val keyPresent = this.cookies.containsKey(key)
        this.cookies[key] = value
        return keyPresent
    }

    /*
    Updates the current Cookies object and returns a pair composed of
    the list of the updated cookie keys and the list of added cookie keys
     */
    fun update(other: Cookies, onlyUpdateExisting: Boolean = false): Pair<List<String>, List<String>> {
        val updatedCookies = ArrayList<String>()
        val addedCookies = ArrayList<String>()
        for (pair in other.getPairs()) {
            val existingValue = this.get(pair.first)
            if (existingValue == pair.second) {
                // Cookie is already there, do nothing
                continue
            } else if (existingValue != null) {
                // Cookie is there but value is different, update
                this.set(pair.first, pair.second)
                updatedCookies.add(pair.first)
            } else if (!onlyUpdateExisting) {
                // New cookie
                this.set(pair.first, pair.second)
                addedCookies.add(pair.first)
            }
        }
        return Pair(updatedCookies, addedCookies)
    }

    fun replace(other: Cookies): Pair<List<String>, List<String>> {
        val keysBefore = this.cookies.keys.toTypedArray()
        val diff = this.update(other)

        // Remove old cookies that are not in the new cookies
        keysBefore.filter { other.get(it) == null }.forEach {
            this.cookies.remove(it)
        }
        return diff
    }

    fun update(otherHeaderValue: String): Pair<List<String>, List<String>> {
        val other = Cookies.fromHeaderValue(otherHeaderValue)
        return this.update(other)
    }

    override fun equals(other: Any?): Boolean {
        if (other == null) return false
        if (other::class.java != this::class.java) return false
        return this.cookies == (other as Cookies).cookies
    }

    /*
    Returns true if all the cookies stored in this Cookies object
    are also contained the other Cookies object
     */
    fun contains(other: Cookies): Boolean {
        for (cookie in other.cookies) {
            if (this.cookies[cookie.key] != cookie.value) {
                return false
            }
        }
        return true
    }

    override fun hashCode(): Int {
        return cookies.hashCode()
    }
}