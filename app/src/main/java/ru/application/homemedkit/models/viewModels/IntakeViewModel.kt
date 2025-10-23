@file:OptIn(ExperimentalMaterial3Api::class)

package ru.application.homemedkit.models.viewModels

import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.TimePickerState
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.viewModelScope
import androidx.navigation.toRoute
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import ru.application.homemedkit.data.dto.Alarm
import ru.application.homemedkit.data.dto.IntakeDay
import ru.application.homemedkit.data.dto.IntakeTime
import ru.application.homemedkit.data.model.IntakeAmountTime
import ru.application.homemedkit.models.events.IntakeEvent
import ru.application.homemedkit.models.states.IntakeState
import ru.application.homemedkit.models.validation.Validation
import ru.application.homemedkit.ui.navigation.Screen.Intake
import ru.application.homemedkit.utils.BLANK
import ru.application.homemedkit.utils.FORMAT_DD_MM_YYYY
import ru.application.homemedkit.utils.FORMAT_H_MM
import ru.application.homemedkit.utils.ZONE
import ru.application.homemedkit.utils.di.AlarmManager
import ru.application.homemedkit.utils.di.Database
import ru.application.homemedkit.utils.di.Preferences
import ru.application.homemedkit.utils.enums.IntakeExtra
import ru.application.homemedkit.utils.enums.Interval
import ru.application.homemedkit.utils.enums.Interval.CUSTOM
import ru.application.homemedkit.utils.enums.Interval.DAILY
import ru.application.homemedkit.utils.enums.Interval.WEEKLY
import ru.application.homemedkit.utils.enums.Period
import ru.application.homemedkit.utils.enums.Period.INDEFINITE
import ru.application.homemedkit.utils.enums.Period.OTHER
import ru.application.homemedkit.utils.enums.Period.PICK
import ru.application.homemedkit.utils.enums.SchemaType
import ru.application.homemedkit.utils.extensions.concat
import ru.application.homemedkit.utils.extensions.toIntake
import ru.application.homemedkit.utils.extensions.toMedicineIntake
import ru.application.homemedkit.utils.extensions.toState
import ru.application.homemedkit.utils.extensions.toggle
import ru.application.homemedkit.utils.getDateTime
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.ZonedDateTime

class IntakeViewModel(saved: SavedStateHandle) : BaseViewModel<IntakeState, IntakeEvent>() {
    private val dao = Database.intakeDAO()
    private val args = saved.toRoute<Intake>()

    override fun initState() = IntakeState()

    override fun loadData() {
        viewModelScope.launch { 
            with(dao.getById(args.intakeId)) {
                if (this != null) {
                    val state = withContext(Dispatchers.Main) { toState() }
                    
                    updateState { state }
                } else {
                    Database.medicineDAO().getById(args.medicineId)?.let { medicine ->
                        updateState { 
                            it.copy(
                                adding = true,
                                isLoading = false,
                                medicineId = medicine.id,
                                medicine = medicine.toMedicineIntake(),
                                amountStock = medicine.prodAmount.toString(),
                                doseType = medicine.doseType.title,
                                image = medicine.images.firstOrNull()?.image.orEmpty()
                            )
                        }
                    }
                }
            }
        }
    }

    fun setExitFirstLaunch() {
        Preferences.setHasLaunched()
        updateState { it.copy(isFirstLaunch = false) }
    }

    fun add() {
        if (validate()) {
            viewModelScope.launch {
                val intakeId = dao.insert(currentState.toIntake())

                val days = currentState.pickedDays.map { IntakeDay(intakeId, it) }
                Database.intakeDayDAO().insert(days)

                val current = ZonedDateTime.now()
                val startDate = LocalDate.parse(currentState.startDate, FORMAT_DD_MM_YYYY)
                val finalDate = LocalDate.parse(currentState.finalDate, FORMAT_DD_MM_YYYY)

                val scheduled = mutableListOf<Alarm>()

                currentState.pickedTime.forEach { pickedTime ->
                    val localTime = LocalTime.of(pickedTime.picker.hour, pickedTime.picker.minute)

                    var initial = ZonedDateTime.of(startDate, localTime, ZONE)
                    val finish = ZonedDateTime.of(finalDate, localTime, ZONE)

                    initial = if (initial.isAfter(current)) initial else {
                        val todayTime = ZonedDateTime.of(current.toLocalDate(), localTime, ZONE)

                        if (todayTime.isAfter(current)) {
                            todayTime
                        } else {
                            todayTime.plusDays(1)
                        }
                    }

                    dao.addIntakeTime(
                        IntakeTime(
                            intakeId = intakeId,
                            time = pickedTime.time,
                            amount = pickedTime.amount.toDouble()
                        )
                    )

                    while (!initial.isAfter(finish)) {
                        if (initial.dayOfWeek in currentState.pickedDays) {
                            scheduled.add(
                                Alarm(
                                    intakeId = intakeId,
                                    trigger = initial.toInstant().toEpochMilli(),
                                    amount = pickedTime.amount.toDouble(),
                                    preAlarm = currentState.preAlarm
                                )
                            )
                        }

                        initial = initial.plusDays(currentState.interval.toLong())
                    }
                }

                Database.alarmDAO().insert(scheduled.sortedBy(Alarm::trigger))
                AlarmManager.setPreAlarm(intakeId)

                updateState {
                    it.copy(
                        intakeId = intakeId,
                        adding = false,
                        default = true,
                        amountError = null,
                        intervalError = null,
                        periodError = null,
                        startDateError = null,
                        finalDateError = null,
                        timesError = null
                    )
                }
            }
        }
    }

    fun update() {
        if (validate()) {
            val finalDate = LocalDateTime.of(
                LocalDate.parse(currentState.finalDate, FORMAT_DD_MM_YYYY),
                LocalTime.parse(currentState.pickedTime.last().time, FORMAT_H_MM)
            )

            viewModelScope.launch {
                AlarmManager.removeAlarm(currentState.intakeId)
                Database.alarmDAO().deleteByIntakeId(currentState.intakeId)

                Database.intakeDayDAO().deleteByIntakeId(currentState.intakeId)

                val days = currentState.pickedDays.map { IntakeDay(currentState.intakeId, it) }
                Database.intakeDayDAO().insert(days)

                if (finalDate >= LocalDateTime.now()) {
                    dao.deleteIntakeTime(currentState.intakeId)

                    val current = ZonedDateTime.now()
                    val startDate = LocalDate.parse(currentState.startDate, FORMAT_DD_MM_YYYY)
                    val finalDate = LocalDate.parse(currentState.finalDate, FORMAT_DD_MM_YYYY)

                    val scheduled = mutableListOf<Alarm>()

                    currentState.pickedTime.forEach { pickedTime ->
                        val localTime = LocalTime.of(pickedTime.picker.hour, pickedTime.picker.minute)

                        var initial = ZonedDateTime.of(startDate, localTime, ZONE)
                        val finish = ZonedDateTime.of(finalDate, localTime, ZONE)

                        initial = if (initial.isAfter(current)) initial else {
                            val todayTime = ZonedDateTime.of(current.toLocalDate(), localTime, ZONE)

                            if (todayTime.isAfter(current)) {
                                todayTime
                            } else {
                                todayTime.plusDays(1)
                            }
                        }

                        dao.addIntakeTime(
                            IntakeTime(
                                intakeId = currentState.intakeId,
                                time = pickedTime.time,
                                amount = pickedTime.amount.toDouble()
                            )
                        )

                        while (!initial.isAfter(finish)) {
                            if (initial.dayOfWeek in currentState.pickedDays) {
                                scheduled.add(
                                    Alarm(
                                        intakeId = currentState.intakeId,
                                        trigger = initial.toInstant().toEpochMilli(),
                                        amount = pickedTime.amount.toDouble(),
                                        preAlarm = currentState.preAlarm
                                    )
                                )
                            }

                            initial = initial.plusDays(currentState.interval.toLong())
                        }
                    }

                    Database.alarmDAO().insert(scheduled)
                    AlarmManager.setPreAlarm(currentState.intakeId)
                }

                dao.update(currentState.toIntake())
                updateState {
                    it.copy(
                        adding = false,
                        editing = false,
                        default = true,
                        amountError = null,
                        intervalError = null,
                        periodError = null,
                        startDateError = null,
                        finalDateError = null,
                        timesError = null
                    )
                }
            }
        }
    }

    fun delete(onBack: () -> Unit) {
        viewModelScope.launch {
            AlarmManager.removeAlarm(currentState.intakeId)
            dao.delete(currentState.toIntake())

            onBack()
        }
    }

    override fun onEvent(event: IntakeEvent) {
        when (event) {
            is IntakeEvent.SetAmount -> {
                val pickedTime = with(currentState) {
                    pickedTime.mapIndexed { index, time ->
                        if (sameAmount) {
                            time.copy(amount = event.amount)
                        } else {
                            if (index != event.index) time
                            else time.copy(amount = event.amount)
                        }
                    }
                }

                updateState { it.copy(pickedTime = pickedTime) }
            }

            is IntakeEvent.SetInterval -> {
                when (val interval = event.interval) {
                    is Interval -> when (interval) {
                        DAILY, WEEKLY -> updateState {
                            it.copy(
                                interval = interval.days.toString(),
                                intervalType = interval,
                                showIntervalTypePicker = false
                            )
                        }

                        CUSTOM -> updateState {
                            it.copy(
                                interval = BLANK,
                                intervalType = interval,
                                showIntervalTypePicker = false
                            )
                        }
                    }

                    is String -> updateState { it.copy(interval = interval) }
                }
            }

            is IntakeEvent.SetPeriod -> {
                when (val period = event.period) {
                    is Period -> when(period) {
                        PICK -> updateState {
                            it.copy(
                                period = period.days.toString(),
                                periodType = period,
                                startDate = BLANK,
                                finalDate = BLANK,
                                showPeriodTypePicker = false
                            )
                        }

                        OTHER -> updateState {
                            it.copy(
                                period = BLANK,
                                periodType = period,
                                startDate = BLANK,
                                finalDate = BLANK,
                                showPeriodTypePicker = false
                            )
                        }

                        INDEFINITE -> updateState {
                            it.copy(
                                startDate = LocalDate.now().format(FORMAT_DD_MM_YYYY),
                                finalDate = LocalDate.now().plusDays(period.days.toLong()).format(FORMAT_DD_MM_YYYY),
                                period = period.days.toString(),
                                periodType = period,
                                showPeriodTypePicker = false
                            )
                        }
                    }

                    is String -> if (period.isNotEmpty()) {
                        val start = currentState.startDate.ifEmpty { LocalDate.now().format(FORMAT_DD_MM_YYYY) }
                        val finish = LocalDate.parse(start, FORMAT_DD_MM_YYYY)
                            .plusDays(period.toLong() - 1)
                            .format(FORMAT_DD_MM_YYYY)

                        updateState {
                            it.copy(
                                startDate = start,
                                finalDate = finish,
                                period = period
                            )
                        }
                    } else {
                        updateState {
                            it.copy(startDate = BLANK, finalDate = BLANK, period = BLANK)
                        }
                    }

                    is Pair<*, *> -> if (period.first != null && period.second != null) updateState {
                        it.copy(
                            startDate = getDateTime(period.first as Long).format(FORMAT_DD_MM_YYYY),
                            finalDate = getDateTime(period.second as Long).format(FORMAT_DD_MM_YYYY),
                            period = PICK.days.toString()
                        )
                    }
                }
            }

            is IntakeEvent.SetStartDate -> {
                val period = currentState.period.let {
                    if (it.isEmpty()) 1L
                    else it.toLong() - 1
                }
                val start = getDateTime(event.millis).format(FORMAT_DD_MM_YYYY)
                val finish = getDateTime(event.millis)
                    .plusDays(period)
                    .format(FORMAT_DD_MM_YYYY)

                updateState {
                    it.copy(
                        startDate = start,
                        finalDate = finish,
                        showDatePicker = false
                    )
                }
            }

            is IntakeEvent.SetFoodType -> {
                updateState {
                    it.copy(foodType = if (event.type == currentState.foodType) -1 else event.type)
                }
            }

            IntakeEvent.IncTime -> {
                val pickedTime = with(currentState.pickedTime) {
                    concat(
                        IntakeAmountTime(
                            amount = firstOrNull()?.amount.orEmpty(),
                            time = BLANK,
                            picker = TimePickerState(12, 0, true)
                        )
                    )
                }

                updateState {
                    it.copy(pickedTime = pickedTime)
                }
            }
            IntakeEvent.DecTime -> {
                val pickedTime = with(currentState.pickedTime) {
                    if (size > 1) dropLast(1)
                    else this
                }

                updateState {
                    it.copy(pickedTime = pickedTime)
                }

            }

            is IntakeEvent.ShowDialogDescription -> {
                updateState {
                    it.copy(
                        extraDesc = event.description,
                        showDialogDescription = !it.showDialogDescription
                    )
                }
            }
            is IntakeEvent.ShowDialogDelete -> {
                updateState { it.copy(showDialogDelete = !it.showDialogDelete) }
            }
            is IntakeEvent.ShowDialogDataLoss -> {
                updateState { it.copy(showDialogDataLoss = event.flag) }
            }


            is IntakeEvent.ShowSchemaTypePicker -> {
                updateState { it.copy(showSchemaTypePicker = !it.showSchemaTypePicker) }
            }
            is IntakeEvent.ShowDatePicker -> {
                if (currentState.periodType == PICK) {
                    updateState {
                        it.copy(showDateRangePicker = !it.showDateRangePicker)
                    }
                }

                if (currentState.periodType == OTHER) {
                    updateState {
                        it.copy(showDatePicker = !it.showDatePicker)
                    }
                }
            }
            is IntakeEvent.ShowPeriodTypePicker -> {
                updateState { it.copy(showPeriodTypePicker = !it.showPeriodTypePicker) }
            }
            is IntakeEvent.ShowIntervalTypePicker -> {
                updateState { it.copy(showIntervalTypePicker = !it.showIntervalTypePicker) }
            }
            is IntakeEvent.ShowTimePicker -> {
                updateState {
                    it.copy(
                        showTimePicker = !it.showTimePicker,
                        timePickerIndex = event.index
                    )
                }
            }

            is IntakeEvent.SetSchemaType -> {
                when (event.type) {
                    SchemaType.INDEFINITELY -> {
                        updateState {
                            it.copy(
                                startDate = LocalDate.now().format(FORMAT_DD_MM_YYYY),
                                finalDate = LocalDate.now().plusDays(INDEFINITE.days.toLong()).format(FORMAT_DD_MM_YYYY),
                                interval = DAILY.days.toString(),
                                period = INDEFINITE.days.toString(),
                                pickedDays = DayOfWeek.entries,
                                schemaType = event.type,
                                showSchemaTypePicker = false
                            )
                        }
                    }

                    SchemaType.BY_DAYS -> updateState {
                        it.copy(
                            startDate = BLANK,
                            finalDate = BLANK,
                            interval = DAILY.days.toString(),
                            period = BLANK,
                            pickedDays = DayOfWeek.entries,
                            schemaType = event.type,
                            showSchemaTypePicker = false
                        )
                    }

                    SchemaType.PERSONAL -> updateState {
                        it.copy(
                            startDate = BLANK,
                            finalDate = BLANK,
                            interval = DAILY.days.toString(),
                            period = BLANK,
                            pickedDays = DayOfWeek.entries,
                            schemaType = event.type,
                            showSchemaTypePicker = false
                        )
                    }
                }
            }
            is IntakeEvent.SetPickedDay -> {
                val toggled = currentState.pickedDays.toggle(event.day)
                val pickedDays = toggled.ifEmpty { DayOfWeek.entries }

                updateState {
                    it.copy(
                        pickedDays = pickedDays.sorted()
                    )
                }
            }
            is IntakeEvent.SetPickedTime -> {
                val pickerIndex = currentState.timePickerIndex
                val picker = currentState.pickedTime[pickerIndex].picker
                val pickerTime = LocalTime.of(picker.hour, picker.minute).format(FORMAT_H_MM)

                val pickedTime = currentState.pickedTime.mapIndexed { index, time ->
                    if (index != pickerIndex) time
                    else time.copy(
                        time = pickerTime,
                        picker = picker
                    )
                }

                updateState {
                    it.copy(
                        showTimePicker = false,
                        pickedTime = pickedTime
                    )
                }
            }

            is IntakeEvent.SetSameAmount -> {
                val pickedTime = currentState.pickedTime.map {
                    it.copy(amount = BLANK)
                }

                updateState {
                    it.copy(
                        sameAmount = event.flag,
                        pickedTime = pickedTime
                    )
                }
            }

            is IntakeEvent.SetIntakeExtra -> {
                when (event.extra) {
                    IntakeExtra.CANCELLABLE -> updateState { it.copy(cancellable = !it.cancellable) }
                    IntakeExtra.FULLSCREEN -> updateState { it.copy(fullScreen = !it.fullScreen) }
                    IntakeExtra.NO_SOUND -> updateState { it.copy(noSound = !it.noSound) }
                    IntakeExtra.PREALARM -> updateState { it.copy(preAlarm = !it.preAlarm) }
                }

                updateState {
                    it.copy(selectedExtras = it.selectedExtras.toggle(event.extra))
                }
            }
        }
    }

    fun setEditing() = updateState { it.copy(adding = false, editing = true, default = false) }

    private fun validate() : Boolean {
        val checkAmount = Validation.checkAmount(currentState.pickedTime)
        val checkInterval = Validation.textNotEmpty(currentState.interval)
        val checkPeriod = Validation.textNotEmpty(currentState.period)
        val checkDateS = Validation.textNotEmpty(currentState.startDate)
        val checkDateF = Validation.textNotEmpty(currentState.finalDate)
        val checkTime = Validation.checkTime(currentState.pickedTime)

        val hasError = listOf(
            checkAmount, checkInterval, checkPeriod, checkDateS, checkDateF, checkTime
        ).any { !it.successful }

        return if (!hasError) true
        else {
            updateState {
                it.copy(
                    amountError = checkAmount.errorMessage,
                    intervalError = checkInterval.errorMessage,
                    periodError = checkPeriod.errorMessage,
                    startDateError = checkDateS.errorMessage,
                    finalDateError = checkDateF.errorMessage,
                    timesError = checkTime.errorMessage
                )
            }

            false
        }
    }
}