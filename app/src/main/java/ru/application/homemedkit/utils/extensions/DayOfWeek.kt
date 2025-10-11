package ru.application.homemedkit.utils.extensions

import java.time.DayOfWeek
import kotlin.enums.EnumEntries

val EnumEntries<DayOfWeek>.weekdays: List<DayOfWeek>
    get() = take(5)

val EnumEntries<DayOfWeek>.weekends: List<DayOfWeek>
    get() = takeLast(2)
