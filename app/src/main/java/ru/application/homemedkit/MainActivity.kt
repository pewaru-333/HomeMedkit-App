package ru.application.homemedkit

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navDeepLink
import androidx.navigation.toRoute
import ru.application.homemedkit.helpers.Intake
import ru.application.homemedkit.helpers.Intakes
import ru.application.homemedkit.helpers.KEY_EXP_IMP
import ru.application.homemedkit.helpers.Medicine
import ru.application.homemedkit.helpers.Medicines
import ru.application.homemedkit.helpers.Menu
import ru.application.homemedkit.helpers.Preferences
import ru.application.homemedkit.helpers.Scanner
import ru.application.homemedkit.helpers.Settings
import ru.application.homemedkit.helpers.isCurrentRoute
import ru.application.homemedkit.helpers.showToast
import ru.application.homemedkit.receivers.AlarmSetter
import ru.application.homemedkit.ui.screens.IntakeScreen
import ru.application.homemedkit.ui.screens.IntakesScreen
import ru.application.homemedkit.ui.screens.MedicineScreen
import ru.application.homemedkit.ui.screens.MedicinesScreen
import ru.application.homemedkit.ui.screens.ScannerScreen
import ru.application.homemedkit.ui.screens.SettingsScreen
import ru.application.homemedkit.ui.theme.AppTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        Preferences.changeLanguage(this, Preferences.getLanguage())
        super.onCreate(savedInstanceState)

        actionBar?.hide()

        if (intent.getBooleanExtra(KEY_EXP_IMP, false)) {
            showToast(true, this)
            AlarmSetter(this).resetAll()
        }

        setContent {
            val navigator = rememberNavController()
            val backStack by navigator.currentBackStackEntryAsState()

            val theme by Preferences.theme.collectAsStateWithLifecycle()
            val dynamicColors by Preferences.dynamicColors.collectAsStateWithLifecycle()

            AppTheme(theme, dynamicColors) {
                Scaffold(
                    bottomBar = {
                        AnimatedVisibility(
                            visible = Menu.entries.any { backStack.isCurrentRoute(it.route::class) },
                            enter = expandVertically(),
                            exit = shrinkVertically()
                        ) {
                            NavigationBar {
                                Menu.entries.forEach { screen ->
                                    NavigationBarItem(
                                        icon = { Icon(painterResource(screen.icon), null) },
                                        label = { Text(stringResource(screen.title)) },
                                        selected = backStack.isCurrentRoute(screen.route::class),
                                        onClick = {
                                            navigator.navigate(screen.route) {
                                                launchSingleTop = true
                                                restoreState = true

                                                popUpTo(navigator.graph.findStartDestination().id) {
                                                    saveState = true
                                                }
                                            }
                                        }
                                    )
                                }
                            }
                        }
                    }
                ) { values ->
                    NavHost(navigator, Medicines, Modifier.padding(values)) {
                        // Bottom menu items //
                        composable<Medicines> {
                            MedicinesScreen(
                                navigateToScanner = { navigator.navigate(Scanner) },
                                navigateToMedicine = { navigator.navigate(Medicine(it)) }
                            )
                        }
                        composable<Intakes> { _ ->
                            IntakesScreen { navigator.navigate(Intake(it)) }
                        }
                        composable<Settings> {
                            SettingsScreen()
                        }

                        // Screens //
                        composable<Scanner> {
                            ScannerScreen(
                                navigateUp = navigator::navigateUp,
                                navigateToMedicine = { id, cis, duplicate ->
                                    navigator.navigate(Medicine(id, cis, duplicate))
                                }
                            )
                        }
                        composable<Medicine>(
                            deepLinks = listOf(navDeepLink<Medicine>("app://medicines/{id}"))
                        ) { back ->
                            val args = back.toRoute<Medicine>()

                            MedicineScreen(
                                id = args.id,
                                cis = args.cis,
                                duplicate = args.duplicate,
                                navigateBack = { navigator.popBackStack(Medicines, false) },
                                navigateToIntake = { navigator.navigate(Intake(medicineId = it)) }
                            )
                        }
                        composable<Intake> {
                            val args = it.toRoute<Intake>()

                            IntakeScreen(
                                intakeId = args.intakeId,
                                medicineId = args.medicineId,
                                navigateUp = {
                                    navigator.navigate(Intakes) {
                                        popUpTo<Medicines> { inclusive = true }
                                    }
                                }
                            )
                        }
                    }
                }
            }
        }
    }
}