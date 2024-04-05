package ru.application.homemedkit.states

import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.TimePickerState
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.snapshots.SnapshotStateList
import ru.application.homemedkit.helpers.BLANK

data class IntakeState @OptIn(ExperimentalMaterial3Api::class) constructor(
    val adding: Boolean = true,
    val editing: Boolean = false,
    val default: Boolean = false,
    val intakeId: Long = 0,
    val medicineId: Long = 0,
    val amount: String = BLANK,
    val interval: String = BLANK,
    val period: String = BLANK,
    val periodD: Int = 0,
    val foodType: Int = -1,
    val time: SnapshotStateList<String> = mutableStateListOf(BLANK),
    val times: SnapshotStateList<TimePickerState> = mutableStateListOf(TimePickerState(12,0,true)),
    val startDate: String = BLANK,
    val finalDate: String = BLANK
)