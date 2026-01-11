@file:OptIn(ExperimentalMaterial3ExpressiveApi::class, ExperimentalMaterial3Api::class)

package ru.application.homemedkit.ui.screens

import android.content.Intent
import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.LoadingIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.DialogProperties
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import kotlinx.coroutines.flow.collectLatest
import ru.application.homemedkit.R
import ru.application.homemedkit.models.viewModels.AuthViewModel
import ru.application.homemedkit.network.AuthStatus
import ru.application.homemedkit.network.Network
import ru.application.homemedkit.ui.elements.IconButton
import ru.application.homemedkit.ui.elements.NavigationIcon
import ru.application.homemedkit.ui.elements.VectorIcon
import ru.application.homemedkit.utils.di.Preferences
import ru.application.homemedkit.utils.enums.SyncMode

@Composable
fun AuthScreen(model: AuthViewModel, onBack: () -> Unit) {

    @Composable
    fun LocalProviderButton(
        onClick: () -> Unit,
        @DrawableRes icon: Int,
        @StringRes text: Int,
        enabled: Boolean = true
    ) {
        OutlinedButton(
            onClick = onClick,
            enabled = enabled,
            shapes = ButtonDefaults.shapes(),
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp)
                .padding(horizontal = 8.dp)
        ) {
            Image(
                painter = painterResource(icon),
                modifier = Modifier.size(24.dp),
                contentDescription = null
            )

            Spacer(Modifier.size(8.dp))

            Text(
                text = buildString {
                    append(stringResource(text))

                    if (!enabled) {
                        append(" ")
                        append(stringResource(R.string.text_in_progress))
                    }
                }
            )
        }
    }

    @Composable
    fun LocalActionButton(
        onClick: () -> Unit,
        enabled: Boolean,
        @StringRes text: Int,
        @DrawableRes icon: Int,
    ) = FilledTonalButton(
        onClick = onClick,
        enabled = enabled,
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 8.dp),
        content = {
            if (enabled) {
                VectorIcon(icon, Modifier.size(24.dp))
            } else {
                LoadingIndicator(Modifier.size(24.dp))
            }

            Spacer(Modifier.size(8.dp))

            Text(stringResource(text))
        }
    )

    val context = LocalContext.current

    val authStatus by model.state.collectAsStateWithLifecycle()
    val snackbarState = remember(::SnackbarHostState)

    LaunchedEffect(model.snackbarEvent) {
        model.snackbarEvent.collectLatest { message->
            snackbarState.showSnackbar(message.asString(context))
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarState) },
        topBar = {
            TopAppBar(
                subtitle = {},
                title = { Text(stringResource(R.string.text_sync)) },
                navigationIcon = { NavigationIcon(onBack) },
                actions = {
                    if (Preferences.token != null) {
                        IconButton(
                            onClick = { model.showExitDialog = true },
                            content = { VectorIcon(R.drawable.vector_logout) }
                        )
                    }
                }
            )
        }
    ) { padding ->
        when (authStatus) {
            AuthStatus.Loading -> {
                Box(
                    content = { LoadingIndicator() },
                    contentAlignment = Alignment.Center,
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(padding)
                )
            }

            AuthStatus.Error -> {
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(padding)
                ) {
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(stringResource(R.string.text_connection_error))
                        FilledTonalButton(model::checkConnection) {
                            Text(stringResource(R.string.text_retry))
                        }
                    }
                }
            }

            AuthStatus.Nothing -> {
                Column(
                    verticalArrangement = Arrangement.spacedBy(8.dp, Alignment.CenterVertically),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(padding)
                ) {
                    Text(
                        text = stringResource(R.string.text_pick_cloud_service),
                        textAlign = TextAlign.Center,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 8.dp)
                    )

                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        LocalProviderButton(
                            icon = R.drawable.vector_yandex,
                            text = R.string.text_yandex,
                            onClick = {
                                context.startActivity(
                                    Intent(Intent.ACTION_VIEW, Network.Yandex.authUri).apply {
                                        flags = Intent.FLAG_ACTIVITY_NEW_TASK
                                    }
                                )
                            }
                        )

                        LocalProviderButton(
                            onClick = {},
                            icon = R.drawable.vector_google,
                            text = R.string.text_google,
                            enabled = false
                        )
                    }
                }
            }

            AuthStatus.Success -> {
                val lastSync by model.lastSync.collectAsStateWithLifecycle()

                Column(
                    verticalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(padding)
                        .verticalScroll(rememberScrollState())
                ) {
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        Surface(
                            shape = CircleShape,
                            color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f),
                            modifier = Modifier.size(120.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Image(
                                    painter = painterResource(R.drawable.vector_yandex),
                                    contentDescription = null,
                                    modifier = Modifier.size(64.dp)
                                )
                            }
                        }

                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                text = stringResource(R.string.text_provider_is_yandex),
                                style = MaterialTheme.typography.headlineSmall.copy(
                                    fontWeight = FontWeight.Bold,
                                    textAlign = TextAlign.Center
                                )
                            )

                            Spacer(Modifier.height(8.dp))

                            Surface(
                                shape = RoundedCornerShape(50),
                                color = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.5f)
                            ) {
                                Row(
                                    modifier = Modifier.padding(12.dp, 6.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    VectorIcon(
                                        icon = R.drawable.vector_time,
                                        modifier = Modifier.size(16.dp),
                                        tint = MaterialTheme.colorScheme.onSecondaryContainer
                                    )
                                    Spacer(Modifier.width(8.dp))
                                    Text(
                                        text = stringResource(R.string.text_synchronized_at, lastSync.asString()),
                                        style = MaterialTheme.typography.labelLarge,
                                        color = MaterialTheme.colorScheme.onSecondaryContainer
                                    )
                                }
                            }
                        }
                    }

                    Column {
                        LocalActionButton(
                            onClick = { model.syncYandex(SyncMode.FORCE_UPLOAD) },
                            enabled = model.buttonEnabled,
                            text = R.string.text_force_upload,
                            icon = R.drawable.vector_upload
                        )
                        LocalActionButton(
                            onClick = { model.syncYandex(SyncMode.FORCE_DOWNLOAD) },
                            enabled = model.buttonEnabled,
                            text = R.string.text_force_download,
                            icon = R.drawable.vector_download
                        )
                    }
                }
            }
        }
    }

    when {
        model.showExitDialog -> DialogExit(model::logoutYandex) { model.showExitDialog = false }
        model.showSyncModeDialog -> {
            DialogSyncMode(
                onDismiss = { model.showSyncModeDialog = false },
                onUpload = { model.syncYandex(SyncMode.FORCE_UPLOAD) },
                onDownload = { model.syncYandex(SyncMode.FORCE_DOWNLOAD) }
            )
        }
    }
}

@Composable
private fun DialogExit(onExit: () -> Unit, onDismiss: () -> Unit) = AlertDialog(
    onDismissRequest = onDismiss,
    title = { Text(stringResource(R.string.text_exit_cloud)) },
    text = {
        Text(
            text = stringResource(R.string.text_exit_cloud_desc),
            style = MaterialTheme.typography.bodyLarge
        )
    },
    confirmButton = {
        TextButton(onExit) { Text(stringResource(R.string.text_confirm)) }
    },
    dismissButton = {
        TextButton(onDismiss) { Text(stringResource(R.string.text_cancel)) }
    }
)

@Composable
private fun DialogSyncMode(onDismiss: () -> Unit, onUpload: () -> Unit, onDownload: () -> Unit) =
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(R.string.text_data_in_cloud)) },
        text = {
            Text(
                text = stringResource(R.string.text_data_in_cloud_desc),
                style = MaterialTheme.typography.bodyLarge
            )
        },
        confirmButton = {
            TextButton(onUpload) { Text(stringResource(R.string.text_upload_to_cloud)) }
        },
        dismissButton = {
            TextButton(onDownload) { Text(stringResource(R.string.text_download_from_cloud)) }
        },
        properties = DialogProperties(
            dismissOnBackPress = false,
            dismissOnClickOutside = false
        )
    )