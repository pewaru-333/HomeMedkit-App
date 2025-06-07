package ru.application.homemedkit.models.states

import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.snapshots.SnapshotStateList
import ru.application.homemedkit.data.dto.Kit
import ru.application.homemedkit.utils.BLANK
import ru.application.homemedkit.utils.Preferences
import ru.application.homemedkit.utils.enums.MedicineTab
import ru.application.homemedkit.utils.enums.Sorting

data class MedicinesState(
    val search: String = BLANK,
    val sorting: Sorting = Preferences.sortingOrder,
    val kits: SnapshotStateList<Kit> = mutableStateListOf(),
    val tab: MedicineTab = MedicineTab.LIST,
    val showSort: Boolean = false,
    val showFilter: Boolean = false,
    val showAdding: Boolean = false,
    val showExit: Boolean = false
)