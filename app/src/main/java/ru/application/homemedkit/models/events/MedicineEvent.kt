package ru.application.homemedkit.models.events

import androidx.compose.runtime.snapshots.SnapshotStateList
import ru.application.homemedkit.data.dto.Kit
import ru.application.homemedkit.helpers.enums.DoseType

sealed interface MedicineEvent {
    data class SetCis(val cis: String) : MedicineEvent
    data class SetProductName(val productName: String) : MedicineEvent
    data class SetNameAlias(val alias: String) : MedicineEvent
    data class SetExpDate(val month: Int, val year: Int) : MedicineEvent
    data class SetPackageDate(val timestamp: Long) : MedicineEvent
    data class SetFormName(val formName: String) : MedicineEvent
    data class SetDoseName(val doseName: String) : MedicineEvent
    data class SetDoseType(val type: DoseType) : MedicineEvent
    data class SetAmount(val amount: String) : MedicineEvent
    data class SetPhKinetics(val phKinetics: String) : MedicineEvent
    data class SetComment(val comment: String) : MedicineEvent

    data class PickKit(val kit: Kit) : MedicineEvent
    data object ClearKit : MedicineEvent

    data class SetIcon(val icon: String) : MedicineEvent
    data class SetImage(val images: SnapshotStateList<String>) : MedicineEvent
    data class ShowDialogFullImage(val index: Int = 0) : MedicineEvent
    data class SetFullImage(val index: Int) : MedicineEvent

    data object ShowKitDialog : MedicineEvent
    data object ShowDatePicker : MedicineEvent
    data object ShowPackageDatePicker : MedicineEvent
    data object ShowDialogPictureChoose : MedicineEvent
    data object ShowIconPicker : MedicineEvent
    data object ShowDialogDelete : MedicineEvent
    data object ShowDoseMenu : MedicineEvent
    data object ShowTakePhoto : MedicineEvent
}