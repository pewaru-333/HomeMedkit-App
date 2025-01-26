@file:OptIn(ExperimentalMaterial3Api::class)

package ru.application.homemedkit.data.model

import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.TimePickerState
import ru.application.homemedkit.helpers.BLANK

data class IntakeAmountTime(
    val amount: String = BLANK,
    val time: String = BLANK,
    val picker: TimePickerState = TimePickerState(12, 0, true)
)
