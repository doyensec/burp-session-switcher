package sessionswitcher.ui.misc

import burp.api.montoya.ui.Theme
import sessionswitcher.SessionSwitcher
import java.awt.BorderLayout
import java.awt.Color
import java.awt.Dimension
import java.awt.event.ComponentAdapter
import java.awt.event.ComponentEvent
import javax.swing.*
import javax.swing.text.SimpleAttributeSet
import javax.swing.text.StyleConstants


class HighlightRequestEditor: JPanel(BorderLayout()) {

    private val normalTextStyle = SimpleAttributeSet()
    private val highlightedTextStyle = SimpleAttributeSet().also {
        if (SessionSwitcher.getApi().userInterface().currentTheme() == Theme.DARK) {
            StyleConstants.setForeground(it, Color.ORANGE)
        } else {
            StyleConstants.setForeground(it, Color.RED)
        }
    }
    data class TextRange(val start: Int, val end: Int)

    private val textPane = JTextPane().also {
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

    private fun normalizeRanges(ranges: Array<out TextRange>): Collection<TextRange> {
        // Order by start
        val output = ranges.sortedBy { it.start }

        //Make sure there are no overlaps
        for (i in 0..<output.size-1) {
            if (output[i].start > output[i+1].end) {
                // Overlap detected, throw exception
                throw Exception("Highlight ranges overlap detected")
            }
        }
        return output
    }

    fun setText(text: String, vararg highlightRanges: TextRange) {
        val cleanedText = text.replace("\r", "")
        val doc = this.textPane.styledDocument
        val ranges = this.normalizeRanges(highlightRanges)

        this.textPane.text = cleanedText
        for (range in ranges) {
            doc.setCharacterAttributes(range.start, range.end-range.start, highlightedTextStyle, true)
        }
        // Return caret to the top
        this.textPane.caretPosition = 0
    }

    fun highlightRanges(vararg highlightRanges: TextRange) {
        val doc = this.textPane.styledDocument
        val ranges = this.normalizeRanges(highlightRanges)
        for (range in ranges) {
            doc.setCharacterAttributes(range.start, range.end-range.start, highlightedTextStyle, true)
        }
    }

    fun appendText(text: String, highlighted: Boolean = false) {
        val cleanedText = text.replace("\r", "")
        val doc = this.textPane.styledDocument

        doc.insertString(doc.length, cleanedText, if (highlighted) this.highlightedTextStyle else this.normalTextStyle )

        // Return caret to the top
        this.textPane.caretPosition = 0
    }
}