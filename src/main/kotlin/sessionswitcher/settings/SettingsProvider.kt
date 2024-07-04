package sessionswitcher.settings

interface SettingsProvider {
    fun getBoolean(key: String, store: SettingsItem.Store = SettingsItem.Store.PROJECT): Boolean?
    fun getInt(key: String, store: SettingsItem.Store = SettingsItem.Store.PROJECT): Int?
    fun getString(key: String, store: SettingsItem.Store = SettingsItem.Store.PROJECT): String?

    fun setBoolean(key: String, value: Boolean, store: SettingsItem.Store = SettingsItem.Store.PROJECT)
    fun setInt(key: String, value: Int, store: SettingsItem.Store = SettingsItem.Store.PROJECT)
    fun setString(key: String, value: String, store: SettingsItem.Store = SettingsItem.Store.PROJECT)

    fun deleteBoolean(key: String, store: SettingsItem.Store = SettingsItem.Store.PROJECT)
    fun deleteInt(key: String, store: SettingsItem.Store = SettingsItem.Store.PROJECT)
    fun deleteString(key: String, store: SettingsItem.Store = SettingsItem.Store.PROJECT)
}