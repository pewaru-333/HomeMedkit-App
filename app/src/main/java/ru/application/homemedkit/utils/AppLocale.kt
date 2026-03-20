package ru.application.homemedkit.utils

import android.app.LocaleManager
import android.content.Context
import android.os.Build
import android.os.LocaleList
import androidx.activity.ComponentActivity
import ru.application.homemedkit.utils.di.Preferences
import java.util.Locale

object AppLocale {
    fun wrapContext(context: Context): Context {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            return context
        }

        val locale = Preferences.language ?: return context

        val newLocale = Locale.forLanguageTag(locale)
        Locale.setDefault(newLocale)

        val configuration = context.resources.configuration
        configuration.setLocales(LocaleList(newLocale))

        return context.createConfigurationContext(configuration)
    }

    fun setLocale(activity: ComponentActivity, locale: String) {
        Preferences.setLanguage(locale)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            val localeManager = activity.getSystemService(LocaleManager::class.java)
            localeManager.applicationLocales = LocaleList.forLanguageTags(locale)
        } else {
            activity.recreate()
        }
    }
}