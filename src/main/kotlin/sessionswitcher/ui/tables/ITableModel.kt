package sessionswitcher.ui.tables

import java.util.*

interface ITableModel<T> {
    fun getAt(index: Int): Optional<T>
    fun refresh()
}