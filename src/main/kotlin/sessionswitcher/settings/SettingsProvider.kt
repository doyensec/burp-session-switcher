package sessionswitcher.settings

interface SettingsProvider {
    fun getBoolean(key: String, store: SettingsItem.Store): Boolean?
    fun getInt(key: String, store: SettingsItem.Store): Int?
    fun getString(key: String, store: SettingsItem.Store): String?

    fun setBoolean(key: String, value: Boolean, store: SettingsItem.Store)
    fun setInt(key: String, value: Int, store: SettingsItem.Store)
    fun setString(key: String, value: String, store: SettingsItem.Store)

    fun deleteBoolean(key: String, store: SettingsItem.Store)
    fun deleteInt(key: String, store: SettingsItem.Store)
    fun deleteString(key: String, store: SettingsItem.Store)
}