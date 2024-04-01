package ru.application.homemedkit.helpers

import ru.application.homemedkit.helpers.DateHelper.FORMAT_S
import ru.application.homemedkit.helpers.DateHelper.ZONE
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime

fun longSeconds(start: String, time: List<LocalTime>): List<Long> {
    val triggers = ArrayList<Long>(time.size)

    time.sortedWith(compareBy { it }).forEach { localTime ->
        val localDate = LocalDate.parse(start, FORMAT_S)
        var unix = LocalDateTime.of(localDate, localTime)

        while (unix.toInstant(ZONE).toEpochMilli() < System.currentTimeMillis()) {
            unix = unix.plusDays(1)
        }

        triggers.add(unix.toInstant(ZONE).toEpochMilli())
    }

    return triggers
}