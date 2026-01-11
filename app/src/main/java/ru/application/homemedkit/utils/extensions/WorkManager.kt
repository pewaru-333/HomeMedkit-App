package ru.application.homemedkit.utils.extensions

import androidx.work.WorkInfo
import androidx.work.WorkManager
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.map
import ru.application.homemedkit.utils.WORK_AUTH_FIRST_TIME

suspend fun WorkManager.awaitSyncWorkResult(onResult: suspend (isSuccess: Boolean) -> Unit) {
    val state = getWorkInfosForUniqueWorkFlow(WORK_AUTH_FIRST_TIME)
        .map { it.firstOrNull()?.state }
        .firstOrNull {
            it == WorkInfo.State.SUCCEEDED || it == WorkInfo.State.FAILED || it == WorkInfo.State.CANCELLED
        }

    onResult(state == WorkInfo.State.SUCCEEDED)
}