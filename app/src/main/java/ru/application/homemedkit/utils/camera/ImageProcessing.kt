package ru.application.homemedkit.utils.camera

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Matrix
import android.net.Uri
import android.provider.OpenableColumns
import android.webkit.MimeTypeMap
import androidx.camera.core.ImageProxy
import androidx.core.net.toUri
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File

class ImageProcessing(private val context: Context) {
    private val imageCompressor = ImageCompressor(context)

    suspend fun compressImage(uri: Uri): String? {
        val mimeType = context.contentResolver.getType(uri)
        val extension = MimeTypeMap.getSingleton().getExtensionFromMimeType(mimeType)

        val fileName = context.contentResolver.query(uri, null, null, null, null)?.use { cursor ->
            val nameIndex = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)

            cursor.moveToFirst()
            cursor.getString(nameIndex)
        }

        val imageName = fileName ?: ("${System.currentTimeMillis()}.$extension")

        val compressedImage = imageCompressor.compressImage(uri, 300 * 1024L) ?: return null

        try {
            withContext(Dispatchers.IO) {
                context.openFileOutput(imageName, Context.MODE_PRIVATE).use {
                    it.write(compressedImage)
                }
            }
        } catch (_: Exception) {
            return null
        }

        return imageName
    }

    suspend fun compressImage(image: ImageProxy): String {
        val matrix = Matrix().apply {
            postRotate(image.imageInfo.rotationDegrees.toFloat())
        }

        val bitmap = withContext(Dispatchers.Default) {
            Bitmap.createBitmap(
                /* source = */ image.toBitmap(),
                /* x = */ 0,
                /* y = */ 0,
                /* width = */ image.width,
                /* height = */ image.height,
                /* m = */ matrix,
                /* filter = */ true
            )
        }

        val name = "${image.imageInfo.timestamp}_product.jpg"
        val file = File(context.filesDir, name)

        context.contentResolver.openOutputStream(file.toUri())?.use {
            withContext(Dispatchers.Default) {
                bitmap.compress(Bitmap.CompressFormat.JPEG, 70, it)
            }
        }

        return name
    }
}