package sessionswitcher

import burp.api.montoya.MontoyaApi
import kotlinx.coroutines.runBlocking
import sessionswitcher.requesteditor.RequestEditor
import sessionswitcher.rules.autoupdate.AutoUpdateProxyListener
import sessionswitcher.rules.autoupdate.UpdateRulesCollection
import sessionswitcher.savestate.CanSaveData
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
        const val SERIALIZED_DATA_VERSION_KEY = "SerializedDataVersion"
        const val SERIALIZED_DATA_VERSION = 2

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

        Logger.setLevel(Logger.Level.WARNING) // Suppress logs while fetching the actual logging level
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
        /* // Experimental feature will be removed
        if (settings.registerUpdaterHandler.get()) {
            montoyaApi.http().registerSessionHandlingAction(SessionUpdaterHandler(this))
        }
        if (settings.registerInjectorHandler.get()) {
            montoyaApi.http().registerSessionHandlingAction(SessionInjectorHandler(this))
        }
        */

        // Register proxy listeners
        autoUpdateProxyListener = AutoUpdateProxyListener(this)
        montoyaApi.proxy().registerRequestHandler(autoUpdateProxyListener)
        montoyaApi.proxy().registerResponseHandler(autoUpdateProxyListener)

        // Reload data from the project file
        runBlocking {
            this@SessionSwitcher.loadSavedData()
        }

        // Register the extension main tab
        if (settings.displayExtensionMainTab.get()) {
            this.mainSuiteTab = MainSuiteTab(this)
            montoyaApi.userInterface().registerSuiteTab("Sessions", mainSuiteTab)
        }
    }

    private suspend fun tryDeserializeData(): Boolean {
        var loadedCorrectly = true
        val store = montoyaApi.persistence().extensionData()
        val firstLevelKeys = store.childObjectKeys()
        loadedCorrectly = loadedCorrectly and (!firstLevelKeys.contains(this.sessions.saveStateKey) or this.sessions.loadFromDataStore(store))
        loadedCorrectly = loadedCorrectly and (!firstLevelKeys.contains(this.updateRulesCollection.saveStateKey) or this.updateRulesCollection.loadFromDataStore(store))
        return loadedCorrectly
    }

    private suspend fun loadSavedData() {
        val storage = montoyaApi.persistence().extensionData()
        val serializedDataVersion = storage.getInteger(SERIALIZED_DATA_VERSION_KEY) ?: return
        val shouldTryLoadIncompatibleVersion = settings.tryLoadDifferentSavedDataVersion.get()

        if (serializedDataVersion < SERIALIZED_DATA_VERSION) {
            if (shouldTryLoadIncompatibleVersion) {
                Logger.info("Saved data was made with an older SessionSwitcher version, trying to load it anyway.")
                val loadedCorrectly = tryDeserializeData()
                if (loadedCorrectly) {
                    Logger.info("Data loaded successfully, upgrading saved data version to current one")
                    storage.setInteger(SERIALIZED_DATA_VERSION_KEY, SERIALIZED_DATA_VERSION)
                } else {
                    Logger.warning("Data could not be loaded.")
                }
            } else {
                Logger.warning("Saved data was made with an older SessionSwitcher version, not loading it.")
                return
            }
        } else if (serializedDataVersion > SERIALIZED_DATA_VERSION) {
            if (shouldTryLoadIncompatibleVersion) {
                val loadedCorrectly = tryDeserializeData()
                if (loadedCorrectly) {
                    Logger.info("Data loaded successfully.")
                } else {
                    Logger.warning("Data could not be loaded.")
                }
                Logger.warning("Saved data was made with a newer SessionSwitcher version, trying to load it anyway.")
            } else {
                Logger.warning("Saved data was made with a newer SessionSwitcher version, not loading it.")
                return
            }
        } else {
            val loadedCorrectly = tryDeserializeData()
            if (loadedCorrectly) {
                Logger.info("Saved data loaded successfully")
            } else {
                Logger.warning("Saved data could not be loaded.")
            }
        }
    }

    fun unload() = runBlocking {
        autoUpdateProxyListener.stop()
        CanSaveData.joinAll()
    }
}
