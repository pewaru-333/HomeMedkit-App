package ru.application.homemedkit.models.events

sealed interface IntakeEvent {
    data class SetAmount(val amount: String) : IntakeEvent
    data class SetInterval(val interval: Any?) : IntakeEvent
    data class SetPeriod(val period: Any?) : IntakeEvent
    data class SetFoodType(val type: Int) : IntakeEvent
    data class SetFullScreen(val flag: Boolean) : IntakeEvent
    data class SetNoSound(val flag: Boolean) : IntakeEvent
    data class SetPreAlarm(val flag: Boolean) : IntakeEvent
    data object SetTime : IntakeEvent
    data object IncTime : IntakeEvent
    data object DecTime : IntakeEvent
    data class ShowIntervalM(val flag: Boolean) : IntakeEvent
    data class ShowPeriodD(val flag: Boolean) : IntakeEvent
    data class ShowPeriodM(val flag: Boolean) : IntakeEvent
    data class ShowTimePicker(val flag: Boolean, val index: Int = 0) : IntakeEvent
    data class ShowDialog(val flag: Boolean) : IntakeEvent
    data class ShowDialogDataLoss(val flag: Boolean) : IntakeEvent
}