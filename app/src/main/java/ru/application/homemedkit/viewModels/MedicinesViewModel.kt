package ru.application.homemedkit.viewModels

import androidx.annotation.StringRes
import androidx.compose.foundation.lazy.LazyListState
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import ru.application.homemedkit.R.string.sorting_a_z
import ru.application.homemedkit.R.string.sorting_from_newest
import ru.application.homemedkit.R.string.sorting_from_oldest
import ru.application.homemedkit.R.string.sorting_z_a
import ru.application.homemedkit.HomeMeds.Companion.database
import ru.application.homemedkit.data.dto.Medicine
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

    @OptIn(ExperimentalCoroutinesApi::class)
    val medicines = _state.flatMapLatest { (search, sorting, kitId) ->
        flow {
            emit(
                database.medicineDAO().getAll().filter { (_, dKitId, _, productName) ->
                    productName.lowercase(ROOT).contains(search.lowercase(ROOT)) &&
                            if (kitId != 0L) dKitId == kitId else true
                }.sortedWith(sorting)
            )
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(), emptyList())

    fun setSearch(text: String) {
        viewModelScope.launch { _state.update { it.copy(search = text) } }
    }

    fun clearSearch() {
        viewModelScope.launch { _state.update { it.copy(search = BLANK) } }
    }

    fun showSort() {
        viewModelScope.launch { _state.update { it.copy(showSort = true) } }
    }

    fun hideSort() {
        viewModelScope.launch { _state.update { it.copy(showSort = false) } }
    }

    fun setSorting(sorting: Comparator<Medicine>) {
        viewModelScope.launch { _state.update { it.copy(sorting = sorting) } }
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
        viewModelScope.launch { _state.update { it.copy(showFilter = false) } }
    }
}

data class MedicinesState(
    val search: String = BLANK,
    val sorting: Comparator<Medicine> = when (Preferences.getSortingOrder()) {
        SORTING[0] -> IN_NAME.sorting
        SORTING[1] -> RE_NAME.sorting
        SORTING[2] -> IN_DATE.sorting
        else -> RE_DATE.sorting
    },
    val kitId: Long = Preferences.getLastKit(),
    val showSort: Boolean = false,
    val showFilter: Boolean = false,
    val listState: LazyListState = LazyListState()
)

enum class SortingItems(@StringRes val text: Int, val sorting: Comparator<Medicine>) {
    IN_NAME(sorting_a_z, comparing(Medicine::productName)),
    RE_NAME(sorting_z_a, comparing(Medicine::productName).reversed()),
    IN_DATE(sorting_from_oldest, comparing(Medicine::expDate)),
    RE_DATE(sorting_from_newest, comparing(Medicine::expDate).reversed())
}