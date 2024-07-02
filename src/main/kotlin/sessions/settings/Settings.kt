package sessions.settings

import burp.Burp
import burp.api.montoya.persistence.PersistedObject
import burp.api.montoya.persistence.Preferences

class Settings private constructor() {
    companion object {
        private lateinit var instance: Settings
        lateinit var globalStore: Preferences
        lateinit var projectStore: PersistedObject
        fun getInstance(): Settings {
            if (this::instance.isInitialized) return instance
            globalStore = Burp.Montoya.persistence().preferences()
            projectStore = Burp.Montoya.persistence().extensionData()
            this.instance = Settings()
            return instance
        }
    }

    private val registeredSettings = ArrayList<SettingsItem<out Any>>()
    fun registerSettingItem(item: SettingsItem<out Any>) {
        this.registeredSettings.add(item)
    }

    // SETTINGS
    public val loggingLevel = StringSetting("logging.level", "Logging level", "INFO")
    public val proxyHighlightInjectedColor = StringSetting("proxy.highlight_injected_color", "Highlight color of injected requests", "yellow")


}