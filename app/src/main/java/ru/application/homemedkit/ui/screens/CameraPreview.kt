package ru.application.homemedkit.ui.screens

import androidx.camera.view.PreviewView
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.compose.LocalLifecycleOwner
import ru.application.homemedkit.models.states.CameraState

@Composable
fun CameraPreview(cameraState: CameraState, modifier: Modifier = Modifier) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current

    AndroidView(
        modifier = modifier,
        factory = {
            PreviewView(context).apply {
                controller = cameraState.controller.apply {
                    bindToLifecycle(lifecycleOwner)
                }
            }
        }
    )
}