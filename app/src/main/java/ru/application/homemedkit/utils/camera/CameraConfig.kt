package ru.application.homemedkit.utils.camera

import android.content.Context
import android.graphics.Rect
import android.graphics.RectF
import android.util.Rational
import android.util.Size
import androidx.annotation.MainThread
import androidx.camera.core.*
import androidx.camera.core.resolutionselector.AspectRatioStrategy
import androidx.camera.core.resolutionselector.ResolutionSelector
import androidx.camera.core.resolutionselector.ResolutionStrategy
import androidx.camera.core.resolutionselector.ResolutionStrategy.FALLBACK_RULE_CLOSEST_HIGHER_THEN_LOWER
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.lifecycle.awaitInstance
import androidx.compose.runtime.*
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.platform.LocalContext
import androidx.core.content.ContextCompat
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.compose.LocalLifecycleOwner
import kotlinx.coroutines.*
import java.io.File
import java.util.UUID
import java.util.concurrent.TimeUnit
import kotlin.math.ceil
import kotlin.math.floor
import androidx.compose.ui.geometry.Rect as ComposeRect

class CameraConfig(private val context: Context, private val useCase: UseCases) {
    private var camera by mutableStateOf<Camera?>(null)
    private var surfaceMeteringPointFactory = SurfaceOrientedMeteringPointFactory(1f, 1f)

    val isReady: Boolean
        get() = camera != null
    val isScanning: Boolean
        get() = useCase == UseCases.IMAGE_ANALYSIS
    val hasFlash: Boolean
        get() = camera?.cameraInfo?.hasFlashUnit() == true

    var detectedBounds by mutableStateOf<ComposeRect?>(null)
        private set

    internal var previewCrop by mutableStateOf<ComposeRect?>(null)
        private set

    internal var onResult: ((String) -> Unit)? = null

    @Volatile
    internal var analysisEnabled = true

    @Volatile
    private var scanArea: ComposeRect? = null

    private val resolutionSelector = ResolutionSelector.Builder()
        .setAspectRatioStrategy(AspectRatioStrategy.RATIO_16_9_FALLBACK_AUTO_STRATEGY)
        .setResolutionStrategy(ResolutionStrategy(if (useCase == UseCases.IMAGE_ANALYSIS) Size(1280, 720) else Size(1920, 1080), FALLBACK_RULE_CLOSEST_HIGHER_THEN_LOWER))
        .build()

    private val imageAnalysis by lazy {
        ImageAnalysis.Builder()
            .setResolutionSelector(resolutionSelector)
            .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
            .build()
    }
    private val imageCapture by lazy {
        ImageCapture.Builder()
            .setIoExecutor(Dispatchers.IO.asExecutor())
            .setResolutionSelector(resolutionSelector)
            .setJpegQuality(70)
            .build()
    }

    var surfaceRequest by mutableStateOf<SurfaceRequest?>(null)
        private set

    @MainThread
    internal suspend fun bindCamera(lifecycleOwner: LifecycleOwner) {
        var provider: ProcessCameraProvider? = null
        val preview = Preview.Builder()
            .setResolutionSelector(resolutionSelector)
            .build()
        val currentCase = when (useCase) {
            UseCases.IMAGE_ANALYSIS -> imageAnalysis
            UseCases.IMAGE_CAPTURE -> imageCapture
        }

        try {
            provider = ProcessCameraProvider.awaitInstance(context)
            preview.setSurfaceProvider { surface ->
                surfaceRequest = surface
                surfaceMeteringPointFactory = SurfaceOrientedMeteringPointFactory(
                    surface.resolution.width.toFloat(),
                    surface.resolution.height.toFloat()
                )
            }

            if (currentCase is ImageAnalysis) {
                currentCase.setAnalyzer(
                    /* executor = */ Dispatchers.Default.asExecutor(),
                    /* analyzer = */ CodeAnalyzer(
                        isEnabled = { analysisEnabled },
                        scanArea = ::getScanRect,
                        onResult = ::onCodeDetected
                    )
                )
            }

            val selector = CameraSelector.DEFAULT_BACK_CAMERA
            val aspectRatio = if (provider.getCameraInfo(selector).getSensorRotationDegrees(preview.targetRotation) % 180 == 0) Rational(16, 9)
            else Rational(9, 16)

            val group = UseCaseGroup.Builder()
                .setViewPort(ViewPort.Builder(aspectRatio, preview.targetRotation).build())
                .addUseCase(preview)
                .addUseCase(currentCase)
                .build()

            camera = provider.bindToLifecycle(lifecycleOwner, selector, group)
            previewCrop = preview.resolutionInfo?.cropRect?.let {
                ComposeRect(it.left.toFloat(), it.top.toFloat(), it.right.toFloat(), it.bottom.toFloat())
            }
            awaitCancellation()
        } catch (e: CancellationException) {
            throw e
        } catch (_: Exception) {
            return
        } finally {
            if (currentCase is ImageAnalysis) {
                currentCase.clearAnalyzer()
            }

            provider?.unbind(preview, currentCase)
            preview.surfaceProvider = null
            camera = null
            surfaceRequest = null
            scanArea = null
            previewCrop = null
            detectedBounds = null
        }
    }

    private fun getScanRect(image: ImageProxy): Rect? {
        val area = scanArea ?: return null
        val crop = image.cropRect
        return Rect(
            /* left = */ crop.left + floor(area.left * crop.width()).toInt(),
            /* top = */ crop.top + floor(area.top * crop.height()).toInt(),
            /* right = */ crop.left + ceil(area.right * crop.width()).toInt(),
            /* bottom = */ crop.top + ceil(area.bottom * crop.height()).toInt()
        )
    }

    private fun onCodeDetected(code: String, rect: RectF) =
        ContextCompat.getMainExecutor(context).execute {
            if (analysisEnabled && camera != null) {
                analysisEnabled = false
                detectedBounds = ComposeRect(rect.left, rect.top, rect.right, rect.bottom)
                onResult?.invoke(code)
            }
        }

    internal fun clearDetection() {
        detectedBounds = null
    }

    fun setScanArea(area: ComposeRect?) {
        scanArea = area
    }

    suspend fun takePicture(): String? {
        if (!isReady || useCase != UseCases.IMAGE_CAPTURE) return null

        val file = File(context.filesDir, "${UUID.randomUUID()}_product.jpg")
        return try {
            withContext(Dispatchers.IO) {
                file.outputStream().use {
                    imageCapture.takePicture(ImageCapture.OutputFileOptions.Builder(it).build())
                }
            }
            file.name
        } catch (e: CancellationException) {
            file.delete()
            throw e
        } catch (_: Exception) {
            file.delete()
            null
        }
    }

    fun tapToFocus(coordinates: Offset) {
        val control = camera?.cameraControl ?: return
        val point = surfaceMeteringPointFactory.createPoint(coordinates.x, coordinates.y)
        val action = FocusMeteringAction.Builder(point, FocusMeteringAction.FLAG_AF)
            .setAutoCancelDuration(2, TimeUnit.SECONDS)
            .build()

        control.startFocusAndMetering(action)
    }

    fun setZoom() {
        val current = camera ?: return
        val zoom = current.cameraInfo.zoomState.value ?: return
        val ratio = if (zoom.zoomRatio <= 1f) 2f else 1f

        current.cameraControl.setZoomRatio(ratio.coerceIn(zoom.minZoomRatio, zoom.maxZoomRatio))
    }

    fun setZoom(zoom: Float) {
        val current = camera ?: return
        val state = current.cameraInfo.zoomState.value ?: return

        current.cameraControl.setZoomRatio((state.zoomRatio * zoom).coerceIn(state.minZoomRatio, state.maxZoomRatio))
    }

    fun toggleTorch() {
        val current = camera ?: return
        if (current.cameraInfo.hasFlashUnit()) {
            current.cameraControl.enableTorch(current.cameraInfo.torchState.value != TorchState.ON)
        }
    }

    enum class UseCases { IMAGE_ANALYSIS, IMAGE_CAPTURE }

    companion object {
        internal const val SCAN_AREA_FRACTION = 0.7f
    }
}

@Composable
fun rememberCameraConfig(
    useCase: CameraConfig.UseCases,
    analysisEnabled: Boolean = true,
    onResult: ((String) -> Unit)? = null
): CameraConfig {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val cameraConfig = remember(context, useCase) { CameraConfig(context, useCase) }

    SideEffect {
        cameraConfig.analysisEnabled = analysisEnabled
        cameraConfig.onResult = onResult
        if (analysisEnabled) {
            cameraConfig.clearDetection()
        }
    }
    DisposableEffect(cameraConfig) {
        onDispose {
            cameraConfig.analysisEnabled = false
            cameraConfig.onResult = null
        }
    }
    LaunchedEffect(cameraConfig, lifecycleOwner) {
        cameraConfig.bindCamera(lifecycleOwner)
    }

    return cameraConfig
}