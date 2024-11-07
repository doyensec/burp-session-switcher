package sessionswitcher.requesteditor

import burp.api.montoya.http.message.ContentType
import burp.api.montoya.http.message.requests.HttpRequest
import burp.api.montoya.ui.Theme
import sessionswitcher.Logger
import sessionswitcher.SessionSwitcher
import sessionswitcher.sessions.Cookies
import sessionswitcher.settings.Settings
import sessionswitcher.utils.JsonPrettifier
import java.awt.Color
import javax.swing.text.SimpleAttributeSet
import javax.swing.text.StyleConstants

class DiffHighlightRequestEditor: StyledTextEditor() {
    companion object {
        fun normalizeHeaderName(name: String): String {
            return name.split('-').joinToString("-") { it.replaceFirstChar { c -> c.uppercase() } }
        }
    }
    val COMMON_HEADER_PREFIXES = setOf<String>(
        "connection",
        "sec-",
        "user-agent",
        "priority",
        "accept",
        "cache",
        "referer",
        "date",
        "http2",
        "via",
        "warning"
    )

    private val modifiedElementStyle = SimpleAttributeSet().also {
        if (SessionSwitcher.getApi().userInterface().currentTheme() == Theme.DARK) {
            StyleConstants.setForeground(it, Color.ORANGE)
        } else {
            StyleConstants.setForeground(it, Color.RED)
        }
    }
    private val addedElementStyle = SimpleAttributeSet().also {
        if (SessionSwitcher.getApi().userInterface().currentTheme() == Theme.DARK) {
            StyleConstants.setForeground(it, Color.GREEN)
        } else {
            StyleConstants.setForeground(it, Color.GREEN)
        }
    }
    private fun appendCookieHeader(value: String, cookiesDiffInfo: Pair<List<String>, List<String>>) {
        // Preparation
        val modifiedCookies = cookiesDiffInfo.first.toSet()
        val addedCookies = cookiesDiffInfo.second.toSet()

        val cookies = value.split(";").map { it.trim().split("=") }.filter { it.size == 2 }
        if (cookies.isEmpty()) {
            return
        }

        this.appendText("Cookie:")
        for (cookiePair in cookies) {
            this.appendText(" ")
            if (modifiedCookies.contains(cookiePair[0])) {
                this.appendText("${cookiePair[0]}=")
                this.appendText(cookiePair[1], modifiedElementStyle)
                Logger.debug("Cookie modified: " + cookiePair[0])
            } else if (addedCookies.contains(cookiePair[0].lowercase())) {
                this.appendText("${cookiePair[0]}=${cookiePair[1]}", addedElementStyle)
                Logger.debug("Cookie added: " + cookiePair[0])
            } else {
                this.appendText("${cookiePair[0]}=${cookiePair[1]}")
                Logger.debug("Cookie noop: " + cookiePair[0])
            }
            this.appendText(";")
        }
    }

    fun setRequest(httpRequest: HttpRequest, headersDiffInfo: Pair<List<String>, List<String>> = Pair(emptyList(), emptyList()), cookiesDiffInfo: Pair<List<String>, List<String>> = Pair(emptyList(), emptyList())) {
        Logger.debug("setRequest, styled")
        val requestLines = httpRequest.toString().split("\r\n")

        // Preparation
        val modifiedHeaders = headersDiffInfo.first.map { it.lowercase() }.toSet()
        val addedHeaders = headersDiffInfo.second.map { it.lowercase() }.toSet()
        val settings = SessionSwitcher.getInstance().settings

        // Clear text pane
        this.clear()

        // Add first line of request as normal text
        this.appendText(requestLines[0] + "\n")

        val hideHeadersMode = settings.editorShowHeadersMode.get()
        val showRequestBody = settings.editorShowRequestBody.get()

        var cookiesProcessed = false

        // Add headers and cookies
        for (header in httpRequest.headers()) {
            val headerName = normalizeHeaderName(header.name()) // Train-Case
            val headerValue = header.value()
            val isCommonHeader = COMMON_HEADER_PREFIXES.any { headerName.lowercase().startsWith(it) }

            if (headerName == ":authority") {
                // Turn HTTP/2 ":authority" header in "Host"
                this.appendText("Host: $headerValue")
            } else if (headerName.startsWith(":")) {
                // Never show other HTTP/2 headers
                continue
            } else if (headerName.lowercase() == "cookie") {
                if (!cookiesProcessed) {
                    Logger.debug("Cookie header detected")
                    cookiesProcessed = true
                    // Ignore the specific header value and normalize cookies
                    val cookies = Cookies.fromHttpRequest(httpRequest)
                    this.appendCookieHeader(cookies.toString(), cookiesDiffInfo)
                } else {
                    Logger.debug("Additional cookie header, ignoring...")
                    continue
                }
            } else if (modifiedHeaders.contains(headerName.lowercase())) {
                Logger.debug("Header modified: $headerName")
                this.appendText("$headerName: ")
                this.appendText(headerValue, modifiedElementStyle)
            } else if (addedHeaders.contains(headerName.lowercase())) {
                Logger.debug("Header added: $headerName")
                this.appendText("$headerName: $headerValue", addedElementStyle)
            } else if (
                    hideHeadersMode == Settings.HideHeadersMode.SHOW_ALL ||
                    (hideHeadersMode == Settings.HideHeadersMode.HIDE_COMMON && isCommonHeader)
                    ) {
                this.appendText("$headerName: $headerValue")
                Logger.debug("Header noop: $headerName")
            } else {
                // If we got here, no text was appended, so let's continue to prevent a newline getting appended
                continue
            }
            this.appendText("\n")
        }

        // Add body
        this.appendText("\n")
        if (showRequestBody) {
            if (httpRequest.contentType() == ContentType.JSON) {
                this.appendText(JsonPrettifier.prettify(httpRequest.bodyToString()))
            } else {
                this.appendText(httpRequest.bodyToString())
            }
        }

        // Leave some extra padding space
        this.appendText("\n")
    }
}