package sessionswitcher.ui.editor

import burp.api.montoya.http.message.requests.HttpRequest
import burp.api.montoya.ui.Theme
import sessionswitcher.Logger
import sessionswitcher.SessionSwitcher
import java.awt.Color
import javax.swing.text.SimpleAttributeSet
import javax.swing.text.StyleConstants

class DiffHighlightRequestEditor: StyledTextEditor() {
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

    public fun setRequest(httpRequest: HttpRequest) {
        Logger.debug("setRequest, no style")
        this.textPane.text = httpRequest.toString().replace("\r", "")
    }

    private fun appendCookieHeader(value: String, cookiesDiffInfo: Pair<List<String>, List<String>>) {
        // Preparation
        val modifiedCookies = cookiesDiffInfo.first.map { it.lowercase() }.toSet()
        val addedCookies = cookiesDiffInfo.second.map { it.lowercase() }.toSet()

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

    public fun setRequest(httpRequest: HttpRequest, headersDiffInfo: Pair<List<String>, List<String>>, cookiesDiffInfo: Pair<List<String>, List<String>>) {
        Logger.debug("setRequest, styled")
        val requestLines = httpRequest.toString().split("\r\n")

        // Preparation
        val modifiedHeaders = headersDiffInfo.first.map { it.lowercase() }.toSet()
        val addedHeaders = headersDiffInfo.second.map { it.lowercase() }.toSet()

        // Clear text pane
        this.clear()

        // Add first line of request as normal text
        this.appendText(requestLines[0] + "\n")

        // Add headers and cookies
        for (header in httpRequest.headers()) {
            if (header.name().lowercase() == "cookie") {
                this.appendCookieHeader(header.value(), cookiesDiffInfo)
                Logger.debug("Cookie header detected")
            } else if (modifiedHeaders.contains(header.name().lowercase())) {
                this.appendText("${header.name()}: ")
                this.appendText(header.value(), modifiedElementStyle)
                Logger.debug("Header modified: " + header.name())
            } else if (addedHeaders.contains(header.name().lowercase())) {
                this.appendText("${header.name()}: ${header.value()}", addedElementStyle)
                Logger.debug("Header added: " + header.name())
            } else {
                this.appendText("${header.name()}: ${header.value()}")
                Logger.debug("Header noop: " + header.name())
            }
            this.appendText("\n")
        }

        // Add body
        this.appendText("\n")
        this.appendText(httpRequest.bodyToString())


        // Return caret to the top
        this.textPane.caretPosition = 0
    }
}