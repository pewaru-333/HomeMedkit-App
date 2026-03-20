package ru.application.homemedkit.utils.camera

import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageProxy
import zxingcpp.BarcodeReader

class CodeAnalyzer(private val onResult: (String) -> Unit) : ImageAnalysis.Analyzer {
    private val reader = BarcodeReader().apply {
        options.formats = setOf(BarcodeReader.Format.DATA_MATRIX, BarcodeReader.Format.EAN_13)
        options.textMode = BarcodeReader.TextMode.ESCAPED

        options.tryInvert = true
        options.tryHarder = true
        options.tryRotate = true
    }

    override fun analyze(image: ImageProxy) {
        image.use {
            reader.read(it).firstOrNull()?.text?.let(onResult)
        }
    }
}