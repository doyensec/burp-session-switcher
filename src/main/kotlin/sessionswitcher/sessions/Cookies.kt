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
            if (httpRequest.headersMap()["Cookie"] != null) {
                return fromHeaderValue(httpRequest.headersMap()["Cookie"]!!)
            } else if (httpRequest.headersMap()["cookie"] != null) {
                return fromHeaderValue(httpRequest.headersMap()["cookie"]!!)
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

    fun get(key: String): String? = this.cookies[key]

    fun getPairs(): List<Pair<String, String>> {
       return this.cookies.entries.map { Pair(it.key, it.value) }
    }

    private fun setPairs(pairs: Collection<Pair<String, String>>) {
        this.cookies.putAll(pairs)
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
    fun update(other: Cookies): Pair<ArrayList<String>, ArrayList<String>> {
        val updatedCookies = ArrayList<String>()
        val addedCookies = ArrayList<String>()
        for (pair in other.getPairs()) {
            if (this.get(pair.first) == pair.second) {
                // Cookie is already there, do nothing
                continue
            }

            if (this.set(pair.first, pair.second)) {
                // If true, the key was there but the value was different
                updatedCookies.add(pair.first)
            } else {
                // If false, the key did not originally exist
                addedCookies.add(pair.first)
            }
        }
        return Pair(updatedCookies, addedCookies)
    }

    fun update(otherHeaderValue: String): Pair<ArrayList<String>, ArrayList<String>> {
        val other = Cookies.fromHeaderValue(otherHeaderValue)
        return this.update(other)
    }
}