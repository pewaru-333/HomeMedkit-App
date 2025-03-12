package ru.application.homemedkit

import android.content.Context
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import ru.application.homemedkit.helpers.KEY_EXP_IMP
import ru.application.homemedkit.helpers.Preferences
import ru.application.homemedkit.helpers.extensions.showToast
import ru.application.homemedkit.helpers.extensions.toBottomBarItem
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
                    modifier = Modifier.safeDrawingPadding(),
                    bottomBar = { BottomNavigationBar(backStack, navigator::toBottomBarItem) },
                    content = { Navigation(navigator, Modifier.padding(it)) }
                )
            }
        }
    }

    override fun onStart() {
        super.onStart()

        if (intent.getBooleanExtra(KEY_EXP_IMP, false)) {
            showToast(R.string.text_success)
            AlarmSetter(this).resetAll()
        }
    }

    override fun attachBaseContext(newBase: Context?) {
        super.attachBaseContext(Preferences.changeLanguage(newBase))
    }
}