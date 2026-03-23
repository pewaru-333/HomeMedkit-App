@file:OptIn(ExperimentalCoroutinesApi::class)

package ru.application.homemedkit.models.viewModels

import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import ru.application.homemedkit.R
import ru.application.homemedkit.data.dto.Kit
import ru.application.homemedkit.data.model.KitModel
import ru.application.homemedkit.data.model.MedicineGrouped
import ru.application.homemedkit.data.model.MedicineList
import ru.application.homemedkit.data.queries.MedicinesQueryBuilder
import ru.application.homemedkit.models.states.MedicinesState
import ru.application.homemedkit.utils.BLANK
import ru.application.homemedkit.utils.ResourceText
import ru.application.homemedkit.utils.di.Database
import ru.application.homemedkit.utils.di.Preferences
import ru.application.homemedkit.utils.enums.MedicineListView
import ru.application.homemedkit.utils.enums.Sorting
import ru.application.homemedkit.utils.extensions.toMedicineList
import ru.application.homemedkit.utils.extensions.toModel
import ru.application.homemedkit.utils.extensions.toggle

class MedicinesViewModel : BaseViewModel<MedicinesState, Unit>() {
    private val currentMillis by lazy { System.currentTimeMillis() }
    private val medicineDAO by lazy { Database.medicineDAO() }
    private val kitDAO by lazy { Database.kitDAO() }

    val kits = kitDAO.getFlow()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000L), emptyList())

    private val _medicines = state.flatMapLatest { query ->
        medicineDAO.getFlow(
            query = MedicinesQueryBuilder.selectBy(
                search = query.search,
                order = query.sorting,
                hideEmpty = query.hideEmpty,
                kits = query.kits
            )
        )
    }

    val medicines = _medicines
        .map { list -> list.map { main -> main.toMedicineList(currentMillis) } }
        .flowOn(Dispatchers.Default)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000L), emptyList())

    val grouped = combine(_medicines, kits, state) { medicines, kits, state ->
        val selectedKits = state.kits.mapTo(mutableSetOf(), Kit::kitId)
        val kitsMap = kits.associateBy(Kit::kitId)

        val filterIsEmpty = selectedKits.isEmpty()
        val groups = mutableMapOf<Long, MutableList<MedicineList>>()
        val noGroup = mutableListOf<MedicineList>()

        for (medicine in medicines) {
            val model = medicine.toMedicineList(currentMillis)
            var anyGroup = false

            for (kitId in medicine.kitIds) {
                if (filterIsEmpty || kitId in selectedKits) {
                    if (kitsMap.containsKey(kitId)) {
                        groups.getOrPut(kitId) { mutableListOf() }.add(model)
                        anyGroup = true
                    }
                }
            }

            if (!anyGroup) {
                noGroup.add(model)
            }
        }

        val result = ArrayList<MedicineGrouped>(groups.size + 1)

        groups.forEach { (kitId, items) ->
            val kit = kitsMap[kitId]
            if (kit != null) {
                result.add(MedicineGrouped(kit.toModel(), items))
            }
        }

        if (noGroup.isNotEmpty()) {
            result.add(
                MedicineGrouped(
                    medicines = noGroup,
                    kit = KitModel(
                        id = if (kits.isEmpty()) -2L else -1L,
                        position = kits.size.toLong(),
                        title = ResourceText.StringResource(R.string.text_no_group)
                    )
                )
            )
        }

        result.sortedBy { it.kit.position }
    }.flowOn(Dispatchers.Default)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000L), emptyList())

    override fun onEvent(event: Unit) = Unit

    override fun initState() = MedicinesState()

    override fun loadData() {
        val kitIds = Preferences.kitsFilter
            .orEmpty()
            .mapNotNullTo(mutableSetOf(), String::toLongOrNull)

        if (kitIds.isNotEmpty()) {
            viewModelScope.launch {
                val kits = kitDAO.getKitList(kitIds).toSet()

                updateState { it.copy(kits = kits) }
            }
        }
    }

    fun pickView(view: MedicineListView) {
        if (currentState.listView != view) {
            updateState { it.copy(listView = view) }
            Preferences.saveListView(view)
        }
    }

    fun toggleAdding() = updateState { it.copy(showAdding = !it.showAdding) }
    fun showExit(flag: Boolean = false) = updateState { it.copy(showExit = flag) }

    fun onSearch(text: String = BLANK) = updateState { it.copy(search = text) }

    fun showSorting() = updateState { it.copy(showSorting = !it.showSorting) }
    fun setSorting(sorting: Sorting) = updateState { it.copy(sorting = sorting) }

    fun toggleFilter() {
        updateState { it.copy(showFilter = !it.showFilter) }

        if (!currentState.showFilter) {
            Preferences.saveKitsFilter(currentState.kits.mapTo(mutableSetOf()) { it.kitId.toString() })
        }
    }

    fun clearFilter() {
        updateState {
            it.copy(
                showFilter = false,
                kits = emptySet()
            )
        }

        Preferences.saveKitsFilter(emptySet())
    }

    fun pickFilter(kit: Kit) = updateState { it.copy(kits = it.kits.toggle(kit)) }

    fun hideEmpty(hide: Boolean) {
        updateState { it.copy(hideEmpty = hide) }
        Preferences.setHideEmptyMedicines(hide)
    }
}