package ru.application.homemedkit.models.states

import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.snapshots.SnapshotStateList
import ru.application.homemedkit.data.model.KitMedicines
import ru.application.homemedkit.data.model.MedicineMain
import ru.application.homemedkit.helpers.BLANK
import ru.application.homemedkit.helpers.Preferences

data class MedicinesState(
    val search: String = BLANK,
    val sorting: Comparator<MedicineMain> = Preferences.sortingOrder.type,
    val kits: SnapshotStateList<KitMedicines> = mutableStateListOf(),
    val showSort: Boolean = false,
    val showFilter: Boolean = false,
    val showAdding: Boolean = false,
    val showExit: Boolean = false,
    val listState: LazyListState = LazyListState()
)