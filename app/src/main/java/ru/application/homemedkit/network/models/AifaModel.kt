package ru.application.homemedkit.network.models

import kotlinx.serialization.Serializable

// Response of the AIFA "Banca Dati Farmaci" search (api.aifa.gov.it/aifa-bdf-eif-be)

@Serializable
data class AifaResponse(
    val data: AifaPage? = null
)

@Serializable
data class AifaPage(
    val content: List<AifaFormaDosaggio>? = null
)

@Serializable
data class AifaFormaDosaggio(
    val principiAttiviIt: List<String>? = null,
    val formaFarmaceutica: String? = null,
    val descrizioneFormaDosaggio: String? = null,
    val medicinale: AifaMedicinale? = null,
    val confezioni: List<AifaConfezione>? = null
)

@Serializable
data class AifaMedicinale(
    val denominazioneMedicinale: String? = null
)

@Serializable
data class AifaConfezione(
    val aic: String? = null,
    val denominazionePackage: String? = null
)

/**
 * Maps the package with the given AIC to the app's [MainModel], or null if it is not in the response.
 * The search can return other packages of the same medicine, so the AIC is matched exactly.
 */
fun AifaResponse.toMainModel(aic: String): MainModel? {
    val (medicine, pack) = data?.content.orEmpty().firstNotNullOfOrNull { item ->
        item.confezioni?.firstOrNull { it.aic == aic }?.let { item to it }
    } ?: return null
    val form = medicine.formaFarmaceutica.orEmpty()

    return MainModel.of(
        code = aic,
        category = MainModel.CATEGORY_DRUGS,
        productName = medicine.medicinale?.denominazioneMedicinale,
        pharmacyData = PharmacyData(
            activeSubstance = medicine.principiAttiviIt?.joinToString(),
            form = form,
            // "600 mg COMPRESSA RIVESTITA" -> "600 mg"
            dosage = medicine.descrizioneFormaDosaggio?.replace(form, "", ignoreCase = true)?.trim(),
            // "\" 600 MG COMPRESSE RIVESTITE\" 30 COMPRESSE IN BLISTER" -> "30 COMPRESSE IN BLISTER"
            quantity = pack.denominazionePackage?.substringAfterLast('"')?.trim()
        )
    )
}
