package ru.application.homemedkit.helpers.extensions

import android.content.SharedPreferences
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.callbackFlow
import ru.application.homemedkit.helpers.KEY_APP_THEME
import ru.application.homemedkit.helpers.KEY_DYNAMIC_COLOR
import ru.application.homemedkit.helpers.enums.Theme

fun <E : Enum<E>> SharedPreferences.Editor.putEnum(key: String, value: E) {
    putString(key, value.name)
}

fun <E : Enum<E>> SharedPreferences.getEnum(key: String, enum: Class<E>): E? {
    val stringValue = getString(key, null) ?: return null
    return enum.enumConstants?.find {
        it.name == stringValue
    }
}

fun <E : Enum<E>> SharedPreferences.getEnum(key: String, defaultValue: E): E {
    return getEnum(key, defaultValue.javaClass) ?: defaultValue
}

fun SharedPreferences.getThemeFlow(changedKey: String = KEY_APP_THEME) = callbackFlow {
    val listener = SharedPreferences.OnSharedPreferenceChangeListener { _, key ->
        if (changedKey == key) trySend(getEnum(key, Theme.SYSTEM))
    }

    registerOnSharedPreferenceChangeListener(listener)

    if (contains(changedKey)) send(getEnum(changedKey, Theme.SYSTEM))

    awaitClose { unregisterOnSharedPreferenceChangeListener(listener) }
}

fun SharedPreferences.getColorsFlow(changedKey: String = KEY_DYNAMIC_COLOR) = callbackFlow {
    val listener = SharedPreferences.OnSharedPreferenceChangeListener { _, key ->
        if (changedKey == key) trySend(getBoolean(key, false))
    }

    registerOnSharedPreferenceChangeListener(listener)

    if (contains(changedKey)) send(getBoolean(changedKey, false))

    awaitClose { unregisterOnSharedPreferenceChangeListener(listener) }
}