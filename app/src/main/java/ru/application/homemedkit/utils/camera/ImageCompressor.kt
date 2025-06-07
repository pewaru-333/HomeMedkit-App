package ru.application.homemedkit.utils.camera

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Build
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ensureActive
import kotlinx.coroutines.isActive
import kotlinx.coroutines.withContext
import java.io.ByteArrayOutputStream
import kotlin.math.roundToInt

class ImageCompressor(private val context: Context) {
    suspend fun compressImage(uri: Uri, size: Long) = withContext(Dispatchers.IO) {
        val mimeType = context.contentResolver.getType(uri)
        val inputBytes = context.contentResolver.openInputStream(uri)
            ?.use { inputStream -> inputStream.readBytes() } ?: return@withContext null

        ensureActive()

        withContext(Dispatchers.Default) {
            val bitmap = BitmapFactory.decodeByteArray(inputBytes, 0, inputBytes.size)

            ensureActive()

            val format = when (mimeType) {
                "image/png" -> Bitmap.CompressFormat.PNG
                "image/jpeg" -> Bitmap.CompressFormat.JPEG
                "image/webp" -> if (Build.VERSION.SDK_INT >= 30) Bitmap.CompressFormat.WEBP_LOSSLESS else Bitmap.CompressFormat.WEBP
                else -> Bitmap.CompressFormat.JPEG
            }

            var outputBytes: ByteArray
            var quality = 90

            do {
                ByteArrayOutputStream().use { outputStream ->
                    bitmap.compress(format, quality, outputStream)
                    outputBytes = outputStream.toByteArray()
                    quality -= (quality * 0.1).roundToInt()
                }
            } while (isActive && outputBytes.size > size && quality > 10 && format != Bitmap.CompressFormat.PNG)

            outputBytes
        }
    }
}