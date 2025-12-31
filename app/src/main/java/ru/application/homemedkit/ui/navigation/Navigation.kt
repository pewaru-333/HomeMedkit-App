package ru.application.homemedkit.ui.navigation

import androidx.compose.animation.ContentTransform
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.systemBars
import androidx.compose.material3.Scaffold
import androidx.compose.material3.ShortNavigationBar
import androidx.compose.material3.ShortNavigationBarItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation3.runtime.NavKey
import androidx.navigation3.runtime.entryProvider
import androidx.navigation3.ui.NavDisplay
import ru.application.homemedkit.models.viewModels.IntakeViewModel
import ru.application.homemedkit.models.viewModels.MedicineViewModel
import ru.application.homemedkit.ui.elements.VectorIcon
import ru.application.homemedkit.ui.screens.IntakeScreen
import ru.application.homemedkit.ui.screens.IntakesScreen
import ru.application.homemedkit.ui.screens.MedicineScreen
import ru.application.homemedkit.ui.screens.MedicinesScreen
import ru.application.homemedkit.ui.screens.ScannerScreen
import ru.application.homemedkit.ui.screens.SettingsScreen
import ru.application.homemedkit.utils.di.Preferences

@Composable
fun Navigation() {
    val barVisibility = LocalBarVisibility.current

    val navigationState = rememberNavigationState(
        startRoute = Preferences.startPage.route,
        topLevelRoutes = TOP_LEVEL_DESTINATIONS.keys
    )

    val navigator = remember { Navigator(navigationState) }

    Scaffold(
        content = { AppNavDisplay(navigator, navigationState, Modifier.padding(it)) },
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
                    SettingsScreen()
                }

                // Screens //
                entry<Screen.Scanner> {
                    ScannerScreen(
                        onBack = navigator::goBack,
                        onGoToMedicine = { id, cis, duplicate ->
                            navigator.navigate(Screen.Medicine(id, cis, duplicate))
                        }
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