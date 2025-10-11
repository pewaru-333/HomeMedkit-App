package ru.application.homemedkit.models.events

import androidx.compose.foundation.lazy.LazyListState
import ru.application.homemedkit.models.states.IntakesDialogState
import ru.application.homemedkit.utils.enums.IntakeTab

sealed interface IntakesEvent {
    data class SetSearch(val search: String) : IntakesEvent
    data class ToggleDialog(val state: IntakesDialogState? = null) : IntakesEvent
    data class ScrollToDate(val tab: IntakeTab, val listState: LazyListState, val time: Long) : IntakesEvent
}