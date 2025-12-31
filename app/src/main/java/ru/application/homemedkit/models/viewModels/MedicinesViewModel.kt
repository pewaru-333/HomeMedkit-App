@file:OptIn(ExperimentalCoroutinesApi::class)

package ru.application.homemedkit.models.viewModels

import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import ru.application.homemedkit.R
import ru.application.homemedkit.data.dto.Kit
import ru.application.homemedkit.data.model.KitModel
import ru.application.homemedkit.data.model.MedicineGrouped
import ru.application.homemedkit.data.model.MedicineMain
import ru.application.homemedkit.data.queries.MedicinesQueryBuilder
import ru.application.homemedkit.models.states.MedicinesState
import ru.application.homemedkit.utils.BLANK
import ru.application.homemedkit.utils.ResourceText
import ru.application.homemedkit.utils.di.Database
import ru.application.homemedkit.utils.di.Preferences
import ru.application.homemedkit.utils.enums.Sorting
import ru.application.homemedkit.utils.extensions.toMedicineList
import ru.application.homemedkit.utils.extensions.toModel
import ru.application.homemedkit.utils.extensions.toggle

class MedicinesViewModel : BaseViewModel<MedicinesState, Unit>() {
    private val currentMillis by lazy { System.currentTimeMillis() }
    private val medicineDAO by lazy { Database.medicineDAO() }
    private val kitDAO by lazy { Database.kitDAO() }
    private val _medicines = state.flatMapLatest { query ->
        medicineDAO.getFlow(MedicinesQueryBuilder.selectBy(query.search, query.sorting, query.kits))
    }

    val medicines = _medicines
        .map { list -> list.map { main -> main.toMedicineList(currentMillis) } }
        .flowOn(Dispatchers.Default)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000L), emptyList())

    val grouped = state.flatMapLatest { state ->
        _medicines.map { medicineList ->
            val selectedKits: Set<Long> = state.kits.mapTo(mutableSetOf(), Kit::kitId)
            val filterIsEmpty = selectedKits.isEmpty()

            val kitsToGroup = if (filterIsEmpty) {
                kitDAO.getKitList(medicineList.flatMap(MedicineMain::kitIds).distinct()).associateBy(Kit::kitId)
            } else {
                kitDAO.getKitList(selectedKits.toList()).associateBy(Kit::kitId)
            }

            val (toGroup, noGroup) = medicineList.partition { medicine ->
                if (filterIsEmpty) {
                    medicine.kitIds.isNotEmpty() && medicine.kitIds.any(kitsToGroup::containsKey)
                } else {
                    medicine.kitIds.any { kitId -> selectedKits.contains(kitId) } && medicine.kitIds.any(kitsToGroup::containsKey)
                }
            }

            val withKits = mutableListOf<MedicineGrouped>()

            if (filterIsEmpty) {
                val groupedByAllTheirKits = toGroup.flatMap { medicine ->
                    medicine.kitIds.mapNotNull { kitId ->
                        kitsToGroup[kitId]?.let { kit -> Pair(kit, medicine) }
                    }
                }
                    .groupBy(Pair<Kit, MedicineMain>::first, Pair<Kit, MedicineMain>::second)
                    .map { (kit, medicinesInKit) ->
                        MedicineGrouped(
                            kit = kit.toModel(),
                            medicines = medicinesInKit.map { main ->
                                main.toMedicineList(currentMillis)
                            }
                        )
                    }
                withKits.addAll(groupedByAllTheirKits)
            } else {
                selectedKits.forEach { selectedKitId ->
                    val kitInfo = kitsToGroup[selectedKitId]
                    if (kitInfo != null) {
                        val medicinesSelectedKits = toGroup.filter { medicine ->
                            medicine.kitIds.contains(selectedKitId)
                        }
                        if (medicinesSelectedKits.isNotEmpty()) {
                            withKits.add(
                                MedicineGrouped(
                                    kit = kitInfo.toModel(),
                                    medicines = medicinesSelectedKits.map { main ->
                                        main.toMedicineList(currentMillis)
                                    }
                                )
                            )
                        }
                    }
                }
            }

            withKits.sortBy { it.kit.position }

            val noGroupMedicines = mutableListOf<MedicineMain>().apply {
                addAll(noGroup)
            }

            mutableListOf<MedicineGrouped>().apply {
                addAll(withKits)

                if (noGroupMedicines.isNotEmpty()) {
                    add(
                        MedicineGrouped(
                            medicines = noGroupMedicines.map { main ->
                                main.toMedicineList(currentMillis)
                            },
                            kit = KitModel(
                                id = -1L,
                                position = Int.MAX_VALUE.toLong(),
                                title = ResourceText.StringResource(R.string.text_no_group)
                            )
                        )
                    )
                }

                sortBy { it.kit.position }
            }
        }
    }.flowOn(Dispatchers.Default)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000L), emptyList())

    val kits = kitDAO.getFlow()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000L), emptyList())

    override fun initState() = MedicinesState()

    override fun loadData() {
        with(Preferences.kitsFilter) {
            if (isNotEmpty()) {
                viewModelScope.launch {
                    val kits = kitDAO.getKitList(toList())

                    updateState { it.copy(kits = kits.toSet()) }
                }
            }
        }
    }
    
    override fun onEvent(event: Unit) = Unit

    fun toggleView() = updateState { it.copy(tab = it.tab.nextTab) }

    fun toggleAdding() = updateState { it.copy(showAdding = !it.showAdding) }
    fun showExit(flag: Boolean = false) = updateState { it.copy(showExit = flag) }

    fun onSearch(text: String = BLANK) = updateState { it.copy(search = text) }

    fun toggleSorting() = updateState { it.copy(showSort = !it.showSort) }
    fun setSorting(sorting: Sorting) = updateState { it.copy(sorting = sorting) }

    fun toggleFilter() {
        updateState { it.copy(showFilter = !it.showFilter) }

        if (!currentState.showFilter) {
            Preferences.saveKitsFilter(currentState.kits.mapTo(mutableSetOf(), Kit::kitId))
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
}