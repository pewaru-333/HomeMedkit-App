package ru.application.homemedkit.utils.camera

import android.content.Context
import android.util.Size
import androidx.camera.core.ImageAnalysis.Analyzer
import androidx.camera.core.ImageProxy
import androidx.camera.core.TorchState
import androidx.camera.core.resolutionselector.AspectRatioStrategy
import androidx.camera.core.resolutionselector.ResolutionSelector
import androidx.camera.core.resolutionselector.ResolutionSelector.PREFER_HIGHER_RESOLUTION_OVER_CAPTURE_RATE
import androidx.camera.core.resolutionselector.ResolutionStrategy
import androidx.camera.view.LifecycleCameraController
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.LifecycleOwner
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.asExecutor

class CameraConfig(val context: Context) {
    private val controller = LifecycleCameraController(context)

    private val defaultExecutor = Dispatchers.Default.asExecutor()

    private val resolutionStrategy = ResolutionStrategy(
        Size(1080, 1920),
        ResolutionStrategy.FALLBACK_RULE_CLOSEST_HIGHER_THEN_LOWER
    )

    private val imageResolution = ResolutionSelector.Builder()
        .setAspectRatioStrategy(AspectRatioStrategy.RATIO_16_9_FALLBACK_AUTO_STRATEGY)
        .setAllowedResolutionMode(PREFER_HIGHER_RESOLUTION_OVER_CAPTURE_RATE)
        .setResolutionStrategy(resolutionStrategy)
        .build()

    private val hasFlashUnit = controller.cameraInfo?.hasFlashUnit() != false

    fun bindToLifecycle(lifecycleOwner: LifecycleOwner): LifecycleCameraController {
        controller.bindToLifecycle(lifecycleOwner)

        return controller
    }

    fun setUseCases(useCases: Int) = with(controller) {
        setEnabledUseCases(useCases)
    }

    fun setAnalyzer(analyzer: Analyzer) = with(controller) {
        setImageAnalysisAnalyzer(defaultExecutor, analyzer)
    }

    fun setResolution() = with(controller) {
        previewResolutionSelector = imageResolution
        imageAnalysisResolutionSelector = imageResolution
        imageCaptureResolutionSelector = imageResolution
    }

    fun toggleTorch() = with(controller) {
        if (hasFlashUnit) cameraControl?.enableTorch(cameraInfo?.torchState?.value != TorchState.ON)
    }

    fun takePicture(onStart: () -> Unit, onResult: (ImageProxy) -> Unit) = with(controller) {
        takePicture(defaultExecutor, PhotoCapture(onStart, onResult))
    }
}

@Composable
fun rememberCameraConfig(useCases: Int, analyzer: Analyzer? = null): CameraConfig {
    val context = LocalContext.current

    return remember(context, useCases, analyzer) {
        CameraConfig(context).apply {
            setResolution()
            setUseCases(useCases)
            analyzer?.let(::setAnalyzer)
        }
    }
}

@Composable
fun rememberImageAnalyzer(onResult: (String) -> Unit) = remember { DataMatrixAnalyzer(onResult) }