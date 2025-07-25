package ru.application.homemedkit.models.states

import androidx.annotation.StringRes
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateSetOf
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.compose.runtime.snapshots.SnapshotStateSet
import ru.application.homemedkit.data.dto.Kit
import ru.application.homemedkit.utils.BLANK
import ru.application.homemedkit.utils.enums.DoseType
import ru.application.homemedkit.utils.enums.DrugType
import ru.application.homemedkit.utils.enums.ImageEditing
import kotlin.random.Random

data class MedicineState(
    val adding: Boolean = true,
    val editing: Boolean = false,
    val default: Boolean = false,
    val id: Long = 0L,
    val kits: SnapshotStateSet<Kit> = mutableStateSetOf(),
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
    val fullImage: Int = 0,
    val images: SnapshotStateList<String> = mutableStateListOf(DrugType.entries[Random.nextInt(0, DrugType.entries.size)].value),
    val technical: TechnicalState = TechnicalState(),
    val snackbarHostState: SnackbarHostState = SnackbarHostState(),
    val imageEditing: ImageEditing = ImageEditing.ADDING,
    val showDialogKits: Boolean = false,
    val showDialogDate: Boolean = false,
    val showDialogPackageDate: Boolean = false,
    val showDialogIcons: Boolean = false,
    val showDialogPictureGrid: Boolean = false,
    val showDialogPictureChoose: Boolean = false,
    val showDialogFullImage: Boolean = false,
    val showDialogDelete: Boolean = false,
    val showMenuDose: Boolean = false,
    val showTakePhoto: Boolean = false
)