package sessionswitcher.settings

import burp.api.montoya.MontoyaApi
import burp.api.montoya.persistence.PersistedObject
import burp.api.montoya.persistence.Preferences

class BurpSettingsProvider(montoyaApi: MontoyaApi) : SettingsProvider {

    val globalStore: Preferences = montoyaApi.persistence().preferences()
    val projectStore: PersistedObject = montoyaApi.persistence().extensionData()
    override fun getBoolean(key: String, store: SettingsItem.Store): Boolean? {
        return when (store) {
            SettingsItem.Store.GLOBAL -> this.globalStore.getBoolean(key)
            SettingsItem.Store.PROJECT -> this.projectStore.getBoolean(key)
        }
    }

    override fun getInt(key: String, store: SettingsItem.Store): Int? {
        return when (store) {
            SettingsItem.Store.GLOBAL -> this.globalStore.getInteger(key)
            SettingsItem.Store.PROJECT -> this.projectStore.getInteger(key)
        }
    }

    override fun getString(key: String, store: SettingsItem.Store): String? {
        return when (store) {
            SettingsItem.Store.GLOBAL -> this.globalStore.getString(key)
            SettingsItem.Store.PROJECT -> this.projectStore.getString(key)
        }
    }

    override fun setBoolean(key: String, value: Boolean, store: SettingsItem.Store) {
        when (store) {
            SettingsItem.Store.GLOBAL -> this.globalStore.setBoolean(key, value)
            SettingsItem.Store.PROJECT -> this.projectStore.setBoolean(key, value)
        }
    }

    override fun setInt(key: String, value: Int, store: SettingsItem.Store) {
        when (store) {
            SettingsItem.Store.GLOBAL -> this.globalStore.setInteger(key, value)
            SettingsItem.Store.PROJECT -> this.projectStore.setInteger(key, value)
        }
    }

    override fun setString(key: String, value: String, store: SettingsItem.Store) {
        when (store) {
            SettingsItem.Store.GLOBAL -> this.globalStore.setString(key, value)
            SettingsItem.Store.PROJECT -> this.projectStore.setString(key, value)
        }
    }

    override fun deleteBoolean(key: String, store: SettingsItem.Store) {
        when (store) {
            SettingsItem.Store.GLOBAL -> this.globalStore.deleteBoolean(key)
            SettingsItem.Store.PROJECT -> this.projectStore.deleteBoolean(key)
        }
    }

    override fun deleteInt(key: String, store: SettingsItem.Store) {
        when (store) {
            SettingsItem.Store.GLOBAL -> this.globalStore.deleteInteger(key)
            SettingsItem.Store.PROJECT -> this.projectStore.deleteInteger(key)
        }
    }

    override fun deleteString(key: String, store: SettingsItem.Store) {
        when (store) {
            SettingsItem.Store.GLOBAL -> this.globalStore.deleteString(key)
            SettingsItem.Store.PROJECT -> this.projectStore.deleteString(key)
        }
    }
}