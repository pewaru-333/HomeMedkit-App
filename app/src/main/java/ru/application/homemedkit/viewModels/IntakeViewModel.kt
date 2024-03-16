package ru.application.homemedkit.viewModels

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import ru.application.homemedkit.databaseController.Intake
import ru.application.homemedkit.databaseController.MedicineDatabase
import ru.application.homemedkit.helpers.ConstantsHelper.BLANK
import ru.application.homemedkit.helpers.ConstantsHelper.DOWN_DASH
import ru.application.homemedkit.helpers.ConstantsHelper.INTERVALS
import ru.application.homemedkit.helpers.ConstantsHelper.PERIODS
import ru.application.homemedkit.helpers.ConstantsHelper.SEMICOLON
import ru.application.homemedkit.helpers.DateHelper
import ru.application.homemedkit.helpers.DateHelper.FORMAT_S
import java.time.LocalDate
import java.time.format.DateTimeParseException

class IntakeViewModel(database: MedicineDatabase, intakeId: Long) : ViewModel() {
    private val _uiState = MutableStateFlow(Intake())

    init {
        if (intakeId != 0L) _uiState.value = database.intakeDAO().getByPK(intakeId)
        else _uiState.value = Intake(-1, 0.0, BLANK, BLANK, BLANK, BLANK, BLANK)
    }

    var add by mutableStateOf(false)
        private set

    var edit by mutableStateOf(false)
        private set

    var amount by mutableStateOf(
        when (_uiState.value.amount) {
            0.0 -> BLANK
            else -> _uiState.value.amount.toString()
        }
    )
        private set

    var interval: String by mutableStateOf(_uiState.value.interval)
        private set

    var intervalD by mutableStateOf(_uiState.value.interval.substringAfter(DOWN_DASH))
        private set

    var period: String by mutableStateOf(_uiState.value.period)

    var periodD by mutableStateOf(BLANK)

    var time: String by mutableStateOf(_uiState.value.time)

    var timesValues = mutableStateListOf(BLANK)
        private set

    var timesAmount by mutableIntStateOf(timesValues.size)
        private set

    var startDate: String by mutableStateOf(_uiState.value.startDate)
        private set

    var finalDate: String by mutableStateOf(_uiState.value.finalDate)
        private set

    fun setAdding(adding: Boolean) {
        add = adding
    }

    fun setEditing(editing: Boolean) {
        edit = editing
    }

    fun updateAmount(amount: String) {
        if (amount.isNotEmpty()) {
            when (amount.replace(',', '.').toDoubleOrNull()) {
                null -> {}
                else -> this.amount = amount.trim()
            }
        } else this.amount = amount
    }

    fun updateInterval(interval: String, intervalD: String = BLANK) {
        when (interval) {
            INTERVALS[0] -> {
                this.interval = INTERVALS[0]; this.intervalD = "1"
            }

            INTERVALS[1] -> {
                this.interval = INTERVALS[1]; this.intervalD = "7"
            }

            INTERVALS[2] -> {
                if (intervalD.length < 3) {
                    this.intervalD = intervalD
                    this.interval = INTERVALS[2] + DOWN_DASH + intervalD
                }
            }
        }
    }

    fun updatePeriod(period: String) {
        if (period.length <= 5) {
            when (period) {
                "7" -> {
                    this.period = PERIODS[0]
                    periodD = "7"
                }

                "30" -> {
                    this.period = PERIODS[1]
                    periodD = "30"
                }

                BLANK -> {
                    this.period = PERIODS[2]
                    periodD = period
                }

                "38500" -> {
                    this.period = PERIODS[3]
                    periodD = "38500"
                    finalDate =
                        LocalDate.parse(startDate, FORMAT_S).plusDays(38500).format(FORMAT_S)
                }

                else -> periodD = period
            }
        }
    }

    fun updateDateS(date: String) {
        startDate = try {
            LocalDate.parse(date, DateHelper.FORMAT_D_MM_Y).format(FORMAT_S)
        } catch (e: DateTimeParseException) {
            date
        }
    }

    fun updateDateF(date: String) {
        finalDate = try {
            LocalDate.parse(date, DateHelper.FORMAT_D_MM_Y).format(FORMAT_S)
        } catch (e: DateTimeParseException) {
            date
        }
    }

    fun incAmount() {
        timesAmount++
    }

    fun decAmount() {
        timesAmount--
    }

    fun updateTime() {
        timesValues.clear()
        timesValues.addAll(time.split(SEMICOLON))
        timesAmount = timesValues.size
    }

    fun validateAll(): Boolean {
        return listOf(amount, interval, intervalD, period, periodD, startDate, finalDate)
            .all(String::isNotEmpty) && timesValues.all(String::isNotEmpty)
    }
}