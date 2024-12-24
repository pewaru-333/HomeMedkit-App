package ru.application.homemedkit.models.viewModels

import androidx.compose.runtime.snapshots.SnapshotStateList
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
import ru.application.homemedkit.data.dto.MedicineKit
import ru.application.homemedkit.helpers.BLANK
import ru.application.homemedkit.models.states.MedicinesState

class MedicinesViewModel : ViewModel() {
    private val _state = MutableStateFlow(MedicinesState())
    val state = _state.asStateFlow()

    val medicines = combine(
        _state,
        database.medicineDAO().getFlow(),
        database.kitDAO().getMedicinesKits()
    ) { query, list, kits ->
        list.fastFilter { (id, _, _, productName) ->
            productName.contains(query.search, true) && if (query.kits.isEmpty()) true
            else id in kits.filter { it.kitId in query.kits }.map(MedicineKit::medicineId)
        }.sortedWith(query.sorting)
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000L),
        initialValue = runBlocking(Dispatchers.IO) {
            database.medicineDAO().getFlow().firstOrNull()
        }
    )

    fun showAdding() = _state.update { it.copy(showAdding = !it.showAdding) }
    fun showExit(flag: Boolean = false) = _state.update { it.copy(showExit = flag) }

    fun setSearch(text: String) = _state.update { it.copy(search = text) }
    fun clearSearch() = _state.update { it.copy(search = BLANK) }

    fun showSort() = _state.update { it.copy(showSort = true) }
    fun hideSort() = _state.update { it.copy(showSort = false) }
    fun setSorting(sorting: Comparator<Medicine>) = _state.update { it.copy(sorting = sorting) }

    fun showFilter() = _state.update { it.copy(showFilter = !it.showFilter) }
    fun clearFilter() = _state.update { it.copy(kits = it.kits.apply(SnapshotStateList<Long>::clear)) }
    fun pickFilter(kitId: Long) = _state.update {
        it.copy(kits = it.kits.apply { if (kitId in this) remove(kitId) else add(kitId) })
    }
}