package ru.application.homemedkit.viewModels

import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.TimePickerState
import androidx.compose.runtime.mutableStateListOf
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
import ru.application.homemedkit.HomeMeds.Companion.database
import ru.application.homemedkit.data.dto.Intake
import ru.application.homemedkit.helpers.BLANK
import ru.application.homemedkit.helpers.FORMAT_D_MM_Y
import ru.application.homemedkit.helpers.FORMAT_H
import ru.application.homemedkit.helpers.FORMAT_S
import ru.application.homemedkit.helpers.getDateTime
import ru.application.homemedkit.helpers.longSeconds
import ru.application.homemedkit.receivers.AlarmSetter
import ru.application.homemedkit.viewModels.IntakeViewModel.Event.Add
import ru.application.homemedkit.viewModels.IntakeViewModel.Event.DecTime
import ru.application.homemedkit.viewModels.IntakeViewModel.Event.Delete
import ru.application.homemedkit.viewModels.IntakeViewModel.Event.IncTime
import ru.application.homemedkit.viewModels.IntakeViewModel.Event.SetAdding
import ru.application.homemedkit.viewModels.IntakeViewModel.Event.SetAmount
import ru.application.homemedkit.viewModels.IntakeViewModel.Event.SetEditing
import ru.application.homemedkit.viewModels.IntakeViewModel.Event.SetFinal
import ru.application.homemedkit.viewModels.IntakeViewModel.Event.SetFoodType
import ru.application.homemedkit.viewModels.IntakeViewModel.Event.SetInterval
import ru.application.homemedkit.viewModels.IntakeViewModel.Event.SetMedicineId
import ru.application.homemedkit.viewModels.IntakeViewModel.Event.SetPeriod
import ru.application.homemedkit.viewModels.IntakeViewModel.Event.SetStart
import ru.application.homemedkit.viewModels.IntakeViewModel.Event.SetTime
import ru.application.homemedkit.viewModels.IntakeViewModel.Event.Update
import ru.application.homemedkit.viewModels.MedicineViewModel.ActivityEvents.Close
import ru.application.homemedkit.viewModels.MedicineViewModel.ActivityEvents.Start
import java.time.LocalDate
import java.time.LocalTime
import java.time.format.DateTimeParseException

@OptIn(ExperimentalMaterial3Api::class)
class IntakeViewModel(intakeId: Long, private val setter: AlarmSetter) : ViewModel() {
    private val dao = database.intakeDAO()

    private val _state = MutableStateFlow(IntakeState())
    val state = _state.asStateFlow()

    private val _events = MutableSharedFlow<MedicineViewModel.ActivityEvents>()
    val events = _events.asSharedFlow()

    init {
        viewModelScope.launch {
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
                        period = intake.period.toString(),
                        periodD = intake.period,
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
                        finalDate = intake.finalDate
                    )
                }
            } ?: IntakeState()
        }
    }

    fun onEvent(event: Event) {
        when (event) {
            Add -> {
                val medicineId = _state.value.medicineId
                val amount = _state.value.amount.toDouble()
                val interval = _state.value.interval.toInt()
                val foodType = _state.value.foodType
                val time = _state.value.time.map { LocalTime.parse(it, FORMAT_H) }
                val period = _state.value.period.toInt()
                val startDate = _state.value.startDate
                val finalDate = _state.value.finalDate

                val intake = Intake(
                    medicineId = medicineId,
                    amount = amount,
                    interval = interval,
                    foodType = foodType,
                    time = time,
                    period = period,
                    startDate = startDate,
                    finalDate = finalDate
                )

                viewModelScope.launch {
                    val id = dao.add(intake)
                    val triggers = longSeconds(startDate, time)

                    setter.setAlarm(intakeId = id, triggers = triggers)
                    _state.update { it.copy(adding = false, default = true, intakeId = id) }
                    _events.emit(Start)
                }
            }

            Update -> {
                val intakeId = _state.value.intakeId
                val medicineId = _state.value.medicineId
                val amount = _state.value.amount.toDouble()
                val interval = _state.value.interval.toInt()
                val foodType = _state.value.foodType
                val time = _state.value.time.map { LocalTime.parse(it, FORMAT_H) }
                val period = _state.value.period.toInt()
                val startDate = _state.value.startDate
                val finalDate = _state.value.finalDate

                val intake = Intake(
                    intakeId = intakeId,
                    medicineId = medicineId,
                    amount = amount,
                    interval = interval,
                    foodType = foodType,
                    time = time,
                    period = period,
                    startDate = startDate,
                    finalDate = finalDate
                )

                viewModelScope.launch {
                    val alarms = dao.getAlarms(intakeId = _state.value.intakeId)
                    val triggers = longSeconds(startDate, time)

                    alarms.forEach { setter.removeAlarm(it.alarmId) }
                    setter.setAlarm(intakeId = _state.value.intakeId, triggers = triggers)

                    dao.update(intake)
                    _state.update { it.copy(adding = false, editing = false, default = true) }
                }
            }

            Delete -> {
                val intake = Intake(intakeId = _state.value.intakeId)
                val alarms = dao.getAlarms(intakeId = _state.value.intakeId)
                alarms.forEach { setter.removeAlarm(it.alarmId) }

                viewModelScope.launch {
                    dao.delete(intake)
                    _events.emit(Close)
                }
            }

            is SetMedicineId -> _state.update { it.copy(medicineId = event.medicineId) }

            is SetAmount -> {
                if (event.amount.isNotEmpty()) {
                    when (event.amount.replace(',', '.').toDoubleOrNull()) {
                        null -> {}
                        else -> _state.update {
                            it.copy(amount = event.amount.replace(',', '.'))
                        }
                    }
                } else _state.update { it.copy(amount = BLANK) }
            }

            is SetInterval -> {
                when (event.interval) {
                    is Int -> {

                        val days: Int = when (event.interval) {
                            0 -> 1
                            1 -> 7
                            else -> 10
                        }

                        _state.update { it.copy(interval = days.toString()) }
                    }

                    is String -> {
                        if (event.interval.isDigitsOnly() && event.interval.length <= 2)
                            _state.update { it.copy(interval = event.interval) }
                    }
                }
            }

            is SetPeriod -> {
                when (event.period) {
                    is Int -> {
                        val days = when (event.period) {
                            0 -> 7
                            1 -> 30
                            2 -> 45
                            3 -> 38500
                            else -> -1
                        }

                        if (days == 38500) {
                            val start = getDateTime(System.currentTimeMillis()).format(FORMAT_S)
                            val final = getDateTime(System.currentTimeMillis())
                                .plusDays(days.toLong())
                                .format(FORMAT_S)

                            _state.update { it.copy(startDate = start, finalDate = final) }
                        }

                        _state.update { it.copy(period = days.toString(), periodD = days) }
                    }

                    is String -> {
                        if (event.period.isEmpty())
                            _state.update { it.copy(period = BLANK, periodD = -1) }
                        else {
                            if (event.period.isDigitsOnly() && event.period.length <= 3)
                                _state.update {
                                    it.copy(period = event.period, periodD = event.period.toInt())
                                }
                        }
                    }
                }
            }

            is IncTime -> {
                _state.update {
                    it.copy(
                        time = it.time.apply { add(BLANK) },
                        times = it.times.apply { add(TimePickerState(12, 0, true)) })
                }
            }

            is DecTime -> {
                if (_state.value.time.size > 1)
                    _state.update {
                        it.copy(
                            time = it.time.apply { removeLast() },
                            times = it.times.apply { removeLast() }
                        )
                    }
            }

            is SetFoodType -> _state.update { it.copy(foodType = event.type) }

            is SetTime -> {
                val picker = _state.value.times[event.time]
                val localTime = LocalTime.of(picker.hour, picker.minute)
                val time = localTime.format(FORMAT_H)

                _state.update { it.copy(time = it.time.apply { this[event.time] = time }) }
            }

            is SetStart -> {
                when (event.start) {
                    BLANK -> {
                        val today = System.currentTimeMillis()
                        val zoned = getDateTime(today).format(FORMAT_S)

                        val date = try {
                            LocalDate.parse(zoned, FORMAT_D_MM_Y).format(FORMAT_S)
                        } catch (e: DateTimeParseException) {
                            zoned
                        }

                        _state.update { it.copy(startDate = date) }
                    }

                    else -> {
                        val date = LocalDate.parse(event.start, FORMAT_D_MM_Y).format(FORMAT_S)
                        _state.update { it.copy(startDate = date) }
                    }
                }
            }

            is SetFinal -> {
                when (event.final) {
                    BLANK -> {
                        val today = System.currentTimeMillis()
                        val zoned = getDateTime(today).toLocalDate()
                            .plusDays(_state.value.periodD.toLong()).format(FORMAT_S)

                        val date = try {
                            LocalDate.parse(zoned, FORMAT_D_MM_Y).format(FORMAT_S)
                        } catch (e: DateTimeParseException) {
                            zoned
                        }

                        _state.update { it.copy(finalDate = date) }
                    }

                    else -> {
                        val date = LocalDate.parse(event.final, FORMAT_D_MM_Y).format(FORMAT_S)
                        _state.update { it.copy(finalDate = date) }
                    }
                }
            }

            SetAdding -> {
                _state.update { it.copy(adding = true, editing = false, default = false) }
            }

            SetEditing -> {
                _state.update { it.copy(adding = false, editing = true, default = false) }
            }
        }
    }

    fun validate(): Boolean {
        return listOf(
            _state.value.amount, _state.value.interval, _state.value.period,
            _state.value.startDate, _state.value.finalDate
        ).all(String::isNotBlank) && _state.value.time.all(String::isNotBlank)
    }

    sealed interface Event {
        data object Add : Event
        data object Update : Event
        data object Delete : Event
        data class SetMedicineId(val medicineId: Long) : Event
        data class SetAmount(val amount: String) : Event
        data class SetInterval(val interval: Any) : Event
        data class SetPeriod(val period: Any) : Event
        data object IncTime : Event
        data object DecTime : Event
        data class SetFoodType(val type: Int) : Event
        data class SetTime(val time: Int) : Event
        data class SetStart(val start: String = BLANK) : Event
        data class SetFinal(val final: String = BLANK) : Event
        data object SetAdding : Event
        data object SetEditing : Event
    }
}

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
    val times: SnapshotStateList<TimePickerState> = mutableStateListOf(TimePickerState(12, 0, true)),
    val startDate: String = BLANK,
    val finalDate: String = BLANK
)