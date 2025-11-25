package sessionswitcher

import burp.api.montoya.MontoyaApi
import kotlinx.coroutines.runBlocking
import sessionswitcher.handlers.SessionInjectorHandler
import sessionswitcher.handlers.SessionUpdaterHandler
import sessionswitcher.requesteditor.RequestEditor
import sessionswitcher.rules.autoupdate.AutoUpdateProxyListener
import sessionswitcher.rules.autoupdate.UpdateRulesCollection
import sessionswitcher.sessions.SessionCollection
import sessionswitcher.settings.BurpSettingsProvider
import sessionswitcher.settings.Settings
import sessionswitcher.settings.SettingsProvider
import sessionswitcher.ui.ContextMenuProvider
import sessionswitcher.ui.maintab.MainSuiteTab

class SessionSwitcher private constructor(
    val montoyaApi: MontoyaApi,
    val settingsProvider: SettingsProvider
) {

    // Singleton pattern to ensure the extension is not initialized more than once
    companion object {
        private lateinit var montoyaApi: MontoyaApi
        fun getApi(): MontoyaApi {
            if (!this::montoyaApi.isInitialized) {
                throw Exception("Montoya API not initialized yet.")
            }
            return this.montoyaApi
        }
        fun getInstance(): SessionSwitcher {
            if (!this::instance.isInitialized) {
                throw Exception("SessionSwitcher not initialized yet.")
            }
            return this.instance
        }
        private lateinit var instance: SessionSwitcher
        fun init(
            montoyaApi: MontoyaApi,
            settingsProvider: SettingsProvider = BurpSettingsProvider(montoyaApi) // Allow to override this to use this as a library
        ): SessionSwitcher {
            if (this::instance.isInitialized) {
                throw Exception("SessionSwitcher instance already initialized.")
            }
            this.montoyaApi = montoyaApi
            this.instance = SessionSwitcher(montoyaApi, settingsProvider)
            return this.instance
        }
    }

    // Core Data
    val sessions = SessionCollection(this)
    val updateRulesCollection = UpdateRulesCollection(this)

    // UI stuff
    val settings = Settings(this.settingsProvider)
    var mainSuiteTab: MainSuiteTab? = null

    // Proxy Listeners
    val autoUpdateProxyListener: AutoUpdateProxyListener

    init {
        montoyaApi.logging().raiseInfoEvent("Session Switcher v${SessionSwitcherExtension.VERSION} Started")

        val logLevel = settings.loggingLevel.get()
        Logger.setLevel(logLevel)

        // Register GraphQL Payload Editor
        if (settings.displayRequestEditor.get()) {
            montoyaApi.userInterface().registerHttpRequestEditorProvider(RequestEditor.getProvider(this))
        }

        // Register context menu handler
        if (settings.registerContextMenu.get()) {
            montoyaApi.userInterface().registerContextMenuItemsProvider(ContextMenuProvider(this))
        }

        // Register session handlers
        if (settings.registerUpdaterHandler.get()) {
            montoyaApi.http().registerSessionHandlingAction(SessionUpdaterHandler(this))
        }
        if (settings.registerInjectorHandler.get()) {
            montoyaApi.http().registerSessionHandlingAction(SessionInjectorHandler(this))
        }

        // Register proxy listeners
        autoUpdateProxyListener = AutoUpdateProxyListener(this)
        montoyaApi.proxy().registerRequestHandler(autoUpdateProxyListener)
        montoyaApi.proxy().registerResponseHandler(autoUpdateProxyListener)

        // Reload data from the project file
        this.sessions.loadFromProjectFile()
        this.updateRulesCollection.loadFromProjectFile()

        // Register the extension main tab
        if (settings.displayExtensionMainTab.get()) {
            this.mainSuiteTab = MainSuiteTab(this)
            montoyaApi.userInterface().registerSuiteTab("Sessions", mainSuiteTab)
        }
    }

    fun unload() = runBlocking {
        //TODO: stop proxy interceptors
    }

    fun focus() {
        this.mainSuiteTab?.focus()
    }
}
