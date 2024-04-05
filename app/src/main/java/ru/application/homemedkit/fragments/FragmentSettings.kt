package ru.application.homemedkit.fragments

import android.content.Context
import android.content.SharedPreferences
import android.content.SharedPreferences.OnSharedPreferenceChangeListener
import android.os.Bundle
import androidx.appcompat.app.AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM
import androidx.appcompat.app.AppCompatDelegate.MODE_NIGHT_NO
import androidx.appcompat.app.AppCompatDelegate.MODE_NIGHT_YES
import androidx.appcompat.app.AppCompatDelegate.setApplicationLocales
import androidx.appcompat.app.AppCompatDelegate.setDefaultNightMode
import androidx.core.os.LocaleListCompat.forLanguageTags
import androidx.preference.PreferenceFragmentCompat
import androidx.preference.PreferenceManager
import ru.application.homemedkit.R
import ru.application.homemedkit.activities.MainActivity
import ru.application.homemedkit.activities.MainActivity.Companion.preferences
import ru.application.homemedkit.helpers.CHECK_EXP_DATE
import ru.application.homemedkit.helpers.KEY_APP_THEME
import ru.application.homemedkit.helpers.KEY_DOWNLOAD
import ru.application.homemedkit.helpers.KEY_FRAGMENT
import ru.application.homemedkit.helpers.KEY_LANGUAGE
import ru.application.homemedkit.helpers.KEY_LIGHT_PERIOD
import ru.application.homemedkit.helpers.KEY_ORDER
import ru.application.homemedkit.helpers.PAGES
import ru.application.homemedkit.helpers.SETTINGS_CHANGED
import ru.application.homemedkit.helpers.SORTING
import ru.application.homemedkit.helpers.THEMES

class FragmentSettings : PreferenceFragmentCompat(), OnSharedPreferenceChangeListener {

    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        setPreferencesFromResource(R.xml.preferences, rootKey)
    }

    override fun onSharedPreferenceChanged(sharedPreferences: SharedPreferences?, key: String?) {
        sharedPreferences?.let { preferences ->
            key?.let { string -> val value = preferences.all[string]
                when (string) {
                    KEY_LIGHT_PERIOD -> {
                        preferences.edit().putBoolean(KEY_LIGHT_PERIOD, value as Boolean).apply()
                    }

                    KEY_ORDER -> {
                        preferences.edit().putString(KEY_ORDER, string).apply()
                    }

                    KEY_DOWNLOAD -> {
                        preferences.edit().putBoolean(KEY_DOWNLOAD, value as Boolean).apply()
                    }

                    CHECK_EXP_DATE -> {
                        preferences.edit().putBoolean(CHECK_EXP_DATE, value as Boolean).apply()
                        (requireActivity() as MainActivity).setExpirationChecker()
                    }

                    KEY_LANGUAGE -> {
                        preferences.edit().putString(KEY_LANGUAGE, value.toString()).apply()
                        activity?.intent?.putExtra(SETTINGS_CHANGED, true)
                        setApplicationLocales(forLanguageTags(value.toString()))
                    }

                    KEY_APP_THEME -> {
                        preferences.edit().putString(KEY_APP_THEME, value.toString()).apply()
                        activity?.intent?.putExtra(SETTINGS_CHANGED, true)

                        when (value) {
                            THEMES[1] -> setDefaultNightMode(MODE_NIGHT_NO)
                            THEMES[2] -> setDefaultNightMode(MODE_NIGHT_YES)
                            else -> setDefaultNightMode(MODE_NIGHT_FOLLOW_SYSTEM)
                        }
                    }

                    else -> {}
                }
            }
        }
    }

    fun getHomePage() = preferences.getString(KEY_FRAGMENT, PAGES[0])!!
    fun getSortingOrder() = preferences.getString(KEY_ORDER, SORTING[0])!!
    fun getDownloadNeeded() = preferences.getBoolean(KEY_DOWNLOAD, false)
    fun getLightPeriod() = preferences.getBoolean(KEY_LIGHT_PERIOD, true)
    fun getAppTheme() = when (preferences.getString(KEY_APP_THEME, THEMES[2])) {
        THEMES[1] -> setDefaultNightMode(MODE_NIGHT_NO)
        THEMES[2] -> setDefaultNightMode(MODE_NIGHT_YES)
        else -> setDefaultNightMode(MODE_NIGHT_FOLLOW_SYSTEM)
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)

        preferences = PreferenceManager.getDefaultSharedPreferences(context)
    }

    override fun onResume() {
        super.onResume()
        preferenceScreen.sharedPreferences?.registerOnSharedPreferenceChangeListener(this)
    }

    override fun onPause() {
        super.onPause()
        preferenceScreen.sharedPreferences?.registerOnSharedPreferenceChangeListener(this)
    }
}