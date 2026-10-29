package ru.application.homemedkit.models.states

import ru.application.homemedkit.data.dto.Kit
import ru.application.homemedkit.data.model.MedicineGrouped
import ru.application.homemedkit.data.model.MedicineList
import ru.application.homemedkit.data.queries.MedicinesQuery

data class MedicinesContent(
    val query: MedicinesQuery? = null,
    val medicines: List<MedicineList> = emptyList(),
    val grouped: List<MedicineGrouped> = emptyList(),
    val kits: List<Kit> = emptyList()
)