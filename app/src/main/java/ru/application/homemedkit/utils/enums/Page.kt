package ru.application.homemedkit.utils.enums

import androidx.annotation.StringRes
import ru.application.homemedkit.R
import ru.application.homemedkit.ui.navigation.Screen

enum class Page(val route: Screen, @StringRes val title: Int, val extras: Array<Any> = arrayOf()) {
    MEDICINES(
        route = Screen.Medicines,
        title = R.string.page_medications
    ),
    INTAKE_LIST(
        route = Screen.Intakes,
        title = R.string.page_intake_list,
        extras = arrayOf(0)
    ),
    INTAKE_CURRENT(
        route = Screen.Intakes,
        title = R.string.page_intake_current,
        extras = arrayOf(1)
    ),
    INTAKE_PAST(
        route = Screen.Intakes,
        title = R.string.page_intake_past,
        extras = arrayOf(2)
    ),
    SETTINGS(
        route = Screen.Settings,
        title = R.string.page_intake_settings
    )
}