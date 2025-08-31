@file:OptIn(ExperimentalMaterial3Api::class)

package ru.application.homemedkit.models.states

import androidx.annotation.StringRes
import androidx.compose.material3.ExperimentalMaterial3Api
import ru.application.homemedkit.R
import ru.application.homemedkit.data.model.IntakeAmountTime
import ru.application.homemedkit.data.model.MedicineIntake
import ru.application.homemedkit.utils.BLANK
import ru.application.homemedkit.utils.di.Preferences
import ru.application.homemedkit.utils.enums.IntakeExtra
import ru.application.homemedkit.utils.enums.Interval
import ru.application.homemedkit.utils.enums.Period
import ru.application.homemedkit.utils.enums.SchemaType
import java.time.DayOfWeek

data class IntakeState(
    val adding: Boolean = false,
    val editing: Boolean = false,
    val default: Boolean = false,
    val isLoading: Boolean = true,
    val intakeId: Long = 0L,
    val medicineId: Long = 0L,
    val medicine: MedicineIntake = MedicineIntake(),
    val image: String = BLANK,
    val schemaType: SchemaType = SchemaType.BY_DAYS,
    val amountStock: String = BLANK,
    @StringRes val amountError: Int? = null,
    val sameAmount: Boolean = true,
    @StringRes val doseType: Int = R.string.blank,
    val interval: String = Interval.DAILY.days.toString(),
    val intervalType: Interval = Interval.DAILY,
    @StringRes val intervalError: Int? = null,
    val period: String = BLANK,
    val periodType: Period = Period.PICK,
    @StringRes val periodError: Int? = null,
    val foodType: Int = -1,
    val pickedDays: List<DayOfWeek> = DayOfWeek.entries,
    val pickedTime: List<IntakeAmountTime> = listOf(IntakeAmountTime()),
    val timePickerIndex: Int = 0,
    @StringRes val timesError: Int? = null,
    val startDate: String = BLANK,
    @StringRes val startDateError: Int? = null,
    val finalDate: String = BLANK,
    @StringRes val finalDateError: Int? = null,
    @StringRes val extraDesc: Int? = null,
    val selectedExtras: Set<IntakeExtra> = setOf(IntakeExtra.CANCELLABLE),
    val showIntervalTypePicker: Boolean = false,
    val showDatePicker: Boolean = false,
    val showDateRangePicker: Boolean = false,
    val showSchemaTypePicker: Boolean = false,
    val showPeriodTypePicker: Boolean = false,
    val showTimePicker: Boolean = false,
    val fullScreen: Boolean = false,
    val noSound: Boolean = false,
    val preAlarm: Boolean = false,
    val cancellable: Boolean = true,
    val showDialogDescription: Boolean = false,
    val showDialogDelete: Boolean = false,
    val showDialogDataLoss: Boolean = false,
    val isFirstLaunch: Boolean = Preferences.isFirstLaunch
)