package com.notilog.ui.theme

import android.content.SharedPreferences

enum class ThemeMode(val storageValue: String) {
    SYSTEM("system"),
    LIGHT("light"),
    DARK("dark");

    companion object {
        fun fromStorageValue(value: String?): ThemeMode {
            return entries.firstOrNull { it.storageValue == value } ?: SYSTEM
        }
    }
}

object ThemePreferences {
    const val KEY_THEME_MODE = "theme_mode"

    fun getThemeMode(prefs: SharedPreferences): ThemeMode {
        return ThemeMode.fromStorageValue(prefs.getString(KEY_THEME_MODE, ThemeMode.SYSTEM.storageValue))
    }

    fun setThemeMode(prefs: SharedPreferences, mode: ThemeMode) {
        prefs.edit().putString(KEY_THEME_MODE, mode.storageValue).apply()
    }
}
