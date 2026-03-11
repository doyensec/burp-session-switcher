package sessionswitcher.requesteditor

import sessionswitcher.SessionSwitcher
import java.awt.BorderLayout
import java.awt.Dimension
import java.awt.event.ComponentAdapter
import java.awt.event.ComponentEvent
import javax.swing.BorderFactory
import javax.swing.BoxLayout
import javax.swing.JPanel
import javax.swing.JScrollPane
import javax.swing.JTextPane
import javax.swing.ScrollPaneConstants
import javax.swing.text.SimpleAttributeSet

open class StyledTextEditor : JPanel(BorderLayout()) {
    val normalTextStyle = SimpleAttributeSet()

    val textPane =
        JTextPane().also {
            it.isEditable = false
            it.font = SessionSwitcher.getApi().userInterface().currentEditorFont()
            it.editorKit = WrapEditorKit()
        }

    val textPaneContainer =
        JPanel().also {
            it.layout = BoxLayout(it, BoxLayout.PAGE_AXIS)
            it.border = BorderFactory.createEmptyBorder(4, 4, 4, 4)
            it.add(textPane, BorderLayout.CENTER)
        }

    val scrollPane =
        JScrollPane(textPaneContainer).also {
            it.horizontalScrollBarPolicy = ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER
            it.verticalScrollBarPolicy = ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS
        }

    class TextPaneComponentAdapter(
        val callback: () -> Unit,
    ) : ComponentAdapter() {
        override fun componentResized(e: ComponentEvent?) {
            callback()
        }
    }

    private fun updateComponentSize() {
        this.textPaneContainer.preferredSize = Dimension(this.scrollPane.width, textPane.preferredSize.height)
    }

    init {
        this.scrollPane.addComponentListener(TextPaneComponentAdapter { this.updateComponentSize() })
        this.add(scrollPane)
        SessionSwitcher.getApi().userInterface().applyThemeToComponent(this)
    }

    fun clear() {
        this.textPane.styledDocument.remove(0, this.textPane.styledDocument.length)
    }

    fun appendText(
        text: String,
        style: SimpleAttributeSet = this.normalTextStyle,
    ) {
        val cleanedText = text.replace("\r", "")
        val doc = this.textPane.styledDocument

        doc.insertString(doc.length, cleanedText, style)

        // Return caret to the top
        this.textPane.caretPosition = 0
        this.updateComponentSize()
    }
}
