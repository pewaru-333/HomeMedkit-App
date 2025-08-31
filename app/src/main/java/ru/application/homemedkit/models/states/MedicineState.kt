package ru.application.homemedkit.models.states

import androidx.annotation.StringRes
import androidx.compose.material3.SnackbarHostState
import ru.application.homemedkit.data.dto.Kit
import ru.application.homemedkit.utils.BLANK
import ru.application.homemedkit.utils.enums.DoseType
import ru.application.homemedkit.utils.enums.DrugType
import ru.application.homemedkit.utils.enums.ImageEditing
import kotlin.random.Random

data class MedicineState(
    val adding: Boolean = false,
    val editing: Boolean = false,
    val default: Boolean = false,
    val isLoading: Boolean = true,
    val id: Long = 0L,
    val kits: Set<Kit> = emptySet(),
    val cis: String = BLANK,
    val productName: String = BLANK,
    @StringRes val productNameError: Int? = null,
    val nameAlias: String = BLANK,
    val expDate: Long = -1L,
    val expDateString: String = BLANK,
    val dateOpened: Long = -1L,
    val dateOpenedString: String = BLANK,
    val prodFormNormName: String = BLANK,
    val structure: String = BLANK,
    val prodDNormName: String = BLANK,
    val prodAmount: String = BLANK,
    val doseType: DoseType = DoseType.UNKNOWN,
    val phKinetics: String = BLANK,
    val recommendations: String = BLANK,
    val storageConditions: String = BLANK,
    val comment: String = BLANK,
    val images: List<String> = listOf(DrugType.entries[Random.nextInt(DrugType.entries.size)].value),
    val technical: TechnicalState = TechnicalState(),
    val snackbarHostState: SnackbarHostState = SnackbarHostState(),
    val imageEditing: ImageEditing = ImageEditing.ADDING,
    val isOpened: Boolean = false,
    val dialogState: MedicineDialogState? = null
)