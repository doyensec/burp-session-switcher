package sessionswitcher.ui.misc

import burp.api.montoya.http.message.requests.HttpRequest
import burp.api.montoya.ui.contextmenu.AuditIssueContextMenuEvent
import burp.api.montoya.ui.contextmenu.ContextMenuEvent
import burp.api.montoya.ui.contextmenu.ContextMenuItemsProvider
import sessionswitcher.SessionSwitcher
import java.awt.Component
import java.awt.Toolkit
import java.awt.event.ActionEvent
import java.awt.event.KeyEvent
import java.awt.event.MouseAdapter
import java.awt.event.MouseEvent
import javax.swing.*

open class MenuAction(val name: String, val keyStroke: KeyStroke?, val action: (ActionEvent) -> Unit) :
    AbstractAction(name) {
    override fun actionPerformed(e: ActionEvent) {
        this.action(e)
    }
}

/*
    This class provides the Context Menu that is opened when the user Right-Clicks inside InQL (e.g. in the Scanner Tab Results
    The actions have associated Keyboard Shortcuts so that standard Burp shortcuts can be used from InQL

    This class is also extended below from SendToInqlHandler which instead provides the Extension Context Menu "InQL >" for
    Burp's standard context menu in other Burp tools (Scanner, Proxy, etc).
 */
abstract class SendFromPluginHandler(val plugin: SessionSwitcher) : MouseAdapter() {
    private val popup = JPopupMenu()

    // ===== Actions associated with Menu Items

    protected val sendToIntruderAction = MenuAction(
        "Send to Intruder",
        KeyStroke.getKeyStroke(KeyEvent.VK_I, Toolkit.getDefaultToolkit().menuShortcutKeyMaskEx),
    ) {
        this.sendRequestToIntruder()
    }
    protected val sendToRepeaterAction = MenuAction(
        "Send to Repeater",
        KeyStroke.getKeyStroke(KeyEvent.VK_R, Toolkit.getDefaultToolkit().menuShortcutKeyMaskEx),
    ) {
        this.sendRequestToRepeater()
    }

    /* The following list is currently used for:
     - Enable/Disable (grey-out) context menu items when the user
        right-clicks something that is not actually a GraphQL item, e.g. a point of interest in the scanner results.
     - Provide Keyboard Shortcuts (CTRL+R, CTRL+I, etc)
     */
    protected val sendFromPluginActions = mutableListOf<MenuAction>(
        sendToIntruderAction,
        sendToRepeaterAction,
    )
    abstract fun getRequest(): HttpRequest?
    override fun mousePressed(e: MouseEvent) {
        if (e.button == MouseEvent.BUTTON3) {    // Right Click only
            this.setContextActions()
            this.popup.show(e.component, e.x, e.y)
        }
    }

    // Populate the right click menu in InQL views (InQL Scanner, GraphQL editor view)
    // The context menus added by Burp itself **are not handled here** (e.g. Repeater - Raw editor - right click)
    // In order to add elements to Burp's menu (Extensions - InQL - ...), modify sendToInqlComponents in SendToInqlHandler class
    private fun setContextActions() {
        this.popup.removeAll()

        this.popup.add(this.sendToIntruderAction)
        this.popup.add(this.sendToRepeaterAction)
    }

    // ===== Convenience methods for the actions
    private fun sendRequestToIntruder() {
        plugin.montoyaApi.intruder().sendToIntruder(this.getRequest() ?: return)
    }

    private fun sendRequestToRepeater() {
        plugin.montoyaApi.repeater().sendToRepeater(this.getRequest() ?: return)
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

class SendToPluginHandler(inql: SessionSwitcher) : SendFromPluginHandler(inql), ContextMenuItemsProvider {
    class BurpMenuItem(action: MenuAction) : JMenuItem(action.name) {
        init {
            this.addActionListener {
                action.action(it)
            }
        }
    }

    private var request: HttpRequest? = null

    // This only sets Right Click handlers for the Burp's own menus. Menus added by InQL are handled
    // in setContextActions()
    private fun sendToInqlComponents(): MutableList<JMenuItem> {
        return mutableListOf<JMenuItem>(
            //BurpMenuItem(super.sendToInqlScannerAction),
            //BurpMenuItem(super.sendToInqlAttackerAction)
        ).apply {
            //for (action in super.sendToEmbeddedToolActions) {
             //   this.add(BurpMenuItem(action))
            //}
        }
    }

    private fun requestFromContext(event: ContextMenuEvent): HttpRequest? {
        val invocationType = event.invocationType()
        if (invocationType.containsHttpRequestResponses()) {
            val requestResponses = event.selectedRequestResponses()
            if (requestResponses.size != 1) return null
            return requestResponses[0].request()
        } else if (invocationType.containsHttpMessage()) {
            val msg = event.messageEditorRequestResponse().orElse(null) ?: return null
            return msg.requestResponse().request()
        }
        return null
    }

    override fun provideMenuItems(event: ContextMenuEvent): MutableList<JMenuItem>? {
        this.request = this.requestFromContext(event) ?: return null
        return this.sendToInqlComponents()
    }

    override fun provideMenuItems(event: AuditIssueContextMenuEvent): MutableList<JMenuItem>? {
        val issues = event.selectedIssues()
        if (issues.size != 1) return null
        val requestResponses = issues[0].requestResponses()
        if (requestResponses.isEmpty()) return null
        this.request = requestResponses[0].request()
        return this.sendToInqlComponents()
    }

    override fun getRequest(): HttpRequest? {
        return this.request
    }
}
