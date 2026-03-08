package sessionswitcher.requesteditor

import javax.swing.text.AbstractDocument
import javax.swing.text.BoxView
import javax.swing.text.ComponentView
import javax.swing.text.Element
import javax.swing.text.IconView
import javax.swing.text.LabelView
import javax.swing.text.ParagraphView
import javax.swing.text.StyleConstants
import javax.swing.text.StyledEditorKit
import javax.swing.text.View
import javax.swing.text.ViewFactory

class WrapEditorKit : StyledEditorKit() {
    private val defaultFactory = WrapFactory()

    override fun getViewFactory(): ViewFactory = defaultFactory

    internal class WrapLabelView(
        elem: Element?,
    ) : LabelView(elem) {
        override fun getMinimumSpan(axis: Int): Float =
            when (axis) {
                X_AXIS -> 0F
                Y_AXIS -> super.getMinimumSpan(axis)
                else -> throw IllegalArgumentException("Invalid axis: $axis")
            }
    }

    class WrapFactory : ViewFactory {
        override fun create(elem: Element?): View {
            val kind = elem!!.name
            if (kind != null) {
                when (kind) {
                    AbstractDocument.ContentElementName -> {
                        return WrapLabelView(elem)
                    }

                    AbstractDocument.ParagraphElementName -> {
                        return ParagraphView(elem)
                    }

                    AbstractDocument.SectionElementName -> {
                        return BoxView(elem, View.Y_AXIS)
                    }

                    StyleConstants.ComponentElementName -> {
                        return ComponentView(elem)
                    }

                    StyleConstants.IconElementName -> {
                        return IconView(elem)
                    }
                }
            }
            return LabelView(elem)
        }
    }
}
