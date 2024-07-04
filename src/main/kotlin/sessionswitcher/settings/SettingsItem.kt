package sessionswitcher.settings

import sessionswitcher.Logger

abstract class SettingsItem<T>(val provider: SettingsProvider, val key: String, val description: String, val default: T) {
    enum class Scope {
        DEFAULT,
        GLOBAL,
        PROJECT,
        EFFECTIVE_GLOBAL, // always match except project
        EFFECTIVE, // always match
    }
    enum class Store {
        GLOBAL,
        PROJECT
    }
    abstract fun getType(): String
    abstract fun _get(key: String, store: Store): T?
    abstract fun _set(key: String, value: T, store: Store)

    public fun get(scope: Scope = Scope.EFFECTIVE): T {
        var output: T? = null
        var scopeLog: Scope = Scope.DEFAULT

        if (scope == Scope.PROJECT || scope == Scope.EFFECTIVE) {
            output = this._get(key, Store.PROJECT)
            scopeLog = Scope.PROJECT
        }
        if (output == null && (scope == Scope.GLOBAL || scope == Scope.EFFECTIVE || scope == Scope.EFFECTIVE_GLOBAL)) {
            output = this._get(key, Store.GLOBAL)
            scopeLog = Scope.GLOBAL
        }
        if (output == null) {
            output = this.default
            scopeLog = Scope.DEFAULT
        }

        var logStr = "Search $key (scope $scope): "
        logStr += if (output != null) {
            "$output (from $scopeLog)"
        } else {
            "not found"
        }
        Logger.debug(logStr)
        return output!!
    }

    fun set(value: T, store: Store = Store.PROJECT) {
        Logger.debug("Setting config value: $key=$value (store: $store)")
        this._set(key, value, store)
    }

    fun clear(key: String, store: Store = Store.PROJECT) {
        provider.deleteBoolean(key, store)
        provider.deleteInt(key, store)
        provider.deleteString(key, store)
    }
}

class BooleanSetting(provider: SettingsProvider, key: String, description: String, default: Boolean): SettingsItem<Boolean>(provider, key, description, default) {
    override fun getType(): String = "Boolean"
    override fun _get(key: String, store: Store): Boolean? = this.provider.getBoolean(key)
    override fun _set(key: String, value: Boolean, store: Store) = this.provider.setBoolean(key, value)
}

class IntSetting(provider: SettingsProvider, key: String, description: String, default: Int): SettingsItem<Int>(provider, key, description, default) {
    override fun getType(): String = "Int"
    override fun _get(key: String, store: Store): Int? = this.provider.getInt(key)
    override fun _set(key: String, value: Int, store: Store) = this.provider.setInt(key, value)
}

class StringSetting(provider: SettingsProvider, key: String, description: String, default: String): SettingsItem<String>(provider, key, description, default) {
    override fun getType(): String = "String"
    override fun _get(key: String, store: Store): String? = this.provider.getString(key)
    override fun _set(key: String, value: String, store: Store) = this.provider.setString(key, value)
}