package ru.application.homemedkit.viewModels

import androidx.compose.foundation.lazy.LazyListState
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import ru.application.homemedkit.R.string.intakes_tab_current
import ru.application.homemedkit.R.string.intakes_tab_list
import ru.application.homemedkit.R.string.intakes_tab_taken
import ru.application.homemedkit.activities.HomeMeds.Companion.database
import ru.application.homemedkit.databaseController.Intake
import ru.application.homemedkit.helpers.BLANK
import java.util.Locale.ROOT

class IntakesViewModel : ViewModel() {
    private val _state = MutableStateFlow(IntakesState())
    val state = _state.asStateFlow()

    val tabs = listOf(intakes_tab_list, intakes_tab_current, intakes_tab_taken)

    fun getAll() {
        viewModelScope.launch {
            _state.update { it.copy(intakes = database.intakeDAO().getAll()) }
        }
    }

    fun setSearch(text: String) {
        viewModelScope.launch {
            _state.update { it.copy(search = text, intakes = search(text)) }
        }
    }

    fun clearSearch() {
        viewModelScope.launch {
            _state.update { it.copy(search = BLANK, intakes = search(BLANK)) }
        }
    }

    fun pickTab(tab: Int) {
        viewModelScope.launch { _state.update { it.copy(tab = tab) } }
    }

    private fun search(text: String): List<Intake> {
        val intakes = database.intakeDAO().getAll()
        val filtered = ArrayList<Intake>(intakes.size)

        if (text.isEmpty()) filtered.addAll(intakes) else intakes.forEach {
            val productName = database.medicineDAO().getProductName(it.medicineId)
            if (productName.lowercase(ROOT).contains(text.lowercase(ROOT))) filtered.add(it)
        }

        return filtered
    }
}

data class IntakesState(
    val search: String = BLANK,
    val tab: Int = 0,
    val intakes: List<Intake> = database.intakeDAO().getAll(),
    val stateOne: LazyListState = LazyListState(),
    val stateTwo: LazyListState = LazyListState(),
    val stateThree: LazyListState = LazyListState()
)