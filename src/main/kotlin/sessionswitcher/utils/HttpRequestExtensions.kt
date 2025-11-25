package sessionswitcher.utils

import burp.api.montoya.http.message.HttpHeader
import burp.api.montoya.http.message.requests.HttpRequest
import com.google.common.net.InternetDomainName
import sessionswitcher.Logger
import sessionswitcher.sessions.Cookies
import java.net.URI

/*
    Returns a Triple with
    1. The new HttpRequest
    2. The list of updated headers
    3. The list of added headers
 */
fun HttpRequest.withHeaders(newHeaders: Map<String, String>): Triple<HttpRequest, List<String>, List<String>> {
    val updated = ArrayList<String>()
    val added = ArrayList<String>()

    var out = this
    for ((k, v) in newHeaders) {
        val existingValue = this.getHeaderValue(k)
        out = if (existingValue != null) {
            if (existingValue == v) {
                Logger.debug("No change for header $k : $v")
                out
            } else {
                Logger.debug("Updating header $k : $existingValue --> $v")
                updated.add(k)
                out.withUpdatedHeader(k, v)
            }
        } else {
            Logger.debug("Adding header $k : $v")
            added.add(k)
            out.withAddedHeader(k, v)
        }
    }
    return Triple(out, updated, added)
}

/*
    Replaces Cookies in HTTP Request
 */
fun HttpRequest.withCookies(cookies: Cookies): HttpRequest {
    var headerName = "Cookie"
    var insertIndex = this.headers().indexOfFirst { it.name() == headerName }

    if (insertIndex == -1) headerName = headerName.lowercase() // Try "cookie"
    insertIndex = this.headers().indexOfFirst { it.name() == headerName }

    if (insertIndex == -1) {
        // Still not found, add a new header
        return this.withAddedHeader("Cookie", cookies.toString())
    } else {
        var outputReq = this.withRemovedHeader(headerName)
        val headersList = outputReq.headers().toMutableList()
        headersList.add(insertIndex, HttpHeader.httpHeader(headerName, cookies.toString()))
        for (header in headersList) {
            outputReq = outputReq.withRemovedHeader(header.name())
        }
        for (header in headersList) {
            outputReq = outputReq.withAddedHeader(header)
        }
        return outputReq
    }
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
    this.mergedHeaders().forEach { map[it.name().lowercase()] = it.value() }
    return map
}

fun HttpRequest.getHeaderValue(name: String): String? {
    return this.mergedHeaders().find { it.name().equals(name, ignoreCase = true)}?.value()
}

fun HttpRequest.host(): String {
    val uri = URI(this.url())
    return uri.host
}

fun HttpRequest.topDomain(): String {
    val uri = URI(this.url())
    return InternetDomainName.from(uri.host).topPrivateDomain().toString()
}