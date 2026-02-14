package ru.application.homemedkit.ui.navigation

import androidx.activity.ComponentActivity
import androidx.activity.compose.LocalActivity
import androidx.compose.animation.ContentTransform
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.systemBars
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Scaffold
import androidx.compose.material3.ShortNavigationBar
import androidx.compose.material3.ShortNavigationBarItem
import androidx.compose.material3.Snackbar
import androidx.compose.material3.SnackbarData
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation3.runtime.NavKey
import androidx.navigation3.runtime.entryProvider
import androidx.navigation3.ui.NavDisplay
import androidx.work.WorkInfo
import kotlinx.coroutines.flow.collectLatest
import ru.application.homemedkit.R
import ru.application.homemedkit.models.viewModels.AuthViewModel
import ru.application.homemedkit.models.viewModels.IntakeViewModel
import ru.application.homemedkit.models.viewModels.MainViewModel
import ru.application.homemedkit.models.viewModels.MedicineViewModel
import ru.application.homemedkit.ui.elements.VectorIcon
import ru.application.homemedkit.ui.screens.AuthScreen
import ru.application.homemedkit.ui.screens.IntakeFullScreen
import ru.application.homemedkit.ui.screens.IntakeScreen
import ru.application.homemedkit.ui.screens.IntakesScreen
import ru.application.homemedkit.ui.screens.MedicineScreen
import ru.application.homemedkit.ui.screens.MedicinesScreen
import ru.application.homemedkit.ui.screens.ScannerScreen
import ru.application.homemedkit.ui.screens.SettingsScreen

@Composable
fun Navigation(model: MainViewModel = viewModel()) {
    val context = LocalContext.current
    val activity = LocalActivity.current as? ComponentActivity
    val barVisibility = LocalBarVisibility.current

    val startRoute = model.getDeepLink(activity?.intent?.data)

    val navigationState = rememberNavigationState(
        startRoute = startRoute,
        topLevelRoutes = TOP_LEVEL_DESTINATIONS.keys,
        deepLink = startRoute
    )

    val navigator = remember { Navigator(navigationState) }

    val snackbarHost = remember(::SnackbarHostState)

    val syncWorkStatus by model.syncWorkState.collectAsStateWithLifecycle()

    LaunchedEffect(model.snackbarEvent) {
        model.snackbarEvent.collectLatest { workStatus ->
            when (workStatus) {
                WorkInfo.State.ENQUEUED, WorkInfo.State.RUNNING -> snackbarHost.showSnackbar(
                    message = context.getString(R.string.text_sync),
                    duration = SnackbarDuration.Indefinite
                )

                WorkInfo.State.SUCCEEDED -> snackbarHost.showSnackbar(
                    message = context.getString(R.string.text_sync_success),
                    duration = SnackbarDuration.Short
                )

                WorkInfo.State.FAILED, WorkInfo.State.CANCELLED -> snackbarHost.showSnackbar(
                    message = context.getString(R.string.text_sync_error),
                    duration = SnackbarDuration.Short
                )

                else -> snackbarHost.currentSnackbarData?.dismiss()
            }
        }
    }

    Scaffold(
        content = { AppNavDisplay(navigator, navigationState, Modifier.padding(it)) },
        snackbarHost = {
            SnackbarHost(
                hostState = snackbarHost,
                snackbar = { SnackbarSync(it, syncWorkStatus) }
            )
        },
        bottomBar = {
            if (barVisibility.isVisible && navigationState.currentRoute in TOP_LEVEL_DESTINATIONS.keys) {
                BottomNavigationBar(
                    selected = navigationState.topLevelRoute,
                    onSelect = navigator::navigate
                )
            }
        }
    )
}

@Composable
private fun AppNavDisplay(navigator: Navigator, state: NavigationState, modifier: Modifier) =
    NavDisplay(
        modifier = modifier.consumeWindowInsets(WindowInsets.systemBars),
        onBack = navigator::goBack,
        entries = state.toEntries(
            entryProvider = entryProvider {
                // Bottom menu items //
                entry<Screen.Medicines>(
                    metadata = NavDisplay.predictivePopTransitionSpec {
                        ContentTransform(
                            targetContentEnter = fadeIn(animationSpec = tween()),
                            initialContentExit = fadeOut(animationSpec = tween()),
                        )
                    }
                ) {
                    MedicinesScreen(onNavigate = navigator::navigate)
                }
                entry<Screen.Intakes>(
                    metadata = NavDisplay.predictivePopTransitionSpec {
                        ContentTransform(
                            targetContentEnter = fadeIn(animationSpec = tween()),
                            initialContentExit = fadeOut(animationSpec = tween()),
                        )
                    }
                ) {
                    IntakesScreen { navigator.navigate(Screen.Intake(intakeId = it)) }
                }
                entry<Screen.Settings>(
                    metadata = NavDisplay.predictivePopTransitionSpec {
                        ContentTransform(
                            targetContentEnter = fadeIn(animationSpec = tween()),
                            initialContentExit = fadeOut(animationSpec = tween()),
                        )
                    }
                ) {
                    SettingsScreen { navigator.navigate(Screen.Auth()) }
                }

                // Screens //
                entry<Screen.Auth>(
                    metadata = NavDisplay.predictivePopTransitionSpec {
                        ContentTransform(
                            targetContentEnter = fadeIn(animationSpec = tween()),
                            initialContentExit = fadeOut(animationSpec = tween()),
                        )
                    }
                ) {
                    AuthScreen(
                        model = viewModel { AuthViewModel(it.code) },
                        onBack = navigator::goBack
                    )
                }
                entry<Screen.Scanner> {
                    ScannerScreen(
                        onBack = navigator::goBack,
                        onNavigate = navigator::navigate
                    )
                }
                entry<Screen.Medicine> { (id, cis, duplicate) ->
                    MedicineScreen(
                        model = viewModel { MedicineViewModel(id, cis, duplicate) },
                        onBack = { navigator.navigateAndClearStack(Screen.Medicines) },
                        onGoToIntake = { navigator.navigate(Screen.Intake(medicineId = it)) }
                    )
                }
                entry<Screen.Intake> { (intakeId, medicineId) ->
                    IntakeScreen(
                        model = viewModel { IntakeViewModel(intakeId, medicineId) },
                        onBack = navigator::goBack
                    )
                }
                entry<Screen.IntakeFullScreen> { (takenId, medicineId, amount) ->
                    val onBack = when {
                        state.previousRoute is Screen.IntakeFullScreen -> navigator::goBack
                        state.currentStack.size <= 2 && state.currentStack.firstOrNull() is Screen.Intakes -> null
                        else -> navigator::goBack
                    }

                    IntakeFullScreen(medicineId, takenId, amount, onBack)
                }
            }
        )
    )

@Composable
private fun BottomNavigationBar(selected: NavKey, onSelect: (NavKey) -> Unit) =
    ShortNavigationBar {
        TOP_LEVEL_DESTINATIONS.forEach { (screen, bottomBarItem) ->
            ShortNavigationBarItem(
                icon = { VectorIcon(bottomBarItem.icon) },
                label = { Text(stringResource(bottomBarItem.title)) },
                selected = screen == selected,
                onClick = { onSelect(screen) }
            )
        }
    }

@Composable
private fun SnackbarSync(data: SnackbarData, workStatus: WorkInfo.State?) =
    Snackbar(
        modifier = Modifier.padding(12.dp),
        content = {
            Row(Modifier.fillMaxWidth(), Arrangement.SpaceBetween, Alignment.CenterVertically) {
                Text(data.visuals.message)

                if (workStatus == WorkInfo.State.RUNNING) {
                    CircularProgressIndicator(Modifier.size(24.dp))
                }
            }
        }
    )