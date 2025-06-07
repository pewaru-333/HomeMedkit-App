package ru.application.homemedkit.models.viewModels

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
import ru.application.homemedkit.R
import ru.application.homemedkit.data.dto.Kit
import ru.application.homemedkit.data.model.KitModel
import ru.application.homemedkit.data.model.MedicineGrouped
import ru.application.homemedkit.data.model.MedicineMain
import ru.application.homemedkit.models.states.MedicinesState
import ru.application.homemedkit.utils.BLANK
import ru.application.homemedkit.utils.ResourceText
import ru.application.homemedkit.utils.enums.MedicineTab
import ru.application.homemedkit.utils.enums.Sorting
import ru.application.homemedkit.utils.extensions.toMedicineList
import ru.application.homemedkit.utils.extensions.toModel

class MedicinesViewModel : ViewModel() {
    private val _state = MutableStateFlow(MedicinesState())
    val state = _state.asStateFlow()

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
        .flowOn(Dispatchers.Default)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(), emptyList())


    @OptIn(ExperimentalCoroutinesApi::class)
    val grouped = _state.flatMapLatest { state ->
        _medicines.map { medicineList ->
            val selectedKits = state.kits.map(Kit::kitId).toSet()
            val filterIsEmpty = selectedKits.isEmpty()

            val kitsToGroup = if (filterIsEmpty) {
                database.kitDAO().getKitList(medicineList.flatMap(MedicineMain::kitIds).distinct()).associateBy(Kit::kitId)
            } else {
                database.kitDAO().getKitList(selectedKits.toList()).associateBy(Kit::kitId)
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
                            medicines = medicinesInKit.map(MedicineMain::toMedicineList)
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
                                    medicines = medicinesSelectedKits.map(MedicineMain::toMedicineList)
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
                            medicines = noGroupMedicines.map(MedicineMain::toMedicineList),
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
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(), emptyList())

    val kits = database.kitDAO().getFlow()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(), emptyList())

    fun toggleView() = _state.update {
        it.copy(tab = MedicineTab.entries.getOrElse(it.tab.ordinal + 1) { MedicineTab.LIST })
    }

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