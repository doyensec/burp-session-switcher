package sessionswitcher.utils

import java.awt.Component
import javax.swing.JComponent

fun JComponent.findComponentWithName(name: String): Component? {
    for (c in this.components) {
        if (c.name != null && c.name == name) return c
        if (c is JComponent) {
            val depthSearch = c.findComponentWithName(name)
            if (depthSearch != null) return depthSearch
        }
    }
    return null
}

