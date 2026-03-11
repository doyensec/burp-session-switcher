package sessionswitcher.ui

import java.awt.event.ActionEvent
import javax.swing.AbstractAction
import javax.swing.JMenuItem
import javax.swing.KeyStroke

open class MenuAction(
    val name: String,
    val keyStroke: KeyStroke?,
    val action: (ActionEvent) -> Unit,
) : AbstractAction(name) {
    override fun actionPerformed(e: ActionEvent) {
        this.action(e)
    }

    fun asJMenuItem(): JMenuItem = JMenuItem(this)
}
