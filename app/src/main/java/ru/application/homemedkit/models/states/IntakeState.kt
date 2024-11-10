@file:OptIn(ExperimentalMaterial3Api::class)

package ru.application.homemedkit.models.states

import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.TimePickerState
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.snapshots.SnapshotStateList
import ru.application.homemedkit.helpers.BLANK
import ru.application.homemedkit.helpers.Intervals
import ru.application.homemedkit.helpers.Periods

data class IntakeState(
    val adding: Boolean = true,
    val editing: Boolean = false,
    val default: Boolean = false,
    val intakeId: Long = 0,
    val medicineId: Long = 0,
    val amount: String = BLANK,
    val interval: String = BLANK,
    val intervalE: Intervals? = null,
    val period: String = BLANK,
    val periodE: Periods = Periods.PICK,
    val foodType: Int = -1,
    val time: SnapshotStateList<String> = mutableStateListOf(BLANK),
    val times: SnapshotStateList<TimePickerState> = mutableStateListOf(TimePickerState(12, 0, true)),
    val timeF: Int = 0,
    val startDate: String = BLANK,
    val finalDate: String = BLANK,
    val showIntervalM: Boolean = false,
    val showPeriodD: Boolean = false,
    val showPeriodM: Boolean = false,
    val showTimeP: Boolean = false,
    val fullScreen: Boolean = false,
    val noSound: Boolean = false,
    val preAlarm: Boolean = false,
    val showDialog: Boolean = false
)