package ru.application.homemedkit.helpers

import android.content.Context
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class FileManager(private val context: Context) {
    suspend fun saveImage(bytes: ByteArray, fileName: String) = withContext(Dispatchers.IO) {
        context.openFileOutput(fileName, Context.MODE_PRIVATE).use { it.write(bytes) }
    }
}