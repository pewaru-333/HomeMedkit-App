package ru.application.homemedkit

import android.os.Bundle
import android.view.Window
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import ru.application.homemedkit.helpers.KEY_EXP_IMP
import ru.application.homemedkit.helpers.Preferences
import ru.application.homemedkit.helpers.showToast
import ru.application.homemedkit.receivers.AlarmSetter
import ru.application.homemedkit.ui.screens.Navigation
import ru.application.homemedkit.ui.theme.AppTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        Preferences.changeLanguage(this, Preferences.getLanguage())
        requestWindowFeature(Window.FEATURE_NO_TITLE)
        super.onCreate(savedInstanceState)

        if (intent.getBooleanExtra(KEY_EXP_IMP, false)) {
            showToast(true, this)
            AlarmSetter(this).resetAll()
        }

        setContent {
            val theme by Preferences.theme.collectAsStateWithLifecycle()
            val dynamicColors by Preferences.dynamicColors.collectAsStateWithLifecycle()

            AppTheme(theme, dynamicColors) { Navigation() }
        }
    }
}