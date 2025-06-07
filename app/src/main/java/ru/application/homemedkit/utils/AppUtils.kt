package ru.application.homemedkit.utils

import android.icu.math.BigDecimal
import android.icu.text.DecimalFormat
import androidx.compose.ui.text.intl.Locale
import ru.application.homemedkit.data.dto.Image
import ru.application.homemedkit.utils.enums.DrugType
import ru.application.homemedkit.network.Network
import java.io.File
import java.time.Instant
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.YearMonth
import java.time.ZoneId
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle

val ZONE: ZoneOffset
    get() = ZoneId.systemDefault().rules.getOffset(Instant.now())

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

fun toTimestamp(month: Int, year: Int) = LocalDateTime.of(
    year,
    month,
    YearMonth.of(year, month).lengthOfMonth(),
    LocalTime.MAX.hour,
    LocalTime.MAX.minute
).toInstant(ZONE).toEpochMilli()

fun expirationCheckTime(): Long {
    var unix = LocalDateTime.of(LocalDate.now(), LocalTime.of(12, 0))

    if (unix.toInstant(ZONE).toEpochMilli() < System.currentTimeMillis()) unix = unix.plusDays(1)

    return unix.toInstant(ZONE).toEpochMilli()
}

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

    val images = if (imageList.isEmpty()) listOf(
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

    return images
}