@file:OptIn(ExperimentalMaterial3Api::class)

package ru.application.homemedkit.models.viewModels

import android.content.Context
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.TimePickerState
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.toMutableStateList
import androidx.compose.ui.util.fastFilter
import androidx.core.app.NotificationManagerCompat
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.conflate
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import ru.application.homemedkit.HomeMeds.Companion.database
import ru.application.homemedkit.data.dto.Alarm
import ru.application.homemedkit.data.dto.IntakeTaken
import ru.application.homemedkit.data.model.IntakeFuture
import ru.application.homemedkit.data.model.IntakePast
import ru.application.homemedkit.helpers.BLANK
import ru.application.homemedkit.helpers.FORMAT_S
import ru.application.homemedkit.helpers.SchemaTypes
import ru.application.homemedkit.helpers.ZONE
import ru.application.homemedkit.helpers.getDateTime
import ru.application.homemedkit.models.states.IntakesState
import ru.application.homemedkit.models.states.TakenState
import java.time.Instant
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
    val takenState = _takenState.asStateFlow()

    val intakes = combine(_state, intakeDAO.getFlow()) { query, list ->
        list.fastFilter { medicineDAO.getProductName(it.medicineId).contains(query.search, true) }
    }.flowOn(Dispatchers.IO)
        .conflate()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000L),
            initialValue = emptyList()
        )

    @OptIn(ExperimentalCoroutinesApi::class)
    val schedule = intakes.flatMapLatest { filtered ->
        flow {
            emit(mutableListOf<Alarm>().apply {
                filtered.forEach { intake ->
                    database.alarmDAO().getByIntake(intake.intakeId).forEach { alarm ->
                        var first = getDateTime(alarm.trigger).toLocalDateTime()

                        val last = LocalDateTime.of(
                            LocalDate.parse(intake.finalDate, FORMAT_S),
                            getDateTime(alarm.trigger).toLocalTime()
                        )

                        while (!first.isAfter(last)) {
                            add(
                                Alarm(
                                    intakeId = intake.intakeId,
                                    trigger = first.toInstant(ZONE).toEpochMilli(),
                                    amount = alarm.amount
                                )
                            )

                            first = first.plusDays(
                                if (intake.schemaType == SchemaTypes.BY_DAYS) intake.schemaType.interval.days.toLong()
                                else intake.interval.toLong()
                            )
                        }
                    }
                }
            }
                .sortedBy { it.trigger }
                .filter { it.trigger > System.currentTimeMillis() }
                .groupBy { Instant.ofEpochMilli(it.trigger).atZone(ZONE).toLocalDate().toEpochDay() }
                .map { IntakeFuture(it.key, it.value) }
            )
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000L), emptyList())

    val taken = combine(_state, takenDAO.getFlow()) { query, list ->
        list.fastFilter { it.productName.contains(query.search, true) }
            .groupBy { Instant.ofEpochMilli(it.trigger).atZone(ZONE).toLocalDate().toEpochDay() }
            .toSortedMap(Comparator.reverseOrder())
            .map { IntakePast(it.key, it.value.toMutableStateList()) }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000L), mutableStateListOf())

    fun setSearch(text: String) = _state.update { it.copy(search = text) }
    fun clearSearch() = _state.update { it.copy(search = BLANK) }

    fun showDialog(taken: IntakeTaken) {
        val time = getDateTime(taken.inFact)

        _takenState.update {
            it.copy(
                takenId = taken.takenId,
                medicineId = taken.medicineId,
                alarmId = taken.alarmId,
                productName = taken.productName,
                amount = taken.amount,
                trigger = taken.trigger,
                inFact = taken.inFact,
                pickerState = TimePickerState(time.hour, time.minute, true),
                taken = taken.taken,
                selection = if (taken.taken) 1 else 0,
                notified = taken.notified
            )
        }
        _state.update { it.copy(showDialog = true) }
    }

    fun pickTab(tab: Int) = _state.update { it.copy(tab = tab) }
    fun hideDialog() = _state.update { it.copy(showDialog = false) }
    fun showPicker(flag: Boolean = false) = _takenState.update { it.copy(showPicker = flag) }

    fun showDialogDate() = _state.update { it.copy(showDialogDate = !it.showDialogDate) }
    fun scrollToClosest(time: Long) {
        val list = if (_state.value.tab == 1) schedule.value else taken.value

        if (list.isEmpty()) {
            _state.update { it.copy(showDialogDate = !it.showDialogDate) }
            return
        }

        val day = Instant.ofEpochMilli(time).atZone(ZONE).toLocalDate().toEpochDay()
        val value = list.map { it.date }.minByOrNull { abs(day - it) } ?: list.first().date
        val itemsIndex = list.indexOfFirst { it.date == value }

        var group = 0
        kotlin.run lit@{
            list.forEachIndexed { index, listScheme ->
                if (index < itemsIndex) group += listScheme.intakes.size
                else return@lit
            }
        }

        viewModelScope.launch {
            _state.value.run {
                (if (tab == 1) stateB else stateC).scrollToItem(group + itemsIndex)
            }
            _state.update { it.copy(showDialogDate = !it.showDialogDate) }
        }
    }

    fun setFactTime() {
        val picker = _takenState.value.pickerState
        val trigger = LocalDateTime.of(
            LocalDate.now(),
            LocalTime.of(picker.hour, picker.minute)
        ).toInstant(ZONE).toEpochMilli()

        _takenState.update { it.copy(inFact = trigger, showPicker = false) }
    }

    fun setSelection(index: Int) = _takenState.update {
        it.copy(selection = index, inFact = if (index == 0) 0L else System.currentTimeMillis())
    }

    fun saveTaken(context: Context, takenNow: Boolean, takenOld: Boolean) {
        NotificationManagerCompat.from(context).cancel(_takenState.value.takenId.toInt())
        NotificationManagerCompat.from(context).cancel(_takenState.value.alarmId.toInt())

        takenDAO.setTaken(_takenState.value.takenId, takenNow, if (takenNow) _takenState.value.inFact else 0L)
        takenDAO.setNotified(_takenState.value.takenId)

        medicineDAO.getById(_takenState.value.medicineId)?.let {
            if (takenNow && !takenOld) medicineDAO.intakeMedicine(it.id, _takenState.value.amount)
            if (!takenNow && takenOld) medicineDAO.untakeMedicine(it.id, _takenState.value.amount)
        }

        _state.update { it.copy(showDialog = false) }
    }
}