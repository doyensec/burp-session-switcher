package sessionswitcher.requesteditor

import sessionswitcher.ui.MenuAction
import java.awt.Component
import java.awt.Toolkit
import java.awt.event.KeyEvent
import java.awt.event.MouseAdapter
import java.awt.event.MouseEvent
import javax.swing.JComponent
import javax.swing.JPopupMenu
import javax.swing.KeyStroke

class ContextMenu(
    private val editor: RequestEditor,
) : MouseAdapter() {
    private val popup = JPopupMenu()

    // ===== Actions associated with Menu Items

    private val sendToIntruderAction =
        MenuAction(
            "Send to Intruder",
            KeyStroke.getKeyStroke(KeyEvent.VK_I, Toolkit.getDefaultToolkit().menuShortcutKeyMaskEx),
        ) {
            editor.sessionSwitcher.montoyaApi
                .intruder()
                .sendToIntruder(this.getRequest())
        }

    private val sendToRepeaterAction =
        MenuAction(
            "Send to Repeater",
            KeyStroke.getKeyStroke(KeyEvent.VK_R, Toolkit.getDefaultToolkit().menuShortcutKeyMaskEx),
        ) {
            editor.sessionSwitcher.montoyaApi
                .repeater()
                .sendToRepeater(this.getRequest())
        }

    private val sendToOrganizerAction =
        MenuAction(
            "Send to Organizer",
            KeyStroke.getKeyStroke(KeyEvent.VK_O, Toolkit.getDefaultToolkit().menuShortcutKeyMaskEx),
        ) {
            editor.sessionSwitcher.montoyaApi
                .organizer()
                .sendToOrganizer(this.getRequest())
        }

    /* The following list is currently used for:
     - Enable/Disable (grey-out) context menu items when the user
        right-clicks something that is not actually a GraphQL item, e.g. a point of interest in the scanner results.
     - Provide Keyboard Shortcuts (CTRL+R, CTRL+I, etc)
     */
    private val sendFromPluginActions =
        mutableListOf(
            sendToIntruderAction,
            sendToRepeaterAction,
            sendToOrganizerAction,
        )

    private fun getRequest() = editor.request

    override fun mousePressed(e: MouseEvent) {
        if (e.button == MouseEvent.BUTTON3) { // Right Click only
            this.setContextActions()
            this.popup.show(e.component, e.x, e.y)
        }
    }

    // Populate the right click menu in the request editor
    // The context menus added by Burp itself **are not handled here** (e.g. Repeater - Raw editor - right click)
    private fun setContextActions() {
        this.popup.removeAll()
        this.sendFromPluginActions.forEach {
            this.popup.add(it)
        }
    }

    fun setEnabled(enabled: Boolean) {
        this.sendFromPluginActions.forEach {
            it.isEnabled = enabled
        }
    }

    fun addRightClickHandler(c: Component) {
        c.addMouseListener(this)
    }

    fun addKeyboardShortcutHandler(c: JComponent) {
        for (action in this.sendFromPluginActions) {
            if (action.keyStroke == null) continue
            c.inputMap.put(action.keyStroke, action.name)
            c.actionMap.put(action.name, action)
        }
    }
}
