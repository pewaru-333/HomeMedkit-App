package ru.application.homemedkit.models.viewModels

import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import ru.application.homemedkit.data.dto.Kit
import ru.application.homemedkit.models.events.SettingsEvent
import ru.application.homemedkit.models.states.SettingsState
import ru.application.homemedkit.utils.ActionResult
import ru.application.homemedkit.utils.di.Database
import ru.application.homemedkit.utils.di.Preferences
import ru.application.homemedkit.utils.enums.Page
import ru.application.homemedkit.utils.enums.Sorting
import ru.application.homemedkit.utils.enums.Theme

class SettingsViewModel : BaseViewModel<SettingsState, SettingsEvent>() {
    override fun initState() = SettingsState()

    override fun loadData() = Unit

    override fun onEvent(event: SettingsEvent) = when (event) {
        SettingsEvent.ShowClearing -> updateState { it.copy(showClearing = !it.showClearing) }
        SettingsEvent.ShowExport -> updateState { it.copy(showExport = !it.showExport) }
        SettingsEvent.ShowFixing -> updateState { it.copy(showFixing = !it.showFixing) }
        SettingsEvent.ShowKits -> updateState { it.copy(showKits = !it.showKits) }
        SettingsEvent.ShowPermissions -> updateState { it.copy(showPermissions = !it.showPermissions) }
    }

    val startPage = Preferences.startPageFlow.stateIn(viewModelScope, SharingStarted.Eagerly, Page.MEDICINES)

    val sortingType = Preferences.sortingOrderFlow.stateIn(viewModelScope, SharingStarted.Eagerly, Sorting.IN_NAME)

    val checkExpiration = Preferences.checkExpiration.stateIn(viewModelScope, SharingStarted.Eagerly, false)

    val theme = Preferences.theme.stateIn(viewModelScope, SharingStarted.Eagerly, Theme.SYSTEM)

    val kits = Database.kitDAO().getFlow()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000L), emptyList())

    fun upsertKit(kit: Kit) {
        viewModelScope.launch {
            Database.kitDAO().upsert(kit)
        }
    }

    fun deleteKit(kit: Kit) {
        viewModelScope.launch {
            Database.kitDAO().delete(kit)
        }
    }

    fun saveKitsPosition(kits: List<Kit>) {
        viewModelScope.launch {
            val newList = kits.mapIndexed { index, kit ->
                Kit(
                    kitId = kit.kitId,
                    title = kit.title,
                    position = index.toLong()
                )
            }

            Database.kitDAO().updatePositions(newList)
        }
    }

    fun onDataAction(actionResult: ActionResult) {
        viewModelScope.launch {
            val isSuccess = actionResult.onAction()
            actionResult.onResult(isSuccess)
        }
    }
}