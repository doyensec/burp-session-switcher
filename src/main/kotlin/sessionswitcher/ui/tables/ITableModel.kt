package sessionswitcher.ui.tables

import java.util.Optional

interface ITableModel<T> {
    fun getAt(index: Int): Optional<T>

    fun refresh()
}
