package ru.application.homemedkit.viewModels

import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.TimePickerState
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.core.text.isDigitsOnly
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import ru.application.homemedkit.alarms.AlarmSetter
import ru.application.homemedkit.databaseController.Intake
import ru.application.homemedkit.databaseController.IntakeDAO
import ru.application.homemedkit.helpers.BLANK
import ru.application.homemedkit.helpers.DateHelper.FORMAT_D_MM_Y
import ru.application.homemedkit.helpers.DateHelper.FORMAT_H
import ru.application.homemedkit.helpers.DateHelper.FORMAT_S
import ru.application.homemedkit.helpers.DateHelper.getDateTime
import ru.application.homemedkit.helpers.longSeconds
import ru.application.homemedkit.states.IntakeState
import java.time.LocalDate
import java.time.LocalTime
import java.time.format.DateTimeParseException

@OptIn(ExperimentalMaterial3Api::class)
class IntakeViewModel(
    private val dao: IntakeDAO,
    intakeId: Long,
    private val setter: AlarmSetter
) : ViewModel() {
    private val _uiState = MutableStateFlow(IntakeState())
    val uiState = _uiState.asStateFlow()

    private val _events = MutableSharedFlow<ActivityEvents>()
    val events = _events.asSharedFlow()

    init {
        viewModelScope.launch {
            dao.getByPK(intakeId)?.let { intake ->
                _uiState.update { state ->
                    state.copy(
                        adding = false,
                        editing = false,
                        default = true,
                        intakeId = intake.intakeId,
                        medicineId = intake.medicineId,
                        amount = intake.amount.toString(),
                        interval = intake.interval.toString(),
                        period = intake.period.toString(),
                        periodD = intake.period,
                        time = intake.time.mapTo(SnapshotStateList()) { it.format(FORMAT_H) },
                        startDate = intake.startDate,
                        finalDate = intake.finalDate
                    )
                }
            } ?: IntakeState()
        }
    }

    fun onEvent(event: IntakeEvent) {
        when (event) {
            IntakeEvent.Add -> {
                val medicineId = uiState.value.medicineId
                val amount = uiState.value.amount.toDouble()
                val interval = uiState.value.interval.toInt()
                val time = uiState.value.time.map { LocalTime.parse(it, FORMAT_H) }
                val period = uiState.value.period.toInt()
                val startDate = uiState.value.startDate
                val finalDate = uiState.value.finalDate

                val intake = Intake(
                    medicineId = medicineId,
                    amount = amount,
                    interval = interval,
                    time = time,
                    period = period,
                    startDate = startDate,
                    finalDate = finalDate
                )

                viewModelScope.launch {
                    val id = dao.add(intake)
                    val triggers = longSeconds(startDate, time)

                    setter.setAlarm(intakeId = id, triggers = triggers)
                    _uiState.update { it.copy(adding = false, default = true, intakeId = id) }
                    _events.emit(ActivityEvents.Start)
                }
            }

            IntakeEvent.Update -> {
                val intakeId = uiState.value.intakeId
                val medicineId = uiState.value.medicineId
                val amount = uiState.value.amount.toDouble()
                val interval = uiState.value.interval.toInt()
                val time = uiState.value.time.map { LocalTime.parse(it, FORMAT_H) }
                val period = uiState.value.period.toInt()
                val startDate = uiState.value.startDate
                val finalDate = uiState.value.finalDate

                val intake = Intake(
                    intakeId = intakeId,
                    medicineId = medicineId,
                    amount = amount,
                    interval = interval,
                    time = time,
                    period = period,
                    startDate = startDate,
                    finalDate = finalDate
                )

                viewModelScope.launch {
                    val alarms = dao.getAlarms(intakeId = uiState.value.intakeId)
                    val triggers = longSeconds(startDate, time)

                    alarms.forEach { setter.removeAlarm(it.alarmId) }
                    setter.setAlarm(intakeId = uiState.value.intakeId, triggers = triggers)

                    dao.update(intake)
                    _uiState.update { it.copy(adding = false, editing = false, default = true) }
                }
            }

            IntakeEvent.Delete -> {
                val intake = Intake(intakeId = uiState.value.intakeId)
                val alarms = dao.getAlarms(intakeId = uiState.value.intakeId)
                alarms.forEach { setter.removeAlarm(it.alarmId) }

                viewModelScope.launch {
                    dao.delete(intake)
                    _events.emit(ActivityEvents.Close)
                }
            }

            is IntakeEvent.SetMedicineId -> {
                _uiState.update { it.copy(medicineId = event.medicineId) }
            }

            is IntakeEvent.SetAmount -> {
                if (event.amount.isNotEmpty()) {
                    when (event.amount.replace(',', '.').toDoubleOrNull()) {
                        null -> {}
                        else -> _uiState.update {
                            it.copy(amount = event.amount.replace(',', '.'))
                        }
                    }
                } else _uiState.update { it.copy(amount = BLANK) }
            }

            is IntakeEvent.SetInterval -> {
                when (event.interval) {
                    is Int -> {

                        val days: Int = when (event.interval) {
                            0 -> 1
                            1 -> 7
                            else -> 10
                        }

                        _uiState.update { it.copy(interval = days.toString()) }
                    }

                    is String -> {
                        if (event.interval.isDigitsOnly() && event.interval.length <= 2)
                            _uiState.update { it.copy(interval = event.interval) }
                    }
                }
            }

            is IntakeEvent.SetPeriod -> {
                when (event.period) {
                    is Int -> {
                        val days = when (event.period) {
                            0 -> 7
                            1 -> 30
                            2 -> 45
                            3 -> 38500
                            else -> -1
                        }

                        _uiState.update { it.copy(period = days.toString(), periodD = days) }
                    }

                    is String -> {
                        if (event.period.isEmpty())
                            _uiState.update { it.copy(period = BLANK, periodD = -1) }
                        else {
                            if (event.period.isDigitsOnly() && event.period.length <= 3)
                                _uiState.update {
                                    it.copy(period = event.period, periodD = event.period.toInt())
                                }
                        }
                    }
                }
            }

            is IntakeEvent.IncTime -> {
                _uiState.update {
                    it.copy(
                        time = it.time.apply { add(BLANK) },
                        times = it.times.apply { add(TimePickerState(12, 0, true)) })
                }
            }

            is IntakeEvent.DecTime -> {
                if (_uiState.value.time.size > 1)
                    _uiState.update {
                        it.copy(
                            time = it.time.apply { removeLast() },
                            times = it.times.apply { removeLast() }
                        )
                    }
            }

            is IntakeEvent.SetTime -> {
                val picker = _uiState.value.times[event.time]
                val localTime = LocalTime.of(picker.hour, picker.minute)
                val time = localTime.format(FORMAT_H)

                _uiState.update { it.copy(time = it.time.apply { this[event.time] = time }) }
            }

            is IntakeEvent.SetStart -> {
                when (event.start) {
                    BLANK -> {
                        val today = System.currentTimeMillis()
                        val zoned = getDateTime(today).format(FORMAT_S)

                        val date = try {
                            LocalDate.parse(zoned, FORMAT_D_MM_Y).format(FORMAT_S)
                        } catch (e: DateTimeParseException) {
                            zoned
                        }

                        _uiState.update { it.copy(startDate = date) }
                    }

                    else -> {
                        val date = LocalDate.parse(event.start, FORMAT_D_MM_Y).format(FORMAT_S)
                        _uiState.update { it.copy(startDate = date) }
                    }
                }
            }

            is IntakeEvent.SetFinal -> {
                when (event.final) {
                    BLANK -> {
                        val today = System.currentTimeMillis()
                        val zoned = getDateTime(today).toLocalDate()
                            .plusDays(_uiState.value.periodD.toLong()).format(FORMAT_S)

                        val date = try {
                            LocalDate.parse(zoned, FORMAT_D_MM_Y).format(FORMAT_S)
                        } catch (e: DateTimeParseException) {
                            zoned
                        }

                        _uiState.update { it.copy(finalDate = date) }
                    }

                    else -> {
                        val date = LocalDate.parse(event.final, FORMAT_D_MM_Y).format(FORMAT_S)
                        _uiState.update { it.copy(finalDate = date) }
                    }
                }
            }

            IntakeEvent.SetAdding -> {
                _uiState.update { it.copy(adding = true, editing = false, default = false) }
            }

            IntakeEvent.SetEditing -> {
                _uiState.update { it.copy(adding = false, editing = true, default = false) }
            }
        }
    }

    fun validate(): Boolean {
        return listOf(
            _uiState.value.amount, _uiState.value.interval, _uiState.value.period,
            _uiState.value.startDate, _uiState.value.finalDate
        ).all(String::isNotBlank) && _uiState.value.time.all(String::isNotBlank)
    }
}

sealed interface IntakeEvent {
    data object Add : IntakeEvent
    data object Update : IntakeEvent
    data object Delete : IntakeEvent
    data class SetMedicineId(val medicineId: Long) : IntakeEvent
    data class SetAmount(val amount: String) : IntakeEvent
    data class SetInterval(val interval: Any) : IntakeEvent
    data class SetPeriod(val period: Any) : IntakeEvent
    data object IncTime : IntakeEvent
    data object DecTime : IntakeEvent
    data class SetTime(val time: Int) : IntakeEvent
    data class SetStart(val start: String = BLANK) : IntakeEvent
    data class SetFinal(val final: String = BLANK) : IntakeEvent
    data object SetAdding : IntakeEvent
    data object SetEditing : IntakeEvent
}