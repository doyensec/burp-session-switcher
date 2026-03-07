package sessionswitcher.settings

import kotlinx.coroutines.runBlocking
import sessionswitcher.Logger
import sessionswitcher.SessionSwitcher
import sessionswitcher.savestate.importexport.JSONImportExport
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
import javax.swing.JTabbedPane

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

    private fun makeRequestEditorSection(store: SettingsItem.Store) = UISection(
        "Request Editor",
        "Settings related to the Request Editor UI",
        settings.editorHideHeadersMode.drawComboBox(store, true),
        Box.createVerticalStrut(6),
        settings.editorShowRequestBody.drawCheckbox(store),
        Box.createVerticalStrut(6),
        settings.filterSessionMode.drawComboBox(store, true),
        Box.createVerticalStrut(6),
        settings.editorDoNotAskOverwriteConfirmation.drawCheckbox(store),
        Box.createVerticalStrut(6),
        settings.cookiesUpdateMode.drawComboBox(store, true),
        Box.createVerticalStrut(6),
        settings.headersUpdateMode.drawComboBox(store, true),
        Box.createVerticalStrut(6),
        settings.cookiesInjectMode.drawComboBox(store, true),
        Box.createVerticalStrut(6),
    )

    private fun makeAutoUpdateSection(store: SettingsItem.Store) = UISection(
        "Auto Updater",
        "Set the behavior of Auto Update Rules",
        settings.stopAtFirstUpdateRule.drawCheckbox(store)
    )

    private fun makeLoggingLevelSection(store: SettingsItem.Store) = UISection(
    "Logging options",
    "Use these settings to configure the logging level of the extension.",
    settings.loggingLevel.drawComboBox(store, true),
    )

    private fun makeProjectDataSection(): UISection {
        val exportJsonButton = JButton("Export to JSON").also {
            it.addActionListener {
                val json = JSONImportExport.exportToJson()
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

        val importJsonButton = JButton("Import from JSON").also {
            it.addActionListener {
                val fileChooser = JFileChooser()
                val userSelection = fileChooser.showOpenDialog(null)
                if (userSelection != JFileChooser.APPROVE_OPTION) {
                    return@addActionListener
                }
                val selectedFile = fileChooser.selectedFile

                try {
                    val jsonText = selectedFile.readText(Charsets.UTF_8)
                    Logger.verbose("Importing data from JSON file...")
                    val success = runBlocking {
                        JSONImportExport.importFromJson(jsonText)
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
            it.add(exportJsonButton)
            it.add(importJsonButton)
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

        return UISection(
            "Project data",
            "Use these buttons to manage the data stored by the extension in the project file",
            importExportPanel,
            Box.createVerticalStrut(6),
            deleteButtonsPanel,
            Box.createVerticalStrut(6),
            deleteEverythingButton
        )
    }

    private fun makeProjectSettingsTab(): JScrollPane {
        val store = SettingsItem.Store.PROJECT
        val requestEditorSection = makeRequestEditorSection(store)
        val autoUpdateSection = makeAutoUpdateSection(store)
        val loggingLevelSection = makeLoggingLevelSection(store)
        val projectDataSection = makeProjectDataSection()

        val panel = JPanel().also {
            it.layout = BoxLayout(it, BoxLayout.Y_AXIS)
            it.add(requestEditorSection)
            it.add(JSeparator(JSeparator.HORIZONTAL))
            //it.add(autoInjectorSections)
            it.add(autoUpdateSection)
            it.add(JSeparator(JSeparator.HORIZONTAL))
            it.add(loggingLevelSection)
            it.add(JSeparator(JSeparator.HORIZONTAL))
            it.add(projectDataSection)
        }

        val scrollable = JScrollPane(panel)
        scrollable.verticalScrollBarPolicy = JScrollPane.VERTICAL_SCROLLBAR_ALWAYS
        scrollable.horizontalScrollBarPolicy = JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED
        return scrollable
    }

    private fun makeGlobalSettingsTab(): JScrollPane {
        val store = SettingsItem.Store.GLOBAL
        val requestEditorSection = makeRequestEditorSection(store)
        val autoUpdateSection = makeAutoUpdateSection(store)
        val loggingLevelSection = makeLoggingLevelSection(store)

        val panel = JPanel().also {
            it.layout = BoxLayout(it, BoxLayout.Y_AXIS)
            it.add(requestEditorSection)
            it.add(JSeparator(JSeparator.HORIZONTAL))
            it.add(autoUpdateSection)
            it.add(JSeparator(JSeparator.HORIZONTAL))
            it.add(loggingLevelSection)
        }

        val scrollable = JScrollPane(panel)
        scrollable.verticalScrollBarPolicy = JScrollPane.VERTICAL_SCROLLBAR_ALWAYS
        scrollable.horizontalScrollBarPolicy = JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED
        return scrollable
    }

    init {
        val tabbedPane = JTabbedPane()
        tabbedPane.addTab("Project Settings", makeProjectSettingsTab())
        tabbedPane.addTab("Global Settings", makeGlobalSettingsTab())

        this.add(tabbedPane)
        this.autoSize()
    }
}
