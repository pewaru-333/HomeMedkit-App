@file:OptIn(ExperimentalMaterial3Api::class)

package ru.application.homemedkit.models.viewModels

import android.app.AlarmManager
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.TimePickerState
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.toMutableStateList
import androidx.compose.ui.util.fastFilter
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.runBlocking
import ru.application.homemedkit.HomeMeds.Companion.database
import ru.application.homemedkit.data.dto.Alarm
import ru.application.homemedkit.data.dto.IntakeTaken
import ru.application.homemedkit.data.model.IntakeFuture
import ru.application.homemedkit.data.model.IntakePast
import ru.application.homemedkit.helpers.BLANK
import ru.application.homemedkit.helpers.FORMAT_DH
import ru.application.homemedkit.helpers.FORMAT_S
import ru.application.homemedkit.helpers.ZONE
import ru.application.homemedkit.helpers.getDateTime
import ru.application.homemedkit.models.states.IntakesState
import ru.application.homemedkit.models.states.TakenState
import java.time.Instant
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime

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
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000L),
        initialValue = runBlocking(Dispatchers.IO) { intakeDAO.getFlow().firstOrNull() }
    )

    @OptIn(ExperimentalCoroutinesApi::class)
    val schedule = intakes.flatMapLatest { filtered ->
        flow {
            emit(mutableListOf<Alarm>().apply {
                filtered?.forEach { (intakeId, _, _, interval, _, time, _, startDate, finalDate) ->
                    if (time.size == 1) {
                        var milliS = LocalDateTime.parse("$startDate ${time[0]}", FORMAT_DH)
                            .toInstant(ZONE).toEpochMilli()
                        val milliF = LocalDateTime.parse("$finalDate ${time[0]}", FORMAT_DH)
                            .toInstant(ZONE).toEpochMilli()

                        while (milliS <= milliF) {
                            add(Alarm(intakeId = intakeId, trigger = milliS))
                            milliS += interval * AlarmManager.INTERVAL_DAY
                        }
                    } else {
                        var localS = LocalDate.parse(startDate, FORMAT_S)
                        val localF = LocalDate.parse(finalDate, FORMAT_S)

                        var milliS = LocalDateTime.of(localS, time.first())
                        val milliF = LocalDateTime.of(localF, time.last())

                        while (milliS <= milliF) {
                            time.forEach {
                                val millis = LocalDateTime.of(localS, it).atOffset(ZONE)
                                    .toInstant().toEpochMilli()
                                add(Alarm(intakeId = intakeId, trigger = millis))
                            }
                            localS = localS.plusDays(interval.toLong())
                            milliS = milliS.plusDays(interval.toLong())
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

    fun setFactTime() {
        val picker = _takenState.value.pickerState
        val trigger = LocalDateTime.of(LocalDate.now(), LocalTime.of(picker.hour, picker.minute))
            .toInstant(ZONE).toEpochMilli()

        _takenState.update { it.copy(inFact = trigger, showPicker = false) }
    }

    fun setSelection(index: Int) = _takenState.update {
        it.copy(selection = index, inFact = if (index == 0) 0L else System.currentTimeMillis())
    }

    fun saveTaken(id: Long, taken: Boolean) {
        takenDAO.setTaken(id, taken, if (taken) _takenState.value.inFact else 0L)
        medicineDAO.getById(_takenState.value.medicineId)?.let {
            if (taken) medicineDAO.intakeMedicine(it.id, _takenState.value.amount)
            else medicineDAO.untakeMedicine(it.id, _takenState.value.amount)
        }

        _state.update { it.copy(showDialog = false) }
    }
}