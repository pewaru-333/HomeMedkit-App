package ru.application.homemedkit.utils.enums

import androidx.annotation.StringRes
import ru.application.homemedkit.R

enum class MedicineTab(@StringRes val title: Int) {
    LIST(R.string.tab_list),
    GROUPS(R.string.tab_groups)
}