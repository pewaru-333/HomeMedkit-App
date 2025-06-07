package ru.application.homemedkit.utils.enums

import androidx.annotation.StringRes
import ru.application.homemedkit.R
import ru.application.homemedkit.ui.navigation.Screen

enum class Page(val route: Screen, @StringRes val title: Int) {
    MEDICINES(
        route = Screen.Medicines,
        title = R.string.page_medications
    ),
    INTAKE_LIST(
        route = Screen.Intakes(IntakeTab.LIST),
        title = R.string.page_intake_list
    ),
    INTAKE_CURRENT(
        route = Screen.Intakes(IntakeTab.CURRENT),
        title = R.string.page_intake_current
    ),
    INTAKE_PAST(
        route = Screen.Intakes(IntakeTab.PAST),
        title = R.string.page_intake_past
    ),
    SETTINGS(
        route = Screen.Settings,
        title = R.string.page_intake_settings
    )
}