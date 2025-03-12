package ru.application.homemedkit.ui.navigation

import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.navigation.NavBackStackEntry
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navDeepLink
import ru.application.homemedkit.helpers.Menu
import ru.application.homemedkit.helpers.extensions.isCurrentRoute
import ru.application.homemedkit.helpers.extensions.toBottomBarItem
import ru.application.homemedkit.ui.navigation.Screen.Intake
import ru.application.homemedkit.ui.navigation.Screen.Intakes
import ru.application.homemedkit.ui.navigation.Screen.KitsManager
import ru.application.homemedkit.ui.navigation.Screen.Medicine
import ru.application.homemedkit.ui.navigation.Screen.Medicines
import ru.application.homemedkit.ui.navigation.Screen.PermissionsScreen
import ru.application.homemedkit.ui.navigation.Screen.Scanner
import ru.application.homemedkit.ui.navigation.Screen.Settings
import ru.application.homemedkit.ui.screens.IntakeScreen
import ru.application.homemedkit.ui.screens.IntakesScreen
import ru.application.homemedkit.ui.screens.KitsManager
import ru.application.homemedkit.ui.screens.MedicineScreen
import ru.application.homemedkit.ui.screens.MedicinesScreen
import ru.application.homemedkit.ui.screens.PermissionsScreen
import ru.application.homemedkit.ui.screens.ScannerScreen
import ru.application.homemedkit.ui.screens.SettingsScreen

@Composable
fun Navigation(navigator: NavHostController, modifier: Modifier) {
    NavHost(navigator, Medicines, modifier) {
        // Bottom menu items //
        composable<Medicines> {
            MedicinesScreen(
                navigateToScanner = { navigator.navigate(Scanner) },
                navigateToMedicine = { navigator.navigate(Medicine(it)) }
            )
        }
        composable<Intakes> { _ ->
            IntakesScreen(
                backClick = { navigator.toBottomBarItem(Medicines) },
                navigateToIntake = { navigator.navigate(Intake(intakeId = it)) },
            )
        }
        composable<Settings> {
            SettingsScreen(
                backClick = { navigator.toBottomBarItem(Intakes) },
                toKitsManager = { navigator.navigate(KitsManager) },
                toPermissionsScreen = { navigator.navigate(PermissionsScreen) }
            )
        }

        // Settings screens //
        composable<KitsManager>(
            enterTransition = { slideIntoContainer(AnimatedContentTransitionScope.SlideDirection.Start) },
            exitTransition = { slideOutOfContainer(AnimatedContentTransitionScope.SlideDirection.End) }
        ) {
            KitsManager(navigator::navigateUp)
        }

        composable<PermissionsScreen>(
            enterTransition = { slideIntoContainer(AnimatedContentTransitionScope.SlideDirection.Start) },
            exitTransition = { slideOutOfContainer(AnimatedContentTransitionScope.SlideDirection.End) }
        ) {
            PermissionsScreen(navigator::navigateUp, navigator::navigateUp)
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
            deepLinks = listOf(
                navDeepLink<Medicine>("app://medicines/{id}")
            )
        ) {
            MedicineScreen(
                navigateBack = { navigator.popBackStack(Medicines, false) },
                navigateToIntake = { navigator.navigate(Intake(medicineId = it)) }
            )
        }
        composable<Intake> {
            IntakeScreen(navigator::popBackStack)
        }
    }
}

@Composable
fun BottomNavigationBar(backStack: NavBackStackEntry?, onClick: (Screen) -> Unit) {
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
                    onClick = { onClick(screen.route) }
                )
            }
        }
    }
}