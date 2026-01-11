package ru.application.homemedkit.worker

import androidx.work.Constraints
import androidx.work.Data
import androidx.work.ExistingWorkPolicy
import androidx.work.NetworkType
import androidx.work.OneTimeWorkRequest
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.OutOfQuotaPolicy
import ru.application.homemedkit.utils.SYNC_MODE
import ru.application.homemedkit.utils.WORK_AUTO_SYNC
import ru.application.homemedkit.utils.di.WorkManager
import ru.application.homemedkit.utils.enums.SyncMode

object WorkerManager {
    fun startAutoSyncWork() = startSyncWork(
        name = WORK_AUTO_SYNC,
        work = createSyncWork(),
        policy = ExistingWorkPolicy.KEEP
    )

    fun startSyncWork(name: String, work: OneTimeWorkRequest, policy: ExistingWorkPolicy) {
        WorkManager.enqueueUniqueWork(
            uniqueWorkName = name,
            existingWorkPolicy = policy,
            request = work
        )
    }

    fun createSyncWork(mode: SyncMode = SyncMode.AUTO) = OneTimeWorkRequestBuilder<SyncWorker>()
        .setExpedited(OutOfQuotaPolicy.RUN_AS_NON_EXPEDITED_WORK_REQUEST)
        .setInputData(Data.Builder().putString(SYNC_MODE, mode.name).build())
        .setConstraints(Constraints.Builder().setRequiredNetworkType(NetworkType.CONNECTED).build())
        .build()
}