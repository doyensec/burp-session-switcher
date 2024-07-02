package sessions.settings

import sessions.Logger

abstract class SettingsItem<T>(val key: String, val description: String, val default: T) {

    protected val globalStore = Settings.globalStore
    protected val projectStore = Settings.projectStore
    enum class Scope {
        DEFAULT,
        GLOBAL,
        PROJECT,
        EFFECTIVE_GLOBAL, // always match except project
        EFFECTIVE, // always match
    }
    abstract fun getType(): String
    abstract fun _getProjectStore(key: String): T?
    abstract fun _getGlobalStore(key: String): T?
    abstract fun _setProjectStore(key: String, value: T)
    abstract fun _setGlobalStore(key: String, value: T)
    public fun get(scope: Scope = Scope.EFFECTIVE): T {
        var output: T? = null
        var scopeLog: Scope = Scope.DEFAULT

        if (scope == Scope.PROJECT || scope == Scope.EFFECTIVE) {
            output = this._getProjectStore(key)
            scopeLog = Scope.PROJECT
        }
        if (output == null && (scope == Scope.GLOBAL || scope == Scope.EFFECTIVE || scope == Scope.EFFECTIVE_GLOBAL)) {
            output = this._getGlobalStore(key)
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

    fun set(value: T, scope: Scope = Scope.PROJECT) {
        Logger.debug("Setting config value: $key=$value (scope: $scope)")
        when (scope) {
            Scope.PROJECT -> this._setProjectStore(key, value)
            Scope.GLOBAL -> this._setGlobalStore(key, value)
            else -> throw Exception("Invalid scope provided to set(): $scope")
        }
    }

    fun clear(key: String, scope: Scope = Scope.PROJECT) {
        when (scope) {
            Scope.PROJECT -> {
                projectStore.deleteBoolean(key)
                projectStore.deleteInteger(key)
                projectStore.deleteString(key)
            }

            Scope.GLOBAL -> {
                globalStore.deleteBoolean(key)
                globalStore.deleteInteger(key)
                globalStore.deleteString(key)
            }

            else -> throw Exception("Invalid scope provided to delete(): $scope")
        }
    }
}

class BooleanSetting(key: String, description: String, default: Boolean): SettingsItem<Boolean>(key, description, default) {
    init {
        Settings.registerSettingItem(this)
    }
    override fun getType(): String = "Boolean"
    override fun _getProjectStore(key: String): Boolean? = this.projectStore.getBoolean(key)
    override fun _getGlobalStore(key: String): Boolean? = this.globalStore.getBoolean(key)
    override fun _setGlobalStore(key: String, value: Boolean) = this.globalStore.setBoolean(key, value)
    override fun _setProjectStore(key: String, value: Boolean) = this.projectStore.setBoolean(key, value)
}

class IntSetting(key: String, description: String, default: Int): SettingsItem<Int>(key, description, default) {
    init {
        Settings.registerSettingItem(this)
    }
    override fun getType(): String = "Int"
    override fun _getProjectStore(key: String): Int? = this.projectStore.getInteger(key)
    override fun _getGlobalStore(key: String): Int? = this.globalStore.getInteger(key)
    override fun _setGlobalStore(key: String, value: Int) = this.globalStore.setInteger(key, value)
    override fun _setProjectStore(key: String, value: Int) = this.projectStore.setInteger(key, value)
}

class StringSetting(key: String, description: String, default: String): SettingsItem<String>(key, description, default) {
    init {
        Settings.registerSettingItem(this)
    }
    override fun getType(): String = "String"
    override fun _getProjectStore(key: String): String? = this.projectStore.getString(key)
    override fun _getGlobalStore(key: String): String? = this.globalStore.getString(key)
    override fun _setGlobalStore(key: String, value: String) = this.globalStore.setString(key, value)
    override fun _setProjectStore(key: String, value: String) = this.projectStore.setString(key, value)
}