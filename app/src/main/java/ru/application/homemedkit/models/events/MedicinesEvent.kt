package ru.application.homemedkit.models.events

import ru.application.homemedkit.utils.enums.MedicineListView
import ru.application.homemedkit.utils.enums.Sorting

sealed interface MedicinesEvent {
    data class Search(val text: String) : MedicinesEvent
    data class PickView(val view: MedicineListView) : MedicinesEvent
    data class SetSorting(val sorting: Sorting) : MedicinesEvent
    data class PickKit(val id: Long) : MedicinesEvent
    data class HideEmpty(val hide: Boolean) : MedicinesEvent
    data object ToggleAdding : MedicinesEvent
    data object ToggleSorting : MedicinesEvent

    data object ToggleFilter : MedicinesEvent
    data object ClearFilter : MedicinesEvent

    data object ShowExit : MedicinesEvent
    data object HideExit : MedicinesEvent
}
