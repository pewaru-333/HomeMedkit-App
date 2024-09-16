package ru.application.homemedkit.viewModels

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
import ru.application.homemedkit.HomeMeds.Companion.database
import ru.application.homemedkit.data.dto.Medicine
import ru.application.homemedkit.helpers.BLANK
import ru.application.homemedkit.helpers.Preferences
import ru.application.homemedkit.helpers.Sorting
import ru.application.homemedkit.helpers.Sorting.InName
import java.util.Locale.ROOT

object MedicinesViewModel : ViewModel() {
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

    fun setSearch(text: String) = _state.update { it.copy(search = text) }
    fun clearSearch() = _state.update { it.copy(search = BLANK) }

    fun showSort() = _state.update { it.copy(showSort = true) }
    fun hideSort() = _state.update { it.copy(showSort = false) }
    fun setSorting(sorting: Comparator<Medicine>) = _state.update { it.copy(sorting = sorting) }

    fun showFilter() = _state.update { it.copy(showFilter = true) }
    fun hideFilter() = _state.update { it.copy(showFilter = false) }

    fun setFilter(kitId: Long) = _state.update { it.copy(kitId = kitId) }
    fun saveFilter() {
        Preferences.setLastKit(_state.value.kitId)
        _state.update { it.copy(showFilter = false) }
    }
}

data class MedicinesState(
    val search: String = BLANK,
    val sorting: Comparator<Medicine> =
        Sorting.entries.find { it.value == Preferences.getSortingOrder() }?.type ?: InName.type,
    val kitId: Long = Preferences.getLastKit(),
    val showSort: Boolean = false,
    val showFilter: Boolean = false,
    val listState: LazyListState = LazyListState()
)