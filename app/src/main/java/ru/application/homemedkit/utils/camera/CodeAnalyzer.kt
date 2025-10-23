package ru.application.homemedkit.utils.camera

import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageProxy
import com.google.zxing.BarcodeFormat
import com.google.zxing.BinaryBitmap
import com.google.zxing.DecodeHintType
import com.google.zxing.MultiFormatReader
import com.google.zxing.PlanarYUVLuminanceSource
import com.google.zxing.common.HybridBinarizer
import java.nio.ByteBuffer
import kotlin.math.roundToInt

class CodeAnalyzer(private val onResult: (String) -> Unit) : ImageAnalysis.Analyzer {

    private val reader = MultiFormatReader().apply {
        setHints(
            mapOf(
                DecodeHintType.POSSIBLE_FORMATS to setOf(BarcodeFormat.DATA_MATRIX, BarcodeFormat.EAN_13),
                DecodeHintType.ALSO_INVERTED to true,
                DecodeHintType.TRY_HARDER to true
            )
        )
    }

    override fun analyze(image: ImageProxy) {
        image.use {
            val plane = it.planes.first()

            val size = it.width.coerceAtMost(it.height) * 0.7f
            val left = (it.width - size) / 2f
            val top = (it.height - size) / 2f

            val source = PlanarYUVLuminanceSource(
                plane.buffer.toByteArray(),
                plane.rowStride,
                it.height,
                left.roundToInt(),
                top.roundToInt(),
                size.roundToInt(),
                size.roundToInt(),
                false
            )

            runCatching {
                val result = reader.decodeWithState(BinaryBitmap(HybridBinarizer(source)))
                onResult(result.text)
            }
        }
    }

    private fun ByteBuffer.toByteArray() = ByteArray(rewind().remaining()).also(::get)
}