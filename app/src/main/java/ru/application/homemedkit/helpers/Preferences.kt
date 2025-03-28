package ru.application.homemedkit.helpers

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
import ru.application.homemedkit.helpers.enums.Sorting
import ru.application.homemedkit.helpers.enums.Themes
import ru.application.homemedkit.helpers.extensions.getColorsFlow
import ru.application.homemedkit.helpers.extensions.getEnum
import ru.application.homemedkit.helpers.extensions.getSelectedLanguage
import ru.application.homemedkit.helpers.extensions.getThemeFlow
import ru.application.homemedkit.helpers.extensions.putEnum
import ru.application.homemedkit.receivers.AlarmSetter
import java.util.Locale

object Preferences : ViewModel() {

    private lateinit var preferences: SharedPreferences
    lateinit var theme: StateFlow<Themes>
    lateinit var dynamicColors: StateFlow<Boolean>

    fun getInstance(context: Context) {
        preferences = context.getSharedPreferences("${context.packageName}_preferences", MODE_PRIVATE)
        theme = preferences.getThemeFlow()
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(), Themes.SYSTEM)
        dynamicColors = preferences.getColorsFlow()
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(), false)
    }

    var sortingOrder: Sorting
        get() = preferences.getEnum(KEY_ORDER, Sorting.IN_NAME)
        set(value) = preferences.edit { putEnum(KEY_ORDER, value) }

    val imageFetch: Boolean
        get() = preferences.getBoolean(KEY_DOWNLOAD, true)

    val checkExpiration: Boolean
        get() = preferences.getBoolean(KEY_CHECK_EXP_DATE, false)

    val confirmExit: Boolean
        get() = preferences.getBoolean(KEY_CONFIRM_EXIT, true)

    var isFirstLaunch: Boolean
        get() = preferences.getBoolean(KEY_FIRST_LAUNCH_INTAKE, true)
        set(_) = preferences.edit { putBoolean(KEY_FIRST_LAUNCH_INTAKE, false) }

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

    fun setTheme(theme: Themes) = preferences.edit {
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