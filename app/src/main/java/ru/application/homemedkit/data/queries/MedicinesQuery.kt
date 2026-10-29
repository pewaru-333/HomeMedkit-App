package ru.application.homemedkit.data.queries

import ru.application.homemedkit.utils.BLANK
import ru.application.homemedkit.utils.enums.Sorting

data class MedicinesQuery(
    val search: String = BLANK,
    val sorting: Sorting = Sorting.IN_NAME,
    val hideEmpty: Boolean = false,
    val kitIds: Set<Long> = emptySet()
)
