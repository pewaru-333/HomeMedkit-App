package ru.application.homemedkit.models.states

import ru.application.homemedkit.data.dto.Kit
import ru.application.homemedkit.utils.BLANK
import ru.application.homemedkit.utils.di.Preferences
import ru.application.homemedkit.utils.enums.MedicineListView
import ru.application.homemedkit.utils.enums.Sorting

data class MedicinesState(
    val search: String = BLANK,
    val sorting: Sorting = Preferences.sortingOrder,
    val hideEmpty: Boolean = Preferences.hideEmptyMedicines,
    val listView: MedicineListView = Preferences.medicinesListView,
    val kits: Set<Kit> = emptySet(),
    val showSorting: Boolean = false,
    val showFilter: Boolean = false,
    val showAdding: Boolean = false,
    val showExit: Boolean = false
)