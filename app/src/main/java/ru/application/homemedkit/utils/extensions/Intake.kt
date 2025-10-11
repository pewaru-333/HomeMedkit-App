@file:OptIn(ExperimentalMaterial3Api::class)

package ru.application.homemedkit.utils.extensions

import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.TimePickerState
import androidx.compose.ui.text.intl.Locale
import ru.application.homemedkit.R
import ru.application.homemedkit.R.string.intake_text_not_taken
import ru.application.homemedkit.data.dto.IntakeTaken
import ru.application.homemedkit.data.model.Intake
import ru.application.homemedkit.data.model.IntakeAmountTime
import ru.application.homemedkit.data.model.IntakeFull
import ru.application.homemedkit.data.model.IntakeList
import ru.application.homemedkit.data.model.IntakePast
import ru.application.homemedkit.data.model.IntakeSchedule
import ru.application.homemedkit.data.model.IntakeTakenFull
import ru.application.homemedkit.data.model.Schedule
import ru.application.homemedkit.data.model.ScheduleModel
import ru.application.homemedkit.data.model.TakenModel
import ru.application.homemedkit.models.states.IntakeState
import ru.application.homemedkit.models.states.TakenState
import ru.application.homemedkit.utils.FORMAT_DD_MM_YYYY
import ru.application.homemedkit.utils.FORMAT_D_MMMM_E
import ru.application.homemedkit.utils.FORMAT_H_MM
import ru.application.homemedkit.utils.FORMAT_LONG
import ru.application.homemedkit.utils.ResourceText
import ru.application.homemedkit.utils.decimalFormat
import ru.application.homemedkit.utils.enums.IntakeExtra
import ru.application.homemedkit.utils.enums.Interval
import ru.application.homemedkit.utils.enums.Period
import ru.application.homemedkit.utils.formName
import ru.application.homemedkit.utils.getDateTime
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.LocalTime
import java.time.format.TextStyle

fun IntakeFull.toState() = IntakeState(
    adding = false,
    editing = false,
    default = true,
    isLoading = false,
    intakeId = intakeId,
    medicineId = medicineId,
    medicine = medicine,
    image = images.firstOrNull().orEmpty(),
    schemaType = schemaType,
    amountStock = medicine.prodAmount.toString(),
    sameAmount = sameAmount,
    doseType = medicine.doseType.title,
    interval = interval.toString(),
    intervalType = Interval.getValue(interval),
    period = period.toString(),
    periodType = Period.getValue(period),
    foodType = foodType,
    pickedTime = pickedTime.map { pickedTime ->
        val localTime = LocalTime.parse(pickedTime.time, FORMAT_H_MM)
        val hour = localTime.hour
        val min = localTime.minute

        IntakeAmountTime(
            amount = pickedTime.amount.toString(),
            time = pickedTime.time,
            picker = TimePickerState(hour, min, true)
        )
    },
    pickedDays = pickedDays.sorted(),
    startDate = startDate,
    finalDate = finalDate,
    fullScreen = fullScreen,
    noSound = noSound,
    preAlarm = preAlarm,
    cancellable = cancellable,
    selectedExtras = mutableSetOf<IntakeExtra>().apply {
        if (cancellable) add(IntakeExtra.CANCELLABLE)
        if (fullScreen) add(IntakeExtra.FULLSCREEN)
        if (noSound) add(IntakeExtra.NO_SOUND)
        if (preAlarm) add(IntakeExtra.PREALARM)
    }
)

fun IntakeState.toIntake() = ru.application.homemedkit.data.dto.Intake(
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

fun IntakeList.toIntake(): Intake {
    val timeText = if (time.size == 1) time.first()
    else time.map { LocalTime.parse(it, FORMAT_H_MM) }
        .sorted()
        .joinToString()

    val daysText = with(days.sorted()) {
        when {
            size == DayOfWeek.entries.size -> ResourceText.StringResource(R.string.text_every_day)
            this == DayOfWeek.entries.weekdays -> ResourceText.StringResource(R.string.text_weekdays)
            this == DayOfWeek.entries.weekends -> ResourceText.StringResource(R.string.text_weekend)
            else -> ResourceText.StaticString(
                joinToString { day ->
                    day.getDisplayName(TextStyle.SHORT, Locale.current.platformLocale)
                }
            )
        }
    }

    return Intake(
        intakeId = intakeId,
        title = nameAlias.ifEmpty(::productName),
        image = image.firstOrNull().orEmpty(),
        time = timeText,
        days = daysText,
        interval = ResourceText.PluralStringResource(R.plurals.intake_times_a_day, time.size, time.size),
        active = LocalDate.parse(finalDate, FORMAT_DD_MM_YYYY) >= LocalDate.now()
    )
}

fun IntakeTaken.toTakenModel() = TakenModel(
    id = takenId,
    alarmId = alarmId,
    title = productName,
    image = image,
    time = getDateTime(trigger).format(FORMAT_H_MM),
    taken = taken,
    doseAmount = ResourceText.StringResource(
        R.string.intake_text_quantity,
        formName.run {
            if (isNotEmpty()) formName(this)
            else ResourceText.StringResource(R.string.text_amount)
        },
        decimalFormat(amount),
        ResourceText.StringResource(doseType.title)
    )
)

fun Map.Entry<Long, List<IntakeTaken>>.toIntakePast(currentYear: Int) = IntakePast(
    epochDay = key,
    date = LocalDate.ofEpochDay(key).run {
        format(if (currentYear == year) FORMAT_D_MMMM_E else FORMAT_LONG)
    },
    intakes = value.map(IntakeTaken::toTakenModel)
)

fun Schedule.toScheduleModel() = ScheduleModel(
    id = alarmId,
    alarmId = alarmId,
    title = nameAlias.ifEmpty(::productName),
    image = image,
    time = getDateTime(trigger).format(FORMAT_H_MM),
    doseAmount = ResourceText.StringResource(
        R.string.intake_text_quantity,
        prodFormNormName.run {
            if (isNotEmpty()) formName(this)
            else ResourceText.StringResource(R.string.text_amount)
        },
        decimalFormat(amount),
        ResourceText.StringResource(doseType.title)
    )
)

fun Map.Entry<Long, List<Schedule>>.toIntakeSchedule(currentYear: Int) = IntakeSchedule(
    epochDay = key,
    date = LocalDate.ofEpochDay(key).run {
        format(if (currentYear == year) FORMAT_D_MMMM_E else FORMAT_LONG)
    },
    intakes = value.map(Schedule::toScheduleModel)
)

fun IntakeTakenFull.toTakenState(): TakenState {
    val triggerZoned = getDateTime(trigger)
    val actualZoned = getDateTime(inFact)

    return TakenState(
        takenId = takenId,
        alarmId = alarmId,
        medicine = medicine,
        productName = productName,
        amount = amount,
        date = triggerZoned.format(FORMAT_LONG),
        scheduled = triggerZoned.format(FORMAT_H_MM),
        actual = if (taken) ResourceText.StaticString(actualZoned.format(FORMAT_H_MM))
        else ResourceText.StringResource(intake_text_not_taken),
        inFact = inFact,
        pickerState = actualZoned.run { TimePickerState(hour, minute, true) },
        taken = taken,
        selection = if (taken) 1 else 0,
        notified = notified
    )
}

fun TakenState?.orDefault() = this ?: TakenState()