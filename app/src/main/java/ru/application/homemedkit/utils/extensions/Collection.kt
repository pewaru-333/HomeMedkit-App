package ru.application.homemedkit.utils.extensions

import androidx.compose.runtime.snapshots.SnapshotStateSet

fun <T> Collection<T>.toMutableStateSet() = SnapshotStateSet<T>().also { it.addAll(this) }

fun <T> MutableCollection<T>.toggle(element: T) = if (element in this) remove(element) else add(element)