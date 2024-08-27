package ru.application.homemedkit.screens

import android.app.Activity
import android.content.Context
import android.os.Bundle
import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.navigation.NavHostController
import com.ramcosta.composedestinations.DestinationsNavHost
import com.ramcosta.composedestinations.animations.defaults.DefaultFadingTransitions
import com.ramcosta.composedestinations.generated.NavGraphs
import com.ramcosta.composedestinations.generated.destinations.HomeScreenDestination
import com.ramcosta.composedestinations.generated.destinations.IntakesScreenDestination
import com.ramcosta.composedestinations.generated.destinations.MedicineScreenDestination
import com.ramcosta.composedestinations.generated.destinations.MedicinesScreenDestination
import com.ramcosta.composedestinations.generated.destinations.SettingsScreenDestination
import com.ramcosta.composedestinations.spec.Direction
import com.ramcosta.composedestinations.utils.currentDestinationAsState
import com.ramcosta.composedestinations.utils.isRouteOnBackStack
import com.ramcosta.composedestinations.utils.rememberDestinationsNavigator
import ru.application.homemedkit.R.drawable.vector_home
import ru.application.homemedkit.R.drawable.vector_medicine
import ru.application.homemedkit.R.drawable.vector_settings
import ru.application.homemedkit.R.drawable.vector_time
import ru.application.homemedkit.R.string.bottom_bar_intakes
import ru.application.homemedkit.R.string.bottom_bar_main
import ru.application.homemedkit.R.string.bottom_bar_medicines
import ru.application.homemedkit.R.string.bottom_bar_settings
import ru.application.homemedkit.helpers.ID
import ru.application.homemedkit.helpers.Preferences

@Composable
fun RootScreen(navController: NavHostController, context: Context = LocalContext.current) {
    val navigator = navController.rememberDestinationsNavigator()
    val current by navController.currentDestinationAsState()
    val activity = context as Activity

    Scaffold(
        bottomBar = {
            if (Menu.entries.any { it.route == current })
                NavigationBar {
                    Menu.entries.forEach { screen ->
                        NavigationBarItem(
                            selected = current == screen.route,
                            onClick = {
                                if(navController.isRouteOnBackStack(screen.route)){
                                    navigator.popBackStack(screen.route, false)
                                    return@NavigationBarItem
                                }

                                navigator.navigate(screen.route) {
                                    popUpTo(NavGraphs.root) {
                                        saveState = true
                                    }

                                    launchSingleTop = true
                                    restoreState = true
                                }
                            },
                            icon = {
                                Image(
                                    painter = painterResource(screen.icon),
                                    contentDescription = null,
                                    colorFilter = ColorFilter.tint(MaterialTheme.colorScheme.onPrimaryContainer)
                                )
                            },
                            label = { Text(stringResource(screen.title)) }
                        )
                    }
                }
        }
    ) { paddingValues ->
        DestinationsNavHost(
            navGraph = NavGraphs.root,
            modifier = Modifier.padding(paddingValues),
            start = Direction(Preferences.getHomePage()),
            defaultTransitions = DefaultFadingTransitions,
            navController = navController
        )
    }
    if (activity.intent.getLongExtra(ID, 0L) != 0L) {
        navigator.navigate(MedicineScreenDestination(activity.intent.getLongExtra(ID, 0L)))
        activity.intent.replaceExtras(Bundle())
    }
}

enum class Menu(val route: Direction, @StringRes val title: Int, @DrawableRes val icon: Int) {
    Home(HomeScreenDestination, bottom_bar_main, vector_home),
    Medicines(MedicinesScreenDestination, bottom_bar_medicines, vector_medicine),
    Intakes(IntakesScreenDestination, bottom_bar_intakes, vector_time),
    Settings(SettingsScreenDestination, bottom_bar_settings, vector_settings)
}