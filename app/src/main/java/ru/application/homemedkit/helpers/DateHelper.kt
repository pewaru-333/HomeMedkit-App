package ru.application.homemedkit.helpers

import java.time.Duration
import java.time.Instant
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.YearMonth
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle

val ZONE = ZoneId.systemDefault().rules.getOffset(Instant.now())
val FORMAT_S = DateTimeFormatter.ofPattern("dd.MM.yyyy")
val FORMAT_D_H = DateTimeFormatter.ofPattern("dd.MM.yyyy H:mm")
val FORMAT_D_M_Y = DateTimeFormatter.ofPattern("d MMMM yyyy")
val FORMAT_D_MM_Y = DateTimeFormatter.ofPattern("d MMM yyyy")
val FORMAT_D_M = DateTimeFormatter.ofPattern("d MMMM, E")
val FORMAT_H = DateTimeFormatter.ofPattern("H:mm")
private val FORMAT_L = DateTimeFormatter.ofLocalizedDate(FormatStyle.LONG)
private val FORMAT_M_Y = DateTimeFormatter.ofPattern("MM/yyyy")

fun toExpDate(milli: Long) = if (milli > 0) getDateTime(milli).format(FORMAT_L) else BLANK

fun toTimestamp(month: Int, year: Int) = LocalDateTime.of(
    year, month, YearMonth.of(year, month).lengthOfMonth(),
    LocalTime.MAX.hour, LocalTime.MAX.minute
).toInstant(ZONE).toEpochMilli()

fun inCard(milli: Long) = if (milli == -1L) BLANK else getDateTime(milli).format(FORMAT_M_Y)

fun getPeriod(dateS: String, dateF: String): String {
    val startD = LocalDate.parse(dateS, FORMAT_S)
    val finalD = LocalDate.parse(dateF, FORMAT_S)

    return Duration.between(startD.atStartOfDay(), finalD.atStartOfDay()).toDays().toString()
}

fun lastAlarm(date: String, time: LocalTime) = LocalDateTime.of(
    LocalDate.parse(date, FORMAT_S), time).toInstant(ZONE).toEpochMilli()

fun expirationCheckTime(): Long {
    var unix = LocalDateTime.of(LocalDate.now(), LocalTime.of(12, 0))

    if (unix.toInstant(ZONE).toEpochMilli() < System.currentTimeMillis()) unix = unix.plusDays(1)

    return unix.toInstant(ZONE).toEpochMilli()
}

fun getDateTime(milli: Long) = Instant.ofEpochMilli(milli).atZone(ZONE)

fun longSeconds(start: String, time: List<LocalTime>) = ArrayList<Long>(time.size).apply {
    time.sortedWith(compareBy { it }).forEach { localTime ->
        val localDate = LocalDate.parse(start, FORMAT_S)
        var unix = LocalDateTime.of(localDate, localTime)

        while (unix.toInstant(ZONE).toEpochMilli() < System.currentTimeMillis()) {
            unix = unix.plusDays(1)
        }

        add(unix.toInstant(ZONE).toEpochMilli())
    }
}