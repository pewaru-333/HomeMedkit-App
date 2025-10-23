package ru.application.homemedkit.utils.extensions

import ru.application.homemedkit.utils.enums.Period
import kotlin.enums.EnumEntries

val EnumEntries<Period>.defined: List<Period>
    get() = dropLast(1)