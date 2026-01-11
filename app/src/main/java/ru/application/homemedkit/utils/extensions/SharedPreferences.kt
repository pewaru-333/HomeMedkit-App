package ru.application.homemedkit.utils.extensions

import android.content.SharedPreferences
import androidx.core.content.edit
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.callbackFlow

fun <E : Enum<E>> SharedPreferences.Editor.putEnum(key: String, value: E) {
    putString(key, value.name)
}

inline fun <reified E : Enum<E>> SharedPreferences.getEnum(key: String, defaultValue: E) =
    runCatching { getString(key, defaultValue.name)?.let { enumValueOf<E>(it) } ?: defaultValue }
        .getOrDefault(defaultValue)

inline fun <reified E : Enum<E>> SharedPreferences.getEnumFlow(key: String, defaultValue: E) =
    flow(key) { getEnum(key, defaultValue) }

inline fun <reified T> SharedPreferences.safeGetValue(key: String, defaultValue: T) =
    when (defaultValue) {
        is Boolean -> runCatching { getBoolean(key, defaultValue as Boolean) as T }.getOrDefault(defaultValue)
        is Int -> runCatching { getInt(key, defaultValue as Int) as T }.getOrDefault(defaultValue)
        is Long -> runCatching { getLong(key, defaultValue as Long) as T }.getOrDefault(defaultValue)
        is Float -> runCatching { getFloat(key, defaultValue as Float) as T }.getOrDefault(defaultValue)
        is String -> runCatching { getString(key, defaultValue as String) as T }.getOrDefault(defaultValue)
        is Set<*> -> runCatching { getStringSet(key, defaultValue as Set<String>) as T }.getOrDefault(defaultValue)
        else -> run { edit { remove(key) } }.let { defaultValue }
    }

inline fun <reified T> SharedPreferences.getFlow(key: String, defaultValue: T) = flow(key) {
    safeGetValue(key, defaultValue)
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