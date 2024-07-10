package sessionswitcher.utils

import burp.api.montoya.http.message.HttpHeader
import burp.api.montoya.http.message.requests.HttpRequest
import sessionswitcher.Logger
import sessionswitcher.sessions.Cookies

fun HttpRequest.withUpsertedHeader(name: String, value: String): HttpRequest {
    var updateOnly = false
    for (header in this.mergedHeaders()) {
        if (header.name().lowercase() == name.lowercase()) {
            updateOnly = true
            break
        }
    }
    return if (updateOnly) {
        if (name.lowercase() == "cookie") {
            this.withUpsertedCookies(value)
        } else {
            this.withUpdatedHeader(name, value)
        }
    } else {
        this.withAddedHeader(name, value)
    }
}
fun HttpRequest.withUpsertedCookies(cookies: String): HttpRequest {
    val existingCookieString = this.getHeaderValue(name="cookie") ?: ""
    val cookieMap = Cookies.fromHeaderValue(existingCookieString)
    cookieMap.update(cookies)
    val newHeaderValue = cookieMap.toString()
    return this.withRemovedHeader("Cookie").withAddedHeader("Cookie", newHeaderValue)
}

fun HttpRequest.withUpsertedHeaders(newHeaders: Map<String, String>): HttpRequest {
    var out = this
    val keys = this.mergedHeaders().map { it.name().lowercase() }.toSet()
    for ((k, v) in newHeaders) {
        Logger.debug("Adding header $k : $v")
        out = if (keys.contains(k.lowercase())) {
            if (k.lowercase() == "cookie") {
                out.withUpsertedCookies(v)
            } else {
                out.withUpdatedHeader(k, v)
            }
        } else {
            out.withAddedHeader(k, v)
        }
    }
    return out
}

/*
    This function merges multiple "cookie" headers into a single one
    This is allowed (and encouraged) in HTTP/2, but it makes it difficult to parse
    headers correctly, so we revert to the old single-header format
 */
fun HttpRequest.mergedHeaders(): List<HttpHeader> {
    if (this.httpVersion() != "HTTP/2") return this.headers()
    val rawHeaders = this.headers()
    val cookies = rawHeaders.filter { it.name().lowercase() == "cookie" }
    if (cookies.size < 2) return rawHeaders
    Logger.debug("Merging multiple Cookie headers from HTTP/2 request")
    val uncompressedCookies = cookies.joinToString("; ") { it.value() }
    val originalCookieIndex = rawHeaders.indexOfFirst { it.name().lowercase() == "cookie" }
    val newHeaders = rawHeaders.filter { it.name() != "cookie" }.toMutableList()
    newHeaders.add(originalCookieIndex, HttpHeader.httpHeader("cookie", uncompressedCookies))
    return newHeaders
}
fun HttpRequest.headersMap(): Map<String, String> {
    val map = mutableMapOf<String, String>()
    this.mergedHeaders().forEach { map.put(it.name(), it.value()) }
    return map
}

fun HttpRequest.getHeaderValue(name: String): String? {
    return this.mergedHeaders().find { it.name().equals(name, ignoreCase = true)}?.value()
}