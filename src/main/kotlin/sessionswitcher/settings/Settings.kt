package sessionswitcher.settings

class Settings(val provider: SettingsProvider) {

    public val loggingLevel = StringSetting(provider,"logging.level", "Logging level", "DEBUG")
    public val proxyHighlightInjectedColor = StringSetting(provider,"proxy.highlight_injected_color", "Highlight color of injected requests", "yellow")

    public val displayRequestEditor = BooleanSetting(provider,"ui.display_request_editor", "Display the request editor", true)
    public val registerContextMenu = BooleanSetting(provider,"ui.register_context_menu", "Register the context menu handler", true)
    public val displayExtensionMainTab = BooleanSetting(provider,"ui.display_extension_main_tab", "Display the extension's main tab", true)

    public val editorShowRequestBody = BooleanSetting(provider, "editor.show_request_body", "Show the body in the request editor", true)
    public val editorShowChangesOnly = BooleanSetting(provider, "editor.show_changes_only", "Only show changed headers", false)
    public val editorHideCommonHeaders = BooleanSetting(provider, "editor.hide_common_headers", "Hide common headers", false)

    public val showAllSessions = BooleanSetting(provider, "session.show_all_sessions", "Do not filter Sessions by hostname", false)
    public val keepOtherCookies = BooleanSetting(provider, "session.keep_other_cookies", "Keep unchanged cookies in request", true)
}