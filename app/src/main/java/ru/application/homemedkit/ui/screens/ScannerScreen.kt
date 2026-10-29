package ru.application.homemedkit.ui.screens

import android.Manifest
import androidx.activity.compose.BackHandler
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithCache
import androidx.compose.ui.geometry.*
import androidx.compose.ui.graphics.ClipOp
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.clipPath
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalResources
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.repeatOnLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import ru.application.homemedkit.R
import ru.application.homemedkit.models.events.ScannerEvent
import ru.application.homemedkit.models.states.ScannerState
import ru.application.homemedkit.models.viewModels.ScannerViewModel
import ru.application.homemedkit.ui.elements.IconButton
import ru.application.homemedkit.ui.elements.Image
import ru.application.homemedkit.ui.elements.VectorIcon
import ru.application.homemedkit.ui.navigation.Screen
import ru.application.homemedkit.utils.BLANK
import ru.application.homemedkit.utils.Code
import ru.application.homemedkit.utils.camera.CameraConfig
import ru.application.homemedkit.utils.camera.rememberCameraConfig
import ru.application.homemedkit.utils.di.Preferences
import ru.application.homemedkit.utils.extensions.vibrate
import ru.application.homemedkit.utils.permissions.PermissionState
import ru.application.homemedkit.utils.permissions.rememberPermissionState

@Composable
fun ScannerScreen(model: ScannerViewModel = viewModel(), onBack: () -> Unit, onNavigate: (Screen) -> Unit) {
    val context = LocalContext.current
    val resources = LocalResources.current
    val lifecycleOwner = LocalLifecycleOwner.current

    val currentOnNavigate by rememberUpdatedState(onNavigate)
    val state by model.state.collectAsStateWithLifecycle()

    val isReady = state == ScannerState.Ready
    val isPaused = state == ScannerState.Paused

    val cameraPermission = rememberPermissionState(Manifest.permission.CAMERA)
    val snackbarHost = remember(::SnackbarHostState)

    val overlayColor = MaterialTheme.colorScheme.scrim.copy(alpha = 0.6f)
    val frameColor = if (isPaused) MaterialTheme.colorScheme.error else Color.White

    val progress = remember { Animatable(0f) }
    var scannedCode by remember { mutableStateOf<Code?>(null) }
    var codeBounds by remember { mutableStateOf<Rect?>(null) }

    fun onLeave() {
        scannedCode = null
        model.cancel()
        onBack()
    }

    LaunchedEffect(model, scannedCode) {
        val code = scannedCode ?: return@LaunchedEffect

        progress.snapTo(0f)
        progress.animateTo(1f, tween(durationMillis = 250))

        if (scannedCode == code) {
            model.fetch(context.filesDir, code)
        }
    }

    LaunchedEffect(isReady, isPaused) {
        if (isReady || isPaused) {
            scannedCode = null
            progress.snapTo(0f)
        }
    }

    DisposableEffect(model) {
        onDispose { model.cancel() }
    }

    LaunchedEffect(model, lifecycleOwner) {
        lifecycleOwner.lifecycle.repeatOnLifecycle(Lifecycle.State.STARTED) {
            model.event.collect { result ->
                when (result) {
                    is ScannerEvent.ShowSnackbar -> {
                        if (Preferences.useVibrationScan) context.vibrate(200L)
                        try {
                            snackbarHost.showSnackbar(resources.getString(result.message))
                        } finally {
                            model.setDefault()
                        }
                    }
                    is ScannerEvent.Navigate -> {
                        if (Preferences.useVibrationScan) context.vibrate(150L)
                        currentOnNavigate(Screen.Medicine(result.id, BLANK, result.duplicate))
                    }
                }
            }
        }
    }

    BackHandler(onBack = ::onLeave)
    Scaffold(
        snackbarHost = {
            SnackbarHost(snackbarHost) {
                Snackbar(
                    snackbarData = it,
                    containerColor = MaterialTheme.colorScheme.errorContainer,
                    contentColor = MaterialTheme.colorScheme.onErrorContainer
                )
            }
        }
    ) { padding ->
        if (cameraPermission.isGranted) {
            val controller = rememberCameraConfig(
                useCase = CameraConfig.UseCases.IMAGE_ANALYSIS,
                analysisEnabled = isReady && scannedCode == null,
                onResult = { raw ->
                    val code = Code.parse(raw)
                    if (code != null) scannedCode = code
                    else model.fetch(context.filesDir, null)
                }
            )

            Box(Modifier.fillMaxSize()) {
                CameraPreview(
                    config = controller,
                    modifier = Modifier.fillMaxSize(),
                    onCodeBounds = { codeBounds = it }
                )
                Spacer(
                    modifier = Modifier
                        .fillMaxSize()
                        .drawWithCache {
                            val framePath = Path()
                            val frameStroke = Stroke(2.dp.toPx())
                            val frameSize = size.minDimension * CameraConfig.SCAN_AREA_FRACTION

                            val cornerRadius = 16.dp.toPx()
                            val minBarcodeSize = 48.dp.toPx()
                            val defaultBounds = Rect(
                                size = Size(frameSize, frameSize),
                                offset = Offset(
                                    x = (size.width - frameSize) / 2,
                                    y = (size.height - frameSize) / 2
                                )
                            )

                            onDrawBehind {
                                val rect = codeBounds
                                val target = if (rect == null) defaultBounds else {
                                    val minSize = if (scannedCode?.isEan13 == true) minBarcodeSize else 0f

                                    val width = (rect.width * size.width).coerceIn(minOf(minSize, size.width), size.width)
                                    val height = (rect.height * size.height).coerceIn(minOf(minSize, size.height), size.height)
                                    val left = (rect.center.x * size.width - width / 2).coerceIn(0f, size.width - width)
                                    val top = (rect.center.y * size.height - height / 2).coerceIn(0f, size.height - height)

                                    Rect(Offset(left, top), Size(width, height))
                                }

                                val bounds = lerp(defaultBounds, target, progress.value)
                                val radius = minOf(cornerRadius, bounds.width / 2, bounds.height / 2)

                                framePath.rewind()
                                framePath.addRoundRect(RoundRect(bounds, CornerRadius(radius)))

                                clipPath(framePath, ClipOp.Difference) { drawRect(overlayColor) }
                                drawPath(framePath, frameColor, style = frameStroke)
                            }
                        }
                )
                Row(
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(padding)
                ) {
                    IconButton(::onLeave) {
                        VectorIcon(R.drawable.vector_arrow_back, tint = Color.White)
                    }
                    if (controller.hasFlash) {
                        IconButton(controller::toggleTorch, enabled = controller.isReady) {
                            VectorIcon(R.drawable.vector_flash, tint = Color.White)
                        }
                    }
                }
                if (isPaused) {
                    Surface(
                        shape = MaterialTheme.shapes.large,
                        color = MaterialTheme.colorScheme.errorContainer,
                        contentColor = MaterialTheme.colorScheme.onErrorContainer,
                        modifier = Modifier
                            .align(Alignment.Center)
                            .padding(24.dp)
                    ) {
                        Row(Modifier.padding(16.dp, 12.dp), Arrangement.spacedBy(8.dp), Alignment.CenterVertically) {
                            VectorIcon(R.drawable.vector_info)
                            Text(stringResource(R.string.text_scanning_paused), style = MaterialTheme.typography.labelLarge)
                        }
                    }
                }
                when (val result = state) {
                    ScannerState.Loading, is ScannerState.DownloadingImages -> DialogProgress(
                        progress = result as? ScannerState.DownloadingImages,
                        onCancel = model::cancel
                    )
                    is ScannerState.ShowingDialog -> DialogMedicineAddition(model::setDefault) {
                        result.code?.let { currentOnNavigate(Screen.Medicine(cis = it)) }
                    }
                    is ScannerState.ConfirmProduct -> DialogEan13(
                        productName = result.model.productName ?: result.model.catalogData?.firstOrNull()?.name.orEmpty(),
                        onDismiss = model::setDefault,
                        onConfirm = { model.confirmAddition(context.filesDir) }
                    )
                    else -> Unit
                }
            }
        } else if (cameraPermission.showRationale) {
            DialogPermission(cameraPermission, onBack)
        } else {
            FirstTimeScreen(onBack, cameraPermission::launchRequest)
        }
    }
}

@Composable
private fun DialogProgress(progress: ScannerState.DownloadingImages?, onCancel: () -> Unit) =
    AlertDialog(
        onDismissRequest = onCancel,
        title = { Text(stringResource(R.string.text_processing_code)) },
        confirmButton = { TextButton(onCancel) { Text(stringResource(R.string.text_cancel)) } },
        text = {
            Column(Modifier.verticalScroll(rememberScrollState()), Arrangement.spacedBy(16.dp)) {
                Text(
                    style = MaterialTheme.typography.bodyLarge,
                    text = if (progress == null) stringResource(R.string.text_looking_up_medicine)
                    else buildString {
                        append(stringResource(R.string.text_downloading_images))
                        progress.percent?.let { append(" (${it}%)") }
                    }
                )
                if (progress != null && progress.total > 1) {
                    Text(
                        text = stringResource(R.string.text_downloading_images_count, progress.completed, progress.total),
                        style = MaterialTheme.typography.bodyLarge
                    )
                }

                val fraction = when {
                    progress == null -> null
                    progress.total > 1 -> progress.completed.toFloat() / progress.total
                    else -> progress.percent?.div(100f)
                }
                if (fraction == null) {
                    LinearProgressIndicator(Modifier.fillMaxWidth())
                } else {
                    LinearProgressIndicator(
                        progress = { fraction },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
        }
    )

@Composable
private fun DialogEan13(productName: String, onConfirm: () -> Unit, onDismiss: () -> Unit) =
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(R.string.text_attention)) },
        confirmButton = { TextButton(onConfirm) { Text(stringResource(R.string.text_add)) } },
        dismissButton = { TextButton(onDismiss) { Text(stringResource(R.string.text_cancel)) } },
        text = {
            Column(Modifier.verticalScroll(rememberScrollState()), Arrangement.spacedBy(16.dp)) {
                Text(productName, style = MaterialTheme.typography.bodyLarge)
                Text(stringResource(R.string.text_catalog_product_ean13), style = MaterialTheme.typography.bodyLarge)
            }
        }
    )

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
            modifier = Modifier.verticalScroll(rememberScrollState()),
            style = MaterialTheme.typography.bodyLarge
        )
    }
)

@Composable
private fun DialogPermission(permission: PermissionState, onDismiss: () -> Unit) = AlertDialog(
    onDismissRequest = onDismiss,
    title = { Text(stringResource(R.string.text_attention)) },
    confirmButton = { TextButton(permission::launchRequest) { Text(stringResource(R.string.text_grant)) } },
    dismissButton = { TextButton(onDismiss) { Text(stringResource(R.string.text_cancel)) } },
    text = {
        Text(
            text = stringResource(R.string.text_request_camera),
            modifier = Modifier.verticalScroll(rememberScrollState()),
            style = MaterialTheme.typography.bodyLarge
        )
    }
)