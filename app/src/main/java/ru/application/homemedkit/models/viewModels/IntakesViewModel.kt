@file:OptIn(ExperimentalMaterial3Api::class)

package ru.application.homemedkit.models.viewModels

import android.content.Context
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.core.app.NotificationManagerCompat
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import ru.application.homemedkit.HomeMeds.Companion.database
import ru.application.homemedkit.data.dto.IntakeTaken
import ru.application.homemedkit.data.model.IntakeList
import ru.application.homemedkit.data.model.IntakeModel
import ru.application.homemedkit.data.model.Schedule
import ru.application.homemedkit.models.events.NewTakenEvent
import ru.application.homemedkit.models.events.TakenEvent
import ru.application.homemedkit.models.states.IntakesState
import ru.application.homemedkit.models.states.NewTakenState
import ru.application.homemedkit.models.states.ScheduledState
import ru.application.homemedkit.models.states.TakenState
import ru.application.homemedkit.receivers.AlarmSetter
import ru.application.homemedkit.utils.BLANK
import ru.application.homemedkit.utils.FORMAT_DD_MM_YYYY
import ru.application.homemedkit.utils.FORMAT_H_MM
import ru.application.homemedkit.utils.ResourceText
import ru.application.homemedkit.utils.ZONE
import ru.application.homemedkit.utils.decimalFormat
import ru.application.homemedkit.utils.enums.IntakeTab
import ru.application.homemedkit.utils.enums.Sorting
import ru.application.homemedkit.utils.extensions.toIntake
import ru.application.homemedkit.utils.extensions.toIntakePast
import ru.application.homemedkit.utils.extensions.toIntakeSchedule
import ru.application.homemedkit.utils.extensions.toTakenState
import ru.application.homemedkit.utils.getDateTime
import java.time.LocalDate
import java.time.LocalTime
import java.time.ZonedDateTime
import kotlin.math.abs

class IntakesViewModel : ViewModel() {
    private val intakeDAO = database.intakeDAO()
    private val medicineDAO = database.medicineDAO()
    private val takenDAO = database.takenDAO()
    private val alarmDAO = database.alarmDAO()

    private val _state = MutableStateFlow(IntakesState())
    val state = _state.asStateFlow()

    private val _newTakenState = MutableStateFlow(NewTakenState())
    val newTakenState = _newTakenState.asStateFlow()

    private val _takenState = MutableStateFlow(TakenState())
    val takenState = _takenState.asStateFlow()

    private val _scheduledState = MutableStateFlow(ScheduledState())
    val scheduleState = _scheduledState.asStateFlow()

    val medicines = medicineDAO.getListFlow(BLANK, Sorting.IN_NAME, emptyList(), false)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(), emptyList())

    @OptIn(ExperimentalCoroutinesApi::class)
    val intakes = _state.flatMapLatest { query ->
        intakeDAO.getFlow(query.search).map { list ->
            withContext(Dispatchers.Default) {
                list.map(IntakeList::toIntake)
            }
        }
    }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(), emptyList())

    @OptIn(ExperimentalCoroutinesApi::class)
    val schedule = _state.flatMapLatest { query ->
        alarmDAO.getFlow(query.search).map { list ->
            withContext(Dispatchers.Default) {
                list.groupBy { getDateTime(it.trigger).toLocalDate().toEpochDay() }
                    .toSortedMap()
                    .map(Map.Entry<Long, List<Schedule>>::toIntakeSchedule)
            }
        }
    }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(), emptyList())

    @OptIn(ExperimentalCoroutinesApi::class)
    val taken = _state.flatMapLatest { query ->
        takenDAO.getFlow(query.search).map { list ->
            withContext(Dispatchers.Default) {
                list.groupBy { getDateTime(it.trigger).toLocalDate().toEpochDay() }
                    .toSortedMap(Comparator.reverseOrder())
                    .map(Map.Entry<Long, List<IntakeTaken>>::toIntakePast)
            }
        }
    }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(), emptyList())

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
                val trigger = ZonedDateTime.of(
                    LocalDate.now(),
                    LocalTime.of(picker.hour, picker.minute),
                    ZONE
                ).toInstant().toEpochMilli()

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

    fun onNewTakenEvent(event: NewTakenEvent) {
        when(event) {
            NewTakenEvent.AddNewTaken -> {
                val trigger = ZonedDateTime.of(
                    LocalDate.parse(_newTakenState.value.date, FORMAT_DD_MM_YYYY),
                    LocalTime.parse(_newTakenState.value.time, FORMAT_H_MM),
                    ZONE
                ).toInstant().toEpochMilli()


                val medicine = _newTakenState.value.medicine!!

                val amount = _newTakenState.value.amount.let {
                    it.toDoubleOrNull() ?: it.replace(',', '.').toDoubleOrNull() ?: 1.0
                }

                val taken = IntakeTaken(
                    medicineId = medicine.id,
                    productName = _newTakenState.value.title,
                    formName = medicine.prodFormNormName,
                    amount = amount,
                    doseType = medicine.doseType,
                    image = medicine.image.firstOrNull().orEmpty(),
                    trigger = trigger,
                    inFact = trigger,
                    taken = true,
                    notified = true
                )

                viewModelScope.launch {
                    takenDAO.insert(taken)
                    medicineDAO.intakeMedicine(taken.medicineId, taken.amount)
                }

                _state.update {
                    it.copy(showDialogAddTaken = false)
                }
            }

            is NewTakenEvent.PickMedicine -> {
                _newTakenState.update {
                    it.copy(
                        title = event.medicine.nameAlias.ifEmpty(event.medicine::productName),
                        amount = BLANK,
                        doseType = ResourceText.StringResource(event.medicine.doseType.title),
                        inStock = decimalFormat(event.medicine.prodAmount),
                        medicine = event.medicine
                    )
                }

                database.takenDAO().getSimilarAmount(event.medicine.id)?.let { amount ->
                    _newTakenState.update {
                        it.copy(amount = decimalFormat(amount))
                    }
                }
            }

            is NewTakenEvent.SetAmount -> if (event.amount.isNotEmpty()) {
                when (event.amount.replace(',', '.').toDoubleOrNull()) {
                    null -> {}
                    else -> _newTakenState.update {
                        it.copy(amount = event.amount.replace(',', '.'))
                    }
                }
            } else {
                _newTakenState.update {
                    it.copy(amount = BLANK)
                }
            }

            is NewTakenEvent.SetDate -> event.pickerState.selectedDateMillis?.let { millis ->
                _newTakenState.update {
                    it.copy(date = getDateTime(millis).format(FORMAT_DD_MM_YYYY))
                }
            }

            is NewTakenEvent.SetTime -> _newTakenState.update {
                it.copy(
                    time = LocalTime.of(event.pickerState.hour, event.pickerState.minute).format(FORMAT_H_MM)
                )
            }
        }
    }

    fun setSearch(text: String) = _state.update { it.copy(search = text) }
    fun clearSearch() = _state.update { it.copy(search = BLANK) }

    fun showDialog(takenId: Long) {
        viewModelScope.launch {
            val taken = takenDAO.getById(takenId)

            withContext(Dispatchers.Main) {
                _takenState.update {
                    taken?.toTakenState() ?: TakenState()
                }

                _state.update {
                    it.copy(showDialog = true)
                }
            }
        }
    }

    fun showDialogDelete(id: Long = 0L) {
        _takenState.update { it.copy(takenId = id) }
        _state.update { it.copy(showDialogDelete = !it.showDialogDelete) }
    }

    fun showDialogScheduleToTaken(item: IntakeModel? = null) {
        item?.let { scheduled ->
            val alarm = alarmDAO.getById(scheduled.alarmId) ?: return
            val inStock = intakeDAO.getById(alarm.intakeId)?.let {
                it.medicine.prodAmount >= alarm.amount
            } ?: false

            _scheduledState.update {
                it.copy(
                    id = scheduled.id,
                    alarmId = scheduled.alarmId,
                    title = scheduled.title,
                    time = scheduled.time,
                    taken = inStock
                )
            }
        }

        _state.update {
            it.copy(showDialogScheduleToTaken = !it.showDialogScheduleToTaken)
        }
    }

    fun showDialogAddTaken() {
        viewModelScope.launch {
            _newTakenState.emit(NewTakenState())
        }

        _state.update {
            it.copy(showDialogAddTaken = !it.showDialogAddTaken)
        }
    }

    fun scheduleToTaken(setter: AlarmSetter) {
        val alarm = alarmDAO.getById(_scheduledState.value.alarmId) ?: return
        val intake = intakeDAO.getById(alarm.intakeId) ?: return
        val medicine = medicineDAO.getById(intake.medicineId) ?: return
        val image = medicineDAO.getMedicineImages(medicine.id).firstOrNull() ?: BLANK

        viewModelScope.launch {
            alarmDAO.delete(alarm)

            takenDAO.insert(
                IntakeTaken(
                    medicineId = medicine.id,
                    intakeId = alarm.intakeId,
                    alarmId = alarm.alarmId,
                    productName = medicine.nameAlias.ifEmpty(medicine::productName),
                    formName = medicine.prodFormNormName,
                    amount = alarm.amount,
                    doseType = medicine.doseType,
                    image = image,
                    trigger = alarm.trigger,
                    inFact = alarm.trigger,
                    taken = true,
                    notified = true
                )
            )

            medicineDAO.intakeMedicine(medicine.id, alarm.amount)

            setter.setPreAlarm(intake.intakeId)
        }

        _state.update {
            it.copy(showDialogScheduleToTaken = false)
        }
    }

    fun deleteTaken() {
        viewModelScope.launch {
            takenDAO.delete(IntakeTaken(_takenState.value.takenId))
        }

        _state.update { it.copy(showDialogDelete = !it.showDialogDelete) }
    }

    fun showDialogDate() = _state.update { it.copy(showDialogDate = !it.showDialogDate) }
    fun scrollToClosest(tab: IntakeTab, listState: LazyListState, time: Long) {
        val list = if (tab == IntakeTab.CURRENT) schedule.value else taken.value

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
                listState.scrollToItem(group + itemsIndex)
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