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
import ru.application.homemedkit.network.models.bio.BioData
import ru.application.homemedkit.network.models.medicine.DrugsData
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
            ResourceText.StringResource(doseType.title)
        )
    ),
    expDateS = Formatter.cardFormat(expDate),
    formName = Formatter.formFormat(prodFormNormName),
    image = image.firstOrNull().orEmpty(),
    inStock = prodAmount >= 0.1,
    isExpired = expDate < currentMillis
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

fun DrugsData.toMedicine() = Medicine(
    productName = prodDescLabel,
    expDate = expireDate,
    prodFormNormName = foiv.prodFormNormName,
    prodDNormName = foiv.prodDNormName.orEmpty(),
    doseType = DrugType.getDoseType(foiv.prodFormNormName),
    phKinetics = vidalData?.phKinetics.orEmpty().asHtml(),
    scanned = true,
    verified = true,
    prodAmount = foiv.prodPack1Size?.let { it.toDouble() * (foiv.prodPack12?.toDoubleOrNull() ?: 1.0) } ?: 0.0
)

fun BioData.toMedicine() = Medicine(
    productName = productName,
    expDate = expireDate ?: 0L,
    prodDNormName = productProperty?.unitVolumeWeight.orEmpty(),
    prodAmount = productProperty?.quantityInPack ?: 0.0,
    phKinetics = productProperty?.applicationArea.orEmpty().asHtml(),
    recommendations = productProperty?.recommendForUse.orEmpty().asHtml(),
    storageConditions = productProperty?.storageConditions.orEmpty().asHtml(),
    structure = productProperty?.structure.orEmpty().asHtml(),
    prodFormNormName = productProperty?.releaseForm.orEmpty().substringBefore(" ").uppercase(),
    doseType = DrugType.getDoseType(productProperty?.releaseForm.orEmpty()),
    scanned = true,
    verified = true
)

fun MainModel.toMedicine() = Medicine(
    productName = productName,
    prodAmount = 0.0,
    scanned = true,
    verified = true
)