package ru.application.homemedkit.models.events

import androidx.annotation.StringRes
import ru.application.homemedkit.helpers.enums.IntakeExtras
import ru.application.homemedkit.helpers.enums.SchemaTypes
import java.time.DayOfWeek

sealed interface IntakeEvent {
    data class SetSchemaType(val type: SchemaTypes) : IntakeEvent
    data class SetAmount(val amount: String, val index: Int = 0) : IntakeEvent
    data class SetInterval(val interval: Any?) : IntakeEvent
    data class SetPeriod(val period: Any?) : IntakeEvent
    data class SetFoodType(val type: Int) : IntakeEvent
    data class SetPickedDay(val day: DayOfWeek) : IntakeEvent
    data class SetSameAmount(val flag: Boolean) : IntakeEvent
    data class SetIntakeExtra(val extra: IntakeExtras) : IntakeEvent
    data object SetPickedTime : IntakeEvent

    data object ShowSchemaTypePicker : IntakeEvent
    data object ShowDateRangePicker : IntakeEvent
    data object ShowPeriodTypePicker : IntakeEvent
    data object ShowIntervalTypePicker : IntakeEvent
    data class ShowTimePicker(val index: Int = 0) : IntakeEvent
    data class ShowDialogDescription(@StringRes val description: Int? = null) : IntakeEvent
    data object ShowDialogDelete : IntakeEvent
    data class ShowDialogDataLoss(val flag: Boolean) : IntakeEvent

    data object IncTime : IntakeEvent
    data object DecTime : IntakeEvent
}