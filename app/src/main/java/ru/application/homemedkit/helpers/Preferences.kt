package ru.application.homemedkit.helpers

import android.app.Activity
import android.app.LocaleManager
import android.content.Context
import android.content.SharedPreferences
import android.os.Build.VERSION.SDK_INT
import android.os.Build.VERSION_CODES.TIRAMISU
import android.os.LocaleList
import androidx.appcompat.app.AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM
import androidx.appcompat.app.AppCompatDelegate.MODE_NIGHT_NO
import androidx.appcompat.app.AppCompatDelegate.MODE_NIGHT_YES
import androidx.appcompat.app.AppCompatDelegate.setApplicationLocales
import androidx.appcompat.app.AppCompatDelegate.setDefaultNightMode
import androidx.core.os.LocaleListCompat
import ru.application.homemedkit.receivers.AlarmSetter

object Preferences {

    private lateinit var preferences: SharedPreferences

    fun getInstance(context: Context) {
        preferences =
            context.getSharedPreferences("${context.packageName}_preferences", Context.MODE_PRIVATE)
    }

    fun getHomePage() = preferences.getString(KEY_FRAGMENT, MENUS[0]) ?: MENUS[0]
    fun getSortingOrder() = preferences.getString(KEY_ORDER, SORTING[0]) ?: SORTING[0]
    fun getLastKit() = preferences.getLong(KEY_LAST_KIT, 0L)
    fun getMedCompactView() = preferences.getBoolean(KEY_MED_COMPACT_VIEW, false)
    fun getDownloadNeeded() = preferences.getBoolean(KEY_DOWNLOAD, false)
    fun getCheckExpDate() = preferences.getBoolean(CHECK_EXP_DATE, false)
    fun getLightPeriod() = preferences.getBoolean(KEY_LIGHT_PERIOD, true)
    fun getLanguage() = preferences.getString(KEY_LANGUAGE, LANGUAGES[0]) ?: LANGUAGES[0]
    fun getAppTheme() = preferences.getString(KEY_APP_THEME, THEMES[0]) ?: THEMES[0]
    fun getDynamicColors() = preferences.getBoolean(KEY_DYNAMIC_COLOR, false)
    fun setLastKit(kitId: Long?) = preferences.edit().putLong(KEY_LAST_KIT, kitId ?: 0L).apply()
    fun setCheckExpDate(context: Context, check: Boolean) =
        AlarmSetter(context).checkExpiration(check)
            .also { preferences.edit().putBoolean(CHECK_EXP_DATE, check).apply() }

    fun setLanguage(context: Context, language: String) = when {
        SDK_INT >= TIRAMISU -> context.getSystemService(LocaleManager::class.java)
            .applicationLocales = LocaleList.forLanguageTags(language)

        else -> setApplicationLocales(LocaleListCompat.forLanguageTags(language))
    }.also { preferences.edit().putString(KEY_LANGUAGE, language).apply() }

    fun setTheme(theme: String) = preferences.edit().putString(KEY_APP_THEME, theme).apply().also {
        when (theme) {
            THEMES[1] -> setDefaultNightMode(MODE_NIGHT_NO)
            THEMES[2] -> setDefaultNightMode(MODE_NIGHT_YES)
            else -> setDefaultNightMode(MODE_NIGHT_FOLLOW_SYSTEM)
        }
    }

    fun setDynamicColors(context: Context, enabled: Boolean) =
        preferences.edit().putBoolean(KEY_DYNAMIC_COLOR, enabled).apply()
            .also { (context as Activity).recreate() }
}