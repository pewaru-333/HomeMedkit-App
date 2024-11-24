@file:OptIn(ExperimentalMaterial3Api::class)

package ru.application.homemedkit.models.viewModels

import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.TimePickerState
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.core.text.isDigitsOnly
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import ru.application.homemedkit.HomeMeds.Companion.database
import ru.application.homemedkit.helpers.BLANK
import ru.application.homemedkit.helpers.FORMAT_H
import ru.application.homemedkit.helpers.FORMAT_S
import ru.application.homemedkit.helpers.Intervals
import ru.application.homemedkit.helpers.Intervals.CUSTOM
import ru.application.homemedkit.helpers.Intervals.DAILY
import ru.application.homemedkit.helpers.Intervals.WEEKLY
import ru.application.homemedkit.helpers.Periods
import ru.application.homemedkit.helpers.Periods.INDEFINITE
import ru.application.homemedkit.helpers.Periods.OTHER
import ru.application.homemedkit.helpers.Periods.PICK
import ru.application.homemedkit.helpers.getDateTime
import ru.application.homemedkit.helpers.longSeconds
import ru.application.homemedkit.helpers.toIntake
import ru.application.homemedkit.models.states.IntakeState
import ru.application.homemedkit.receivers.AlarmSetter
import java.time.LocalDate
import java.time.LocalTime

class IntakeViewModel(intakeId: Long, private val setter: AlarmSetter) : ViewModel() {
    private val dao = database.intakeDAO()

    private val _state = MutableStateFlow(IntakeState())
    val state = _state.asStateFlow()
        .onStart {
            dao.getById(intakeId)?.let { intake ->
                _state.update { state ->
                    state.copy(
                        adding = false,
                        editing = false,
                        default = true,
                        intakeId = intake.intakeId,
                        medicineId = intake.medicineId,
                        amount = intake.amount.toString(),
                        interval = intake.interval.toString(),
                        intervalE = Intervals.getValue(intake.interval),
                        period = intake.period.toString(),
                        periodE = Periods.getValue(intake.period),
                        foodType = intake.foodType,
                        time = intake.time.mapTo(SnapshotStateList()) { it.format(FORMAT_H) },
                        times = SnapshotStateList<TimePickerState>().apply {
                            intake.time.mapTo(SnapshotStateList()) { it.format(FORMAT_H) }.forEach {
                                val hour = LocalTime.parse(it, FORMAT_H).hour
                                val min = LocalTime.parse(it, FORMAT_H).minute
                                add(TimePickerState(hour, min, true))
                            }
                        },
                        startDate = intake.startDate,
                        finalDate = intake.finalDate,
                        fullScreen = intake.fullScreen,
                        noSound = intake.noSound,
                        preAlarm = intake.preAlarm
                    )
                }
            }
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000L), IntakeState())

    fun getIntervalTitle() = if (_state.value.intervalE == CUSTOM) CUSTOM.title
    else Intervals.getTitle(_state.value.interval)

    fun add() {
        val time = _state.value.time.map { LocalTime.parse(it, FORMAT_H) }
        val startDate = _state.value.startDate

        viewModelScope.launch(Dispatchers.IO) {
            val id = dao.add(_state.value.toIntake(time))

            if (_state.value.preAlarm) {
                val preTriggers = longSeconds(startDate, time.map { it.minusMinutes(30) })
                setter.setAlarm(intakeId = id, triggers = preTriggers, preAlarm = true)
            }

            setter.setAlarm(intakeId = id, triggers = longSeconds(startDate, time))
            _state.update { it.copy(adding = false, default = true, intakeId = id) }
        }
    }

    fun update() {
        val time = _state.value.time.map { LocalTime.parse(it, FORMAT_H) }
        val startDate = _state.value.startDate

        viewModelScope.launch(Dispatchers.IO) {
            dao.getAlarms(intakeId = _state.value.intakeId).forEach { setter.removeAlarm(it.alarmId) }
            setter.setAlarm(intakeId = _state.value.intakeId, triggers = longSeconds(startDate, time))

            if (_state.value.preAlarm) {
                val preTriggers = longSeconds(startDate, time.map { it.minusMinutes(30) })
                setter.setAlarm(intakeId = _state.value.intakeId, triggers = preTriggers, preAlarm = true)
            }

            dao.update(_state.value.toIntake(time))
            _state.update { it.copy(adding = false, editing = false, default = true) }
        }
    }

    fun delete() {
        dao.getAlarms(intakeId = _state.value.intakeId).forEach { setter.removeAlarm(it.alarmId) }

        viewModelScope.launch(Dispatchers.IO) { dao.delete(_state.value.toIntake(emptyList())) }
    }

    fun setEditing() = _state.update { it.copy(adding = false, editing = true, default = false) }
    fun setMedicineId(medicineId: Long) = _state.update { it.copy(medicineId = medicineId) }

    fun setAmount(amount: String) = if (amount.isNotEmpty())
        when (amount.replace(',', '.').toDoubleOrNull()) {
            null -> {}
            else -> _state.update { it.copy(amount = amount.replace(',', '.')) }
        } else _state.update { it.copy(amount = BLANK) }

    fun setInterval(interval: Any?) {
        when (interval) {
            is Intervals -> when (interval) {
                DAILY, WEEKLY -> _state.update {
                    it.copy(
                        interval = interval.days.toString(),
                        intervalE = interval,
                        showIntervalM = false
                    )
                }

                CUSTOM -> _state.update {
                    it.copy(
                        interval = BLANK,
                        intervalE = interval,
                        showIntervalM = false
                    )
                }
            }

            is String -> if (interval.isDigitsOnly() && interval.length <= 2)
                _state.update { it.copy(interval = interval) }
        }
    }

    fun setPeriod(period: Any?) {
        when (period) {
            is Periods -> when(period) {
                PICK -> _state.update {
                    it.copy(
                        period = period.days.toString(),
                        periodE = period,
                        startDate = BLANK,
                        finalDate = BLANK,
                        showPeriodM = false
                    )
                }
                OTHER -> _state.update {
                    it.copy(
                        period = BLANK,
                        periodE = period,
                        startDate = BLANK,
                        finalDate = BLANK,
                        showPeriodM = false
                    )
                }
                INDEFINITE -> _state.update {
                    it.copy(
                        startDate = LocalDate.now().format(FORMAT_S),
                        finalDate = LocalDate.now().plusDays(period.days.toLong()).format(FORMAT_S),
                        period = period.days.toString(),
                        periodE = period,
                        showPeriodM = false
                    )
                }
            }

            is String -> if (period.isEmpty()) _state.update {
                it.copy(startDate = BLANK, finalDate = BLANK, period = BLANK)
            }
            else if (period.isDigitsOnly() && period.length <= 3) _state.update {
                it.copy(
                    startDate = LocalDate.now().format(FORMAT_S),
                    finalDate = LocalDate.now().plusDays(period.toLong()).format(FORMAT_S),
                    period = period
                )
            }

            is Pair<*, *> -> if (period.first != null && period.second != null) _state.update {
                it.copy(
                    startDate = getDateTime(period.first as Long).format(FORMAT_S),
                    finalDate = getDateTime(period.second as Long).format(FORMAT_S),
                    period = PICK.days.toString(),
                    showPeriodD = false
                )
            }
        }
    }

    fun setFoodType(type: Int) = _state.update {
        it.copy(foodType = if (type == _state.value.foodType) -1 else type)
    }

    fun incTime() = _state.update {
        it.copy(
            time = it.time.apply { add(BLANK) },
            times = it.times.apply { add(TimePickerState(12, 0, true)) }
        )
    }

    fun decTime() = if (_state.value.time.size > 1) _state.update {
        it.copy(
            time = it.time.apply { removeAt(size - 1) },
            times = it.times.apply { removeAt(size - 1) }
        )
    } else {}

    fun setTime() {
        val picker = _state.value.times[_state.value.timeF]
        val time = LocalTime.of(picker.hour, picker.minute).format(FORMAT_H)

        _state.update {
            it.copy(time = it.time.apply { this[_state.value.timeF] = time }, showTimeP = false)
        }
    }

    fun showIntervalM(flag: Boolean) = if (_state.value.adding || state.value.editing)
        _state.update { it.copy(showIntervalM = flag) } else {}

    fun showPeriodD(flag: Boolean = false) =
        if (_state.value.periodE == PICK && (_state.value.adding || _state.value.editing))
            _state.update { it.copy(showPeriodD = flag) } else {}

    fun showPeriodM(flag: Boolean) = if (_state.value.adding || state.value.editing)
        _state.update { it.copy(showPeriodM = flag) } else {}

    fun showTimePicker(flag: Boolean = false, index: Int = 0) =
        _state.update { it.copy(showTimeP = flag, timeF = index) }

    fun setFullScreen(flag: Boolean) = _state.update { it.copy(fullScreen = flag) }
    fun setNoSound(flag: Boolean) = _state.update { it.copy(noSound = flag) }
    fun setPreAlarm(flag: Boolean) = _state.update { it.copy(preAlarm = flag) }

    fun showDialog() = _state.update { it.copy(showDialog = true) }
    fun hideDialog() = _state.update { it.copy(showDialog = false) }

    fun validate() = mutableListOf(
        _state.value.amount, _state.value.interval, _state.value.period,
        _state.value.startDate, _state.value.finalDate
    ).apply { addAll(_state.value.time) }.all(String::isNotBlank)
}