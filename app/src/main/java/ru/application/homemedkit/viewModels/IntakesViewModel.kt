package ru.application.homemedkit.viewModels

import android.app.AlarmManager
import androidx.compose.foundation.lazy.LazyListState
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.mapLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import ru.application.homemedkit.HomeMeds.Companion.database
import ru.application.homemedkit.R.string.intakes_tab_current
import ru.application.homemedkit.R.string.intakes_tab_list
import ru.application.homemedkit.R.string.intakes_tab_taken
import ru.application.homemedkit.data.dto.Alarm
import ru.application.homemedkit.data.dto.IntakeTaken
import ru.application.homemedkit.helpers.BLANK
import ru.application.homemedkit.helpers.FORMAT_DH
import ru.application.homemedkit.helpers.FORMAT_S
import ru.application.homemedkit.helpers.ZONE
import java.time.Instant
import java.time.LocalDate
import java.time.LocalDateTime
import java.util.Locale.ROOT

object IntakesViewModel : ViewModel() {
    private val _state = MutableStateFlow(IntakesState())
    val state = _state.asStateFlow()

    private val _takenState = MutableStateFlow(TakenState())
    val takenState = _takenState.asStateFlow()

    val tabs = listOf(intakes_tab_list, intakes_tab_current, intakes_tab_taken)

    @OptIn(ExperimentalCoroutinesApi::class)
    val intakes = _state.flatMapLatest { (search) ->
        flow {
            emit(
                database.intakeDAO().getAll().filter { (_, medicineId) ->
                    database.medicineDAO().getProductName(medicineId).lowercase(ROOT)
                        .contains(search.lowercase(ROOT))
                }
            )
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(), emptyList())

    @OptIn(ExperimentalCoroutinesApi::class)
    val schedule = intakes.flatMapLatest { filtered ->
        flow {
            emit(mutableListOf<Alarm>().apply {
                filtered.forEach { (intakeId, _, _, interval, _, time, _, startDate, finalDate) ->
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
            }.sortedBy { it.trigger }
                .filter { it.trigger > System.currentTimeMillis() }
                .groupBy { Instant.ofEpochMilli(it.trigger).atZone(ZONE).toLocalDate().toEpochDay() }
            )
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(), emptyMap())

    @OptIn(ExperimentalCoroutinesApi::class)
    val taken = _state.flatMapLatest { (search) ->
        database.takenDAO().getFlow().mapLatest { list ->
            list.filter { it.productName.lowercase(ROOT).contains(search.lowercase(ROOT)) }
                .sortedByDescending { it.trigger }
                .groupBy { Instant.ofEpochMilli(it.trigger).atZone(ZONE).toLocalDate().toEpochDay() }
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(), emptyMap())

    fun setSearch(text: String) = _state.update { it.copy(search = text) }
    fun clearSearch() = _state.update { it.copy(search = BLANK) }

    fun showDialog(taken: IntakeTaken) {
        _takenState.update {
            it.copy(
                takenId = taken.takenId,
                medicineId = taken.medicineId,
                productName = taken.productName,
                amount = taken.amount,
                trigger = taken.trigger,
                taken = taken.taken,
                notified = taken.notified
            )
        }
        _state.update { it.copy(showDialog = true) }
    }

    fun hideDialog() = _state.update { it.copy(showDialog = false) }

    fun pickTab(tab: Int) = _state.update { it.copy(tab = tab) }

    fun setTaken(id: Long, taken: Boolean) {
        viewModelScope.launch {
            database.takenDAO().setTaken(id, taken)
            database.medicineDAO().getById(_takenState.value.medicineId)?.let {
                if (taken) database.medicineDAO().intakeMedicine(it.id, _takenState.value.amount)
                else database.medicineDAO().untakeMedicine(it.id, _takenState.value.amount)
            }
            _state.update { it.copy(showDialog = false) }
        }
    }
}

data class IntakesState(
    val search: String = BLANK,
    val tab: Int = 0,
    val stateA: LazyListState = LazyListState(),
    val stateB: LazyListState = LazyListState(),
    val stateC: LazyListState = LazyListState(),
    val showDialog: Boolean = false
)

data class TakenState(
    val takenId: Long = 0L,
    val medicineId: Long = 0L,
    val productName: String = BLANK,
    val amount: Double = 0.0,
    val trigger: Long = 0L,
    val taken: Boolean = false,
    val notified: Boolean = false
)