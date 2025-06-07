package ru.application.homemedkit.utils.camera

import androidx.camera.core.ImageCapture
import androidx.camera.core.ImageCaptureException
import androidx.camera.core.ImageProxy

class PhotoCapture(private val onCapture: () -> Unit, private val onSave: (ImageProxy) -> Unit) : ImageCapture.OnImageCapturedCallback() {
    override fun onCaptureSuccess(image: ImageProxy) {
        super.onCaptureSuccess(image)

        onSave(image)
    }

    override fun onCaptureStarted() {
        super.onCaptureStarted()

        onCapture()
    }

    override fun onError(exception: ImageCaptureException) {
        exception.printStackTrace()
    }
}