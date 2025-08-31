package ru.application.homemedkit.utils.extensions

import android.content.SharedPreferences
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.map

fun <E : Enum<E>> SharedPreferences.Editor.putEnum(key: String, value: E) {
    putString(key, value.name)
}

inline fun <reified E : Enum<E>> SharedPreferences.getEnum(key: String, defaultValue: E) =
    getString(key, defaultValue.name)?.let { enumValueOf<E>(it) } ?: defaultValue

inline fun <reified E : Enum<E>> SharedPreferences.getEnumFlow(key: String, defaultValue: E) =
    flow(key) { getString(key, defaultValue.name) }.map { stringValue ->
        stringValue?.let { enumValueOf<E>(it) } ?: defaultValue
    }

inline fun <reified T> SharedPreferences.getFlow(key: String, defaultValue: T) = flow(key) {
    when (defaultValue) {
        is Boolean -> getBoolean(key, defaultValue as Boolean) as T
        is Int -> getInt(key, defaultValue as Int) as T
        is Float -> getFloat(key, defaultValue as Float) as T
        is String -> getString(key, defaultValue as String) as T
        is Set<*> -> getStringSet(key, defaultValue as Set<String>) as T
        else -> throw IllegalArgumentException()
    }
}

inline fun <T> SharedPreferences.flow(
    key: String,
    crossinline mapper: SharedPreferences.(key: String) -> T
) = callbackFlow {
    val listener = SharedPreferences.OnSharedPreferenceChangeListener { preferences, changedKey ->
        if (key == changedKey) {
            trySend(preferences.mapper(changedKey))
        }
    }

    registerOnSharedPreferenceChangeListener(listener)

    if (contains(key)) {
        send(mapper(key))
    }

    awaitClose { unregisterOnSharedPreferenceChangeListener(listener) }
}