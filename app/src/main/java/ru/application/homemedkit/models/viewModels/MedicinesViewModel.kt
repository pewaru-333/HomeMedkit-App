package ru.application.homemedkit.models.viewModels

import androidx.compose.ui.util.fastFilter
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.runBlocking
import ru.application.homemedkit.HomeMeds.Companion.database
import ru.application.homemedkit.data.dto.Medicine
import ru.application.homemedkit.helpers.BLANK
import ru.application.homemedkit.helpers.Preferences
import ru.application.homemedkit.models.states.MedicinesState

class MedicinesViewModel : ViewModel() {
    private val _state = MutableStateFlow(MedicinesState())
    val state = _state.asStateFlow()

    val medicines = _state.combine(database.medicineDAO().getFlow()) { query, list ->
        list.fastFilter { (_, dKitId, _, productName) ->
            productName.contains(query.search, true) &&
                    if (query.kitId != 0L) dKitId == query.kitId else true
        }.sortedWith(query.sorting)
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000L),
        initialValue = runBlocking(Dispatchers.IO) {
            database.medicineDAO().getFlow().firstOrNull()
        }
    )

    fun setSearch(text: String) = _state.update { it.copy(search = text) }
    fun clearSearch() = _state.update { it.copy(search = BLANK) }

    fun showSort() = _state.update { it.copy(showSort = true) }
    fun hideSort() = _state.update { it.copy(showSort = false) }
    fun setSorting(sorting: Comparator<Medicine>) = _state.update { it.copy(sorting = sorting) }

    fun showFilter() = _state.update { it.copy(showFilter = true) }
    fun hideFilter() = _state.update { it.copy(showFilter = false) }

    fun showAdding() = _state.update { it.copy(showAdding = !_state.value.showAdding) }

    fun setFilter(kitId: Long) = _state.update { it.copy(kitId = kitId) }
    fun saveFilter() {
        Preferences.setLastKit(_state.value.kitId)
        _state.update { it.copy(showFilter = false) }
    }
}