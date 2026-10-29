package ru.application.homemedkit.models.states

import ru.application.homemedkit.utils.BLANK
import ru.application.homemedkit.utils.enums.MedicineListView
import ru.application.homemedkit.utils.enums.Sorting

data class MedicinesState(
    val search: String = BLANK,
    val sorting: Sorting = Sorting.IN_NAME,
    val hideEmpty: Boolean = false,
    val listView: MedicineListView = MedicineListView.LIST,
    val kitIds: Set<Long> = emptySet(),
    val showSorting: Boolean = false,
    val showFilter: Boolean = false,
    val showAdding: Boolean = false,
    val showExit: Boolean = false
)