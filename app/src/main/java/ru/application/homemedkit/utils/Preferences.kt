package ru.application.homemedkit.utils

import android.content.Context
import android.content.Context.MODE_PRIVATE
import android.os.Build
import android.os.LocaleList
import androidx.core.content.edit
import kotlinx.coroutines.flow.Flow
import ru.application.homemedkit.network.models.auth.Token
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

class Preferences internal constructor(context: Context) {
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

    val useVibrationScan: Boolean
        get() = preferences.getBoolean(KEY_USE_VIBRATION_SCAN, false)

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

    val wasDataImported: Boolean
        get() = preferences.getBoolean(KEY_IMPORTED_DATA, false)

    val codeVerifier: String?
        get() = preferences.getString(KEY_CODE_VERIFIER, null)

    val authIsYandex: Boolean
        get() = preferences.getBoolean(KEY_AUTH_IS_YANDEX, false)

    val lastSyncMillis: Long
        get() = preferences.getLong(KEY_LAST_SYNC_MILLIS, -1L)

    val lastSyncMillisFlow: Flow<Long>
        get() = preferences.getFlow(KEY_LAST_SYNC_MILLIS, -1L)

    val isAutoSyncEnabled: Boolean
        get() = preferences.getBoolean(KEY_AUTO_SYNC_ENABLED, false)

    val token: Token?
        get() {
            val accessToken = preferences.getString(KEY_ACCESS_TOKEN, BLANK)
            val refreshToken = preferences.getString(KEY_REFRESH_TOKEN, BLANK)

            if (accessToken == null || refreshToken == null)
                return null

            if (accessToken.isBlank() || refreshToken.isBlank())
                return null

            return Token(accessToken, refreshToken)
        }

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

    fun addImportedKey() = preferences.edit(commit = true) {
        putBoolean(KEY_IMPORTED_DATA, true)
    }

    fun removeImportedKey() = preferences.edit {
        remove(KEY_IMPORTED_DATA)
    }

    fun updateSyncMillis(millis: Long = System.currentTimeMillis()) = preferences.edit {
        putLong(KEY_LAST_SYNC_MILLIS, millis)
    }

    fun setAutoSync(enabled: Boolean = false) = preferences.edit {
        putBoolean(KEY_AUTO_SYNC_ENABLED, enabled)
    }

    fun setTheme(theme: Theme) = preferences.edit {
        putEnum(KEY_APP_THEME, theme)
    }

    fun saveCodeVerifier(verifier: String) = preferences.edit {
        putString(KEY_CODE_VERIFIER, verifier)
    }

    fun removeCodeVerifier() = preferences.edit {
        remove(KEY_CODE_VERIFIER)
    }

    fun saveToken(token: Token) {
        preferences.edit(commit = true) {
            putString(KEY_ACCESS_TOKEN, token.accessToken)
            putString(KEY_REFRESH_TOKEN, token.refreshToken)
        }
    }

    fun setAuthYandex(flag: Boolean) = preferences.edit { putBoolean(KEY_AUTH_IS_YANDEX, flag) }

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