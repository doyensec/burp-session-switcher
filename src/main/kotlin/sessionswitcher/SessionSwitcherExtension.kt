package sessionswitcher

import burp.api.montoya.BurpExtension
import burp.api.montoya.MontoyaApi
import burp.api.montoya.extension.ExtensionUnloadingHandler

class SessionSwitcherExtension :
    BurpExtension,
    ExtensionUnloadingHandler {
    companion object {
        val VERSION = SessionSwitcherExtension::class.java.`package`.implementationVersion ?: "Unknown"
    }

    lateinit var instance: SessionSwitcher

    // Montoya API gets instantiated second
    @Suppress("DEPRECATION")
    override fun initialize(montoyaApi: MontoyaApi) {
        // Set the name of the extension
        montoyaApi.extension().setName("Session Switcher")
        montoyaApi.extension().registerUnloadingHandler(this)

        // Redirect stdout and stderr
        System.setOut(montoyaApi.logging().output())
        System.setErr(montoyaApi.logging().error())

        this.instance = SessionSwitcher.init(montoyaApi)
        Logger.info("Session Switcher v$VERSION Loaded Successfully")
    }

    override fun extensionUnloaded() {
        this.instance.unload()
    }
}
