package sessionswitcher.ui.editor

import sessionswitcher.SessionSwitcher
import sessionswitcher.ui.misc.WrapEditorKit
import java.awt.BorderLayout
import java.awt.Dimension
import java.awt.event.ComponentAdapter
import java.awt.event.ComponentEvent
import javax.swing.*
import javax.swing.text.SimpleAttributeSet


open class StyledTextEditor: JPanel(BorderLayout()) {

    public val normalTextStyle = SimpleAttributeSet()
    data class TextRange(val start: Int, val length: Int)

    protected val textPane = JTextPane().also {
        it.isEditable = false
        it.font = SessionSwitcher.getApi().userInterface().currentEditorFont()
        it.editorKit = WrapEditorKit()
    }

    private val textPaneContainer = JPanel().also {
        it.layout = BoxLayout(it, BoxLayout.PAGE_AXIS)
        it.border = BorderFactory.createEmptyBorder(4, 4, 4, 4)
        it.add(textPane, BorderLayout.CENTER)
    }

    private val scrollPane = JScrollPane(textPaneContainer).also {
        it.horizontalScrollBarPolicy = ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER
        it.verticalScrollBarPolicy = ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS
    }

    class TextPaneComponentAdapter(val panel: JPanel, val textPane: JTextPane): ComponentAdapter() {
        override fun componentResized(e: ComponentEvent?) {
            this.panel.preferredSize = Dimension(e!!.component.width, textPane.height)
        }
    }

    init {
        this.scrollPane.addComponentListener(TextPaneComponentAdapter(this.textPaneContainer, this.textPane))
        this.add(scrollPane)
        SessionSwitcher.getApi().userInterface().applyThemeToComponent(this)
    }

    fun clear() {
        this.textPane.styledDocument.remove(0, this.textPane.styledDocument.length)
    }

    fun highlightRanges(vararg highlightRanges: TextRange, highlightedTextStyle: SimpleAttributeSet) {
        val doc = this.textPane.styledDocument
        for (range in highlightRanges) {
            doc.setCharacterAttributes(range.start, range.length, highlightedTextStyle, true)
        }
    }

    fun appendText(text: String, style: SimpleAttributeSet = this.normalTextStyle) {
        val cleanedText = text.replace("\r", "")
        val doc = this.textPane.styledDocument

        doc.insertString(doc.length, cleanedText, style)

        // Return caret to the top
        this.textPane.caretPosition = 0
    }
}