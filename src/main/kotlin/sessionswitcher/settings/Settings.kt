package sessionswitcher.settings

import burp.api.montoya.core.HighlightColor
import sessionswitcher.Logger

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
    val editorShowHeadersMode = EnumSetting(provider, "editor.hide_headers_mode", "Hide headers in the request editor:", HideHeadersMode::class.java, HideHeadersMode.HIDE_COMMON)
    val filterSessionMode = EnumSetting(provider, "sessions.switcher_filter_mode", "In the selector, show only the Sessions that match:", FilterSessionMode::class.java, FilterSessionMode.BY_DOMAIN)
    val editorDoNotAskOverwriteConfirmation = BooleanSetting(provider, "editor.no_confirm_overwrite", "Do not ask confirmation when overwriting a Session", false)

    /* Injector settings */
    val removeOtherCookies = BooleanSetting(provider, "session.remove_other_cookies", "Remove from the request the cookies that are not in the Session", false)

    /* Update settings */
    val updateOnlyExistingHeaders = BooleanSetting(provider, "updater.only_update_existing_headers", "Update only the headers that already in the Session", true)
    val updateOnlyExistingCookies = BooleanSetting(provider, "updater.only_update_existing_cookies", "Update only the cookies that already in the Session", true)

    /* Auto Injector settings */
    val injectorHighlightColor = EnumSetting(provider, "injector.highlight_color", "Highlight injected requests with color:", HighlightColor::class.java, HighlightColor.ORANGE)
    val injectorAnnotateRequest = BooleanSetting(provider, "injector.annotate_request", "Annotate injected request with session name", true)

    /* Logging settings */
    val loggingLevel = EnumSetting(provider,"logging.level", "Logging level:", Logger.Level::class.java, Logger.Level.DEBUG)
}