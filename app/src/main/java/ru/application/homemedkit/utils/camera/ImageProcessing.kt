package ru.application.homemedkit.utils.camera

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Matrix
import android.net.Uri
import android.provider.OpenableColumns
import android.webkit.MimeTypeMap
import androidx.camera.core.ImageProxy
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.io.IOException
import java.io.File

class ImageProcessing(private val context: Context) {
    private val imageCompressor = ImageCompressor(context)

    suspend fun compressImage(uri: Uri) = withContext(Dispatchers.IO) {
        val mimeType = context.contentResolver.getType(uri)
        val extension = MimeTypeMap.getSingleton().getExtensionFromMimeType(mimeType)

        val fileName = context.contentResolver.query(uri, null, null, null, null)?.use { cursor ->
            val nameIndex = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)

            cursor.moveToFirst()
            cursor.getString(nameIndex)
        }

        val imageName = fileName ?: ("${System.currentTimeMillis()}.$extension")

        val compressedImage = imageCompressor.compressImage(uri, 300 * 1024L) ?: return@withContext null

        try {
            context.openFileOutput(imageName, Context.MODE_PRIVATE).use {
                it.write(compressedImage)
            }
        } catch (_: IOException) {
            return@withContext null
        }

        imageName
    }

    suspend fun compressImage(image: ImageProxy) = withContext(Dispatchers.Default) {
        try {
            val matrix = Matrix().apply {
                postRotate(image.imageInfo.rotationDegrees.toFloat())
            }

            val bitmap = Bitmap.createBitmap(
                /* source = */ image.toBitmap(),
                /* x = */ 0,
                /* y = */ 0,
                /* width = */ image.width,
                /* height = */ image.height,
                /* m = */ matrix,
                /* filter = */ true
            )

            val name = "${image.imageInfo.timestamp}_product.jpg"
            val file = File(context.filesDir, name)

            file.outputStream().use {
                bitmap.compress(Bitmap.CompressFormat.JPEG, 70, it)
            }

            name
        } catch (_: RuntimeException) {
            null
        } finally {
            image.close()
        }
    }
}