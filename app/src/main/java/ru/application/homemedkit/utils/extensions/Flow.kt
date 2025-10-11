package ru.application.homemedkit.utils.extensions

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.flow

fun <T> Flow<T>.pairwise(): Flow<Pair<T, T>> = flow {
    var previous: T? = null
    collect { value ->
        previous?.let { emit(it to value) }
        previous = value
    }
}

suspend fun <T> Flow<T>.collectLatestChanged(action: suspend (T) -> Unit) = pairwise()
    .collectLatest { (old, new) ->
        if (old != new) {
            action(new)
        }
    }