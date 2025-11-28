package sessionswitcher.ui

import sessionswitcher.SessionSwitcher
import java.awt.Dimension
import javax.swing.*
import javax.swing.JOptionPane.*

class ConfirmationDialog(
    private val sessionSwitcher: SessionSwitcher,
    private val text: String,
    private val title: String
) {
    private var savePreference = false
    private var answer = false

    fun show(): Boolean {
        val frame = sessionSwitcher.montoyaApi.userInterface().swingUtils().suiteFrame()

        val panel = JPanel()
        panel.layout = BoxLayout(panel, BoxLayout.Y_AXIS)

        val label = JLabel(this.text)
        panel.add(label)

        val checkBox = JCheckBox("Don't ask again")
        panel.add(Box.createRigidArea(Dimension(0, 5)))
        panel.add(checkBox)

        answer = showConfirmDialog(frame, panel, title, YES_NO_OPTION, WARNING_MESSAGE) == YES_OPTION

        savePreference = checkBox.isSelected
        return answer
    }

    fun shouldSavePreference(): Boolean = savePreference
    fun getAnswer(): Boolean = answer
}