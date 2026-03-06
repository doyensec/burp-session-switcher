package sessionswitcher.settings

import burp.api.montoya.persistence.PersistedObject
import com.google.gson.JsonParser
import kotlinx.coroutines.runBlocking
import sessionswitcher.Logger
import sessionswitcher.SessionSwitcher
import sessionswitcher.savestate.importexport.JsonPersistedObject
import sessionswitcher.savestate.importexport.toJsonObject
import sessionswitcher.ui.UISection
import sessionswitcher.ui.Window
import java.awt.GridLayout
import java.io.File
import java.text.Normalizer
import javax.swing.Box
import javax.swing.BoxLayout
import javax.swing.JButton
import javax.swing.JFileChooser
import javax.swing.JOptionPane
import javax.swing.JPanel
import javax.swing.JScrollPane
import javax.swing.JSeparator

class SettingsWindow(val settings: Settings) : Window("SessionSwitcher Settings") {
    companion object {
        private fun deleteAllData() {
            val data = SessionSwitcher.getApi().persistence().extensionData()
            data.childObjectKeys().forEach { s -> data.deleteChildObject(s) }
            data.stringKeys().forEach { s -> data.deleteString(s) }
            data.booleanKeys().forEach { s -> data.deleteBoolean(s) }
            data.integerKeys().forEach { s -> data.deleteInteger(s) }
            data.longKeys().forEach { s -> data.deleteLong(s) }
            data.shortKeys().forEach { s -> data.deleteShort(s) }
            data.stringListKeys().forEach { s -> data.deleteStringList(s) }
        }
    }
    init {
        // Build the different section first
        val requestEditorSection = UISection(
            "Request Editor",
            "Settings related to the Request Editor UI",
            settings.editorHideHeadersMode.drawComboBox(true),
            Box.createVerticalStrut(6),
            settings.editorShowRequestBody.drawCheckbox(),
            Box.createVerticalStrut(6),
            settings.filterSessionMode.drawComboBox(true),
            Box.createVerticalStrut(6),
            settings.editorDoNotAskOverwriteConfirmation.drawCheckbox(),
            Box.createVerticalStrut(6),
            settings.cookiesUpdateMode.drawComboBox(true),
            Box.createVerticalStrut(6),
            settings.headersUpdateMode.drawComboBox(true),
            Box.createVerticalStrut(6),
            settings.cookiesInjectMode.drawComboBox(true),
            Box.createVerticalStrut(6),
        )

        val autoUpdateSections = UISection(
            "Auto Updater",
            "Set the behavior of Auto Update Rules",
            settings.stopAtFirstUpdateRule.drawCheckbox()
        )

        val loggingLevelSection = UISection(
            "Logging options",
            "Use these settings to configure the logging level of the extension.",
            settings.loggingLevel.drawComboBox(true),
        )

        val exportJson = JButton("Export to JSON").also {
            it.addActionListener {
                val data = SessionSwitcher.getApi().persistence().extensionData()
                val json = data.toJsonObject().toString()
                val rawProjectName = SessionSwitcher.getApi().project().name()
                val projectName = Normalizer.normalize(rawProjectName, Normalizer.Form.NFD)
                    .replace("[^\\w-]".toRegex(), "_") // Replace invalid characters with underscores

                val fileChooser = JFileChooser().apply {
                    selectedFile = File("SessionSwitcher_$projectName.json")
                }

                val userSelection = fileChooser.showSaveDialog(null)
                if (userSelection == JFileChooser.APPROVE_OPTION) {
                    val selectedFile = fileChooser.selectedFile
                    selectedFile.writeText(json)
                }
            }
        }

        val importJson = JButton("Import from JSON").also {
            it.addActionListener {
                val fileChooser = JFileChooser()
                val userSelection = fileChooser.showOpenDialog(null)
                if (userSelection != JFileChooser.APPROVE_OPTION) {
                    return@addActionListener
                }
                val selectedFile = fileChooser.selectedFile
                
                try {
                    val sessionSwitcher = SessionSwitcher.getInstance()
                    val jsonText = selectedFile.readText(Charsets.UTF_8)
                    val jsonObject = JsonParser.parseString(jsonText).asJsonObject

                    Logger.verbose("Importing data from JSON file...")

                    val success: Boolean
                    runBlocking {
                        // Load the data
                        success = sessionSwitcher.tryDeserializeData(JsonPersistedObject(jsonObject))
                        Logger.verbose("Triggering save of new data on project file")

                        // Save in project file
                        val extensionStore = SessionSwitcher.getApi().persistence().extensionData()
                        sessionSwitcher.sessions.saveToDataStore(extensionStore, true)
                        sessionSwitcher.updateRulesCollection.saveToDataStore(extensionStore, true)

                        // Import settings
                        if (!jsonObject.has("Settings") || !jsonObject.get("Settings").isJsonObject) return@runBlocking
                        Logger.verbose("Importing settings...")
                        val settingsObject = extensionStore.getChildObject("Settings") ?: PersistedObject.persistedObject()
                        for (key in jsonObject.getAsJsonObject("Settings").keySet()) {
                            val value = jsonObject.getAsJsonObject("Settings").get(key)
                            if (!value.isJsonPrimitive) continue
                            val primitive = value.asJsonPrimitive
                            if (primitive.isString) {
                                settingsObject.setString(key, primitive.asString)
                            } else if (primitive.isNumber) {
                                settingsObject.setInteger(key, primitive.asInt)
                            } else if (primitive.isBoolean) {
                                settingsObject.setBoolean(key, primitive.asBoolean)
                            }
                        }
                        extensionStore.setChildObject("Settings", settingsObject)
                    }
                    if (success) {
                        JOptionPane.showMessageDialog(
                            null,
                            "Data imported successfully!",
                            "Import Success",
                            JOptionPane.INFORMATION_MESSAGE
                        )
                    } else {
                        JOptionPane.showMessageDialog(
                            null,
                            "Data imported with errors.",
                            "Import Partial",
                            JOptionPane.WARNING_MESSAGE
                        )
                    }
                } catch (_: Exception) {
                    JOptionPane.showMessageDialog(
                        null,
                        "Error importing the file. The selected file is not valid or it was created by a different version of SessionSwitcher.",
                        "Import Error",
                        JOptionPane.ERROR_MESSAGE
                    )
                }
            }
        }

        val importExportPanel = JPanel().also {
            it.layout = GridLayout(1, 2, 5, 2)
            it.add(exportJson)
            it.add(importJson)
        }

        val deleteSessionsButton = JButton("Delete All Sessions").also {
            it.addActionListener {
                val data = SessionSwitcher.getApi().persistence().extensionData()
                data.deleteChildObject("SessionCollection")
            }
        }

        val deleteUpdateRulesButton = JButton("Delete All Update Rules").also {
            it.addActionListener {
                val data = SessionSwitcher.getApi().persistence().extensionData()
                data.deleteChildObject("UpdateRulesCollection")
            }
        }

        val deleteEverythingButton = JButton("Delete Everything").also {
            it.addActionListener {
                deleteAllData()
            }
        }

        val deleteButtonsPanel = JPanel().also {
            it.layout = GridLayout(1, 2, 5, 2)
            it.add(deleteSessionsButton)
            it.add(deleteUpdateRulesButton)
        }

        val resetButtonsPanel = UISection(
            "Project data",
            "Use these buttons to manage the data stored by the extension in the project file",
            importExportPanel,
            Box.createVerticalStrut(6),
            deleteButtonsPanel,
            Box.createVerticalStrut(6),
            deleteEverythingButton
            )

        // Build the main window
        val panel = JPanel().also {
            it.layout = BoxLayout(it, BoxLayout.Y_AXIS)
            it.add(requestEditorSection)
            it.add(JSeparator(JSeparator.HORIZONTAL))
            //it.add(autoInjectorSections)
            it.add(autoUpdateSections)
            it.add(JSeparator(JSeparator.HORIZONTAL))
            it.add(loggingLevelSection)
            it.add(JSeparator(JSeparator.HORIZONTAL))
            it.add(resetButtonsPanel)
        }

        val scrollable = JScrollPane(panel)
        scrollable.verticalScrollBarPolicy = JScrollPane.VERTICAL_SCROLLBAR_ALWAYS
        scrollable.horizontalScrollBarPolicy = JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED

        this.add(scrollable)
        this.autoSize()
    }
}
