package ru.application.homemedkit.ui.screens

import androidx.camera.compose.CameraXViewfinder
import androidx.camera.viewfinder.compose.MutableCoordinateTransformer
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.gestures.rememberTransformableState
import androidx.compose.foundation.gestures.transformable
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.pointerInput
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import ru.application.homemedkit.utils.camera.CameraConfig

@Composable
fun CameraPreview(cameraConfig: CameraConfig, modifier: Modifier = Modifier) {
    val currentSurface by cameraConfig.surfaceRequests.collectAsStateWithLifecycle()

    val coordinateTransformer = remember(::MutableCoordinateTransformer)

    val currentFocus by rememberUpdatedState(cameraConfig::tapToFocus)

    val zoomState = rememberTransformableState { zoomChange, _, _ ->
        cameraConfig.setZoom(zoomChange)
    }

    currentSurface?.let { surface ->
        CameraXViewfinder(
            surfaceRequest = surface,
            coordinateTransformer = coordinateTransformer,
            modifier = modifier
                .transformable(zoomState)
                .pointerInput(coordinateTransformer) {
                    detectTapGestures(
                        onTap = { offset ->
                            with(coordinateTransformer) {
                                currentFocus(offset.transform())
                            }
                        },
                        onDoubleTap = { _ ->
                            cameraConfig.setZoom()
                        }
                    )
                }
        )
    }
}