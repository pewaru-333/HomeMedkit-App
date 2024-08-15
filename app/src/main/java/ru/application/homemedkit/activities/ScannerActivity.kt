package ru.application.homemedkit.activities

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.provider.Settings
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ElevatedCard
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.core.content.ContextCompat.checkSelfPermission
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.budiyev.android.codescanner.CodeScanner
import com.budiyev.android.codescanner.CodeScannerView
import com.google.zxing.BarcodeFormat
import com.ramcosta.composedestinations.annotation.Destination
import com.ramcosta.composedestinations.annotation.RootGraph
import com.ramcosta.composedestinations.generated.destinations.MedicineScreenDestination
import com.ramcosta.composedestinations.generated.destinations.ScannerScreenDestination
import com.ramcosta.composedestinations.navigation.DestinationsNavigator
import ru.application.homemedkit.R.string.manual_add
import ru.application.homemedkit.R.string.text_connection_error
import ru.application.homemedkit.R.string.text_grant_permission
import ru.application.homemedkit.R.string.text_no
import ru.application.homemedkit.R.string.text_request_camera
import ru.application.homemedkit.R.string.text_try_again
import ru.application.homemedkit.R.string.text_yes
import ru.application.homemedkit.viewModels.ScannerViewModel
import ru.application.homemedkit.viewModels.ScannerViewModel.Response.AfterError
import ru.application.homemedkit.viewModels.ScannerViewModel.Response.Default
import ru.application.homemedkit.viewModels.ScannerViewModel.Response.Duplicate
import ru.application.homemedkit.viewModels.ScannerViewModel.Response.Error
import ru.application.homemedkit.viewModels.ScannerViewModel.Response.Loading
import ru.application.homemedkit.viewModels.ScannerViewModel.Response.NoNetwork
import ru.application.homemedkit.viewModels.ScannerViewModel.Response.Success

@Destination<RootGraph>
@Composable
fun ScannerScreen(navigator: DestinationsNavigator, context: Context = LocalContext.current) {
    val viewModel = viewModel<ScannerViewModel>()
    val response by viewModel.response.collectAsStateWithLifecycle()

    var permissionGranted by remember { mutableStateOf(checkCameraPermission(context)) }
    var showRationale by remember { mutableStateOf(false) }

    val lifecycle = LocalLifecycleOwner.current.lifecycle
    val launcher = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) {
        if (it) permissionGranted = true else showRationale = true
    }

    DisposableEffect(LocalLifecycleOwner.current) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                permissionGranted = checkCameraPermission(context)
                showRationale = !permissionGranted
            }
        }
        lifecycle.addObserver(observer)
        onDispose { lifecycle.removeObserver(observer) }
    }

    when (permissionGranted) {
        false -> {
            LaunchedEffect(Unit) { launcher.launch(Manifest.permission.CAMERA) }
            if (showRationale) PermissionDialog(text_request_camera)
        }

        true -> {
            AndroidView(
                factory = {
                    val scannerView = CodeScannerView(it).apply {
                        frameCornersRadius = 48.dp.value.toInt()
                        frameThickness = 16.dp.value.toInt()
                        isMaskVisible = true
                    }

                    CodeScanner(it, scannerView).apply {
                        setErrorCallback { viewModel.throwError() }
                        setDecodeCallback { result ->
                            if (result.barcodeFormat == BarcodeFormat.DATA_MATRIX)
                                viewModel.fetchData(it, result.text.substring(1))
                            else viewModel.throwError()
                        }

                        startPreview()
                    }

                    scannerView
                }
            )
        }
    }

    when (val data = response) {
        Default -> {}
        Loading -> LoadingDialog()
        is Duplicate -> navigator.navigate(MedicineScreenDestination(id = data.id, duplicate = true))
        is Success -> navigator.navigate(MedicineScreenDestination(data.id))
        is NoNetwork -> AddMedicineDialog(navigator, data.cis)
        Error -> Snackbar(text_try_again)
        AfterError -> LaunchedEffect(Unit) { navigator.navigate(ScannerScreenDestination) }
    }
}

@Composable
fun Snackbar(id: Int) = Dialog({}, DialogProperties(usePlatformDefaultWidth = false)) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .background(Color.Transparent),
        contentAlignment = Alignment.BottomCenter
    ){
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .height(48.dp)
                .background(MaterialTheme.colorScheme.errorContainer, RoundedCornerShape(4.dp)),
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = stringResource(id),
                modifier = Modifier.padding(start = 16.dp),
                color = MaterialTheme.colorScheme.onErrorContainer,
                style = MaterialTheme.typography.labelLarge
            )
        }
    }
}

@Composable
private fun AddMedicineDialog(navigator: DestinationsNavigator, cis: String) = AlertDialog(
    onDismissRequest = { navigator.navigate(ScannerScreenDestination) },
    confirmButton = {
        TextButton({ navigator.navigate(MedicineScreenDestination(cis = cis)) }) {
            Text(stringResource(text_yes), color = MaterialTheme.colorScheme.onSecondaryContainer)
        }
    },
    dismissButton = {
        TextButton({ navigator.navigate(ScannerScreenDestination) }) {
            Text(stringResource(text_no), color = MaterialTheme.colorScheme.onSecondaryContainer)
        }
    },
    title = {
        Text(
            text = stringResource(text_connection_error),
            style = MaterialTheme.typography.titleMedium.copy(
                fontSize = 18.sp,
                fontWeight = FontWeight.SemiBold
            )
        )
    },
    text = { Text(stringResource(manual_add), style = MaterialTheme.typography.bodyLarge) },
    containerColor = MaterialTheme.colorScheme.secondaryContainer,
    titleContentColor = MaterialTheme.colorScheme.onSecondaryContainer,
    textContentColor = MaterialTheme.colorScheme.onSecondaryContainer
)

@Composable
fun LoadingDialog() = Dialog({}) {
    Box(Modifier.fillMaxSize(), Alignment.Center)
    { CircularProgressIndicator() }
}

@Composable
fun PermissionDialog(id: Int, context: Context = LocalContext.current) {
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