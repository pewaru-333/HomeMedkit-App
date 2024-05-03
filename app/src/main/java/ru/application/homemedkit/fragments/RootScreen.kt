package ru.application.homemedkit.fragments

import android.content.Context
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.navigation.NavHostController
import com.ramcosta.composedestinations.DestinationsNavHost
import com.ramcosta.composedestinations.generated.NavGraphs
import com.ramcosta.composedestinations.generated.destinations.HomeScreenDestination
import com.ramcosta.composedestinations.generated.destinations.IntakesScreenDestination
import com.ramcosta.composedestinations.generated.destinations.MedicinesScreenDestination
import com.ramcosta.composedestinations.generated.destinations.SettingsScreenDestination
import com.ramcosta.composedestinations.navigation.navigate
import com.ramcosta.composedestinations.navigation.popBackStack
import com.ramcosta.composedestinations.navigation.popUpTo
import com.ramcosta.composedestinations.spec.DirectionDestinationSpec
import com.ramcosta.composedestinations.utils.currentDestinationAsState
import com.ramcosta.composedestinations.utils.findDestination
import com.ramcosta.composedestinations.utils.isRouteOnBackStack
import com.ramcosta.composedestinations.utils.startDestination
import ru.application.homemedkit.R
import ru.application.homemedkit.helpers.Preferences

@Composable
fun RootScreen(navController: NavHostController, context: Context = LocalContext.current) {
    val current by navController.currentDestinationAsState()

    Scaffold(
        bottomBar = {
            if (Menu.entries.any { it.route == current })
                NavigationBar {
                    Menu.entries.forEach { screen ->
                        NavigationBarItem(
                            selected = current == screen.route,
                            onClick = {
                                if(navController.isRouteOnBackStack(screen.route)){
                                    navController.popBackStack(screen.route, false)
                                    return@NavigationBarItem
                                }

                                navController.navigate(screen.route) {
                                    popUpTo(NavGraphs.root) {
                                        saveState = true
                                    }

                                    launchSingleTop = true
                                    restoreState = true
                                }
                            },
                            icon = { Icon(painterResource(screen.icon), null) },
                            label = { Text(context.getString(screen.title)) }
                        )
                    }
                }
        }
    ) { paddingValues ->
        DestinationsNavHost(
            navGraph = NavGraphs.root,
            modifier = Modifier.padding(paddingValues),
            startRoute = NavGraphs.root
                .findDestination(Preferences(context).getHomePage())?.startDestination ?: HomeScreenDestination,
            navController = navController
        )
    }
}

enum class Menu(val route: DirectionDestinationSpec, val title: Int, val icon: Int) {
    Home(HomeScreenDestination, R.string.bottom_bar_main, R.drawable.vector_home),
    Medicines(MedicinesScreenDestination, R.string.bottom_bar_medicines, R.drawable.vector_medicine),
    Intakes(IntakesScreenDestination, R.string.bottom_bar_intakes, R.drawable.vector_time),
    Settings(SettingsScreenDestination, R.string.bottom_bar_settings, R.drawable.vector_settings)
}