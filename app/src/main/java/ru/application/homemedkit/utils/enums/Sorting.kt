package ru.application.homemedkit.utils.enums

import androidx.annotation.StringRes
import ru.application.homemedkit.R

enum class Sorting(@StringRes val title: Int) {
    IN_NAME(R.string.sorting_a_z),
    RE_NAME(R.string.sorting_z_a),
    IN_DATE(R.string.sorting_from_oldest),
    RE_DATE(R.string.sorting_from_newest)
}