@file:OptIn(ExperimentalSerializationApi::class)

package ru.application.homemedkit.worker

import android.content.Context
import android.webkit.MimeTypeMap
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.supervisorScope
import kotlinx.coroutines.withContext
import kotlinx.io.IOException
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.decodeFromStream
import kotlinx.serialization.json.encodeToStream
import ru.application.homemedkit.data.MedicineDatabase
import ru.application.homemedkit.data.dto.Medicine
import ru.application.homemedkit.network.Network
import ru.application.homemedkit.network.models.auth.BackupData
import ru.application.homemedkit.network.models.auth.FileMetadata
import ru.application.homemedkit.utils.MimeType
import ru.application.homemedkit.utils.SYNC_MODE
import ru.application.homemedkit.utils.di.Preferences
import ru.application.homemedkit.utils.enums.SyncMode
import ru.application.homemedkit.utils.extensions.md5
import java.io.File

class SyncWorker(val context: Context, params: WorkerParameters) : CoroutineWorker(context, params) {
    override suspend fun doWork(): Result {
        val mode = when (val name = inputData.getString(SYNC_MODE)) {
            null -> SyncMode.AUTO
            else -> SyncMode.valueOf(name)
        }

        return try {
            when (mode) {
                SyncMode.FORCE_DOWNLOAD -> download()
                SyncMode.FORCE_UPLOAD -> upload(serializeData())
                SyncMode.AUTO -> {
                    val coroutineScope = CoroutineScope(Dispatchers.IO)

                    val getRemote = coroutineScope.async {
                        Network.Yandex.getFileMetadata("/homemeds/data/medicines.json")
                    }

                    val getLocal = coroutineScope.async {
                        serializeData()
                    }

                    val fileRemote = getRemote.await()
                    val fileLocal = getLocal.await()

                    if (fileRemote != null) {
                        val localMd5 = fileLocal.md5()
                        val remoteMd5 = fileRemote.md5

                        if (localMd5.equals(remoteMd5, ignoreCase = true)) {
                            fileLocal.delete()
                            return Result.success()
                        }

                        if (fileRemote.modified > Preferences.lastSyncMillis) {
                            fileLocal.delete()
                            download()
                        } else {
                            upload(fileLocal)
                        }
                    } else {
                        upload(fileLocal)
                    }
                }
            }

            Preferences.updateSyncMillis()
            Result.success()
        } catch (_: Exception) {
            Result.failure()
        }
    }

    private suspend fun upload(file: File) {
        if (!Network.Yandex.checkConnection()) {
            throw Exception()
        }

        var imagesSizeBytes = 0L

        val mimeTypeMap = MimeTypeMap.getSingleton()
        val images = context.filesDir.listFiles { file ->
            val mimeType = mimeTypeMap.getMimeTypeFromExtension(file.extension)
            val isImage = mimeType != null && mimeType.startsWith(MimeType.IMAGES)

            if (isImage) {
                imagesSizeBytes += file.length()
            }

            isImage
        }

        try {
            val totalUploadSize = file.length() + imagesSizeBytes
            val availableSpace = Network.Yandex.getAvailableSpace()

            if (availableSpace < (totalUploadSize + 1024 * 1024L)) {
                throw Exception()
            }

            Network.Yandex.createFolder("/homemeds")
            Network.Yandex.createFolder("/homemeds/data")
            Network.Yandex.createFolder("/homemeds/images")


            Network.Yandex.uploadFile("/homemeds/data/${file.name}", file)
            if (!images.isNullOrEmpty()) {
                uploadImages(images)
            }
        } catch (e: IOException) {
            throw Exception(e)
        } finally {
            file.delete()
        }
    }

    private suspend fun uploadImages(images: Array<File>) {
        val remoteData = Network.Yandex.getImagesMetadata() ?: return
        val remoteMap = remoteData.associateBy(FileMetadata::name)

        val uploadList = images.filter { image ->
            val remoteImage = remoteMap[image.name] ?: return@filter true

            val localMd5 = image.md5()
            val remote = remoteImage.mapper()

            !localMd5.equals(remote.md5, true) && image.lastModified() > remote.modified
        }

        try {
            supervisorScope {
                val uploadJobs = uploadList.map { file ->
                    async(Dispatchers.IO.limitedParallelism(3)) {
                        Network.Yandex.uploadFile("/homemeds/images/${file.name}", file)
                    }
                }

                uploadJobs.awaitAll()
            }
        } catch (e: IOException) {
            throw Exception(e)
        }
    }

    private suspend fun downloadImages(images: Array<File>?) {
        val remoteData = Network.Yandex.getImagesMetadata() ?: return
        val localData = images?.associateBy { it.name.orEmpty() }

        val downloadList = remoteData.filter { image ->
            val localFile = localData?.get(image.name) ?: return@filter true

            val remote = image.mapper()
            val localMd5 = localFile.md5()

            !localMd5.equals(remote.md5, true) && remote.modified > localFile.lastModified()
        }

        try {
            supervisorScope {
                val downloadJobs = downloadList.map { image ->
                    async(Dispatchers.IO.limitedParallelism(3)) {
                        val name = image.name ?: return@async
                        val file = File(context.filesDir, name)

                        Network.Yandex.downloadFile("/homemeds/images/$name", file)

                        val remoteTime = image.mapper().modified
                        file.setLastModified(remoteTime)
                    }
                }

                downloadJobs.awaitAll()
            }
        } catch (e: IOException) {
            throw Exception(e)
        }
    }

    private suspend fun download() {
        val tempFile = File(context.cacheDir, "medicines_temp.json")
        val downloadSuccess = Network.Yandex.downloadFile("/homemeds/data/medicines.json", tempFile)

        if (downloadSuccess) {
            val database = MedicineDatabase.getInstance(context)

            try {
                tempFile.inputStream().use { inputStream ->
                    val backup = Json.decodeFromStream<BackupData<List<Medicine>>>(inputStream)

                    if (backup.version > database.openHelper.readableDatabase.version) {
                        throw Exception()
                    }

                    database.medicineDAO().syncMedicines(backup.data)
                }
            } catch (e: Exception) {
                throw e
            } finally {
                tempFile.delete()
            }
        }

        val mimeTypeMap = MimeTypeMap.getSingleton()
        val images = context.filesDir.listFiles { file ->
            val mimeType = mimeTypeMap.getMimeTypeFromExtension(file.extension)

            mimeType != null && mimeType.startsWith(MimeType.IMAGES)
        }

        downloadImages(images)
    }

    private suspend fun serializeData() = withContext(Dispatchers.IO) {
        val database = MedicineDatabase.getInstance(context)
        val file = File(context.cacheDir, "medicines.json")
        val medicines = database.medicineDAO().getAll()
        val backupData = BackupData(
            version = database.openHelper.readableDatabase.version,
            data = medicines
        )

        try {
            file.outputStream().buffered().use { outputStream ->
                Json.encodeToStream(backupData, outputStream)
            }
        } catch (e: Exception) {
            throw e
        }

        return@withContext file
    }
}