package ru.application.homemedkit.utils.extensions

import ru.application.homemedkit.data.dto.Image
import ru.application.homemedkit.data.dto.Medicine
import ru.application.homemedkit.data.model.MedicineFull
import ru.application.homemedkit.data.model.MedicineIntake
import ru.application.homemedkit.data.model.MedicineList
import ru.application.homemedkit.data.model.MedicineMain
import ru.application.homemedkit.models.states.MedicineState
import ru.application.homemedkit.models.states.TechnicalState
import ru.application.homemedkit.network.models.MainModel
import ru.application.homemedkit.utils.Formatter
import ru.application.homemedkit.utils.ResourceText
import ru.application.homemedkit.utils.enums.DrugType

fun MedicineFull.toState() = MedicineState(
    adding = false,
    editing = false,
    default = true,
    isLoading = false,
    isOpened = packageOpenedDate > 0L,
    id = id,
    kits = kits.toSet(),
    code = cis,
    productName = productName,
    nameAlias = nameAlias.ifEmpty { productName },
    expDate = expDate,
    expDateString = Formatter.toExpDate(expDate),
    dateOpened = packageOpenedDate,
    dateOpenedString = Formatter.toExpDate(packageOpenedDate),
    prodFormNormName = prodFormNormName,
    structure = structure,
    prodDNormName = prodDNormName,
    prodAmount = prodAmount.toString(),
    doseType = doseType,
    phKinetics = phKinetics,
    recommendations = recommendations,
    storageConditions = storageConditions,
    comment = comment,
    images = images.sortedBy(Image::position).map(Image::image),
    technical = TechnicalState(
        scanned = scanned,
        verified = verified
    )
)

fun MedicineFull.toMedicineIntake() = MedicineIntake(
    productName = productName,
    nameAlias = nameAlias,
    prodFormNormName = prodFormNormName,
    expDate = expDate,
    prodAmount = prodAmount,
    doseType = doseType
)

fun MedicineMain.toMedicineList(currentMillis: Long) = MedicineList(
    id = id,
    title = nameAlias.ifEmpty(::productName),
    prodAmountDoseType = ResourceText.MultiString(
        value = listOf(
            ResourceText.StaticString(Formatter.decimalFormat(prodAmount)),
            ResourceText.StaticString(" "),
            ResourceText.StringResource(doseType.title)
        )
    ),
    expDateS = Formatter.cardFormat(expDate),
    formName = Formatter.formFormat(prodFormNormName),
    image = image.orEmpty(),
    inStock = prodAmount > 0.0,
    isExpired = expDate in 1..<currentMillis
)

fun MedicineState.toMedicine() = Medicine(
    id = id,
    cis = code,
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
    scanned = code.isNotBlank(),
    verified = technical.verified
)

fun MainModel.asMedicine(): Medicine {
    val attributes = attributes
    val pharmacy = pharmacyInfo
    val form = pharmacy?.form ?: attributes["Форма выпуска"].orEmpty()
    val dose = pharmacy?.dosage ?: attributes["Объём / Масса единицы потребления"].orEmpty()
    val quantity = pharmacy?.quantity
        ?: attributes["Количество единиц потребления"]
        ?: attributes["Объём"]

    val parsedAmount = quantity
        ?.substringBefore(' ')
        ?.toDoubleOrNull()
        ?: -1.0

    val phKinetics = attributes["Показания к применению"]
        ?: attributes["Область применения"].orEmpty()

    return Medicine(
        productName = productName ?: catalogData?.firstOrNull()?.name.orEmpty(),
        expDate = expireDate ?: -1L,
        prodFormNormName = form.substringBefore(" ").uppercase(),
        prodDNormName = dose,
        doseType = DrugType.getDoseType(form),
        phKinetics = phKinetics.asHtml(),
        prodAmount = parsedAmount,

        // БАДы
        recommendations = attributes["Рекомендации по употреблению"].orEmpty().asHtml(),
        storageConditions = attributes["Условия хранения"].orEmpty().asHtml(),
        structure = attributes["Состав"].orEmpty().asHtml(),

        // Статус
        verified = isValidCategory,
        scanned = true
    )
}