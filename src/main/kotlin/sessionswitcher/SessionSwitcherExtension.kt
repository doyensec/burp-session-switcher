package sessionswitcher

import burp.api.montoya.BurpExtension
import burp.api.montoya.MontoyaApi
import burp.api.montoya.extension.ExtensionUnloadingHandler

class SessionSwitcherExtension : BurpExtension, ExtensionUnloadingHandler {
    companion object {
        const val VERSION = "1.0"
    }

    lateinit var instance: SessionSwitcher

    // Montoya API gets instantiated second
    override fun initialize(montoyaApi: MontoyaApi) {
        // Set the name of the extension
        montoyaApi.extension().setName("Session Switcher v$VERSION")
        montoyaApi.extension().registerUnloadingHandler(this)

        // Redirect stdout and stderr
        System.setOut(montoyaApi.logging().output())
        System.setErr(montoyaApi.logging().error())

        this.instance = SessionSwitcher(montoyaApi)
    }

    override fun extensionUnloaded() {
        this.instance.unload()
    }
}