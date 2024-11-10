@file:OptIn(ExperimentalMaterial3Api::class)

package ru.application.homemedkit.models.states

import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.TimePickerState
import ru.application.homemedkit.helpers.BLANK

data class TakenState(
    val takenId: Long = 0L,
    val medicineId: Long = 0L,
    val productName: String = BLANK,
    val amount: Double = 0.0,
    val trigger: Long = 0L,
    val inFact: Long = 0L,
    val pickerState: TimePickerState = TimePickerState(12, 0, true),
    val taken: Boolean = false,
    val selection: Int = 0,
    val notified: Boolean = false,
    val showPicker: Boolean = false
)