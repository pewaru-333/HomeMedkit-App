package ru.application.homemedkit.states


import ru.application.homemedkit.helpers.BLANK
import ru.application.homemedkit.helpers.ICONS_MED
import ru.application.homemedkit.viewModels.ResponseState

data class MedicineState(
    val adding: Boolean = true,
    val editing: Boolean = false,
    val default: Boolean = false,
    val fetch: ResponseState = ResponseState.Default,
    val id: Long = 0,
    val cis: String = BLANK,
    val productName: String = BLANK,
    val expDate: Long = 0,
    val prodFormNormName: String = BLANK,
    val prodDNormName: String = BLANK,
    val prodAmount: String = BLANK,
    val phKinetics: String = BLANK,
    val comment: String = BLANK,
    val image: String = ICONS_MED.keys.elementAt(0),
    val technical: TechnicalState = TechnicalState()
)
