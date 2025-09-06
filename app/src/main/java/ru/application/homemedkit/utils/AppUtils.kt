package ru.application.homemedkit.utils

import android.icu.math.BigDecimal
import android.icu.text.DecimalFormat
import androidx.compose.ui.text.intl.Locale
import ru.application.homemedkit.data.dto.Image
import ru.application.homemedkit.network.Network
import ru.application.homemedkit.utils.di.Preferences
import ru.application.homemedkit.utils.enums.DrugType
import java.io.File
import java.time.Instant
import java.time.LocalTime
import java.time.YearMonth
import java.time.ZoneId
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle

val ZONE: ZoneId
    get() = ZoneId.systemDefault()

val FORMAT_LONG: DateTimeFormatter
    get() = DateTimeFormatter.ofLocalizedDate(FormatStyle.LONG).withLocale(Locale.current.platformLocale)

val FORMAT_D_MMMM_E: DateTimeFormatter
    get() = DateTimeFormatter.ofPattern("d MMMM, E", Locale.current.platformLocale)

val FORMAT_DD_MM_YYYY: DateTimeFormatter
    get() = DateTimeFormatter.ofPattern("dd.MM.yyyy")

val FORMAT_H_MM: DateTimeFormatter
    get() = DateTimeFormatter.ofPattern("H:mm")

private val FORMAT_MM_YYYY: DateTimeFormatter
    get() = DateTimeFormatter.ofPattern("MM/yyyy")

fun getDateTime(milli: Long) = Instant.ofEpochMilli(milli).atZone(ZONE)

fun inCard(milli: Long) = if (milli == -1L) BLANK else getDateTime(milli).format(FORMAT_MM_YYYY)

fun toExpDate(milli: Long) = if (milli > 0) getDateTime(milli).format(FORMAT_LONG) else BLANK

fun toTimestamp(month: Int, year: Int) = ZonedDateTime.of(
    year,
    month,
    YearMonth.of(year, month).lengthOfMonth(),
    LocalTime.MAX.hour,
    LocalTime.MAX.minute,
    LocalTime.MAX.second,
    LocalTime.MAX.nano,
    ZONE
).toInstant().toEpochMilli()

fun formName(name: String) = name.substringBefore(" ")

fun decimalFormat(text: Any?): String {
    val amount = try {
        text.toString().toDouble()
    } catch (_: NumberFormatException) {
        0.0
    }

    return DecimalFormat.getInstance().apply {
        maximumFractionDigits = 4
        roundingMode = BigDecimal.ROUND_HALF_EVEN
    }.format(amount)
}

suspend fun getMedicineImages(
    medicineId: Long,
    form: String,
    directory: File,
    urls: List<String>?
): List<Image> {
    val imageList = if (Preferences.imageFetch && !urls.isNullOrEmpty()) {
        Network.getImage(directory, urls)
    } else {
        emptyList()
    }

    return if (imageList.isEmpty()) listOf(
        Image(
            medicineId = medicineId,
            image = DrugType.setIcon(form)
        )
    ) else imageList.map { image ->
        Image(
            medicineId = medicineId,
            image = image
        )
    }
}