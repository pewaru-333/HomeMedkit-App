package ru.application.homemedkit.helpers.enums

import androidx.annotation.StringRes
import ru.application.homemedkit.R
import ru.application.homemedkit.data.model.MedicineMain

enum class Sorting(
    @StringRes val title: Int,
    val type: Comparator<MedicineMain>
) {
    IN_NAME(
        title = R.string.sorting_a_z,
        type = compareBy { it.nameAlias.ifEmpty(it::productName) }
    ),
    RE_NAME(
        title = R.string.sorting_z_a,
        type = compareByDescending { it.nameAlias.ifEmpty(it::productName) }
    ),
    IN_DATE(
        title = R.string.sorting_from_oldest,
        type = compareBy(MedicineMain::expDate)
    ),
    RE_DATE(
        title = R.string.sorting_from_newest,
        type = compareByDescending(MedicineMain::expDate)
    )
}