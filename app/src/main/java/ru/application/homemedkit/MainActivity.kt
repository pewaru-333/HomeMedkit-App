package ru.application.homemedkit

import android.os.Bundle
import android.view.Window
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.compose.rememberNavController
import ru.application.homemedkit.helpers.Preferences
import ru.application.homemedkit.screens.RootScreen
import ru.application.homemedkit.ui.theme.AppTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        Preferences.changeLanguage(this, Preferences.getLanguage())
        requestWindowFeature(Window.FEATURE_NO_TITLE)
        super.onCreate(savedInstanceState)

        setContent {
            val navController = rememberNavController()
            val theme by Preferences.theme.collectAsStateWithLifecycle()
            val dynamicColors by Preferences.dynamicColors.collectAsStateWithLifecycle()

            AppTheme(theme, dynamicColors) { RootScreen(navController) }
        }
    }
}