package ru.application.homemedkit.helpers

import android.graphics.ImageFormat.YUV_420_888
import android.graphics.ImageFormat.YUV_422_888
import android.graphics.ImageFormat.YUV_444_888
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageProxy
import com.google.zxing.BarcodeFormat
import com.google.zxing.BinaryBitmap
import com.google.zxing.DecodeHintType
import com.google.zxing.MultiFormatReader
import com.google.zxing.PlanarYUVLuminanceSource
import com.google.zxing.common.HybridBinarizer
import java.nio.ByteBuffer

class DataMatrixAnalyzer(private val onResult: (String) -> Unit) : ImageAnalysis.Analyzer {
    override fun analyze(image: ImageProxy) {
        if (image.format in listOf(YUV_420_888, YUV_422_888, YUV_444_888)) {
            val source = PlanarYUVLuminanceSource(
                image.planes.first().buffer.toByteArray(),
                image.width, image.height,
                0, 0,
                image.width, image.height,
                false
            )

            try {
                val result = MultiFormatReader().apply {
                    setHints(
                        mapOf(
                            DecodeHintType.POSSIBLE_FORMATS to setOf(BarcodeFormat.DATA_MATRIX),
                            DecodeHintType.TRY_HARDER to true
                        )
                    )
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