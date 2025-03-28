package ru.application.homemedkit.helpers.enums

import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import ru.application.homemedkit.R
import ru.application.homemedkit.ui.navigation.Screen

enum class Menu(
    val route: Screen,
    @StringRes val title: Int,
    @DrawableRes val icon: Int
) {
    MEDICINES(
        route = Screen.Medicines,
        title = R.string.bottom_bar_medicines,
        icon = R.drawable.vector_medicine
    ),
    INTAKES(
        route = Screen.Intakes,
        title = R.string.bottom_bar_intakes,
        icon = R.drawable.vector_time
    ),
    SETTINGS(
        route = Screen.Settings,
        title = R.string.bottom_bar_settings,
        icon = R.drawable.vector_settings
    )
}