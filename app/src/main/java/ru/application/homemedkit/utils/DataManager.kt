package ru.application.homemedkit.utils

import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.net.Uri
import android.webkit.MimeTypeMap
import androidx.activity.compose.ManagedActivityResultLauncher
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContract
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.result.contract.ActivityResultContracts.CreateDocument
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import ru.application.homemedkit.R
import ru.application.homemedkit.data.MedicineDatabase
import ru.application.homemedkit.receivers.AlarmSetter
import ru.application.homemedkit.utils.di.Preferences
import ru.application.homemedkit.utils.extensions.restartApplication
import ru.application.homemedkit.utils.extensions.showToast
import java.io.File
import java.util.zip.ZipEntry
import java.util.zip.ZipInputStream
import java.util.zip.ZipOutputStream

fun interface ActionHandler {
    fun handle(actionResult: ActionResult)
}

sealed interface ContractResult {
    data object Error : ContractResult
    data class Success(val uri: Uri) : ContractResult
}

data class ActionResult(
    val onAction: suspend () -> Boolean?,
    val onResult: (Boolean?) -> Unit
)

class ActionExport(
    private val launcher: ManagedActivityResultLauncher<String, *>,
    private val fileName: String
) {
    fun launch() = launcher.launch(fileName)
}

class ActionImport<I>(
    private val launcher: ManagedActivityResultLauncher<I, *>,
    private val mimeType: I
) {
    fun launch() = launcher.launch(mimeType)
}

object DataManager {
    suspend fun exportImages(context: Context, uri: Uri) = withContext(Dispatchers.IO) {
        val mimeTypeMap = MimeTypeMap.getSingleton()
        val images = context.filesDir.listFiles { file ->
            val mimeType = mimeTypeMap.getMimeTypeFromExtension(file.extension)

            mimeType != null && mimeType.startsWith(MimeType.IMAGES)
        }

        if (images.isNullOrEmpty()) {
            false
        } else {
            try {
                context.contentResolver.openOutputStream(uri)?.let { outputStream ->
                    ZipOutputStream(outputStream).use { zipOutputStream ->
                        images.forEach { file ->
                            file.inputStream().use { inputStream ->
                                val zipEntry = ZipEntry(file.name)
                                zipOutputStream.putNextEntry(zipEntry)
                                inputStream.copyTo(zipOutputStream)
                                zipOutputStream.closeEntry()
                            }
                        }
                    }
                }

                true
            } catch (_: Exception) {
                false
            }
        }
    }

    suspend fun importImages(context: Context, uri: Uri) = withContext(Dispatchers.IO) {
        try {
            context.contentResolver.openInputStream(uri)?.let { inputStream ->
                ZipInputStream(inputStream).use { zipInputStream ->
                    var entry: ZipEntry? = zipInputStream.nextEntry

                    while (entry != null) {
                        File(context.filesDir, entry.name).outputStream().use { outputStream ->
                            zipInputStream.copyTo(outputStream)
                        }

                        zipInputStream.closeEntry()
                        entry = zipInputStream.nextEntry
                    }
                }
            }

            true
        } catch (_: Exception) {
            false
        }
    }

    suspend fun exportDatabase(context: Context, uri: Uri) = withContext(Dispatchers.IO) {
        val database = MedicineDatabase.getInstance(context)
        val path = context.getDatabasePath(database.openHelper.databaseName)

        try {
            database.close()

            context.contentResolver.openOutputStream(uri)?.use { outputStream ->
                path.inputStream().use { inputStream ->
                    inputStream.copyTo(outputStream)
                }
            }

            true
        } catch (_: Exception) {
            false
        }
    }

    suspend fun importDatabase(context: Context, uri: Uri) = withContext(Dispatchers.IO) {
        fun getHash(path: String) = try {
            SQLiteDatabase.openDatabase(path, null, SQLiteDatabase.OPEN_READONLY).use { database ->
                if (database.isDatabaseIntegrityOk) {
                    val query = "SELECT identity_hash FROM room_master_table LIMIT 1"
                    database.rawQuery(query, null).use { cursor ->
                        if (cursor.moveToFirst()) cursor.getString(0) else null
                    }
                } else {
                    null
                }
            }
        } catch (_: Exception) {
            null
        }

        val database = MedicineDatabase.getInstance(context)
        val path = context.getDatabasePath(database.openHelper.databaseName)
        val tempFile = File(context.cacheDir, "database_temp.sqlite")

        try {
            context.contentResolver.openInputStream(uri)?.use { inputStream ->
                tempFile.outputStream().use { outputStream ->
                    inputStream.copyTo(outputStream)
                }
            }

            val currentHash = getHash(path.path)
            val newHash = getHash(tempFile.path)

            if (currentHash == newHash) {
                AlarmSetter.getInstance(context).cancelAll()
                database.close()

                tempFile.inputStream().use { inputStream ->
                    path.outputStream().use { outputStream ->
                        inputStream.copyTo(outputStream)
                    }
                }

                true
            } else {
                false
            }
        } catch (_: Exception) {
            false
        } finally {
            tempFile.delete()
        }
    }

    suspend fun clearCache(context: Context) = withContext(Dispatchers.IO) {
        val mimeTypeMap = MimeTypeMap.getSingleton()
        val images = MedicineDatabase.getInstance(context).medicineDAO().getAllImageNames().toSet()

        coroutineScope {
            launch { context.cacheDir.deleteRecursively() }
            launch {
                context.filesDir.listFiles { file ->
                    val mimeType = mimeTypeMap.getMimeTypeFromExtension(file.extension)

                    mimeType != null && mimeType.startsWith(MimeType.IMAGES) && file.name !in images
                }?.forEach(File::delete)
            }
        }

        true
    }
}

@Composable
fun launcherExportImages(actionHandler: ActionHandler): ActionExport {
    val context = LocalContext.current
    val launcher = rememberContractLauncher(
        contract = CreateDocument(MimeType.ZIP),
        onResult = rememberDataAction(actionHandler) { uri ->
            DataManager.exportImages(context, uri)
        }
    )

    return remember(launcher) {
        ActionExport(
            launcher = launcher,
            fileName = "images.zip"
        )
    }
}

@Composable
fun launcherImportImages(actionHandler: ActionHandler): ActionImport<String> {
    val context = LocalContext.current
    val launcher = rememberContractLauncher(
        contract = ActivityResultContracts.GetContent(),
        onResult = rememberDataAction(actionHandler) { uri ->
            DataManager.importImages(context, uri)
        }
    )

    return remember(launcher) {
        ActionImport(
            launcher = launcher,
            mimeType = MimeType.ZIP
        )
    }
}

@Composable
fun launcherExportDatabase(actionHandler: ActionHandler): ActionExport {
    val context = LocalContext.current
    val launcher = rememberContractLauncher(
        contract = CreateDocument(MimeType.Database.DB_SQLITE_VND),
        onResult = rememberDataAction(actionHandler) { uri ->
            DataManager.exportDatabase(context, uri).also { isSuccess ->
                if (isSuccess) {
                    Preferences.addImportedKey()
                    context.restartApplication()
                }
            }
        }
    )

    return remember(launcher) {
        ActionExport(
            launcher = launcher,
            fileName = "exported.sqlite3"
        )
    }
}

@Composable
fun launcherImportDatabase(actionHandler: ActionHandler): ActionImport<Array<String>> {
    val context = LocalContext.current
    val launcher = rememberContractLauncher(
        contract = ActivityResultContracts.OpenDocument(),
        onResult = rememberDataAction(actionHandler) { uri ->
            DataManager.importDatabase(context, uri).also { isSuccess ->
                if (isSuccess) {
                    Preferences.addImportedKey()
                    context.restartApplication()
                }
            }
        }
    )

    return remember(launcher) {
        ActionImport(
            launcher = launcher,
            mimeType = MimeType.Database.array
        )
    }
}

@Composable
private fun <I, O> rememberContractLauncher(
    contract: ActivityResultContract<I, O>,
    onResult: (ContractResult) -> Unit
) = rememberLauncherForActivityResult(contract) { result ->
    val isSuccess = when (result) {
        is Uri? -> result != null
        is Boolean -> result
        else -> false
    }

    if (isSuccess && result is Uri) {
        onResult(ContractResult.Success(result))
    } else {
        onResult(ContractResult.Error)
    }
}

@Composable
private fun rememberDataAction(
    actionHandler: ActionHandler,
    action: suspend (Uri) -> Boolean?
): (ContractResult) -> Unit {
    val context = LocalContext.current

    return { result ->
        when (result) {
            ContractResult.Error -> context.showToast(R.string.text_error)
            is ContractResult.Success -> {
                actionHandler.handle(
                    actionResult = ActionResult(
                        onAction = { action(result.uri) },
                        onResult = { isSuccess ->
                            context.showToast(
                                message = if (isSuccess == true) R.string.text_success
                                else R.string.text_error
                            )
                        }
                    )
                )
            }
        }
    }
}