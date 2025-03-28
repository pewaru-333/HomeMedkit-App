package ru.application.homemedkit.helpers.enums

import androidx.annotation.StringRes
import ru.application.homemedkit.R

enum class IntakeTabs(@StringRes val title: Int) {
    LIST(R.string.intakes_tab_list),
    CURRENT(R.string.intakes_tab_current),
    PAST(R.string.intakes_tab_past)
}