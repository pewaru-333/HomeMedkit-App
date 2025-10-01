package ru.application.homemedkit.utils.camera

import android.content.Context
import android.util.Size
import androidx.camera.core.Camera
import androidx.camera.core.CameraSelector
import androidx.camera.core.FocusMeteringAction
import androidx.camera.core.ImageProxy
import androidx.camera.core.Preview
import androidx.camera.core.SurfaceOrientedMeteringPointFactory
import androidx.camera.core.SurfaceRequest
import androidx.camera.core.TorchState
import androidx.camera.core.UseCase
import androidx.camera.core.resolutionselector.AspectRatioStrategy
import androidx.camera.core.resolutionselector.ResolutionSelector
import androidx.camera.core.resolutionselector.ResolutionSelector.PREFER_HIGHER_RESOLUTION_OVER_CAPTURE_RATE
import androidx.camera.core.resolutionselector.ResolutionStrategy
import androidx.camera.core.resolutionselector.ResolutionStrategy.FALLBACK_RULE_CLOSEST_HIGHER_THEN_LOWER
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.lifecycle.awaitInstance
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.compose.LocalLifecycleOwner
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.asExecutor
import kotlinx.coroutines.awaitCancellation
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.withContext
import java.util.concurrent.Executors
import kotlin.math.min

class CameraConfig(val context: Context) {
    private lateinit var camera: Camera

    private var surfaceMeteringPointFactory = SurfaceOrientedMeteringPointFactory(0f, 0f)

    private var minZoom by mutableFloatStateOf(1f)

    private var maxZoom by mutableFloatStateOf(1f)

    private val _surfaceRequests = MutableStateFlow<SurfaceRequest?>(null)
    val surfaceRequests = _surfaceRequests.asStateFlow()

    suspend fun bindCamera(lifecycleOwner: LifecycleOwner, useCase: UseCases) {
        val cameraPreview = Preview.Builder()
            .setResolutionSelector(useCase.useCaseConfig.resolutionSelector)
            .build()
            .apply {
                setSurfaceProvider { surface ->
                    _surfaceRequests.update { surface }

                    with(surface.resolution) {
                        surfaceMeteringPointFactory = SurfaceOrientedMeteringPointFactory(width.toFloat(), height.toFloat())
                    }
                }
            }

        val cameraProvider = ProcessCameraProvider.awaitInstance(context)
        camera = withContext(Dispatchers.Main) {
            cameraProvider.bindToLifecycle(
                lifecycleOwner = lifecycleOwner,
                cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA,
                useCases = arrayOf(cameraPreview, useCase.useCaseConfig.config)
            )
        }

        with(camera.cameraInfo.zoomState) {
            value?.let {
                minZoom = it.minZoomRatio
                maxZoom = it.maxZoomRatio
            }
        }

        try {
            awaitCancellation()
        } finally {
            withContext(Dispatchers.Main) {
                cameraProvider.unbindAll()
            }
        }
    }

    internal fun setImageAnalyzer(onResult: (String) -> Unit) = with(ImageAnalysis.config) {
        setAnalyzer(Executors.newSingleThreadExecutor(), DataMatrixAnalyzer(onResult))
    }

    fun takePicture(onStart: () -> Unit, onResult: (ImageProxy) -> Unit) = with(ImageCapture.config) {
        takePicture(Dispatchers.Default.asExecutor(), PhotoCapture(onStart, onResult))
    }

    fun tapToFocus(coordinates: Offset) {
        val point = surfaceMeteringPointFactory.createPoint(coordinates.x, coordinates.y)
        val meteringAction = FocusMeteringAction.Builder(point).build()

        camera.cameraControl.startFocusAndMetering(meteringAction)
    }

    fun setZoom() = with(camera) {
        val currentZoom = cameraInfo.zoomState.value?.zoomRatio ?: 1f
        val newZoom = if (currentZoom == 1f) 2f.coerceIn(minZoom, maxZoom)
        else min(1f, minZoom)

        cameraControl.setZoomRatio(newZoom)
    }

    fun setZoom(zoom: Float) = with(camera) {
        val currentZoom = cameraInfo.zoomState.value?.zoomRatio ?: 1f
        val newZoom = (currentZoom * zoom).coerceIn(minZoom, maxZoom)

        cameraControl.setZoomRatio(newZoom)
    }

    fun toggleTorch() = with(camera) {
        if (cameraInfo.hasFlashUnit()) {
            cameraControl.enableTorch(cameraInfo.torchState.value != TorchState.ON)
        }
    }

    enum class UseCases(val useCaseConfig: UseCaseConfig) {
        IMAGE_ANALYSIS(ImageAnalysis),
        IMAGE_CAPTURE(ImageCapture)
    }

    interface UseCaseConfig {
        val resolutionSelector: ResolutionSelector
        val config: UseCase
    }

    private object ImageAnalysis : UseCaseConfig {
        private val size = Size(720, 1280)

        override val resolutionSelector = ResolutionSelector.Builder()
            .setAspectRatioStrategy(AspectRatioStrategy.RATIO_4_3_FALLBACK_AUTO_STRATEGY)
            .setResolutionStrategy(ResolutionStrategy(size, FALLBACK_RULE_CLOSEST_HIGHER_THEN_LOWER))
            .build()

        override val config = androidx.camera.core.ImageAnalysis.Builder()
            .setBackgroundExecutor(Dispatchers.Default.asExecutor())
            .setResolutionSelector(resolutionSelector)
            .build()
    }

    private object ImageCapture : UseCaseConfig {
        private val size = Size(1080, 1920)

        override val resolutionSelector = ResolutionSelector.Builder()
            .setAspectRatioStrategy(AspectRatioStrategy.RATIO_16_9_FALLBACK_AUTO_STRATEGY)
            .setAllowedResolutionMode(PREFER_HIGHER_RESOLUTION_OVER_CAPTURE_RATE)
            .setResolutionStrategy(ResolutionStrategy(size, FALLBACK_RULE_CLOSEST_HIGHER_THEN_LOWER))
            .build()

        override val config = androidx.camera.core.ImageCapture.Builder()
            .setIoExecutor(Dispatchers.IO.asExecutor())
            .setResolutionSelector(resolutionSelector)
            .build()
    }
}

@Composable
fun rememberCameraConfig(
    useCase: CameraConfig.UseCases,
    onResult: ((String) -> Unit)? = null
): CameraConfig {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current

    val cameraConfig = remember(useCase) {
        CameraConfig(context).apply {
            onResult?.let(::setImageAnalyzer)
        }
    }

    LaunchedEffect(cameraConfig, lifecycleOwner) {
        cameraConfig.bindCamera(lifecycleOwner, useCase)
    }

    return cameraConfig
}