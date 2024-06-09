package ru.application.homemedkit.fragments

import android.app.Activity
import android.content.Context
import android.os.Bundle
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.res.vectorResource
import androidx.navigation.NavHostController
import com.ramcosta.composedestinations.DestinationsNavHost
import com.ramcosta.composedestinations.generated.NavGraphs
import com.ramcosta.composedestinations.generated.destinations.HomeScreenDestination
import com.ramcosta.composedestinations.generated.destinations.IntakesScreenDestination
import com.ramcosta.composedestinations.generated.destinations.MedicineScreenDestination
import com.ramcosta.composedestinations.generated.destinations.MedicinesScreenDestination
import com.ramcosta.composedestinations.generated.destinations.SettingsScreenDestination
import com.ramcosta.composedestinations.spec.DirectionDestinationSpec
import com.ramcosta.composedestinations.utils.currentDestinationAsState
import com.ramcosta.composedestinations.utils.findDestination
import com.ramcosta.composedestinations.utils.isRouteOnBackStack
import com.ramcosta.composedestinations.utils.rememberDestinationsNavigator
import com.ramcosta.composedestinations.utils.startDestination
import ru.application.homemedkit.R
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
                                    painter = rememberVectorPainter(
                                        if (screen.icon is ImageVector) screen.icon
                                        else ImageVector.vectorResource(screen.icon as Int)
                                    ),
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
            startRoute = NavGraphs.root.findDestination(Preferences(context).getHomePage())?.startDestination
                ?: HomeScreenDestination,
            navController = navController
        )

        if (activity.intent.getLongExtra(ID, 0L) != 0L) {
            navigator.navigate(MedicineScreenDestination(activity.intent.getLongExtra(ID, 0L)))
            activity.intent.replaceExtras(Bundle())
        }
    }
}

enum class Menu(val route: DirectionDestinationSpec, val title: Int, val icon: Any) {
    Home(HomeScreenDestination, R.string.bottom_bar_main, Icons.Default.Home),
    Medicines(MedicinesScreenDestination, R.string.bottom_bar_medicines, R.drawable.vector_medicine),
    Intakes(IntakesScreenDestination, R.string.bottom_bar_intakes, R.drawable.vector_time),
    Settings(SettingsScreenDestination, R.string.bottom_bar_settings, Icons.Default.Settings)
}