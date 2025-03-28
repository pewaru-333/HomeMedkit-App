package ru.application.homemedkit.models.viewModels

import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import ru.application.homemedkit.HomeMeds.Companion.database
import ru.application.homemedkit.data.dto.MedicineKit
import ru.application.homemedkit.data.model.MedicineList
import ru.application.homemedkit.data.model.MedicineMain
import ru.application.homemedkit.helpers.BLANK
import ru.application.homemedkit.helpers.decimalFormat
import ru.application.homemedkit.helpers.formName
import ru.application.homemedkit.helpers.inCard
import ru.application.homemedkit.models.states.MedicinesState

class MedicinesViewModel : ViewModel() {
    private val _state = MutableStateFlow(MedicinesState())
    val state = _state.asStateFlow()

    private val medicineDAO = database.medicineDAO()
    private val kitDAO = database.kitDAO()

    val medicines = combine(_state, medicineDAO.getFlow(), kitDAO.getMedicinesKits()) { query, list, kits ->
        list.filter {
            listOf(it.productName, it.nameAlias, it.structure, it.phKinetics).any { it.contains(query.search, true) } &&
            if (query.kits.isEmpty()) true
            else it.id in kits.filter { it.kitId in query.kits }.map(MedicineKit::medicineId)
        }.sortedWith(query.sorting)
            .map {
                MedicineList(
                    id = it.id,
                    title = it.nameAlias.ifEmpty(it::productName),
                    prodAmount = decimalFormat(it.prodAmount),
                    doseType = it.doseType.title,
                    expDateS = inCard(it.expDate),
                    expDateL = it.expDate,
                    formName = formName(it.prodFormNormName),
                    image =  it.image.firstOrNull() ?: BLANK
                )
            }
    }.flowOn(Dispatchers.IO)
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000L),
            initialValue = mutableStateListOf()
        )

    fun showAdding() = _state.update { it.copy(showAdding = !it.showAdding) }
    fun showExit(flag: Boolean = false) = _state.update { it.copy(showExit = flag) }

    fun setSearch(text: String) = _state.update { it.copy(search = text) }
    fun clearSearch() = _state.update { it.copy(search = BLANK) }

    fun showSort() = _state.update { it.copy(showSort = !it.showSort) }
    fun setSorting(sorting: Comparator<MedicineMain>) = _state.update { it.copy(sorting = sorting) }

    fun showFilter() = _state.update { it.copy(showFilter = !it.showFilter) }
    fun clearFilter() = _state.update { it.copy(kits = it.kits.apply(SnapshotStateList<Long>::clear)) }
    fun pickFilter(kitId: Long) = _state.update {
        it.copy(kits = it.kits.apply { if (kitId in this) remove(kitId) else add(kitId) })
    }
}