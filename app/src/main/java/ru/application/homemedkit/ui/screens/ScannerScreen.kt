package ru.application.homemedkit.ui.screens

import android.Manifest
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Snackbar
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarResult
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.RoundRect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.ClipOp
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.clipPath
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import kotlinx.coroutines.flow.collectLatest
import ru.application.homemedkit.R
import ru.application.homemedkit.models.events.ScannerEvent
import ru.application.homemedkit.models.states.ScannerState
import ru.application.homemedkit.models.viewModels.ScannerViewModel
import ru.application.homemedkit.ui.elements.BoxLoading
import ru.application.homemedkit.ui.elements.IconButton
import ru.application.homemedkit.ui.elements.Image
import ru.application.homemedkit.ui.elements.VectorIcon
import ru.application.homemedkit.ui.navigation.Screen
import ru.application.homemedkit.utils.BLANK
import ru.application.homemedkit.utils.camera.CameraConfig
import ru.application.homemedkit.utils.camera.rememberCameraConfig
import ru.application.homemedkit.utils.di.Preferences
import ru.application.homemedkit.utils.extensions.vibrate
import ru.application.homemedkit.utils.permissions.PermissionState
import ru.application.homemedkit.utils.permissions.rememberPermissionState

@Composable
fun ScannerScreen(model: ScannerViewModel = viewModel(), onBack: () -> Unit, onNavigate: (Screen) -> Unit) {
    val context = LocalContext.current
    val filesDir = context.filesDir

    val state by model.state.collectAsStateWithLifecycle()

    val cameraPermission = rememberPermissionState(Manifest.permission.CAMERA)
    val controller = rememberCameraConfig(CameraConfig.UseCases.IMAGE_ANALYSIS) {
        if (state == ScannerState.Default) {
            model.fetch(filesDir, it)
        }
    }

    val snackbarHost = remember(::SnackbarHostState)

    val overlayColor = MaterialTheme.colorScheme.scrim.copy(alpha = 0.6f)

    LaunchedEffect(model.event) {
        model.event.collectLatest { result ->
            when (result) {
                is ScannerEvent.ShowSnackbar -> {
                    if (Preferences.useVibrationScan) {
                        context.vibrate(200L)
                    }

                    val result = snackbarHost.showSnackbar(context.getString(result.message))
                    if (result == SnackbarResult.Dismissed) {
                        model.setDefault()
                    }
                }

                is ScannerEvent.Navigate -> {
                    if (Preferences.useVibrationScan) {
                        context.vibrate(150L)
                    }

                    onNavigate(Screen.Medicine(result.id, BLANK, result.duplicate))
                }
            }
        }
    }

    BackHandler(onBack = onBack)
    if (cameraPermission.isGranted) {
        Box(Modifier.fillMaxSize()) {
            Scaffold(
                snackbarHost = {
                    SnackbarHost(snackbarHost) {
                        Snackbar(
                            snackbarData = it,
                            containerColor = MaterialTheme.colorScheme.errorContainer,
                            contentColor = MaterialTheme.colorScheme.onErrorContainer,
                        )
                    }
                }
            ) {
                CameraPreview(controller, Modifier.fillMaxSize())
                Canvas(Modifier.fillMaxSize()) {
                    val frameSize = size.minDimension * 0.7f
                    val framePath = Path().apply {
                        addRoundRect(
                            RoundRect(
                                cornerRadius = CornerRadius(16.dp.toPx()),
                                rect = Rect(
                                    offset = Offset(center.x - frameSize / 2, center.y - frameSize / 2),
                                    size = Size(frameSize, frameSize)
                                )
                            )
                        )
                    }

                    clipPath(framePath, ClipOp.Difference) { drawRect(overlayColor) }
                    drawPath(framePath, Color.White, style = Stroke(2.dp.toPx()))
                }
                Row(Modifier.fillMaxWidth(), Arrangement.End, Alignment.CenterVertically) {
                    IconButton(controller::toggleTorch, Modifier.padding(it)) {
                        VectorIcon(R.drawable.vector_flash, Modifier, Color.White)
                    }
                }
            }

            when (val result = state) {
                ScannerState.Default, ScannerState.Idle -> Unit
                ScannerState.Loading -> BoxLoading()
                is ScannerState.ShowDialog -> DialogMedicineAddition(model::setDefault) {
                    result.code?.let { onNavigate(Screen.Medicine(0L, it, false)) }
                }
            }
        }
    } else if (cameraPermission.showRationale) {
        DialogPermission(cameraPermission, onBack)
    } else {
        FirstTimeScreen(onBack, cameraPermission::launchRequest)
    }
}

@Composable
private fun FirstTimeScreen(navigateUp: () -> Unit, onPermissionGrant: () -> Unit) =
    Column(
        verticalArrangement = Arrangement.SpaceBetween,
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .fillMaxSize()
            .padding(12.dp)
            .verticalScroll(rememberScrollState())
    ) {
        Column(Modifier, Arrangement.spacedBy(8.dp), Alignment.CenterHorizontally) {
            Image(R.drawable.vector_camera, Modifier.size(64.dp))
            Text(
                text = stringResource(R.string.text_pay_attention),
                style = MaterialTheme.typography.titleLarge
            )
            Text(
                text = stringResource(R.string.text_explain_camera),
                textAlign = TextAlign.Center,
                style = MaterialTheme.typography.bodyMedium
            )
        }
        Row(Modifier.fillMaxWidth(), Arrangement.SpaceAround, Alignment.CenterVertically) {
            Column(Modifier.weight(1f), Arrangement.spacedBy(12.dp), Alignment.CenterHorizontally) {
                Image(
                    image = R.drawable.vector_barcode,
                    modifier = Modifier.size(128.dp),
                    contentScale = ContentScale.Crop,
                    colorFilter = ColorFilter.tint(MaterialTheme.colorScheme.onSurface)
                )
                Text(
                    text = stringResource(R.string.text_explain_ean_13),
                    textAlign = TextAlign.Center,
                    style = MaterialTheme.typography.labelLarge
                )
            }

            Spacer(Modifier.width(8.dp))

            Column(Modifier.weight(1f), Arrangement.spacedBy(12.dp), Alignment.CenterHorizontally) {
                Image(
                    image = R.drawable.vector_datamatrix,
                    modifier = Modifier.size(128.dp),
                    contentScale = ContentScale.Crop,
                    colorFilter = ColorFilter.tint(MaterialTheme.colorScheme.onSurface)
                )
                Text(
                    text = stringResource(R.string.text_explain_datamatrix),
                    textAlign = TextAlign.Center,
                    style = MaterialTheme.typography.labelLarge
                )
            }
        }
        Row(Modifier.fillMaxWidth(), Arrangement.SpaceBetween, Alignment.CenterVertically) {
            TextButton(navigateUp) { Text(stringResource(R.string.text_exit)) }
            Button(onPermissionGrant) { Text(stringResource(R.string.text_grant)) }
        }
    }

@Composable
private fun DialogMedicineAddition(onDismiss: () -> Unit, onNavigate: () -> Unit) = AlertDialog(
    onDismissRequest = onDismiss,
    confirmButton = { TextButton(onNavigate) { Text(stringResource(R.string.text_yes)) } },
    dismissButton = { TextButton(onDismiss) { Text(stringResource(R.string.text_no)) } },
    title = { Text(stringResource(R.string.text_connection_error)) },
    icon = { VectorIcon(R.drawable.vector_info) },
    text = {
        Text(
            text = stringResource(R.string.manual_add),
            style = MaterialTheme.typography.bodyLarge
        )
    }
)

@Composable
private fun DialogPermission(permission: PermissionState, onDismiss: () -> Unit) = Dialog(onDismiss) {
    ElevatedCard {
        Text(
            text = stringResource(R.string.text_request_camera),
            modifier = Modifier.padding(16.dp),
            style = MaterialTheme.typography.bodyLarge
        )
        TextButton(permission::launchRequest, Modifier.fillMaxWidth()) {
            Text(stringResource(R.string.text_permission_grant_full))
        }
    }
}