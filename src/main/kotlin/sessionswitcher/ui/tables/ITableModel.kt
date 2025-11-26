package sessionswitcher.ui.tables

import java.util.*

interface ITableModel<T> {
    public fun getAt(index: Int): Optional<T>
    public fun refresh()
}