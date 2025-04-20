package ru.application.homemedkit.helpers.extensions

import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.compose.runtime.toMutableStateList
import androidx.core.text.HtmlCompat.FROM_HTML_SEPARATOR_LINE_BREAK_PARAGRAPH
import androidx.core.text.HtmlCompat.fromHtml
import ru.application.homemedkit.data.dto.Medicine
import ru.application.homemedkit.data.dto.Technical
import ru.application.homemedkit.data.model.KitModel
import ru.application.homemedkit.data.model.MedicineFull
import ru.application.homemedkit.data.model.MedicineIntake
import ru.application.homemedkit.data.model.MedicineList
import ru.application.homemedkit.data.model.MedicineMain
import ru.application.homemedkit.helpers.BLANK
import ru.application.homemedkit.helpers.decimalFormat
import ru.application.homemedkit.helpers.enums.Types
import ru.application.homemedkit.helpers.formName
import ru.application.homemedkit.helpers.inCard
import ru.application.homemedkit.helpers.toExpDate
import ru.application.homemedkit.models.states.MedicineState
import ru.application.homemedkit.models.states.TechnicalState
import ru.application.homemedkit.network.models.bio.BioData
import ru.application.homemedkit.network.models.medicine.DrugsData

fun MedicineFull.toState() = MedicineState(
    adding = false,
    editing = false,
    default = true,
    id = id,
    kits = kits.mapTo(SnapshotStateList()) { KitModel(it.kitId, id, it.title, it.position) },
    cis = cis,
    productName = productName,
    nameAlias = nameAlias,
    expDate = expDate,
    expDateString = toExpDate(expDate),
    dateOpened = packageOpenedDate,
    dateOpenedString = toExpDate(packageOpenedDate),
    prodFormNormName = prodFormNormName,
    structure = structure,
    prodDNormName = prodDNormName,
    prodAmount = prodAmount.toString(),
    doseType = doseType,
    phKinetics = fromHtml(phKinetics, FROM_HTML_SEPARATOR_LINE_BREAK_PARAGRAPH).toString(),
    recommendations = fromHtml(recommendations, FROM_HTML_SEPARATOR_LINE_BREAK_PARAGRAPH).toString(),
    storageConditions = fromHtml(storageConditions, FROM_HTML_SEPARATOR_LINE_BREAK_PARAGRAPH).toString(),
    comment = comment,
    images = images.toMutableStateList(),
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

fun MedicineMain.toMedicineList() = MedicineList(
    id = id,
    title = nameAlias.ifEmpty(::productName),
    prodAmount = decimalFormat(prodAmount),
    doseType = doseType.title,
    expDateS = inCard(expDate),
    expDateL = expDate,
    formName = formName(prodFormNormName),
    image =  image.firstOrNull() ?: BLANK
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

fun DrugsData.toMedicine() = Medicine(
    productName = prodDescLabel,
    expDate = expireDate,
    prodFormNormName = foiv.prodFormNormName,
    prodDNormName = foiv.prodDNormName.orEmpty(),
    doseType = Types.getDoseType(foiv.prodFormNormName),
    phKinetics = vidalData?.phKinetics.orEmpty(),
    technical = Technical(scanned = true, verified = true),
    prodAmount = foiv.prodPack1Size?.let { it.toDouble() * (foiv.prodPack12?.toDoubleOrNull() ?: 1.0) } ?: 0.0
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