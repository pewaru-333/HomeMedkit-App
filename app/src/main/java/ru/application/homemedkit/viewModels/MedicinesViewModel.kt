package ru.application.homemedkit.viewModels

import androidx.annotation.StringRes
import androidx.compose.foundation.lazy.LazyListState
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import ru.application.homemedkit.R.string.sorting_a_z
import ru.application.homemedkit.R.string.sorting_from_newest
import ru.application.homemedkit.R.string.sorting_from_oldest
import ru.application.homemedkit.R.string.sorting_z_a
import ru.application.homemedkit.activities.HomeMeds.Companion.database
import ru.application.homemedkit.databaseController.Medicine
import ru.application.homemedkit.helpers.BLANK
import ru.application.homemedkit.helpers.Preferences
import ru.application.homemedkit.helpers.SORTING
import ru.application.homemedkit.viewModels.SortingItems.IN_DATE
import ru.application.homemedkit.viewModels.SortingItems.IN_NAME
import ru.application.homemedkit.viewModels.SortingItems.RE_DATE
import ru.application.homemedkit.viewModels.SortingItems.RE_NAME
import java.util.Comparator.comparing
import java.util.Locale.ROOT

class MedicinesViewModel : ViewModel() {
    private val _state = MutableStateFlow(MedicinesState())
    val state = _state.asStateFlow()

    fun getAll() {
        viewModelScope.launch {
            _state.update {
                it.copy(medicines = database.medicineDAO().getAll().sortedWith(getSorting()))
            }
        }
    }

    fun setSearch(text: String) {
        viewModelScope.launch {
            _state.update { it.copy(search = text, medicines = search(text, _state.value.kitId)) }
        }
    }

    fun clearSearch() {
        viewModelScope.launch {
            _state.update { it.copy(search = BLANK, medicines = search(BLANK, _state.value.kitId)) }
        }
    }

    fun showSort() {
        viewModelScope.launch { _state.update { it.copy(showSort = true) } }
    }

    fun hideSort() {
        viewModelScope.launch { _state.update { it.copy(showSort = false) } }
    }

    fun setSorting(sorting: Comparator<Medicine>) {
        viewModelScope.launch {
            _state.update {
                it.copy(sorting = sorting, medicines = _state.value.medicines.sortedWith(sorting))
            }
        }
    }

    fun showFilter() {
        viewModelScope.launch { _state.update { it.copy(showFilter = true) } }
    }

    fun hideFilter() {
        viewModelScope.launch { _state.update { it.copy(showFilter = false) } }
    }

    fun setFilter(kitId: Long) {
        viewModelScope.launch { _state.update { it.copy(kitId = kitId) } }
    }

    fun saveFilter() {
        Preferences.setLastKit(_state.value.kitId)
        viewModelScope.launch {
            _state.update {
                it.copy(showFilter = false, medicines = search(_state.value.search, _state.value.kitId))
            }
        }
    }

    private fun search(text: String, kitId: Long): List<Medicine> {
        val medicines = if (kitId == 0L) database.medicineDAO().getAll()
        else database.medicineDAO().getByKitId(kitId)
        val filtered = ArrayList<Medicine>(medicines.size)

        if (text.isEmpty()) filtered.addAll(medicines) else medicines.forEach {
            val productName = database.medicineDAO().getProductName(it.id)
            if (productName.lowercase(ROOT).contains(text.lowercase(ROOT))) filtered.add(it)
        }

        return filtered.sortedWith(_state.value.sorting)
    }
}

data class MedicinesState(
    val search: String = BLANK,
    val sorting: Comparator<Medicine> = getSorting(),
    val kitId: Long = Preferences.getLastKit(),
    val showSort: Boolean = false,
    val showFilter: Boolean = false,
    val medicines: List<Medicine> = database.medicineDAO().getAll().sortedWith(getSorting()),
    val listState: LazyListState = LazyListState()
)

enum class SortingItems(@StringRes val text: Int, val sorting: Comparator<Medicine>) {
    IN_NAME(sorting_a_z, comparing(Medicine::productName)),
    RE_NAME(sorting_z_a, comparing(Medicine::productName).reversed()),
    IN_DATE(sorting_from_oldest, comparing(Medicine::expDate)),
    RE_DATE(sorting_from_newest, comparing(Medicine::expDate).reversed())
}

fun getSorting() = when (Preferences.getSortingOrder()) {
    SORTING[0] -> IN_NAME.sorting
    SORTING[1] -> RE_NAME.sorting
    SORTING[2] -> IN_DATE.sorting
    else -> RE_DATE.sorting
}