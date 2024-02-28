package ru.application.homemedkit.helpers

import ru.application.homemedkit.helpers.ConstantsHelper.SEMICOLON
import ru.application.homemedkit.helpers.DateHelper.FORMAT_H
import ru.application.homemedkit.helpers.DateHelper.FORMAT_S
import ru.application.homemedkit.helpers.DateHelper.ZONE
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime

fun longSeconds(start: String, time: String): LongArray {
    val times = time.split(SEMICOLON).sortedWith(compareBy { LocalTime.parse(it, FORMAT_H) })
    val triggers = ArrayList<Long>(times.size)

    times.forEach {
        val localDate = LocalDate.parse(start, FORMAT_S)
        val localTime = LocalTime.parse(it, FORMAT_H)
        var unix = LocalDateTime.of(localDate, localTime)

        while (unix.toInstant(ZONE).toEpochMilli() < System.currentTimeMillis()) {
            unix = unix.plusDays(1)
        }

        triggers.add(unix.toInstant(ZONE).toEpochMilli())
    }

    return triggers.toLongArray()
}