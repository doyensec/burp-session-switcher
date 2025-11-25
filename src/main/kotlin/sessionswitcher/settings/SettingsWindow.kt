package sessionswitcher.settings

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
        }

        val scrollable = JScrollPane(panel)
        scrollable.verticalScrollBarPolicy = JScrollPane.VERTICAL_SCROLLBAR_ALWAYS
        scrollable.horizontalScrollBarPolicy = JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED

        this.add(scrollable)
        this.autoSize()
    }
}
