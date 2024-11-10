package ru.application.homemedkit.models.states

import ru.application.homemedkit.helpers.BLANK
import ru.application.homemedkit.helpers.DoseTypes
import ru.application.homemedkit.helpers.Types
import ru.application.homemedkit.models.events.Response
import kotlin.random.Random

data class MedicineState(
    val adding: Boolean = true,
    val editing: Boolean = false,
    val default: Boolean = false,
    val fetch: Response = Response.Default,
    val id: Long = 0L,
    val kitId: Long? = null,
    val kitTitle: String = BLANK,
    val cis: String = BLANK,
    val productName: String = BLANK,
    val expDate: Long = -1L,
    val prodFormNormName: String = BLANK,
    val prodDNormName: String = BLANK,
    val prodAmount: String = BLANK,
    val doseType: String = BLANK,
    val doseTypeE: DoseTypes? = null,
    val phKinetics: String = BLANK,
    val comment: String = BLANK,
    val image: String = Types.entries[Random.nextInt(0, Types.entries.size)].value,
    val technical: TechnicalState = TechnicalState(),
    val showDialogKits: Boolean = false,
    val showDialogDate: Boolean = false,
    val showDialogIcons: Boolean = false,
    val showMenuDose: Boolean = false
)