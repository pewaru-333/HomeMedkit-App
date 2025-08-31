package ru.application.homemedkit

import android.content.Context
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import ru.application.homemedkit.ui.navigation.BottomNavigationBar
import ru.application.homemedkit.ui.navigation.LocalBarVisibility
import ru.application.homemedkit.ui.navigation.Navigation
import ru.application.homemedkit.ui.navigation.rememberNavigationBarVisibility
import ru.application.homemedkit.ui.theme.AppTheme
import ru.application.homemedkit.utils.KEY_EXP_IMP
import ru.application.homemedkit.utils.di.AlarmManager
import ru.application.homemedkit.utils.di.Preferences
import ru.application.homemedkit.utils.extensions.showToast
import ru.application.homemedkit.utils.extensions.toBottomBarItem

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            val navigator = rememberNavController()
            val backStack by navigator.currentBackStackEntryAsState()
            val barVisibility = rememberNavigationBarVisibility()

            CompositionLocalProvider(LocalBarVisibility provides barVisibility) {
                AppTheme {
                    Scaffold(
                        bottomBar = { BottomNavigationBar(backStack, barVisibility, navigator::toBottomBarItem) },
                        content = { Navigation(navigator, Modifier.padding(it)) }
                    )
                }
            }

            val imported by remember {
                mutableStateOf(intent.getBooleanExtra(KEY_EXP_IMP, false))
            }

            LaunchedEffect(imported) {
                if (imported) {
                    showToast(R.string.text_success)
                    AlarmManager.resetAll()
                }
            }
        }
    }

    override fun attachBaseContext(newBase: Context) {
        super.attachBaseContext(Preferences.changeLanguage(newBase))
    }
}