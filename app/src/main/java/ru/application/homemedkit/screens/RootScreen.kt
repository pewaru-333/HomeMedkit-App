package ru.application.homemedkit.screens

import android.app.Activity
import android.content.Context
import android.os.Bundle
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.navigation.NavHostController
import com.ramcosta.composedestinations.DestinationsNavHost
import com.ramcosta.composedestinations.animations.defaults.DefaultFadingTransitions
import com.ramcosta.composedestinations.generated.NavGraphs
import com.ramcosta.composedestinations.generated.destinations.MedicineScreenDestination
import com.ramcosta.composedestinations.spec.Direction
import com.ramcosta.composedestinations.utils.currentDestinationAsState
import com.ramcosta.composedestinations.utils.isRouteOnBackStack
import com.ramcosta.composedestinations.utils.rememberDestinationsNavigator
import ru.application.homemedkit.helpers.ID
import ru.application.homemedkit.helpers.Menu
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