package ru.application.homemedkit.models.events

import androidx.camera.core.ImageProxy
import ru.application.homemedkit.data.dto.Kit
import ru.application.homemedkit.models.states.MedicineDialogState
import ru.application.homemedkit.utils.camera.ImageProcessing
import ru.application.homemedkit.utils.enums.DoseType

sealed interface MedicineEvent {
    data object MakeDuplicate : MedicineEvent
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

    data object ClearPackageDate : MedicineEvent

    data class PickKit(val kit: Kit) : MedicineEvent
    data object ClearKit : MedicineEvent

    data class SetIcon(val icon: String) : MedicineEvent
    data class SetImage(val imageProcessing: ImageProcessing, val image: ImageProxy) : MedicineEvent
    data class RemoveImage(val image: String) : MedicineEvent

    data class OnImageReodering(val fromIndex: Int, val toIndex: Int) : MedicineEvent

    data object EditImagesOrder : MedicineEvent

    data class ToggleDialog(val dialog: MedicineDialogState) : MedicineEvent
}