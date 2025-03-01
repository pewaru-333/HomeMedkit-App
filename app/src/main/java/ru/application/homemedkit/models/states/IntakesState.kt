package ru.application.homemedkit.models.states

import androidx.compose.foundation.lazy.LazyListState
import ru.application.homemedkit.helpers.BLANK

data class IntakesState(
    val search: String = BLANK,
    val tab: Int = 0,
    val stateA: LazyListState = LazyListState(),
    val stateB: LazyListState = LazyListState(),
    val stateC: LazyListState = LazyListState(),
    val showDialog: Boolean = false,
    val showDialogDate: Boolean = false,
    val showDialogDelete: Boolean = false
)