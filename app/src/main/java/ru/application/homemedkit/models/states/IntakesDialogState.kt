package ru.application.homemedkit.models.states

import ru.application.homemedkit.data.model.IntakeModel

sealed interface IntakesDialogState {
    data object TakenAdd : IntakesDialogState
    data class TakenDelete(val takenId: Long = 0L) : IntakesDialogState
    data class TakenInfo(val takenId: Long) : IntakesDialogState
    data class ScheduleToTaken(val item: IntakeModel) : IntakesDialogState
    data object DatePicker : IntakesDialogState
}