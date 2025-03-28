@file:OptIn(ExperimentalMaterial3Api::class)

package ru.application.homemedkit.models.viewModels

import android.content.Context
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.TimePickerState
import androidx.core.app.NotificationManagerCompat
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import ru.application.homemedkit.HomeMeds.Companion.database
import ru.application.homemedkit.R
import ru.application.homemedkit.R.string.intake_text_not_taken
import ru.application.homemedkit.data.dto.IntakeTaken
import ru.application.homemedkit.data.model.Intake
import ru.application.homemedkit.data.model.IntakePast
import ru.application.homemedkit.data.model.IntakeSchedule
import ru.application.homemedkit.data.model.ScheduleModel
import ru.application.homemedkit.data.model.TakenModel
import ru.application.homemedkit.helpers.BLANK
import ru.application.homemedkit.helpers.FORMAT_D_MMMM_E
import ru.application.homemedkit.helpers.FORMAT_H_MM
import ru.application.homemedkit.helpers.FORMAT_LONG
import ru.application.homemedkit.helpers.ResourceText
import ru.application.homemedkit.helpers.ZONE
import ru.application.homemedkit.helpers.decimalFormat
import ru.application.homemedkit.helpers.enums.IntakeTabs
import ru.application.homemedkit.helpers.enums.Intervals
import ru.application.homemedkit.helpers.formName
import ru.application.homemedkit.helpers.getDateTime
import ru.application.homemedkit.models.events.TakenEvent
import ru.application.homemedkit.models.states.IntakesState
import ru.application.homemedkit.models.states.TakenState
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import kotlin.math.abs

class IntakesViewModel : ViewModel() {
    private val intakeDAO = database.intakeDAO()
    private val medicineDAO = database.medicineDAO()
    private val takenDAO = database.takenDAO()

    private val _state = MutableStateFlow(IntakesState())
    val state = _state.asStateFlow()

    private val _takenState = MutableStateFlow(TakenState())
    val takenState = _takenState
        .onStart {
            takenDAO.getById(_state.value.pickedTakenId)?.let { taken ->
                _takenState.update {
                    it.copy(
                        takenId = taken.takenId,
                        alarmId = taken.alarmId,
                        medicine = medicineDAO.getById(taken.medicineId),
                        productName = taken.productName,
                        amount = taken.amount,
                        date = getDateTime(taken.trigger).format(FORMAT_LONG),
                        scheduled = getDateTime(taken.trigger).format(FORMAT_H_MM),
                        actual = if (taken.taken) ResourceText.StaticString(getDateTime(taken.inFact).format(FORMAT_H_MM))
                        else ResourceText.StringResource(intake_text_not_taken),
                        inFact = taken.inFact,
                        pickerState = getDateTime(taken.inFact).run {
                            TimePickerState(hour, minute, true)
                        },
                        taken = taken.taken,
                        selection = if (taken.taken) 1 else 0,
                        notified = taken.notified
                    )
                }
            }
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(),
            initialValue = TakenState()
        )

    val intakes = combine(_state, intakeDAO.getFlow()) { query, list ->
        list.filter { listOf(it.nameAlias, it.productName).any { it.contains(query.search, true) } }
            .map {
                Intake(
                    intakeId = it.intakeId,
                    title = it.nameAlias.ifEmpty(it::productName),
                    image = it.image.firstOrNull() ?: BLANK,
                    time = it.time.joinToString(),
                    interval = it.time.run {
                        if (size == 1) ResourceText.StringResource(Intervals.getTitle(it.interval.toString()))
                        else ResourceText.PluralStringResource(R.plurals.intake_times_a_day, size, size)
                    }
                )
            }
    }.flowOn(Dispatchers.IO)
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000L),
            initialValue = emptyList()
        )

    val schedule = combine(_state, database.alarmDAO().getFlow()) { query, list ->
        list.filter { it.productName.contains(query.search, true) }
            .groupBy { getDateTime(it.trigger).toLocalDate().toEpochDay() }
            .toSortedMap()
            .map { map ->
                IntakeSchedule(
                    epochDay = map.key,
                    date = LocalDate.ofEpochDay(map.key).run {
                        format(if (LocalDate.now().year == year) FORMAT_D_MMMM_E else FORMAT_LONG)
                    },
                    intakes = map.value.map {
                        ScheduleModel(
                            id = it.alarmId,
                            alarmId = it.alarmId,
                            title = it.nameAlias.ifEmpty(it::productName),
                            image = it.image,
                            time = getDateTime(it.trigger).format(FORMAT_H_MM),
                            doseAmount = ResourceText.StringResource(
                                R.string.intake_text_quantity,
                                it.prodFormNormName.run {
                                    if (isNotEmpty()) formName(this)
                                    else ResourceText.StringResource(R.string.text_amount)
                                },
                                decimalFormat(it.amount),
                                ResourceText.StringResource(it.doseType.title)
                            )
                        )
                    }
                )
            }
    }.flowOn(Dispatchers.IO)
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000L),
            initialValue = emptyList()
        )

    val taken = combine(_state, takenDAO.getFlow()) { query, list ->
        list.filter { it.productName.contains(query.search, true) }
            .groupBy { getDateTime(it.trigger).toLocalDate().toEpochDay() }
            .toSortedMap(Comparator.reverseOrder())
            .map { map ->
                IntakePast(
                    epochDay = map.key,
                    date = LocalDate.ofEpochDay(map.key).run {
                        format(if (LocalDate.now().year == year) FORMAT_D_MMMM_E else FORMAT_LONG)
                    },
                    intakes = map.value.map {
                        TakenModel(
                            id = it.takenId,
                            alarmId = it.alarmId,
                            title = it.productName,
                            image = it.image,
                            time = getDateTime(it.trigger).format(FORMAT_H_MM),
                            taken = it.taken,
                            doseAmount = ResourceText.StringResource(
                                R.string.intake_text_quantity,
                                it.formName.run {
                                    if (isNotEmpty()) formName(this)
                                    else ResourceText.StringResource(R.string.text_amount)
                                },
                                decimalFormat(it.amount),
                                ResourceText.StringResource(it.doseType.title)
                            )
                        )
                    }
                )
            }
    }.flowOn(Dispatchers.IO)
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000L),
            initialValue = emptyList()
        )

    fun onTakenEvent(event: TakenEvent) {
        when (event) {
            is TakenEvent.SaveTaken -> saveTaken(event.context)

            is TakenEvent.SetSelection -> _takenState.update {
                it.copy(
                    selection = event.index,
                    inFact = if (event.index == 0) 0L else System.currentTimeMillis()
                )
            }

            TakenEvent.SetFactTime -> {
                val picker = _takenState.value.pickerState
                val trigger = LocalDateTime.of(
                    LocalDate.now(),
                    LocalTime.of(picker.hour, picker.minute)
                ).toInstant(ZONE).toEpochMilli()

                _takenState.update {
                    it.copy(
                        showPicker = false,
                        inFact = trigger,
                        actual = ResourceText.StaticString(getDateTime(trigger).format(FORMAT_H_MM))
                    )
                }
            }

            is TakenEvent.ShowTimePicker -> _takenState.update { it.copy(showPicker = event.flag) }
            TakenEvent.HideDialog -> _state.update { it.copy(showDialog = false) }
        }
    }

    fun setSearch(text: String) = _state.update { it.copy(search = text) }
    fun clearSearch() = _state.update { it.copy(search = BLANK) }

    fun showDialog(takenId: Long) = _state.update {
        it.copy(
            pickedTakenId = takenId,
            showDialog = true
        )
    }

    fun showDialogDelete(id: Long = 0L) {
        _takenState.update { it.copy(takenId = id) }
        _state.update { it.copy(showDialogDelete = !it.showDialogDelete) }
    }

    fun deleteTaken() {
        viewModelScope.launch(Dispatchers.IO) {
            takenDAO.delete(IntakeTaken(_takenState.value.takenId))
        }

        _state.update { it.copy(showDialogDelete = !it.showDialogDelete) }
    }

    fun pickTab(tab: IntakeTabs) = _state.update { it.copy(tab = tab) }

    fun showDialogDate() = _state.update { it.copy(showDialogDate = !it.showDialogDate) }
    fun scrollToClosest(time: Long) {
        val list = if (_state.value.tab == IntakeTabs.CURRENT) schedule.value else taken.value

        if (list.isEmpty()) {
            _state.update { it.copy(showDialogDate = !it.showDialogDate) }
            return
        }

        val day = getDateTime(time).toLocalDate().toEpochDay()
        val value = list.map { it.epochDay }.minByOrNull { abs(day - it) } ?: list.first().epochDay
        val itemsIndex = list.indexOfFirst { it.epochDay == value }

        var group = 0
        kotlin.run lit@{
            list.forEachIndexed { index, listScheme ->
                if (index < itemsIndex) group += listScheme.intakes.size
                else return@lit
            }
        }

        viewModelScope.launch {
            _state.value.run {
                (if (tab == IntakeTabs.CURRENT) stateB else stateC).scrollToItem(group + itemsIndex)
            }
            _state.update { it.copy(showDialogDate = !it.showDialogDate) }
        }
    }

    private fun saveTaken(context: Context) {
        val takenId = _takenState.value.takenId
        val takenNow = _takenState.value.selection == 1
        val takenOld = _takenState.value.taken

        with(NotificationManagerCompat.from(context)) {
            cancel(takenId.toInt())
            cancel(_takenState.value.alarmId.toInt())
        }

        takenDAO.setTaken(takenId, takenNow, if (takenNow) _takenState.value.inFact else 0L)
        takenDAO.setNotified(takenId)

        _takenState.value.medicine?.let { medicine ->
            medicineDAO.getById(medicine.id)?.let {
                if (takenNow && !takenOld) medicineDAO.intakeMedicine(it.id, _takenState.value.amount)
                if (!takenNow && takenOld) medicineDAO.untakeMedicine(it.id, _takenState.value.amount)
            }
        }

        _state.update { it.copy(showDialog = false) }
    }
}