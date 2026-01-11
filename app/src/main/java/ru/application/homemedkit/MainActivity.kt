package ru.application.homemedkit

import android.content.Context
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import ru.application.homemedkit.ui.navigation.LocalBarVisibility
import ru.application.homemedkit.ui.navigation.Navigation
import ru.application.homemedkit.ui.navigation.rememberNavigationBarVisibility
import ru.application.homemedkit.ui.theme.AppTheme
import ru.application.homemedkit.utils.di.AlarmManager
import ru.application.homemedkit.utils.di.Preferences
import ru.application.homemedkit.utils.extensions.showToast

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            val barVisibility = rememberNavigationBarVisibility()

            CompositionLocalProvider(LocalBarVisibility provides barVisibility) {
                AppTheme {
                    Navigation()
                }
            }

            LaunchedEffect(Unit) {
                if (Preferences.wasDataImported) {
                    showToast(R.string.text_success)
                    Preferences.removeImportedKey()
                    AlarmManager.resetAll()
                }
            }
        }
    }

    override fun attachBaseContext(newBase: Context) {
        super.attachBaseContext(Preferences.changeLanguage(newBase))
    }
}