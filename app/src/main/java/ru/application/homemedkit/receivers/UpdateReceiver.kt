package ru.application.homemedkit.receivers

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Context.MODE_PRIVATE
import android.content.Intent
import androidx.core.content.edit
import ru.application.homemedkit.helpers.KEY_APP_SYSTEM
import ru.application.homemedkit.helpers.KEY_APP_THEME
import ru.application.homemedkit.helpers.KEY_APP_VIEW
import ru.application.homemedkit.helpers.KEY_CHECK_EXP_DATE
import ru.application.homemedkit.helpers.KEY_DOWNLOAD
import ru.application.homemedkit.helpers.KEY_DYNAMIC_COLOR
import ru.application.homemedkit.helpers.KEY_EXP_IMP
import ru.application.homemedkit.helpers.KEY_KITS
import ru.application.homemedkit.helpers.KEY_LANGUAGE
import ru.application.homemedkit.helpers.KEY_LAST_KIT
import ru.application.homemedkit.helpers.KEY_MED_COMPACT_VIEW
import ru.application.homemedkit.helpers.KEY_ORDER

class UpdateReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context?, intent: Intent?) {
        if (context != null && intent != null && intent.action == Intent.ACTION_MY_PACKAGE_REPLACED) {
            val preferences = context.getSharedPreferences("${context.packageName}_preferences", MODE_PRIVATE)
            val old = preferences.all

            preferences.edit {
                clear()
                old.entries.forEach { (key, value) ->
                    if (key in listOf(
                            KEY_APP_THEME, KEY_APP_SYSTEM, KEY_APP_VIEW, KEY_MED_COMPACT_VIEW,
                            KEY_CHECK_EXP_DATE, KEY_DYNAMIC_COLOR, KEY_DOWNLOAD, KEY_EXP_IMP,
                            KEY_KITS, KEY_LANGUAGE, KEY_LAST_KIT, KEY_ORDER
                        )
                    )
                        when (value) {
                            is Boolean -> putBoolean(key, value)
                            is Int -> putInt(key, value)
                            is Long -> putLong(key, value)
                            is Float -> putFloat(key, value)
                            is String -> putString(key, value)
                            is Set<*> -> @Suppress("UNCHECKED_CAST") putStringSet(key, value as Set<String>)
                            else -> throw IllegalArgumentException()
                        }
                }
            }
        }
    }
}