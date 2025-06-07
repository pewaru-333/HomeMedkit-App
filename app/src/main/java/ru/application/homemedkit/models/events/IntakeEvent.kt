package ru.application.homemedkit.models.events

import androidx.annotation.StringRes
import kotlinx.serialization.descriptors.PrimitiveKind
import ru.application.homemedkit.utils.enums.IntakeExtra
import ru.application.homemedkit.utils.enums.SchemaType
import java.time.DayOfWeek

sealed interface IntakeEvent {
    data class SetSchemaType(val type: SchemaType) : IntakeEvent
    data class SetAmount(val amount: String, val index: Int = 0) : IntakeEvent
    data class SetInterval(val interval: Any?) : IntakeEvent
    data class SetPeriod(val period: Any?) : IntakeEvent
    data class SetStartDate(val millis: Long) : IntakeEvent
    data class SetFoodType(val type: Int) : IntakeEvent
    data class SetPickedDay(val day: DayOfWeek) : IntakeEvent
    data class SetSameAmount(val flag: Boolean) : IntakeEvent
    data class SetIntakeExtra(val extra: IntakeExtra) : IntakeEvent
    data object SetPickedTime : IntakeEvent

    data object ShowSchemaTypePicker : IntakeEvent
    data object ShowDatePicker : IntakeEvent
    data object ShowPeriodTypePicker : IntakeEvent
    data object ShowIntervalTypePicker : IntakeEvent
    data class ShowTimePicker(val index: Int = 0) : IntakeEvent
    data class ShowDialogDescription(@StringRes val description: Int? = null) : IntakeEvent
    data object ShowDialogDelete : IntakeEvent
    data class ShowDialogDataLoss(val flag: Boolean) : IntakeEvent

    data object IncTime : IntakeEvent
    data object DecTime : IntakeEvent
}