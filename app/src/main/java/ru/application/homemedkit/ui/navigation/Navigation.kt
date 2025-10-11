package ru.application.homemedkit.ui.navigation

import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.systemBars
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.navDeepLink
import ru.application.homemedkit.ui.elements.VectorIcon
import ru.application.homemedkit.ui.navigation.Screen.Intake
import ru.application.homemedkit.ui.navigation.Screen.Intakes
import ru.application.homemedkit.ui.navigation.Screen.Medicine
import ru.application.homemedkit.ui.navigation.Screen.Medicines
import ru.application.homemedkit.ui.navigation.Screen.Scanner
import ru.application.homemedkit.ui.navigation.Screen.Settings
import ru.application.homemedkit.ui.screens.IntakeScreen
import ru.application.homemedkit.ui.screens.IntakesScreen
import ru.application.homemedkit.ui.screens.MedicineScreen
import ru.application.homemedkit.ui.screens.MedicinesScreen
import ru.application.homemedkit.ui.screens.ScannerScreen
import ru.application.homemedkit.ui.screens.SettingsScreen
import ru.application.homemedkit.utils.di.Preferences
import ru.application.homemedkit.utils.enums.Menu
import ru.application.homemedkit.utils.extensions.isCurrentRoute
import ru.application.homemedkit.utils.extensions.onBottomItemClick
import kotlin.reflect.KClass

@Composable
fun Navigation(navigator: NavHostController) {
    val barVisibility = LocalBarVisibility.current
    val backStack by navigator.currentBackStackEntryAsState()

    val routes = remember { Menu.entries.map { it.route::class } }

    LaunchedEffect(backStack, barVisibility) {
        if (routes.any { backStack.isCurrentRoute(it) }) barVisibility.show()
        else barVisibility.hide()
    }

    Scaffold(
        content = { AppNavHost(navigator, Modifier.padding(it)) },
        bottomBar = {
            if (barVisibility.isVisible) {
                BottomNavigationBar(
                    selected = { backStack.isCurrentRoute(it) },
                    onClick = { navigator.onBottomItemClick(it) }
                )
            }
        }
    )
}

@Composable
private fun AppNavHost(navigator: NavHostController, modifier: Modifier) =
    NavHost(navigator, Preferences.startPage.route, modifier.consumeWindowInsets(WindowInsets.systemBars)) {
        // Bottom menu items //
        composable<Medicines> {
            MedicinesScreen(navigator::navigate)
        }
        composable<Intakes> {
            IntakesScreen { navigator.navigate(Intake(intakeId = it)) }
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

@Composable
private fun BottomNavigationBar(selected: (route: KClass<*>) -> Boolean, onClick: (Screen) -> Unit) =
    NavigationBar {
        Menu.entries.forEach { screen ->
            NavigationBarItem(
                icon = { VectorIcon(screen.icon) },
                label = { Text(stringResource(screen.title)) },
                selected = selected(screen.route::class),
                onClick = { onClick(screen.route) }
            )
        }
    }