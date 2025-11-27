package sessionswitcher.settings

import burp.api.montoya.core.HighlightColor
import sessionswitcher.Logger
import sessionswitcher.sessions.CookiesInjectMode
import sessionswitcher.sessions.CookiesUpdateMode
import sessionswitcher.sessions.HeadersUpdateMode

class Settings(val provider: SettingsProvider) {

    enum class FilterSessionMode(val readable: String) {
        BY_SUBDOMAIN("The request's subdomain"),
        BY_DOMAIN("The request's main domain"),
        NO_FILTER("Show all Sessions");

        override fun toString(): String {
            return this.readable
        }
    }

    enum class HideHeadersMode(val readable: String) {
        SHOW_ALL("Show all headers"),
        HIDE_COMMON("Hide common headers"),
        SHOW_CHANGES_ONLY("Show only changed headers");

        override fun toString(): String {
            return this.readable
        }
    }


    /* Internal */
    val displayRequestEditor = BooleanSetting(provider,"ui.display_request_editor", "Display the request editor", true)
    val registerContextMenu = BooleanSetting(provider,"ui.register_context_menu", "Register the context menu handler", true)
    val displayExtensionMainTab = BooleanSetting(provider,"ui.display_extension_main_tab", "Display the extension's main tab", true)
    val registerUpdaterHandler = BooleanSetting(provider, "handler.register_updater", "Register updater handler", true)
    val registerInjectorHandler = BooleanSetting(provider, "handler.register_injector", "Register injector handler", true)

    /* Editor Settings */
    val editorShowRequestBody = BooleanSetting(provider, "editor.show_request_body", "Show the body in the request editor", true)
    val editorHideHeadersMode = EnumSetting(provider, "editor.hide_headers_mode", "Hide headers in the request editor:", HideHeadersMode::class.java, HideHeadersMode.HIDE_COMMON)
    val filterSessionMode = EnumSetting(provider, "sessions.switcher_filter_mode", "In the Switcher menu, show only sessions matching:", FilterSessionMode::class.java, FilterSessionMode.BY_DOMAIN)
    val editorDoNotAskOverwriteConfirmation = BooleanSetting(provider, "editor.no_confirm_overwrite", "Do not ask confirmation when updating a Session", false)

    val cookiesUpdateMode = EnumSetting(provider, "editor.cookies_update_mode", "When updating cookies from a request:", CookiesUpdateMode::class.java, CookiesUpdateMode.MIRROR)
    val headersUpdateMode = EnumSetting(provider, "editor.headers_update_mode", "When updating headers from a request:", HeadersUpdateMode::class.java, HeadersUpdateMode.UPDATE_EXISTING)

    val cookiesInjectMode = EnumSetting(provider, "editor.cookies_inject_mode", "When injecting cookies in a request:", CookiesInjectMode::class.java, CookiesInjectMode.ADD_ALL)

    /* Auto Update Rules */
    val stopAtFirstUpdateRule = BooleanSetting(provider, "auto_updater.stop_at_first_match", "Stop after the first matching rule", true)

    /* Auto Injector settings */
    val injectorHighlightColor = EnumSetting(provider, "auto_injector.highlight_color", "Highlight injected requests with color:", HighlightColor::class.java, HighlightColor.ORANGE)
    val injectorAnnotateRequest = BooleanSetting(provider, "auto_injector.annotate_request", "Annotate injected request with session name", true)

    /* Logging settings */
    val loggingLevel = EnumSetting(provider,"logging.level", "Logging level:", Logger.Level::class.java, Logger.Level.DEBUG)
}