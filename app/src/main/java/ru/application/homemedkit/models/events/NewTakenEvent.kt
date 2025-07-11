@file:OptIn(ExperimentalMaterial3Api::class)

package ru.application.homemedkit.models.events

import androidx.compose.material3.DatePickerState
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.TimePickerState
import ru.application.homemedkit.data.model.MedicineMain

sealed interface NewTakenEvent {
    data object AddNewTaken : NewTakenEvent

    data class PickMedicine(val medicine: MedicineMain) : NewTakenEvent

    data class SetAmount(val amount: String) : NewTakenEvent
    data class SetDate(val pickerState: DatePickerState) : NewTakenEvent
    data class SetTime(val pickerState: TimePickerState) : NewTakenEvent
}