package sessionswitcher.ui

import java.awt.Component
import java.awt.event.MouseWheelEvent
import java.awt.event.MouseWheelListener
import javax.swing.JScrollBar
import javax.swing.JScrollPane

// From: https://stackoverflow.com/a/1379695
/**
 * A JScrollPane that will bubble a mouse wheel scroll event to the parent
 * JScrollPane if one exists when this scrollpane either tops out or bottoms out.
 */
class PDControlScrollPane(view: Component?) : JScrollPane(view) {
    init {
        addMouseWheelListener(PDMouseWheelListener())
    }

    internal inner class PDMouseWheelListener : MouseWheelListener {
        private val bar: JScrollBar = getVerticalScrollBar()
        private var previousValue = 0
        private var parentScrollPane: JScrollPane? = null
            get() {
                if (field == null) {
                    var parent: Component? = parent
                    while (parent !is JScrollPane && parent != null) {
                        parent = parent.parent
                    }
                    field = parent as JScrollPane?
                }
                return field
            }

        override fun mouseWheelMoved(e: MouseWheelEvent) {
            val parent = parentScrollPane
            if (parent != null) {
             /*
             * Only dispatch if we have reached top/bottom on previous scroll
             */
                if (e.wheelRotation < 0) {
                    if (bar.value == 0 && previousValue == 0) {
                        parent.dispatchEvent(cloneEvent(e))
                    }
                } else {
                    if (bar.value == max && previousValue == max) {
                        parent.dispatchEvent(cloneEvent(e))
                    }
                }
                previousValue = bar.value
            } else {
                removeMouseWheelListener(this)
            }
        }

        private val max: Int
            get() = bar.maximum - bar.visibleAmount

        private fun cloneEvent(e: MouseWheelEvent): MouseWheelEvent {
            return MouseWheelEvent(
                parentScrollPane, e.id, e
                    .getWhen(), e.modifiersEx, 1, 1, e
                    .clickCount, false, e.scrollType, e
                    .scrollAmount, e.wheelRotation
            )
        }
    }
}