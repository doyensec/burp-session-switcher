package sessionswitcher.settings

import burp.api.montoya.core.HighlightColor

class Settings(val provider: SettingsProvider) {

    val loggingLevel = StringSetting(provider,"logging.level", "Logging level", "DEBUG")

    val displayRequestEditor = BooleanSetting(provider,"ui.display_request_editor", "Display the request editor", true)
    val registerContextMenu = BooleanSetting(provider,"ui.register_context_menu", "Register the context menu handler", true)
    val displayExtensionMainTab = BooleanSetting(provider,"ui.display_extension_main_tab", "Display the extension's main tab", true)

    val editorShowRequestBody = BooleanSetting(provider, "editor.show_request_body", "Show the body in the request editor", true)
    val editorShowChangesOnly = BooleanSetting(provider, "editor.show_changes_only", "Only show changed headers", false)
    val editorHideCommonHeaders = BooleanSetting(provider, "editor.hide_common_headers", "Hide common headers", false)

    val filterSessionByDomain = BooleanSetting(provider, "session.filter_by_domain", "Do not show sessions from different domains", true)
    val filterSessionBySubdomain = BooleanSetting(provider, "session.filter_by_subdomain", "Do not show sessions from different subdomains", true)
    val keepOtherCookies = BooleanSetting(provider, "session.keep_other_cookies", "Keep unchanged cookies in request", true)

    val updateOnlyExistingHeaders = BooleanSetting(provider, "updater.only_update_existing_headers", "Only update existing headers", true)
    val updateOnlyExistingCookies = BooleanSetting(provider, "updater.only_update_existing_cookies", "Only update existing cookies", true)

    val registerUpdaterHandler = BooleanSetting(provider, "handler.register_updater", "Register updater handler", true)
    val registerInjectorHandler = BooleanSetting(provider, "handler.register_injector", "Register injector handler", true)

    val injectorHighlightColor = StringSetting(provider, "injector.highlight_color", "Highlight injected requests", HighlightColor.ORANGE.displayName())
    val injectorAnnotateRequest = BooleanSetting(provider, "injector.annotate_request", "Annotate injected request with session name", true)

    val editorDoNotAskOverwriteConfirmation = BooleanSetting(provider, "editor.no_confirm_overwrite", "Do not ask confirmation when overwriting a Session", false)
}