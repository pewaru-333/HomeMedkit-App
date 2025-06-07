package ru.application.homemedkit.utils.camera

import android.graphics.ImageFormat
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageProxy
import com.google.zxing.BarcodeFormat
import com.google.zxing.BinaryBitmap
import com.google.zxing.DecodeHintType
import com.google.zxing.MultiFormatReader
import com.google.zxing.PlanarYUVLuminanceSource
import com.google.zxing.ReaderException
import com.google.zxing.common.HybridBinarizer
import java.nio.ByteBuffer
import kotlin.math.roundToInt

class DataMatrixAnalyzer(private val onResult: (String) -> Unit) : ImageAnalysis.Analyzer {

    private val reader = MultiFormatReader().apply {
        setHints(
            mapOf(
                DecodeHintType.POSSIBLE_FORMATS to setOf(BarcodeFormat.DATA_MATRIX),
                DecodeHintType.ALSO_INVERTED to true,
                DecodeHintType.TRY_HARDER to true
            )
        )
    }

    override fun analyze(image: ImageProxy) {
        if (image.format !in listOf(ImageFormat.YUV_420_888, ImageFormat.YUV_422_888, ImageFormat.YUV_444_888)) {
            image.close()

            return
        }

        try {
            val length = if (image.width > image.height) image.height * 0.5f else image.width * 0.7f

            val source = PlanarYUVLuminanceSource(
                image.planes.first().buffer.toByteArray(),
                image.planes.first().rowStride,
                image.height,
                ((image.width - length) / 2).roundToInt(),
                ((image.height - length) / 2).roundToInt(),
                length.roundToInt(),
                length.roundToInt(),
                false
            )

            try {
                val result = reader.decodeWithState(BinaryBitmap(HybridBinarizer(source)))
                onResult(result.text)
            } catch (_: ReaderException) {
            }
        } catch (_: Exception) {
        } finally {
            image.close()
        }
    }

    private fun ByteBuffer.toByteArray() = ByteArray(rewind().remaining()).also(::get)
}