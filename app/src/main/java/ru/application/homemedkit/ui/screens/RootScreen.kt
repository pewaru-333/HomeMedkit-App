package ru.application.homemedkit.ui.screens

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.navigation.NavHostController
import com.ramcosta.composedestinations.DestinationsNavHost
import com.ramcosta.composedestinations.animations.defaults.DefaultFadingTransitions
import com.ramcosta.composedestinations.generated.NavGraphs
import com.ramcosta.composedestinations.utils.currentDestinationAsState
import com.ramcosta.composedestinations.utils.isRouteOnBackStack
import com.ramcosta.composedestinations.utils.rememberDestinationsNavigator
import ru.application.homemedkit.helpers.Menu

@Composable
fun RootScreen(navController: NavHostController) {
    val navigator = navController.rememberDestinationsNavigator()
    val current by navController.currentDestinationAsState()

    Scaffold(
        bottomBar = {
            if (Menu.entries.any { it.route == current })
                NavigationBar {
                    Menu.entries.forEach { screen ->
                        NavigationBarItem(
                            selected = current == screen.route,
                            label = { Text(stringResource(screen.title)) },
                            icon = {
                                Icon(
                                    painter = painterResource(screen.icon),
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.onPrimaryContainer
                                )
                            },
                            onClick = {
                                if (navController.isRouteOnBackStack(screen.route)) {
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
                            }
                        )
                    }
                }
        }
    ) { values ->
        DestinationsNavHost(
            navGraph = NavGraphs.root,
            modifier = Modifier.padding(values),
            defaultTransitions = DefaultFadingTransitions,
            navController = navController
        )
    }
}