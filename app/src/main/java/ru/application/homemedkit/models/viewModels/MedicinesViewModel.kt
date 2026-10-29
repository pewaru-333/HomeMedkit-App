@file:OptIn(ExperimentalCoroutinesApi::class)

package ru.application.homemedkit.models.viewModels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.*
import ru.application.homemedkit.R
import ru.application.homemedkit.data.dto.Kit
import ru.application.homemedkit.data.model.KitModel
import ru.application.homemedkit.data.model.MedicineGrouped
import ru.application.homemedkit.data.model.MedicineList
import ru.application.homemedkit.data.queries.MedicinesQuery
import ru.application.homemedkit.data.queries.MedicinesQueryBuilder
import ru.application.homemedkit.models.events.MedicinesEvent
import ru.application.homemedkit.models.states.MedicinesContent
import ru.application.homemedkit.models.states.MedicinesState
import ru.application.homemedkit.utils.ResourceText
import ru.application.homemedkit.utils.di.Database
import ru.application.homemedkit.utils.di.Preferences
import ru.application.homemedkit.utils.extensions.toMedicineList
import ru.application.homemedkit.utils.extensions.toModel
import ru.application.homemedkit.utils.extensions.toggle

class MedicinesViewModel : ViewModel() {
    private val currentMillis = System.currentTimeMillis()

    val state: StateFlow<MedicinesState>
        field = MutableStateFlow(
            value = MedicinesState(
                sorting = Preferences.sortingOrder,
                hideEmpty = Preferences.hideEmptyMedicines,
                listView = Preferences.medicinesListView,
                kitIds = Preferences.kitsFilter.orEmpty()
                    .mapNotNullTo(mutableSetOf()) { id ->
                        id.toLongOrNull()?.takeIf { it > 0L }
                    }
            )
        )

    private val mappedResults = state
        .map {
            MedicinesQuery(
                search = it.search.trim(),
                sorting = it.sorting,
                hideEmpty = it.hideEmpty,
                kitIds = it.kitIds
            )
        }
        .distinctUntilChanged()
        .flatMapLatest { query ->
            Database.medicineDAO().getFlow(
                query = MedicinesQueryBuilder.selectBy(
                    search = query.search,
                    order = query.sorting,
                    hideEmpty = query.hideEmpty,
                    kitIds = query.kitIds
                )
            )
                .distinctUntilChanged()
                .map { medicines ->
                    MappedResult(
                        query = query,
                        medicines = medicines.map {
                            MedicinesKits(it.toMedicineList(currentMillis), it.kitIds)
                        }
                    )
                }
                .catch { emit(MappedResult(query)) }
        }

    private val kitsFlow = Database.kitDAO().getFlow()
        .distinctUntilChanged()
        .onEach { kits ->
            val selected = state.value.kitIds
            if (selected.isEmpty()) return@onEach

            val idSet = kits.mapTo(HashSet(), Kit::kitId)
            if (idSet.containsAll(selected)) return@onEach

            val updatedState = state.updateAndGet {
                it.copy(kitIds = it.kitIds.filterTo(mutableSetOf(), idSet::contains))
            }

            Preferences.saveKitsFilter(updatedState.kitIds.mapTo(mutableSetOf(), Long::toString))
        }
        .catch { emit(emptyList()) }

    val content = combine(mappedResults, kitsFlow) { result, kits ->
        MedicinesContent(
            query = result.query,
            medicines = result.medicines.map(MedicinesKits::medicine),
            grouped = group(result.medicines, kits, result.query.kitIds),
            kits = kits
        )
    }
        .flowOn(Dispatchers.Default)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000L), MedicinesContent())

    fun onEvent(event: MedicinesEvent) {
        when (event) {
            is MedicinesEvent.Search -> state.update { it.copy(search = event.text) }
            is MedicinesEvent.PickView -> {
                state.update { it.copy(listView = event.view) }

                Preferences.saveListView(event.view)
            }

            is MedicinesEvent.SetSorting -> {
                state.update { it.copy(sorting = event.sorting) }

                Preferences.setSortingType(event.sorting)
            }

            is MedicinesEvent.PickKit -> state.update { it.copy(kitIds = it.kitIds.toggle(event.id)) }
            is MedicinesEvent.HideEmpty -> {
                state.update { it.copy(hideEmpty = event.hide) }

                Preferences.setHideEmptyMedicines(event.hide)
            }

            MedicinesEvent.ToggleAdding -> state.update { it.copy(showAdding = !it.showAdding) }
            MedicinesEvent.ToggleSorting -> state.update { it.copy(showSorting = !it.showSorting) }
            MedicinesEvent.ToggleFilter -> {
                val updatedState = state.updateAndGet { it.copy(showFilter = !it.showFilter) }

                if (!updatedState.showFilter) {
                    Preferences.saveKitsFilter(updatedState.kitIds.mapTo(mutableSetOf(), Long::toString))
                }
            }

            MedicinesEvent.ClearFilter -> {
                state.update { it.copy(showFilter = false, kitIds = emptySet()) }

                Preferences.saveKitsFilter(emptySet())
            }

            MedicinesEvent.ShowExit -> state.update { it.copy(showExit = true) }
            MedicinesEvent.HideExit -> state.update { it.copy(showExit = false) }
        }
    }

    private fun group(rows: List<MedicinesKits>, kits: List<Kit>, kitIds: Set<Long>): List<MedicineGrouped> {
        if (rows.isEmpty()) return emptyList()

        val idSet = kits.mapTo(HashSet(), Kit::kitId)
        val groups = HashMap<Long, MutableList<MedicineList>>()
        val ungrouped = mutableListOf<MedicineList>()

        for ((medicine, selectedIds) in rows) {
            var assigned = false
            for (kitId in selectedIds) {
                if (kitId in idSet && (kitIds.isEmpty() || kitId in kitIds)) {
                    groups.getOrPut(kitId) { mutableListOf() }.add(medicine)
                    assigned = true
                }
            }

            if (!assigned) {
                ungrouped.add(medicine)
            }
        }

        val result = ArrayList<MedicineGrouped>(groups.size + 1)
        for (kit in kits) {
            groups[kit.kitId]?.let { result.add(MedicineGrouped(kit.toModel(), it)) }
        }

        if (ungrouped.isNotEmpty()) {
            result.add(
                MedicineGrouped(
                    medicines = ungrouped,
                    kit = KitModel(
                        id = -1L,
                        position = Long.MAX_VALUE,
                        title = ResourceText.StringResource(R.string.text_no_group)
                    )
                )
            )
        }

        return result
    }

    private data class MedicinesKits(
        val medicine: MedicineList,
        val kitIds: Set<Long>
    )

    private data class MappedResult(
        val query: MedicinesQuery,
        val medicines: List<MedicinesKits> = emptyList()
    )
}