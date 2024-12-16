@file:OptIn(ExperimentalMaterial3Api::class)

package ru.application.homemedkit.models.states

import androidx.annotation.StringRes
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
    val intakeId: Long = 0L,
    val medicineId: Long = 0L,
    val amount: String = BLANK,
    @StringRes val amountError: Int? = null,
    val interval: String = BLANK,
    val intervalE: Intervals? = null,
    @StringRes val intervalError: Int? = null,
    val period: String = BLANK,
    val periodE: Periods = Periods.PICK,
    @StringRes val periodError: Int? = null,
    val foodType: Int = -1,
    val time: SnapshotStateList<String> = mutableStateListOf(BLANK),
    val times: SnapshotStateList<TimePickerState> = mutableStateListOf(TimePickerState(12, 0, true)),
    val timeF: Int = 0,
    @StringRes val timesError: Int? = null,
    val startDate: String = BLANK,
    @StringRes val startDateError: Int? = null,
    val finalDate: String = BLANK,
    @StringRes val finalDateError: Int? = null,
    val showIntervalM: Boolean = false,
    val showPeriodD: Boolean = false,
    val showPeriodM: Boolean = false,
    val showTimeP: Boolean = false,
    val fullScreen: Boolean = false,
    val noSound: Boolean = false,
    val preAlarm: Boolean = false,
    val showDialog: Boolean = false,
    val showDialogDataLoss: Boolean = false
)