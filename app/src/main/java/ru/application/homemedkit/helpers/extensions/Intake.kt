@file:OptIn(ExperimentalMaterial3Api::class)

package ru.application.homemedkit.helpers.extensions

import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.TimePickerState
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.compose.runtime.toMutableStateList
import ru.application.homemedkit.data.dto.Intake
import ru.application.homemedkit.data.model.IntakeAmountTime
import ru.application.homemedkit.data.model.IntakeFull
import ru.application.homemedkit.helpers.BLANK
import ru.application.homemedkit.helpers.FORMAT_H_MM
import ru.application.homemedkit.helpers.enums.IntakeExtras
import ru.application.homemedkit.helpers.enums.Intervals
import ru.application.homemedkit.helpers.enums.Periods
import ru.application.homemedkit.models.states.IntakeState
import java.time.LocalTime

fun IntakeFull.toState() = IntakeState(
    adding = false,
    editing = false,
    default = true,
    intakeId = intakeId,
    medicineId = medicineId,
    medicine = medicine,
    image = images.firstOrNull() ?: BLANK,
    schemaType = schemaType,
    amountStock = medicine.prodAmount.toString(),
    sameAmount = sameAmount,
    doseType = medicine.doseType.title,
    interval = interval.toString(),
    intervalType = Intervals.getValue(interval),
    period = period.toString(),
    periodType = Periods.getValue(period),
    foodType = foodType,
    pickedTime = pickedTime.mapTo(SnapshotStateList()) { pickedTime ->
        val localTime = LocalTime.parse(pickedTime.time, FORMAT_H_MM)
        val hour = localTime.hour
        val min = localTime.minute

        IntakeAmountTime(
            amount = pickedTime.amount.toString(),
            time = pickedTime.time,
            picker = TimePickerState(hour, min, true)
        )
    },
    pickedDays = pickedDays.sorted().toMutableStateList(),
    startDate = startDate,
    finalDate = finalDate,
    fullScreen = fullScreen,
    noSound = noSound,
    preAlarm = preAlarm,
    cancellable = cancellable,
    selectedExtras = SnapshotStateList<IntakeExtras>().apply {
        if (cancellable) add(IntakeExtras.CANCELLABLE)
        if (fullScreen) add(IntakeExtras.FULLSCREEN)
        if (noSound) add(IntakeExtras.NO_SOUND)
        if (preAlarm) add(IntakeExtras.PREALARM)
    }
)

fun IntakeState.toIntake() = Intake(
    intakeId = intakeId,
    medicineId = medicineId,
    interval = interval.toInt(),
    foodType = foodType,
    period = period.toInt(),
    startDate = startDate,
    finalDate = finalDate,
    schemaType = schemaType,
    sameAmount = sameAmount,
    fullScreen = fullScreen,
    noSound = noSound,
    preAlarm = preAlarm,
    cancellable = cancellable
)