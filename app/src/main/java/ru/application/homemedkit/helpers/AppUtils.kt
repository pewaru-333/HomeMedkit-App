package ru.application.homemedkit.helpers

import android.icu.math.BigDecimal
import android.icu.text.DecimalFormat
import androidx.compose.runtime.toMutableStateList
import androidx.compose.ui.text.intl.Locale
import ru.application.homemedkit.HomeMeds.Companion.database
import ru.application.homemedkit.data.dto.Image
import ru.application.homemedkit.data.dto.Intake
import ru.application.homemedkit.data.dto.Medicine
import ru.application.homemedkit.data.dto.Technical
import ru.application.homemedkit.models.states.IntakeState
import ru.application.homemedkit.models.states.MedicineState
import ru.application.homemedkit.models.states.TechnicalState
import ru.application.homemedkit.network.Network
import ru.application.homemedkit.network.models.bio.BioData
import ru.application.homemedkit.network.models.medicine.DrugsData
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
): Array<Image> {
    val imageList = mutableListOf<String>()

    if (Preferences.getImageFetch()) {
        val images = Network.getImage(directory, urls)
        imageList.addAll(images)
    }

    val images = if (imageList.isEmpty()) arrayOf(
        Image(
            medicineId = medicineId,
            image = Types.setIcon(form)
        )
    ) else imageList.map { image ->
        Image(
            medicineId = medicineId,
            image = image
        )
    }.toTypedArray()

    return images
}

fun Medicine.toState() = MedicineState(
    adding = false,
    editing = false,
    default = true,
    id = id,
    kits = database.kitDAO().getIdList(id).toMutableStateList(),
    cis = cis,
    productName = productName,
    nameAlias = nameAlias,
    expDate = expDate,
    dateOpened = packageOpenedDate,
    prodFormNormName = prodFormNormName,
    structure = structure,
    prodDNormName = prodDNormName,
    prodAmount = prodAmount.toString(),
    doseType = doseType,
    phKinetics = phKinetics,
    recommendations = recommendations,
    storageConditions = storageConditions,
    comment = comment,
    images = database.medicineDAO().getMedicineImages(id).toMutableStateList(),
    technical = TechnicalState(
        scanned = technical.scanned,
        verified = technical.verified
    )
)

fun MedicineState.toMedicine() = Medicine(
    id = id,
    cis = cis,
    productName = productName,
    nameAlias = nameAlias,
    expDate = expDate,
    packageOpenedDate = dateOpened,
    prodFormNormName = prodFormNormName,
    structure = structure,
    prodDNormName = prodDNormName,
    prodAmount = prodAmount.ifEmpty { "0.0" }.toDouble(),
    doseType = doseType,
    phKinetics = phKinetics,
    recommendations = recommendations,
    storageConditions = storageConditions,
    comment = comment,
    technical = Technical(
        scanned = cis.isNotBlank(),
        verified = technical.verified
    )
)

fun IntakeState.toIntake() = Intake(
    intakeId = intakeId,
    medicineId = medicineId,
    schemaType = schemaType,
    interval = interval.toInt(),
    foodType = foodType,
    period = period.toInt(),
    startDate = startDate,
    finalDate = finalDate,
    sameAmount = sameAmount,
    fullScreen = fullScreen,
    noSound = noSound,
    preAlarm = preAlarm,
    cancellable = cancellable
)

fun DrugsData.toMedicine() = Medicine(
    productName = prodDescLabel,
    expDate = expireDate,
    prodFormNormName = foiv.prodFormNormName,
    prodDNormName = foiv.prodDNormName.orEmpty(),
    doseType = Types.getDoseType(foiv.prodFormNormName),
    phKinetics = vidalData?.phKinetics.orEmpty(),
    technical = Technical(scanned = true, verified = true),
    prodAmount = foiv.prodPack1Size?.let {
        it.toDouble() * (foiv.prodPack12?.toDoubleOrNull() ?: 1.0)
    } ?: 0.0
)

fun BioData.toMedicine() = Medicine(
    productName = productName,
    expDate = expireDate,
    prodDNormName = productProperty.unitVolumeWeight.orEmpty(),
    prodAmount = productProperty.quantityInPack ?: 0.0,
    phKinetics = productProperty.applicationArea.orEmpty(),
    recommendations = productProperty.recommendForUse.orEmpty(),
    storageConditions = productProperty.storageConditions.orEmpty(),
    structure = productProperty.structure.orEmpty(),
    prodFormNormName = productProperty.releaseForm.orEmpty().substringBefore(" ").uppercase(),
    doseType = Types.getDoseType(productProperty.releaseForm.orEmpty()),
    technical = Technical(
        scanned = true,
        verified = true
    )
)