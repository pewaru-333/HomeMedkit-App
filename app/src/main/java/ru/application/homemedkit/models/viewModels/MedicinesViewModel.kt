package ru.application.homemedkit.models.viewModels

import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.runtime.mutableStateListOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import ru.application.homemedkit.HomeMeds.Companion.database
import ru.application.homemedkit.data.dto.Kit
import ru.application.homemedkit.data.model.MedicineGrouped
import ru.application.homemedkit.data.model.MedicineMain
import ru.application.homemedkit.helpers.BLANK
import ru.application.homemedkit.helpers.enums.MedicineTab
import ru.application.homemedkit.helpers.enums.Sorting
import ru.application.homemedkit.helpers.extensions.toMedicineList
import ru.application.homemedkit.models.states.MedicinesState

class MedicinesViewModel : ViewModel() {
    private val _state = MutableStateFlow(MedicinesState())
    val state = _state.asStateFlow()

    private val listStates = MedicineTab.entries.associateWith { LazyListState() }
    val listState: LazyListState
        get() = listStates.getValue(_state.value.tab)

    @OptIn(ExperimentalCoroutinesApi::class)
    private val _medicines = _state.flatMapLatest { query ->
        database.medicineDAO().getListFlow(
            search = query.search,
            sorting = query.sorting,
            kitIds = query.kits.map(Kit::kitId),
            kitsEnabled = query.kits.isNotEmpty()
        )
    }

    val medicines = _medicines
        .map { list -> list.map(MedicineMain::toMedicineList) }
        .flowOn(Dispatchers.IO)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(), emptyList())

    val grouped = _medicines.map { list ->
        val kitIds = list.flatMap(MedicineMain::kitIds).distinct()
        val kitsMap = database.kitDAO().getKitList(kitIds).associateBy(Kit::kitId)

        list.flatMap { medicine ->
            medicine.kitIds.mapNotNull { kitId ->
                kitsMap[kitId]?.let { kit -> Pair(kit, medicine) }
            }
        }
            .groupBy(Pair<Kit, MedicineMain>::first, Pair<Kit, MedicineMain>::second)
            .map { (kit, medicines) ->
                MedicineGrouped(
                    kit = kit,
                    medicines = medicines.map(MedicineMain::toMedicineList)
                )
            }
            .sortedBy { it.kit.position }
    }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(), emptyList())

    val kits = database.kitDAO().getFlow()
        .flowOn(Dispatchers.IO)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(), emptyList())

    fun pickTab(tab: MedicineTab) = _state.update { it.copy(tab = tab) }

    fun showAdding() = _state.update { it.copy(showAdding = !it.showAdding) }
    fun showExit(flag: Boolean = false) = _state.update { it.copy(showExit = flag) }

    fun setSearch(text: String) = _state.update { it.copy(search = text) }
    fun clearSearch() = _state.update { it.copy(search = BLANK) }

    fun showSort() = _state.update { it.copy(showSort = !it.showSort) }
    fun setSorting(sorting: Sorting) = _state.update { it.copy(sorting = sorting) }

    fun showFilter() = _state.update { it.copy(showFilter = !it.showFilter) }
    fun clearFilter() = _state.update { it.copy(showFilter = false, kits = mutableStateListOf()) }
    fun pickFilter(kit: Kit) = _state.update {
        it.copy(kits = it.kits.apply { if (kit in this) remove(kit) else add(kit) })
    }
}