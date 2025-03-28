package ru.application.homemedkit.models.states

import androidx.compose.foundation.lazy.LazyListState
import ru.application.homemedkit.helpers.BLANK
import ru.application.homemedkit.helpers.enums.IntakeTabs

data class IntakesState(
    val pickedTakenId: Long = 0L,
    val search: String = BLANK,
    val tab: IntakeTabs = IntakeTabs.LIST,
    val stateA: LazyListState = LazyListState(),
    val stateB: LazyListState = LazyListState(),
    val stateC: LazyListState = LazyListState(),
    val showDialog: Boolean = false,
    val showDialogDate: Boolean = false,
    val showDialogDelete: Boolean = false
)