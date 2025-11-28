package sessionswitcher.requesteditor

import burp.api.montoya.ui.Theme
import sessionswitcher.SessionSwitcher
import java.awt.Color
import javax.swing.text.SimpleAttributeSet
import javax.swing.text.StyleConstants

interface ColorScheme {
    val updatedElement: Color
    val addedElement: Color
    val headerName: Color
    val cookieName: Color
    val cookieValue: Color
}

fun Color.asAttributeSet(): SimpleAttributeSet = SimpleAttributeSet().also { StyleConstants.setForeground(it, this) }

object EditorColors : ColorScheme {
    object DarkModeScheme : ColorScheme {
        override val updatedElement: Color = Color.ORANGE
        override val addedElement: Color = Color.decode("#00e33c")
        override val headerName: Color = Color.decode("#d6e7f8")
        override val cookieName: Color = Color.decode("#b9c6f5")
        override val cookieValue: Color = Color.decode("#aac163")
    }

    object LightModeScheme : ColorScheme {
        override val updatedElement: Color = Color.decode("#ba7c00")
        override val addedElement: Color = Color.decode("#017a22")
        override val headerName: Color = Color.decode("#191b7f")
        override val cookieName: Color = Color.decode("#0108bc")
        override val cookieValue: Color = Color.decode("#932618")
    }

    val currentScheme: ColorScheme
        get() = if (SessionSwitcher.getApi().userInterface()
                .currentTheme() == Theme.DARK
        ) DarkModeScheme else LightModeScheme

    override val updatedElement: Color
        get() = currentScheme.updatedElement
    override val addedElement: Color
        get() = currentScheme.addedElement
    override val headerName: Color
        get() = currentScheme.headerName
    override val cookieName: Color
        get() = currentScheme.cookieName
    override val cookieValue: Color
        get() = currentScheme.cookieValue

}