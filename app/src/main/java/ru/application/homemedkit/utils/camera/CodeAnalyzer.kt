package ru.application.homemedkit.utils.camera

import android.graphics.Rect
import android.graphics.RectF
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageProxy
import zxingcpp.BarcodeReader

class CodeAnalyzer(
    private val isEnabled: () -> Boolean,
    private val scanArea: (ImageProxy) -> Rect?,
    private val onResult: (String, RectF) -> Unit
) : ImageAnalysis.Analyzer {
    private val reader = BarcodeReader().apply {
        options.formats = setOf(BarcodeReader.Format.DATA_MATRIX, BarcodeReader.Format.EAN_13)
        options.textMode = BarcodeReader.TextMode.PLAIN
        options.maxNumberOfSymbols = 1
        options.tryInvert = true
        options.tryHarder = true
        options.tryRotate = true
    }

    override fun analyze(image: ImageProxy) {
        image.use {
            if (!isEnabled()) return

            val crop = image.cropRect
            val area = scanArea(image)?.let(::Rect) ?: return
            if (!area.intersect(crop)) return

            image.setCropRect(area)
            val result = try {
                reader.read(image).firstOrNull()
            } catch (_: RuntimeException) {
                null
            }

            val text = result?.text
            if (text != null && isEnabled()) {
                onResult(text, getRect(result.position, image, area, crop))
            }
        }
    }

    private fun getRect(position: BarcodeReader.Position, image: ImageProxy, area: Rect, crop: Rect): RectF {
        val topLeft = position.topLeft
        val topRight = position.topRight
        val bottomRight = position.bottomRight
        val bottomLeft = position.bottomLeft

        val left = minOf(minOf(topLeft.x, topRight.x), minOf(bottomRight.x, bottomLeft.x)).toFloat()
        val top = minOf(minOf(topLeft.y, topRight.y), minOf(bottomRight.y, bottomLeft.y)).toFloat()
        val right = maxOf(maxOf(topLeft.x, topRight.x), maxOf(bottomRight.x, bottomLeft.x)) + 1f
        val bottom = maxOf(maxOf(topLeft.y, topRight.y), maxOf(bottomRight.y, bottomLeft.y)) + 1f

        return RectF(left, top, right, bottom).apply {
            when (image.imageInfo.rotationDegrees) {
                90 -> set(top, area.height() - right, bottom, area.height() - left)
                180 -> set(area.width() - right, area.height() - bottom, area.width() - left, area.height() - top)
                270 -> set(area.width() - bottom, left, area.width() - top, right)
            }
            offset((area.left - crop.left).toFloat(), (area.top - crop.top).toFloat())
            set(
                /* left = */ (this.left / crop.width()).coerceIn(0f, 1f),
                /* top = */ (this.top / crop.height()).coerceIn(0f, 1f),
                /* right = */ (this.right / crop.width()).coerceIn(0f, 1f),
                /* bottom = */ (this.bottom / crop.height()).coerceIn(0f, 1f)
            )
        }
    }
}