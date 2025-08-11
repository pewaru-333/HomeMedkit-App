package ru.application.homemedkit.utils

import android.app.Activity
import android.content.Context
import android.content.Context.MODE_PRIVATE
import android.content.SharedPreferences
import android.os.Build
import android.os.LocaleList
import androidx.core.content.edit
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import ru.application.homemedkit.receivers.AlarmSetter
import ru.application.homemedkit.utils.enums.Page
import ru.application.homemedkit.utils.enums.Sorting
import ru.application.homemedkit.utils.enums.Theme
import ru.application.homemedkit.utils.extensions.getColorsFlow
import ru.application.homemedkit.utils.extensions.getEnum
import ru.application.homemedkit.utils.extensions.getSelectedLanguage
import ru.application.homemedkit.utils.extensions.getThemeFlow
import ru.application.homemedkit.utils.extensions.putEnum
import java.util.Locale

object Preferences : ViewModel() {

    private lateinit var preferences: SharedPreferences
    lateinit var theme: StateFlow<Theme>
    lateinit var dynamicColors: StateFlow<Boolean>

    fun getInstance(context: Context) {
        preferences = context.getSharedPreferences("${context.packageName}_preferences", MODE_PRIVATE)
        theme = preferences.getThemeFlow()
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(), Theme.SYSTEM)
        dynamicColors = preferences.getColorsFlow()
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(), false)
    }

    var sortingOrder: Sorting
        get() = preferences.getEnum(KEY_ORDER, Sorting.IN_NAME)
        set(value) = preferences.edit { putEnum(KEY_ORDER, value) }

    val kitsFilter: Set<Long>
        get() = preferences.getStringSet(KEY_KITS_FILTER, emptySet())
            .orEmpty()
            .mapNotNull(String::toLongOrNull)
            .toSet()

    val imageFetch: Boolean
        get() = preferences.getBoolean(KEY_DOWNLOAD, true)

    val checkExpiration: Boolean
        get() = preferences.getBoolean(KEY_CHECK_EXP_DATE, false)

    val confirmExit: Boolean
        get() = preferences.getBoolean(KEY_CONFIRM_EXIT, true)

    var startPage: Page
        get() = preferences.getEnum(KEY_START_PAGE, Page.MEDICINES)
        set(value) = preferences.edit { putEnum(KEY_START_PAGE, value) }

    var useAlarmClock: Boolean
        get() = preferences.getBoolean(KEY_USE_ALARM_CLOCK, false)
        set(value) = preferences.edit { putBoolean(KEY_USE_ALARM_CLOCK, value) }

    var isFirstLaunch: Boolean
        get() = preferences.getBoolean(KEY_FIRST_LAUNCH_INTAKE, true)
        set(_) = preferences.edit { putBoolean(KEY_FIRST_LAUNCH_INTAKE, false) }

    fun saveKitsFilter(kitsId: Set<Long>) = preferences.edit {
        putStringSet(KEY_KITS_FILTER, kitsId.map(Long::toString).toSet())
    }

    fun getLanguage(context: Context?) = if (context == null) Locale.ENGLISH.language
    else preferences.getString(KEY_LANGUAGE, context.getSelectedLanguage()) ?: Locale.ENGLISH.language

    fun setCheckExpDate(context: Context, check: Boolean) {
        AlarmSetter(context).checkExpiration(check)
        preferences.edit { putBoolean(KEY_CHECK_EXP_DATE, check) }
    }

    fun setLocale(context: Context, locale: String) {
        preferences.edit { putString(KEY_LANGUAGE, locale) }
        changeLanguage(context)
        (context as Activity).recreate()
    }

    fun setTheme(theme: Theme) = preferences.edit {
        putEnum(KEY_APP_THEME, theme)
    }

    fun changeLanguage(context: Context?) =
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) context
        else {
            val newLocale = Locale.forLanguageTag(getLanguage(context))
            Locale.setDefault(newLocale)

            val resources = context?.resources
            val configuration = resources?.configuration
            configuration?.setLocales(LocaleList(newLocale))

            configuration?.let { context.createConfigurationContext(it) } ?: context
        }
}