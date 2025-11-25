package sessionswitcher.settings

import sessionswitcher.SessionSwitcher
import sessionswitcher.ui.UISection
import sessionswitcher.ui.Window
import javax.swing.*

class SettingsWindow(val settings: Settings) : Window("SessionSwitcher Settings") {
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

        /*
        val autoInjectorSections = UISection(
            "Auto Injector",
            "Customize the options of the Auto Injector",
            settings.injectorHighlightColor.drawComboBox(true),
            Box.createVerticalStrut(6),
            settings.injectorAnnotateRequest.drawCheckbox()
          )
          */
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

        val deleteSessionsButton = JButton("Delete All Sessions From Project File").also {
            it.addActionListener {
                val data = SessionSwitcher.getApi().persistence().extensionData()
                data.childObjectKeys().filter { s -> s.startsWith("Session.")}.forEach { s -> data.deleteChildObject(s) }
                data.deleteChildObject("SessionCollection")
                }
            }

        val deleteUpdateRulesButton = JButton("Delete All Update Rules").also {
            it.addActionListener {
                val data = SessionSwitcher.getApi().persistence().extensionData()
                data.childObjectKeys().filter { s -> s.startsWith("UpdateRule.")}.forEach { s -> data.deleteChildObject(s) }
                data.deleteChildObject("UpdateRulesCollection")
            }
        }

        val resetButtonsPanel = UISection(
            "Project data",
            "Use these buttons to reset the project data in case of issues.",
            deleteSessionsButton,
            Box.createVerticalStrut(6),
            deleteUpdateRulesButton
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
