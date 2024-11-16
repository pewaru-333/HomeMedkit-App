package ru.application.homemedkit.helpers

import android.graphics.ImageFormat.YUV_420_888
import android.graphics.ImageFormat.YUV_422_888
import android.graphics.ImageFormat.YUV_444_888
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageProxy
import com.google.zxing.BarcodeFormat.DATA_MATRIX
import com.google.zxing.BinaryBitmap
import com.google.zxing.DecodeHintType.POSSIBLE_FORMATS
import com.google.zxing.DecodeHintType.TRY_HARDER
import com.google.zxing.MultiFormatReader
import com.google.zxing.PlanarYUVLuminanceSource
import com.google.zxing.common.HybridBinarizer
import java.nio.ByteBuffer
import kotlin.math.roundToInt

class DataMatrixAnalyzer(private val onResult: (String) -> Unit) : ImageAnalysis.Analyzer {
    override fun analyze(image: ImageProxy) {
        if (image.format in listOf(YUV_420_888, YUV_422_888, YUV_444_888)) {
            val length = if (image.width > image.height) image.height * 0.5f else image.width * 0.7f

            val source = PlanarYUVLuminanceSource(
                image.planes.first().buffer.toByteArray(),
                image.width,
                image.height,
                ((image.width - length) / 2).roundToInt(),
                ((image.height - length) / 2).roundToInt(),
                length.roundToInt(),
                length.roundToInt(),
                false
            )

            try {
                val result = MultiFormatReader().apply {
                    setHints(mapOf(POSSIBLE_FORMATS to setOf(DATA_MATRIX), TRY_HARDER to true))
                }.decodeWithState(BinaryBitmap(HybridBinarizer(source)))

                onResult(result.text)
            } catch (_: Throwable) {
            } finally {
                image.close()
            }
        }
    }

    private fun ByteBuffer.toByteArray() = ByteArray(rewind().remaining()).also(::get)
}