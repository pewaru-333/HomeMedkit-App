package ru.application.homemedkit.ui.screens

import androidx.camera.compose.CameraXViewfinder
import androidx.camera.viewfinder.compose.MutableCoordinateTransformer
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.gestures.rememberTransformableState
import androidx.compose.foundation.gestures.transformable
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.Matrix
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.unit.IntSize
import ru.application.homemedkit.utils.camera.CameraConfig
import kotlin.math.min

@Composable
fun CameraPreview(config: CameraConfig, modifier: Modifier = Modifier, onCodeBounds: (Rect?) -> Unit = {}) {
    val currentSurface = config.surfaceRequest

    val coordinateTransformer = remember(currentSurface, ::MutableCoordinateTransformer)
    var viewSize by remember(currentSurface) { mutableStateOf(IntSize.Zero) }

    if (config.isScanning && currentSurface != null) {
        val currentOnCodeBounds by rememberUpdatedState(onCodeBounds)

        LaunchedEffect(config, currentSurface, coordinateTransformer) {
            try {
                snapshotFlow {
                    val transform = coordinateTransformer.transformMatrix
                    val bounds = config.detectedBounds
                    val crop = config.previewCrop
                    val size = viewSize

                    if (bounds == null || crop == null || size.width <= 0 || size.height <= 0) {
                        null
                    } else {
                        val inverse = Matrix(transform.values.copyOf())
                        inverse.invert()

                        val rect = inverse.map(Rect(crop.left + bounds.left * crop.width, crop.top + bounds.top * crop.height, crop.left + bounds.right * crop.width, crop.top + bounds.bottom * crop.height))
                        Rect(rect.left / size.width, rect.top / size.height, rect.right / size.width, rect.bottom / size.height)
                            .intersect(Rect(0f, 0f, 1f, 1f))
                            .takeUnless(Rect::isEmpty)
                    }
                }.collect { currentOnCodeBounds(it) }
            } finally {
                currentOnCodeBounds(null)
            }
        }

        LaunchedEffect(config, currentSurface, coordinateTransformer) {
            try {
                snapshotFlow {
                    val matrix = coordinateTransformer.transformMatrix
                    val crop = config.previewCrop
                    val size = viewSize

                    if (crop == null || size.width <= 0 || size.height <= 0) {
                        null
                    } else {
                        val width = size.width.toFloat()
                        val height = size.height.toFloat()
                        val half = min(width, height) * CameraConfig.SCAN_AREA_FRACTION / 2
                        val area = matrix.map(Rect(width / 2 - half, height / 2 - half, width / 2 + half, height / 2 + half))

                        Rect((area.left - crop.left) / crop.width, (area.top - crop.top) / crop.height, (area.right - crop.left) / crop.width, (area.bottom - crop.top) / crop.height)
                            .intersect(Rect(0f, 0f, 1f, 1f))
                            .takeUnless(Rect::isEmpty)
                    }
                }.collect { area ->
                    config.setScanArea(area)
                }
            } finally {
                config.setScanArea(null)
            }
        }
    }

    val zoomState = rememberTransformableState { _, zoomChange, _, _ ->
        if (zoomChange != 1f) {
            config.setZoom(zoomChange)
        }
    }

    currentSurface?.let { surface ->
        CameraXViewfinder(
            surfaceRequest = surface,
            coordinateTransformer = coordinateTransformer,
            modifier = modifier
                .onSizeChanged { viewSize = it }
                .transformable(zoomState)
                .pointerInput(config, coordinateTransformer) {
                    detectTapGestures(
                        onTap = { offset ->
                            with(coordinateTransformer) {
                                config.tapToFocus(offset.transform())
                            }
                        },
                        onDoubleTap = { _ ->
                            config.setZoom()
                        }
                    )
                }
        )
    }
}