package ru.application.homemedkit.helpers.extensions

import androidx.navigation.NavBackStackEntry
import androidx.navigation.NavDestination.Companion.hasRoute
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import ru.application.homemedkit.ui.navigation.Screen
import kotlin.reflect.KClass

fun <T : Any> NavBackStackEntry?.isCurrentRoute(route: KClass<T>) =
    this?.destination?.hierarchy?.any { it.hasRoute(route) } == true

fun NavHostController.toBottomBarItem(route: Screen) = navigate(route) {
    launchSingleTop = true
    restoreState = true

    popUpTo(this@toBottomBarItem.graph.findStartDestination().id) {
        saveState = true
    }
}