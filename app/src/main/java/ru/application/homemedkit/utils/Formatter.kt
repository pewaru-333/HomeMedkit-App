package ru.application.homemedkit.utils

import android.icu.text.DecimalFormat
import androidx.compose.ui.text.intl.Locale
import androidx.compose.ui.text.intl.PlatformLocale
import java.time.Instant
import java.time.LocalDate
import java.time.LocalTime
import java.time.YearMonth
import java.time.ZoneId
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle

object Formatter {
    private val locale: PlatformLocale
        get() = Locale.current.platformLocale

    private val formatter = DecimalFormat.getInstance(locale).apply {
        maximumFractionDigits = 4
    }

    val ZONE: ZoneId
        get() = ZoneId.systemDefault()

    val FORMAT_LONG: DateTimeFormatter
        get() = DateTimeFormatter.ofLocalizedDate(FormatStyle.LONG).withLocale(locale)

    val FORMAT_D_MMMM_E: DateTimeFormatter
        get() = DateTimeFormatter.ofPattern("d MMMM, E", locale)

    val FORMAT_DD_MM_YYYY: DateTimeFormatter
        get() = DateTimeFormatter.ofPattern("dd.MM.yyyy")

    val FORMAT_DD_MM: DateTimeFormatter
        get() = DateTimeFormatter.ofPattern("dd MMMM")

    val FORMAT_H_MM: DateTimeFormatter
        get() = DateTimeFormatter.ofPattern("H:mm")

    val FORMAT_DD_MM_YYYY_HH_MM: DateTimeFormatter
        get() = DateTimeFormatter.ofPattern("dd.MM.yyyy H:mm")

    private val FORMAT_MM_YYYY: DateTimeFormatter
        get() = DateTimeFormatter.ofPattern("MM/yyyy")

    fun getDateTime(millis: Long): ZonedDateTime = Instant.ofEpochMilli(millis).atZone(ZONE)

    fun decimalFormat(text: Any?): String {
        val value = text.toString().toDoubleOrNull() ?: 0.0

        return formatter.format(value)
    }

    fun formFormat(form: String) = form.substringBefore(" ")

    fun dateFormat(millis: Long, formatter: DateTimeFormatter): String = getDateTime(millis).format(formatter)

    fun timeFormat(millis: Long) = dateFormat(millis, FORMAT_H_MM)

    fun timeFormat(hour: Int, minute: Int): String = LocalTime.of(hour, minute).format(FORMAT_H_MM)

    fun toExpDate(milli: Long): String = if (milli <= 0) BLANK
    else getDateTime(milli).format(FORMAT_LONG)

    fun cardFormat(millis: Long): String = if (millis == -1L) BLANK
    else getDateTime(millis).format(FORMAT_MM_YYYY)

    fun toTimestamp(date: String? = null, time: String? = null, hour: Int? = null, minute: Int? = null): Long {
        val localDate = date?.let { LocalDate.parse(it, FORMAT_DD_MM_YYYY) } ?: LocalDate.now()
        val localTime = when {
            time != null -> LocalTime.parse(time, FORMAT_H_MM)
            hour != null && minute != null -> LocalTime.of(hour, minute)
            else -> LocalTime.of(12, 0, 0)
        }

        return ZonedDateTime.of(localDate, localTime, ZONE).toInstant().toEpochMilli()
    }

    fun toTimestamp(month: Int, year: Int) = ZonedDateTime.of(
        /* year = */         year,
        /* month = */        month,
        /* dayOfMonth = */   YearMonth.of(year, month).lengthOfMonth(),
        /* hour = */         LocalTime.MAX.hour,
        /* minute = */       LocalTime.MAX.minute,
        /* second = */       LocalTime.MAX.second,
        /* nanoOfSecond = */ LocalTime.MAX.nano,
        /* zone = */         ZONE
    ).toInstant().toEpochMilli()
}