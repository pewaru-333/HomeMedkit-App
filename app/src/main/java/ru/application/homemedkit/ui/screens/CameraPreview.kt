package ru.application.homemedkit.ui.screens

import androidx.camera.view.PreviewView
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.compose.LocalLifecycleOwner
import ru.application.homemedkit.utils.camera.CameraConfig

@Composable
fun CameraPreview(cameraConfig: CameraConfig, modifier: Modifier = Modifier) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current

    AndroidView(
        modifier = modifier,
        factory = {
            PreviewView(context).apply {
                controller = cameraConfig.bindToLifecycle(lifecycleOwner)
            }
        }
    )
}