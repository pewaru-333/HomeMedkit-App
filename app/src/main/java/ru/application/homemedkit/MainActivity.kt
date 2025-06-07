package ru.application.homemedkit

import android.content.Context
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import ru.application.homemedkit.utils.KEY_EXP_IMP
import ru.application.homemedkit.utils.Preferences
import ru.application.homemedkit.utils.extensions.showToast
import ru.application.homemedkit.utils.extensions.toBottomBarItem
import ru.application.homemedkit.receivers.AlarmSetter
import ru.application.homemedkit.ui.navigation.BottomNavigationBar
import ru.application.homemedkit.ui.navigation.Navigation
import ru.application.homemedkit.ui.theme.AppTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            val navigator = rememberNavController()
            val backStack by navigator.currentBackStackEntryAsState()

            AppTheme {
                Scaffold(
                    bottomBar = { BottomNavigationBar(backStack, navigator::toBottomBarItem) },
                    content = { Navigation(navigator, Modifier.padding(it)) }
                )
            }

            val imported by remember {
                mutableStateOf(intent.getBooleanExtra(KEY_EXP_IMP, false))
            }

            LaunchedEffect(imported) {
                if (imported) {
                    showToast(R.string.text_success)
                    AlarmSetter(this@MainActivity).resetAll()
                }
            }
        }
    }

    override fun attachBaseContext(newBase: Context?) {
        super.attachBaseContext(Preferences.changeLanguage(newBase))
    }
}