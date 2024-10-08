package ru.application.homemedkit.helpers

import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalConfiguration
import androidx.core.os.ConfigurationCompat
import androidx.core.os.LocaleListCompat
import java.time.Instant
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.YearMonth
import java.time.ZoneId
import java.time.format.DateTimeFormatter

val LOCALE
    @Composable get() = ConfigurationCompat.getLocales(LocalConfiguration.current)[0]
        ?: LocaleListCompat.getDefault()[0]!!
val ZONE = ZoneId.systemDefault().rules.getOffset(Instant.now())
val FORMAT_S = DateTimeFormatter.ofPattern("dd.MM.yyyy")
val FORMAT_DH = DateTimeFormatter.ofPattern("dd.MM.yyyy H:mm")
val FORMAT_DMMMMY @Composable get() = DateTimeFormatter.ofPattern("d MMMM yyyy", LOCALE)
val FORMAT_DME @Composable get() = DateTimeFormatter.ofPattern("d MMMM, E", LOCALE)
val FORMAT_H = DateTimeFormatter.ofPattern("H:mm")
private val FORMAT_MY = DateTimeFormatter.ofPattern("MM/yyyy")

@Composable
fun toExpDate(milli: Long) = if (milli > 0) getDateTime(milli).format(FORMAT_DMMMMY) else BLANK

fun toTimestamp(month: Int, year: Int) = LocalDateTime.of(
    year, month, YearMonth.of(year, month).lengthOfMonth(),
    LocalTime.MAX.hour, LocalTime.MAX.minute
).toInstant(ZONE).toEpochMilli()

fun inCard(milli: Long) = if (milli == -1L) BLANK else getDateTime(milli).format(FORMAT_MY)

fun lastAlarm(date: String, time: LocalTime) = LocalDateTime.of(
    LocalDate.parse(date, FORMAT_S), time
).toInstant(ZONE).toEpochMilli()

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