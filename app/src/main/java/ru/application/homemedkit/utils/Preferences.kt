package ru.application.homemedkit.utils

import android.content.Context
import android.content.Context.MODE_PRIVATE
import android.os.Build
import android.os.LocaleList
import androidx.core.content.edit
import kotlinx.coroutines.flow.Flow
import ru.application.homemedkit.utils.di.AlarmManager
import ru.application.homemedkit.utils.enums.Page
import ru.application.homemedkit.utils.enums.Sorting
import ru.application.homemedkit.utils.enums.Theme
import ru.application.homemedkit.utils.extensions.getActivity
import ru.application.homemedkit.utils.extensions.getEnum
import ru.application.homemedkit.utils.extensions.getEnumFlow
import ru.application.homemedkit.utils.extensions.getFlow
import ru.application.homemedkit.utils.extensions.getSelectedLanguage
import ru.application.homemedkit.utils.extensions.putEnum
import java.util.Locale

class Preferences(context: Context) {
    private val preferences = context.getSharedPreferences("${context.packageName}_preferences", MODE_PRIVATE)

    val kitsFilter: Set<Long>
        get() = preferences.getStringSet(KEY_KITS_FILTER, emptySet())
            .orEmpty()
            .mapNotNull(String::toLongOrNull)
            .toSet()

    val startPage: Page
        get() = preferences.getEnum(KEY_START_PAGE, Page.MEDICINES)

    val startPageFlow: Flow<Page>
        get() = preferences.getEnumFlow(KEY_START_PAGE, Page.MEDICINES)

    val sortingOrder: Sorting
        get() = preferences.getEnum(KEY_ORDER, Sorting.IN_NAME)

    val sortingOrderFlow: Flow<Sorting>
        get() = preferences.getEnumFlow(KEY_ORDER, Sorting.IN_NAME)

    val confirmExit: Boolean
        get() = preferences.getBoolean(KEY_CONFIRM_EXIT, true)

    val imageFetch: Boolean
        get() = preferences.getBoolean(KEY_DOWNLOAD, true)

    val checkExpiration: Flow<Boolean>
        get() = preferences.getFlow(KEY_CHECK_EXP_DATE, false)

    val useAlarmClock: Boolean
        get() = preferences.getBoolean(KEY_USE_ALARM_CLOCK, false)

    val theme: Flow<Theme>
        get() = preferences.getEnumFlow(KEY_APP_THEME, Theme.SYSTEM)

    val dynamicColors: Flow<Boolean>
        get() = preferences.getFlow(KEY_DYNAMIC_COLOR, false)

    val isFirstLaunch: Boolean
        get() = preferences.getBoolean(KEY_FIRST_LAUNCH_INTAKE, true)

    fun setStartPage(page: Page) = preferences.edit { putEnum(KEY_START_PAGE, page) }

    fun setSortingType(type: Sorting) = preferences.edit { putEnum(KEY_ORDER, type) }

    fun setHasLaunched() = preferences.edit { putBoolean(KEY_FIRST_LAUNCH_INTAKE, false) }

    fun saveKitsFilter(kitsId: Set<Long>) = preferences.edit {
        putStringSet(KEY_KITS_FILTER, kitsId.map(Long::toString).toSet())
    }

    fun getLanguage(context: Context) = preferences.getString(KEY_LANGUAGE, context.getSelectedLanguage()) ?: Locale.ENGLISH.language

    fun setCheckExpDate(check: Boolean) {
        preferences.edit { putBoolean(KEY_CHECK_EXP_DATE, check) }
        AlarmManager.checkExpiration(check)
    }

    fun setLocale(context: Context, locale: String) {
        preferences.edit { putString(KEY_LANGUAGE, locale) }
        changeLanguage(context)
        context.getActivity()?.recreate()
    }

    fun setTheme(theme: Theme) = preferences.edit {
        putEnum(KEY_APP_THEME, theme)
    }

    fun changeLanguage(context: Context) =
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) context
        else {
            val newLocale = Locale.forLanguageTag(getLanguage(context))
            Locale.setDefault(newLocale)

            val configuration = context.resources?.configuration
            configuration?.setLocales(LocaleList(newLocale))

            configuration?.let { context.createConfigurationContext(it) } ?: context
        }

    companion object {
        @Volatile
        private var INSTANCE: Preferences? = null

        fun getInstance(context: Context) =
            INSTANCE ?: synchronized(this) { Preferences(context) }.also { INSTANCE = it }
    }
}