package ru.application.homemedkit.models.viewModels

import androidx.compose.runtime.mutableStateListOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import ru.application.homemedkit.HomeMeds.Companion.database
import ru.application.homemedkit.data.model.KitMedicines
import ru.application.homemedkit.data.model.MedicineMain
import ru.application.homemedkit.helpers.BLANK
import ru.application.homemedkit.helpers.extensions.toMedicineList
import ru.application.homemedkit.models.states.MedicinesState

class MedicinesViewModel : ViewModel() {
    private val _state = MutableStateFlow(MedicinesState())
    val state = _state.asStateFlow()

    val medicines = combine(_state, database.medicineDAO().getFlow()) { query, list ->
        list.filter {
            listOf(it.productName, it.nameAlias, it.structure, it.phKinetics).any { it.contains(query.search, true) } &&
                if (query.kits.isEmpty()) true
                else it.id in query.kits.flatMap(KitMedicines::medicineIdList)
        }
            .sortedWith(query.sorting)
            .map(MedicineMain::toMedicineList)
    }.flowOn(Dispatchers.IO)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(), emptyList())

    val kits = database.kitDAO().getAllKits()
        .flowOn(Dispatchers.IO)
        .map { list -> list.sortedBy(KitMedicines::position) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(), emptyList())

    fun showAdding() = _state.update { it.copy(showAdding = !it.showAdding) }
    fun showExit(flag: Boolean = false) = _state.update { it.copy(showExit = flag) }

    fun setSearch(text: String) = _state.update { it.copy(search = text) }
    fun clearSearch() = _state.update { it.copy(search = BLANK) }

    fun showSort() = _state.update { it.copy(showSort = !it.showSort) }
    fun setSorting(sorting: Comparator<MedicineMain>) = _state.update { it.copy(sorting = sorting) }

    fun showFilter() = _state.update { it.copy(showFilter = !it.showFilter) }
    fun clearFilter() = _state.update { it.copy(showFilter = false, kits = mutableStateListOf()) }
    fun pickFilter(kit: KitMedicines) = _state.update {
        it.copy(kits = it.kits.apply { if (kit in this) remove(kit) else add(kit) })
    }
}