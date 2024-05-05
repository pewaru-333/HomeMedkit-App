package ru.application.homemedkit.activities

import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.compose.rememberNavController
import ru.application.homemedkit.alarms.AlarmSetter
import ru.application.homemedkit.fragments.RootScreen
import ru.application.homemedkit.ui.theme.AppTheme

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        AlarmSetter(this).checkExpiration()

        setContent {
            val navController = rememberNavController()

            AppTheme { RootScreen(navController) }
        }
    }
}