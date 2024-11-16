package ru.application.homemedkit.ui.screens

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.provider.Settings
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.OptIn
import androidx.annotation.StringRes
import androidx.camera.core.TorchState
import androidx.camera.core.resolutionselector.ResolutionSelector
import androidx.camera.core.resolutionselector.ResolutionStrategy
import androidx.camera.view.CameraController
import androidx.camera.view.LifecycleCameraController
import androidx.camera.view.TransformExperimental
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.RoundRect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.ClipOp
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.clipPath
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.core.content.ContextCompat.checkSelfPermission
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.asExecutor
import ru.application.homemedkit.MainActivity
import ru.application.homemedkit.R
import ru.application.homemedkit.R.string.manual_add
import ru.application.homemedkit.R.string.text_connection_error
import ru.application.homemedkit.R.string.text_grant_permission
import ru.application.homemedkit.R.string.text_no
import ru.application.homemedkit.R.string.text_request_camera
import ru.application.homemedkit.R.string.text_try_again
import ru.application.homemedkit.R.string.text_yes
import ru.application.homemedkit.helpers.BLANK
import ru.application.homemedkit.helpers.DataMatrixAnalyzer
import ru.application.homemedkit.models.events.Response.Default
import ru.application.homemedkit.models.events.Response.Duplicate
import ru.application.homemedkit.models.events.Response.Error
import ru.application.homemedkit.models.events.Response.Loading
import ru.application.homemedkit.models.events.Response.NoNetwork
import ru.application.homemedkit.models.events.Response.Success
import ru.application.homemedkit.models.viewModels.ScannerViewModel

@OptIn(TransformExperimental::class)
@Composable
fun ScannerScreen(navigateUp: () -> Unit, navigateToMedicine: (Long, String, Boolean) -> Unit) {
    val model = viewModel<ScannerViewModel>()
    val response by model.response.collectAsStateWithLifecycle()

    val context = LocalContext.current

    var permissionGranted by remember { mutableStateOf(checkCameraPermission(context)) }
    var showRationale by remember { mutableStateOf(false) }

    val lifecycleOwner = LocalLifecycleOwner.current
    val lifecycle = lifecycleOwner.lifecycle
    val launcher = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) {
        if (it) permissionGranted = true else showRationale = true
    }

    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                permissionGranted = checkCameraPermission(context)
                showRationale = !permissionGranted
            }
        }
        lifecycle.addObserver(observer)
        onDispose { lifecycle.removeObserver(observer) }
    }

    val controller = remember {
        LifecycleCameraController(context).apply {
            setEnabledUseCases(CameraController.IMAGE_ANALYSIS)
            imageAnalysisResolutionSelector = ResolutionSelector.Builder().setResolutionStrategy(
                ResolutionStrategy(
                    android.util.Size(960,960),
                    ResolutionStrategy.FALLBACK_RULE_CLOSEST_HIGHER_THEN_LOWER
                )
            ).build()
        }
    }

    LaunchedEffect(lifecycleOwner, controller) {
        controller.unbind()
        controller.bindToLifecycle(lifecycleOwner)
    }

    BackHandler(onBack = navigateUp)
    if (permissionGranted) Box {
        CameraPreview(controller, Modifier.fillMaxSize())
        Canvas(Modifier.fillMaxSize()) {
            val length = if (size.width > size.height) size.height * 0.5f else size.width * 0.7f

            val frame = Path().apply {
                addRoundRect(
                    RoundRect(
                        cornerRadius = CornerRadius(16.dp.toPx()),
                        rect = Rect(
                            offset = Offset(center.x - length / 2, center.y - length / 2),
                            size = Size(length, length)
                        )
                    )
                )
            }
            clipPath(frame, ClipOp.Difference) { drawRect(Color.Black.copy(0.55f)) }
            drawPath(frame, Color.White, style = Stroke(5f))
        }
        Row(Modifier.fillMaxWidth(), Arrangement.End, Alignment.CenterVertically) {
            IconButton(
                onClick = {
                    controller.cameraControl?.enableTorch(controller.cameraInfo?.torchState?.value != TorchState.ON)
                }
            ) {
                Icon(
                    painter = painterResource(R.drawable.vector_flash),
                    contentDescription = null,
                    tint = Color.White
                )
            }
        }
    } else {
        LaunchedEffect(Unit) { launcher.launch(Manifest.permission.CAMERA) }
        if (showRationale) PermissionDialog(text_request_camera)
    }

    when (val data = response) {
        Default -> controller.setImageAnalysisAnalyzer(
            Dispatchers.Main.immediate.asExecutor(),
            DataMatrixAnalyzer { model.fetchData(context, it.substring(1)) }
        )

        Loading -> LoadingDialog()
        is Duplicate -> navigateToMedicine(data.id, BLANK, true)
        is Success -> navigateToMedicine(data.id, BLANK, false)
        is NoNetwork -> {
            controller.clearImageAnalysisAnalyzer()
            AddMedicineDialog(model::setDefault) { navigateToMedicine(0L, data.cis, false) }
        }

        Error -> {
            controller.clearImageAnalysisAnalyzer()
            Snackbar(text_try_again)
        }
    }
}

@Composable
fun Snackbar(id: Int) = Dialog({}, DialogProperties(usePlatformDefaultWidth = false)) {
    Box(
        contentAlignment = Alignment.BottomCenter,
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .background(Color.Transparent),
    ) {
        Column(
            verticalArrangement = Arrangement.Center,
            modifier = Modifier
                .fillMaxWidth()
                .height(48.dp)
                .background(
                    MaterialTheme.colorScheme.errorContainer,
                    MaterialTheme.shapes.extraSmall
                ),
        ) {
            Text(
                text = stringResource(id),
                modifier = Modifier.padding(start = 16.dp),
                color = MaterialTheme.colorScheme.onErrorContainer,
                style = MaterialTheme.typography.bodyMedium
            )
        }
    }
}

@Composable
private fun AddMedicineDialog(setDefault: () -> Unit, navigateWithCis: () -> Unit) = AlertDialog(
    onDismissRequest = setDefault,
    confirmButton = { TextButton(navigateWithCis) { Text(stringResource(text_yes)) } },
    dismissButton = { TextButton(setDefault) { Text(stringResource(text_no)) } },
    title = { Text(stringResource(text_connection_error)) },
    text = { Text(stringResource(manual_add)) },
    icon = { Icon(Icons.Outlined.Info, null) }
)

@Composable
fun LoadingDialog() = Dialog({}) {
    Box(Modifier.fillMaxSize(), Alignment.Center)
    { CircularProgressIndicator() }
}

@Composable
fun PermissionDialog(@StringRes id: Int, context: Context = LocalContext.current) {
    Dialog({ Intent(context, MainActivity::class.java).also(context::startActivity) }) {
        ElevatedCard {
            Text(
                text = stringResource(id),
                modifier = Modifier.padding(16.dp),
                style = MaterialTheme.typography.bodyLarge
            )
            TextButton(
                onClick = {
                    Intent(
                        Settings.ACTION_APPLICATION_DETAILS_SETTINGS,
                        Uri.fromParts("package", context.packageName, null)
                    )
                        .setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK)
                        .also(context::startActivity)
                },
                modifier = Modifier.fillMaxWidth()
            ) { Text(stringResource(text_grant_permission)) }
        }
    }
}

private fun checkCameraPermission(context: Context) =
    checkSelfPermission(context, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED