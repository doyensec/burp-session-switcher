package sessionswitcher.ui

import burp.api.montoya.ui.Theme
import sessionswitcher.SessionSwitcher
import java.awt.*
import javax.swing.*
import kotlin.math.min


class Label(text: String, bold: Boolean = false, big: Boolean = false, relativeSize: Double = 0.0) : JLabel(text) {
    init {
        isOpaque = false
        if (big) {
            this.font = this.font.deriveFont(this.font.size + 5.0.toFloat()).deriveFont(Font.BOLD)
        } else if (bold) {
            this.font = this.font.deriveFont(Font.BOLD)
        }

        if (relativeSize != 0.0) {
            this.font = this.font.deriveFont(this.font.size + relativeSize.toFloat())
        }
    }
}

/* Create a window (JFrame) with reasonable defaults. */
open class Window(windowTitle: String) : JFrame(windowTitle) {
    init {
        this.defaultCloseOperation = DISPOSE_ON_CLOSE
        this.layout = BorderLayout()
    }

    open fun autoSize() {
        // Pack the window to fit its content
        this.pack()

        val preferredSize = this.preferredSize
        val screenHeight = Toolkit.getDefaultToolkit().screenSize.height
        val reasonableHeight = min(preferredSize.height, screenHeight - 50)
        this.preferredSize = Dimension(preferredSize.width, reasonableHeight)

        // Set the maximum size of the frame to match its content
        this.maximumSize = Dimension(preferredSize.width, preferredSize.height)

        // Set the minimum size to something reasonable as well
        this.minimumSize = Dimension(preferredSize.width, 400)

        this.setLocationRelativeTo(SessionSwitcher.getApi().userInterface().swingUtils().suiteFrame())
    }
}

class ButtonPrimary(label: String) : JButton(label) {
    init {
        this.foreground = Color.WHITE
        this.background = getAccentColor()
        this.font = this.font.deriveFont(Font.BOLD)
        this.isBorderPainted = false
    }

    private fun getAccentColor(): Color {
        return if (SessionSwitcher.getApi().userInterface().currentTheme() == Theme.DARK) {
            Color(201, 110, 59)
        } else {
            Color(236, 98, 43)
        }
    }
}

class UISection(val sectionTitle: String, val description: String?, vararg elements: Component?) : JPanel() {
    private val gap = 10

    init {
        val innerBox = JPanel().also {
            it.layout = BoxLayout(it, BoxLayout.Y_AXIS)
        }

        for (e in elements) {
            if (e != null) {
                innerBox.add(JPanel(BorderLayout()).also { it.add(e) })
            } else {
                innerBox.add(Box.createVerticalStrut(gap))
            }
        }

        // Set layout
        val outerBox = JPanel().also {
            it.layout = BoxLayout(it, BoxLayout.Y_AXIS)
        }

        this.layout = BorderLayout()
        this.border = BorderFactory.createEmptyBorder(gap, gap, gap, gap)

        // Add components
        outerBox.add(JPanel(BorderLayout()).also { it.add(Label(sectionTitle, big = true)) })
        if (!description.isNullOrEmpty()) {
            outerBox.add(Box.createVerticalStrut(4))
            outerBox.add(JPanel(BorderLayout()).also { it.add(Label(description)) })
        }
        outerBox.add(Box.createVerticalStrut(gap))
        outerBox.add(JPanel(BorderLayout()).also { it.add(innerBox) })
        outerBox.add(Box.createVerticalStrut(gap))
        this.add(outerBox)
    }
}

fun JTable.withScrollPane(rows: Int = 15): JScrollPane {
    val scrollPane = PDControlScrollPane(this)
    if (rows > 0) {
        val tableHeight = this.rowHeight * rows
        scrollPane.preferredSize = Dimension(scrollPane.preferredSize.width, tableHeight)
    }
    this.fillsViewportHeight = true
    return scrollPane
}


class TextFieldWithPlaceholder(text: String, var placeholder: String) : JTextField(text) {
    override fun paintComponent(g: Graphics?) {
        super.paintComponent(g)
        if (this.placeholder.isEmpty()) return
        if (this.text.isNotBlank()) return

        val g2d = g as Graphics2D
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON)
        g2d.color = disabledTextColor
        g2d.drawString(this.placeholder, this.insets.left, this.getFontMetrics(font).maxAscent + insets.top)
    }
}