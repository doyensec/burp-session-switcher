package burp

import burp.api.montoya.BurpExtension
import burp.api.montoya.MontoyaApi
import burp.api.montoya.extension.ExtensionUnloadingHandler
import sessions.BurpSessions
import java.io.PrintWriter
import java.util.*

@Suppress("unused")
class BurpExtender : IBurpExtender, ExtensionUnloadingHandler, BurpExtension {

    companion object {
        val version =
            Properties().also { it.load(this::class.java.getResourceAsStream("/version.properties")) }.getProperty("version") ?: ""
    }
    private lateinit var burpSessions: BurpSessions

    private fun checkBurpVersion(callbacks: IBurpExtenderCallbacks) {
        val versionArray = callbacks.burpVersion
        val year = versionArray[1].toInt()
        val versionParts = versionArray[2].split(".")
        val major = versionParts[0].toInt()
        val minor = if (versionParts.size > 1) versionParts[1].toInt() else 0

        if ((year < 2023) || ((year == 2023) && (major == 1) && (minor < 2))) {
            val stdout = PrintWriter(callbacks.stdout, true)
            stdout.println("Sessions relies on the Montoya API, which is only supported in Burp versions 2023.1.2 or higher.")

            callbacks.unloadExtension()
            throw Exception("Sessions is not compatible with your current Burp version.")
        }
    }

    // Legacy API gets instantiated first
    override fun registerExtenderCallbacks(callbacks: IBurpExtenderCallbacks) {
        checkBurpVersion(callbacks)
    }

    // Montoya API gets instantiated second
    override fun initialize(montoyaApi: MontoyaApi) {
        // The new Montoya API should be used for all the new functionality in InQL
        Burp.initialize(montoyaApi)

        // Set the name of the extension
        montoyaApi.extension().setName("Sessions")

        burpSessions = BurpSessions()
        montoyaApi.extension().registerUnloadingHandler(this)
    }

    override fun extensionUnloaded() {
        this.burpSessions.unload()
    }
}
