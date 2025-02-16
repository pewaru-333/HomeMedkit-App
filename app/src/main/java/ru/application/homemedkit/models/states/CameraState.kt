package ru.application.homemedkit.models.states

import android.content.Context
import android.util.Size
import androidx.camera.core.ImageAnalysis.Analyzer
import androidx.camera.core.TorchState
import androidx.camera.core.resolutionselector.AspectRatioStrategy
import androidx.camera.core.resolutionselector.ResolutionSelector
import androidx.camera.core.resolutionselector.ResolutionStrategy
import androidx.camera.view.LifecycleCameraController
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.asExecutor
import ru.application.homemedkit.helpers.DataMatrixAnalyzer
import ru.application.homemedkit.helpers.PhotoCapture

class CameraState(val context: Context) {
    val controller = LifecycleCameraController(context)

    private val defaultExecutor = Dispatchers.Default.asExecutor()

    private val resolution = ResolutionStrategy(
        Size(1920, 1080),
        ResolutionStrategy.FALLBACK_RULE_CLOSEST_HIGHER_THEN_LOWER
    )

    private val imageResolution = ResolutionSelector.Builder()
        .setAspectRatioStrategy(AspectRatioStrategy.RATIO_16_9_FALLBACK_AUTO_STRATEGY)
        .setResolutionStrategy(resolution)
        .build()

    private val hasFlashUnit = controller.cameraInfo?.hasFlashUnit() ?: true

    fun setUseCases(useCases: Int) = with(controller) { setEnabledUseCases(useCases) }

    fun setAnalyzer(analyzer: Analyzer) = with(controller) {
        setImageAnalysisAnalyzer(defaultExecutor, analyzer)
    }

    fun setResolution() = with(controller) {
        imageAnalysisResolutionSelector = imageResolution
        imageCaptureResolutionSelector = imageResolution
    }

    fun toggleTorch() = with(controller) {
        if (hasFlashUnit) cameraControl?.enableTorch(cameraInfo?.torchState?.value != TorchState.ON)
    }

    fun takePicture(onResult: (String) -> Unit) = with(controller) {
        takePicture(
            defaultExecutor,
            PhotoCapture(context, onResult)
        )
    }
}

@Composable
fun rememberCameraState(useCases: Int, analyzer: Analyzer? = null): CameraState {
    val context = LocalContext.current

    return remember {
        CameraState(context).apply {
            setResolution()
            setUseCases(useCases)
            analyzer?.let(::setAnalyzer)
        }
    }
}

@Composable
fun rememberImageAnalyzer(onResult: (String) -> Unit) = remember { DataMatrixAnalyzer(onResult) }