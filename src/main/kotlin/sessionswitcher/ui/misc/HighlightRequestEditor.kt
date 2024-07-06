package sessionswitcher.ui.misc

import sessionswitcher.SessionSwitcher
import java.awt.BorderLayout
import java.awt.Color
import javax.swing.JPanel
import javax.swing.JScrollPane
import javax.swing.JTextPane
import javax.swing.ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER
import javax.swing.ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS
import javax.swing.text.SimpleAttributeSet
import javax.swing.text.StyleConstants

class HighlightRequestEditor: JPanel(BorderLayout()) {
    data class TextRange(val start: Int, val end: Int)

    private val textPane = JTextPane().also {
        it.isEditable = false
        it.font = SessionSwitcher.getApi().userInterface().currentEditorFont()
    }

    private val normalTextStyle = SimpleAttributeSet()
    private val highlightedTextStyle = SimpleAttributeSet().also {
        StyleConstants.setForeground(it, Color.ORANGE)
    }

    init {
        val scrollPane = JScrollPane(textPane)
        scrollPane.verticalScrollBar.unitIncrement = 2
        scrollPane.horizontalScrollBarPolicy = HORIZONTAL_SCROLLBAR_NEVER
        scrollPane.verticalScrollBarPolicy = VERTICAL_SCROLLBAR_ALWAYS
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