package ru.application.homemedkit.ui.navigation

import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import ru.application.homemedkit.R

data class BottomBarItem(
    @StringRes val title: Int,
    @DrawableRes val icon: Int
)

val TOP_LEVEL_DESTINATIONS = mapOf(
    Screen.Medicines to BottomBarItem(
        title = R.string.bottom_bar_medicines,
        icon = R.drawable.vector_medicine
    ),
    Screen.Intakes to BottomBarItem(
        title = R.string.bottom_bar_intakes,
        icon = R.drawable.vector_time
    ),
    Screen.Settings to BottomBarItem(
        title = R.string.bottom_bar_settings,
        icon = R.drawable.vector_settings
    )
)