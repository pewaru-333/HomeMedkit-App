@file:OptIn(ExperimentalMaterial3Api::class)

package ru.application.homemedkit.models.viewModels

import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.TimePickerState
import androidx.compose.runtime.toMutableStateList
import androidx.core.text.isDigitsOnly
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.toRoute
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import ru.application.homemedkit.HomeMeds.Companion.database
import ru.application.homemedkit.data.dto.Alarm
import ru.application.homemedkit.data.dto.IntakeDay
import ru.application.homemedkit.data.dto.IntakeTime
import ru.application.homemedkit.data.model.IntakeAmountTime
import ru.application.homemedkit.helpers.BLANK
import ru.application.homemedkit.helpers.FORMAT_DD_MM_YYYY
import ru.application.homemedkit.helpers.FORMAT_H_MM
import ru.application.homemedkit.helpers.Preferences
import ru.application.homemedkit.helpers.ZONE
import ru.application.homemedkit.helpers.enums.IntakeExtra
import ru.application.homemedkit.helpers.enums.Interval
import ru.application.homemedkit.helpers.enums.Interval.CUSTOM
import ru.application.homemedkit.helpers.enums.Interval.DAILY
import ru.application.homemedkit.helpers.enums.Interval.WEEKLY
import ru.application.homemedkit.helpers.enums.Period
import ru.application.homemedkit.helpers.enums.Period.INDEFINITE
import ru.application.homemedkit.helpers.enums.Period.OTHER
import ru.application.homemedkit.helpers.enums.Period.PICK
import ru.application.homemedkit.helpers.enums.SchemaType
import ru.application.homemedkit.helpers.extensions.toIntake
import ru.application.homemedkit.helpers.extensions.toMedicineIntake
import ru.application.homemedkit.helpers.extensions.toState
import ru.application.homemedkit.helpers.getDateTime
import ru.application.homemedkit.models.events.IntakeEvent
import ru.application.homemedkit.models.states.IntakeState
import ru.application.homemedkit.models.validation.Validation
import ru.application.homemedkit.receivers.AlarmSetter
import ru.application.homemedkit.ui.navigation.Screen.Intake
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime

class IntakeViewModel(saved: SavedStateHandle) : ViewModel() {
    private lateinit var setter: AlarmSetter

    private val dao = database.intakeDAO()
    private val args = saved.toRoute<Intake>()

    private val _state = MutableStateFlow(IntakeState())
    val state = _state
        .onStart {
            dao.getById(args.intakeId)?.let { _state.value = it.toState() } ?: run {
                database.medicineDAO().getById(args.medicineId)?.let { medicine ->
                    _state.update { state ->
                        state.copy(
                            medicineId = medicine.id,
                            medicine = medicine.toMedicineIntake(),
                            amountStock = medicine.prodAmount.toString(),
                            doseType = medicine.doseType.title,
                            image = medicine.images.firstOrNull() ?: BLANK
                        )
                    }
                }
            }
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(), IntakeState())

    fun setAlarmSetter(alarmSetter: AlarmSetter) {
        setter = alarmSetter
    }

    fun setExitFirstLaunch() {
        Preferences.isFirstLaunch = false
        _state.update { it.copy(isFirstLaunch = false) }
    }

    fun add() {
        if (validate()) {
            viewModelScope.launch(Dispatchers.IO) {
                val intakeId = dao.insert(_state.value.toIntake())

                val days = _state.value.pickedDays.map { IntakeDay(intakeId, it) }
                database.intakeDayDAO().insert(days)

                val scheduled = mutableListOf<Alarm>()

                _state.value.pickedTime.forEach { pickedTime ->
                    var initial = LocalDateTime.of(
                        LocalDate.parse(_state.value.startDate, FORMAT_DD_MM_YYYY),
                        LocalTime.of(pickedTime.picker.hour, pickedTime.picker.minute)
                    )

                    val finish = LocalDateTime.of(
                        LocalDate.parse(_state.value.finalDate, FORMAT_DD_MM_YYYY),
                        LocalTime.of(pickedTime.picker.hour, pickedTime.picker.minute)
                    )

                    initial = initial.let {
                        var unix = it

                        while (unix.toInstant(ZONE).toEpochMilli() < System.currentTimeMillis()) {
                            unix = unix.plusDays(1)
                        }

                        unix
                    }

                    dao.addIntakeTime(
                        IntakeTime(
                            intakeId = intakeId,
                            time = pickedTime.time,
                            amount = pickedTime.amount.toDouble()
                        )
                    )

                    while (!initial.isAfter(finish)) {
                        if (initial.dayOfWeek in _state.value.pickedDays) {
                            scheduled.add(
                                Alarm(
                                    intakeId = intakeId,
                                    trigger = initial.toInstant(ZONE).toEpochMilli(),
                                    amount = pickedTime.amount.toDouble(),
                                    preAlarm = _state.value.preAlarm
                                )
                            )
                        }

                        initial = initial.plusDays(_state.value.interval.toLong())
                    }
                }

                scheduled.sortedBy(Alarm::trigger)
                database.alarmDAO().insert(scheduled)
                setter.setPreAlarm(intakeId)

                _state.update {
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
                LocalDate.parse(_state.value.finalDate, FORMAT_DD_MM_YYYY),
                LocalTime.parse(_state.value.pickedTime.last().time, FORMAT_H_MM)
            )

            viewModelScope.launch(Dispatchers.IO) {
                setter.removeAlarm(_state.value.intakeId)
                database.alarmDAO().deleteByIntakeId(_state.value.intakeId)

                database.intakeDayDAO().deleteByIntakeId(_state.value.intakeId)

                val days = _state.value.pickedDays.map { IntakeDay(_state.value.intakeId, it) }
                database.intakeDayDAO().insert(days)



                if (finalDate >= LocalDateTime.now()) {
                    dao.deleteIntakeTime(_state.value.intakeId)

                    val scheduled = mutableListOf<Alarm>()

                    _state.value.pickedTime.forEach { pickedTime ->
                        var initial = LocalDateTime.of(
                            LocalDate.parse(_state.value.startDate, FORMAT_DD_MM_YYYY),
                            LocalTime.of(pickedTime.picker.hour, pickedTime.picker.minute)
                        )

                        val finish = LocalDateTime.of(
                            LocalDate.parse(_state.value.finalDate, FORMAT_DD_MM_YYYY),
                            LocalTime.of(pickedTime.picker.hour, pickedTime.picker.minute)
                        )

                        initial = initial.let {
                            var unix = it

                            while (unix.toInstant(ZONE).toEpochMilli() < System.currentTimeMillis()) {
                                unix = unix.plusDays(1)
                            }

                            unix
                        }

                        dao.addIntakeTime(
                            IntakeTime(
                                intakeId = _state.value.intakeId,
                                time = pickedTime.time,
                                amount = pickedTime.amount.toDouble()
                            )
                        )

                        while (!initial.isAfter(finish)) {
                            if (initial.dayOfWeek in _state.value.pickedDays) {
                                scheduled.add(
                                    Alarm(
                                        intakeId = _state.value.intakeId,
                                        trigger = initial.toInstant(ZONE).toEpochMilli(),
                                        amount = pickedTime.amount.toDouble(),
                                        preAlarm = _state.value.preAlarm
                                    )
                                )
                            }

                            initial = initial.plusDays(_state.value.interval.toLong())
                        }
                    }

                    database.alarmDAO().insert(scheduled)
                    setter.setPreAlarm(_state.value.intakeId)
                }

                dao.update(_state.value.toIntake())
                _state.update {
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

    fun delete() {
        viewModelScope.launch(Dispatchers.IO) {
            dao.getAlarms(_state.value.intakeId).forEach { setter.removeAlarm(it.alarmId) }
        }
        viewModelScope.launch(Dispatchers.IO) {
            dao.delete(_state.value.toIntake())
        }
    }

    fun onEvent(event: IntakeEvent) {
        when(event) {
            is IntakeEvent.SetAmount ->
                if (event.amount.isNotEmpty()) when(event.amount.replace(',','.').toDoubleOrNull()) {
                    null -> {}
                    else -> _state.update {
                        it.copy(
                            pickedTime = it.pickedTime.apply {
                                if (it.sameAmount) forEachIndexed { index, _ ->
                                    this[index] = this[index].copy(
                                        amount = event.amount.replace(',', '.')
                                    )
                                } else this[event.index] = this[event.index].copy(
                                    amount = event.amount.replace(',', '.')
                                )
                            }
                        )
                    }
                } else _state.update {
                    it.copy(
                        pickedTime = it.pickedTime.apply {
                            if (it.sameAmount) forEachIndexed { index, _ ->
                                this[index] = this[index].copy(
                                    amount = BLANK
                                )
                            } else this[event.index] = this[event.index].copy(
                                amount = BLANK
                            )
                        }
                    )
                }

            is IntakeEvent.SetInterval -> when (val interval = event.interval) {
                is Interval -> when (interval) {
                    DAILY, WEEKLY -> _state.update {
                        it.copy(
                            interval = interval.days.toString(),
                            intervalType = interval,
                            showIntervalTypePicker = false
                        )
                    }

                    CUSTOM -> _state.update {
                        it.copy(
                            interval = BLANK,
                            intervalType = interval,
                            showIntervalTypePicker = false
                        )
                    }
                }

                is String -> if (interval.isDigitsOnly() && interval.length <= 2)
                    _state.update { it.copy(interval = interval) }
            }

            is IntakeEvent.SetPeriod -> when (val period = event.period) {
                is Period -> when(period) {
                    PICK -> _state.update {
                        it.copy(
                            period = period.days.toString(),
                            periodType = period,
                            startDate = BLANK,
                            finalDate = BLANK,
                            showPeriodTypePicker = false
                        )
                    }
                    OTHER -> _state.update {
                        it.copy(
                            period = BLANK,
                            periodType = period,
                            startDate = BLANK,
                            finalDate = BLANK,
                            showPeriodTypePicker = false
                        )
                    }
                    INDEFINITE -> _state.update {
                        it.copy(
                            startDate = LocalDate.now().format(FORMAT_DD_MM_YYYY),
                            finalDate = LocalDate.now().plusDays(period.days.toLong()).format(FORMAT_DD_MM_YYYY),
                            period = period.days.toString(),
                            periodType = period,
                            showPeriodTypePicker = false
                        )
                    }
                }

                is String -> if (period.isEmpty()) _state.update {
                    it.copy(startDate = BLANK, finalDate = BLANK, period = BLANK)
                }
                else if (period.isDigitsOnly() && period.length <= 3) _state.update {
                    it.copy(
                        startDate = LocalDate.now().format(FORMAT_DD_MM_YYYY),
                        finalDate = LocalDate.now().plusDays(period.toLong() - 1).format(FORMAT_DD_MM_YYYY),
                        period = period
                    )
                }

                is Pair<*, *> -> if (period.first != null && period.second != null) _state.update {
                    it.copy(
                        startDate = getDateTime(period.first as Long).format(FORMAT_DD_MM_YYYY),
                        finalDate = getDateTime(period.second as Long).format(FORMAT_DD_MM_YYYY),
                        period = PICK.days.toString()
                    )
                }
            }
            is IntakeEvent.SetFoodType -> _state.update {
                it.copy(foodType = if (event.type == _state.value.foodType) -1 else event.type)
            }

            IntakeEvent.IncTime -> _state.update {
                it.copy(
                    pickedTime = it.pickedTime.apply {
                        add(
                            IntakeAmountTime(
                                amount = it.pickedTime.first().amount,
                                time = BLANK,
                                picker = TimePickerState(12, 0, true)
                            )
                        )
                    }
                )
            }
            IntakeEvent.DecTime -> if (_state.value.pickedTime.size > 1) _state.update {
                it.copy(
                    pickedTime = it.pickedTime.apply { removeAt(size - 1) }
                )
            }

            is IntakeEvent.ShowDialogDescription -> _state.update {
                it.copy(
                    extraDesc = event.description,
                    showDialogDescription = !it.showDialogDescription
                )
            }
            is IntakeEvent.ShowDialogDelete -> _state.update { it.copy(showDialogDelete = !it.showDialogDelete) }
            is IntakeEvent.ShowDialogDataLoss -> _state.update { it.copy(showDialogDataLoss = event.flag) }


            is IntakeEvent.ShowSchemaTypePicker -> _state.update { it.copy(showSchemaTypePicker = !it.showSchemaTypePicker) }
            is IntakeEvent.ShowDateRangePicker -> _state.update { it.copy(showDateRangePicker = !it.showDateRangePicker) }
            is IntakeEvent.ShowPeriodTypePicker -> _state.update { it.copy(showPeriodTypePicker = !it.showPeriodTypePicker) }
            is IntakeEvent.ShowIntervalTypePicker -> _state.update { it.copy(showIntervalTypePicker = !it.showIntervalTypePicker) }
            is IntakeEvent.ShowTimePicker -> _state.update {
                it.copy(
                    showTimePicker = !it.showTimePicker,
                    timePickerIndex = event.index
                )
            }

            is IntakeEvent.SetSchemaType -> when (event.type) {
                SchemaType.INDEFINITELY -> {
                    _state.update {
                        it.copy(
                            startDate = LocalDate.now().format(FORMAT_DD_MM_YYYY),
                            finalDate = LocalDate.now().plusDays(INDEFINITE.days.toLong()).format(FORMAT_DD_MM_YYYY),
                            interval = DAILY.days.toString(),
                            period = INDEFINITE.days.toString(),
                            pickedDays = DayOfWeek.entries.toMutableStateList(),
                            schemaType = event.type,
                            showSchemaTypePicker = false
                        )
                    }
                }

                SchemaType.BY_DAYS -> _state.update {
                    it.copy(
                        startDate = BLANK,
                        finalDate = BLANK,
                        interval = DAILY.days.toString(),
                        period = BLANK,
                        pickedDays = DayOfWeek.entries.toMutableStateList(),
                        schemaType = event.type,
                        showSchemaTypePicker = false
                    )
                }
                SchemaType.PERSONAL -> _state.update {
                    it.copy(
                        startDate = BLANK,
                        finalDate = BLANK,
                        interval = DAILY.days.toString(),
                        period = BLANK,
                        pickedDays = DayOfWeek.entries.toMutableStateList(),
                        schemaType = event.type,
                        showSchemaTypePicker = false
                    )
                }
            }
            is IntakeEvent.SetPickedDay -> _state.update {
                it.copy(
                    pickedDays = it.pickedDays.apply {
                        if (event.day in this) remove(event.day) else add(event.day)
                        if (isEmpty()) addAll(DayOfWeek.entries)
                        sort()
                    }
                )
            }
            is IntakeEvent.SetPickedTime -> {
                val index = _state.value.timePickerIndex
                val picker = _state.value.pickedTime[index].picker
                val time = LocalTime.of(picker.hour, picker.minute).format(FORMAT_H_MM)

                _state.update {
                    it.copy(
                        showTimePicker = false,
                        pickedTime = it.pickedTime.apply {
                            this[index] = this[index].copy(
                                time = time,
                                picker = picker
                            )
                        }
                    )
                }
            }

            is IntakeEvent.SetSameAmount -> _state.update {
                it.copy(
                    sameAmount = event.flag,
                    pickedTime = it.pickedTime.apply {
                        forEachIndexed { index, _ ->
                            this[index] = this[index].copy(
                                amount = BLANK
                            )
                        }
                    }
                )
            }

            is IntakeEvent.SetIntakeExtra -> when (event.extra) {
                IntakeExtra.CANCELLABLE -> _state.update { it.copy(cancellable = !it.cancellable) }
                IntakeExtra.FULLSCREEN -> _state.update { it.copy(fullScreen = !it.fullScreen) }
                IntakeExtra.NO_SOUND -> _state.update { it.copy(noSound = !it.noSound) }
                IntakeExtra.PREALARM -> _state.update { it.copy(preAlarm = !it.preAlarm) }
            }.also {
                _state.update {
                    it.copy(
                        selectedExtras = it.selectedExtras.apply {
                            if (event.extra in this) remove(event.extra) else add(event.extra)
                        }
                    )
                }
            }
        }
    }

    fun setEditing() = _state.update { it.copy(adding = false, editing = true, default = false) }

    private fun validate() : Boolean {
        val checkAmount = Validation.checkAmount(_state.value.pickedTime)
        val checkInterval = Validation.textNotEmpty(_state.value.interval)
        val checkPeriod = Validation.textNotEmpty(_state.value.period)
        val checkDateS = Validation.textNotEmpty(_state.value.startDate)
        val checkDateF = Validation.textNotEmpty(_state.value.finalDate)
        val checkTime = Validation.checkTime(_state.value.pickedTime)

        val hasError = listOf(
            checkAmount, checkInterval, checkPeriod, checkDateS, checkDateF, checkTime
        ).any { !it.successful }

        return if (!hasError) true
        else {
            _state.update {
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