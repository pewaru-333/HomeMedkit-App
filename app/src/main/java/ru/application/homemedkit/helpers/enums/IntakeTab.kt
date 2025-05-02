package ru.application.homemedkit.helpers.enums

import androidx.annotation.StringRes
import ru.application.homemedkit.R

enum class IntakeTab(@StringRes val title: Int) {
    LIST(R.string.tab_list),
    CURRENT(R.string.tab_current),
    PAST(R.string.tab_past)
}