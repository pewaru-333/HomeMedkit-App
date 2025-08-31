package ru.application.homemedkit.models.viewModels

import android.webkit.MimeTypeMap
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import ru.application.homemedkit.data.dto.Kit
import ru.application.homemedkit.models.states.SettingsState
import ru.application.homemedkit.utils.di.Database
import ru.application.homemedkit.utils.di.Preferences
import ru.application.homemedkit.utils.enums.Page
import ru.application.homemedkit.utils.enums.Sorting
import ru.application.homemedkit.utils.enums.Theme
import java.io.File

class SettingsViewModel : BaseViewModel<SettingsState, Unit>() {
    override fun initState() = SettingsState()

    override fun loadData() = Unit

    override fun onEvent(event: Unit) = Unit

    val startPage = Preferences.startPageFlow.stateIn(viewModelScope, SharingStarted.Eagerly, Page.MEDICINES)

    val sortingType = Preferences.sortingOrderFlow.stateIn(viewModelScope, SharingStarted.Eagerly, Sorting.IN_NAME)

    val checkExpiration = Preferences.checkExpiration.stateIn(viewModelScope, SharingStarted.Eagerly, false)

    val theme = Preferences.theme.stateIn(viewModelScope, SharingStarted.Eagerly, Theme.SYSTEM)

    val kits = Database.kitDAO().getFlow()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000L), emptyList())

    fun changeStartPage(page: Page) {
        Preferences.setStartPage(page)
    }

    fun changeSortingType(type: Sorting) {
        Preferences.setSortingType(type)
    }

    fun changeExpirationCheck(flag: Boolean) {
        Preferences.setCheckExpDate(flag)
    }

    fun toggleKits() = updateState { it.copy(showKits = !it.showKits) }

    fun toggleExport() = updateState { it.copy(showExport = !it.showExport) }

    fun toggleFixing() = updateState { it.copy(showFixing = !it.showFixing) }

    fun toggleClearing() = updateState { it.copy(showClearing = !it.showClearing) }

    fun togglePermissions() = updateState { it.copy(showPermissions = !it.showPermissions) }

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

    fun clearCache(cacheDir: File, filesDir: File) {
        viewModelScope.launch {
            val images = Database.medicineDAO().getAllImages()

            coroutineScope {
                launch { cacheDir.deleteRecursively() }
                launch {
                    filesDir.listFiles()?.forEach { file ->
                        MimeTypeMap.getSingleton().getMimeTypeFromExtension(file.extension)?.let {
                            if (file.name !in images && it.startsWith("image/"))
                                file.deleteRecursively()
                        }
                    }
                }
            }
        }

        updateState { it.copy(showClearing = false) }
    }
}