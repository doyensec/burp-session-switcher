package sessionswitcher.settings

class Settings(val provider: SettingsProvider) {

    public val loggingLevel = StringSetting(provider,"logging.level", "Logging level", "INFO")
    public val proxyHighlightInjectedColor = StringSetting(provider,"proxy.highlight_injected_color", "Highlight color of injected requests", "yellow")

    public val displayRequestEditor = BooleanSetting(provider,"ui.display_request_editor", "Display the request editor", true)
    public val registerContextMenu = BooleanSetting(provider,"ui.register_context_menu", "Register the context menu handler", true)
    public val displayExtensionMainTab = BooleanSetting(provider,"ui.display_extension_main_tab", "Display the extension's main tab", true)

    public val editorFontSize = IntSetting(provider, "editor.font_size", "Request Editor Font Size", 12)
}