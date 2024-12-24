package ru.application.homemedkit.models.events

import ru.application.homemedkit.helpers.DoseTypes

sealed interface MedicineEvent {
    data class SetCis(val cis: String) : MedicineEvent
    data class SetProductName(val productName: String) : MedicineEvent
    data class SetNameAlias(val alias: String) : MedicineEvent
    data class SetExpDate(val month: Int, val year: Int) : MedicineEvent
    data class SetFormName(val formName: String) : MedicineEvent
    data class SetDoseName(val doseName: String) : MedicineEvent
    data class SetDoseType(val type: DoseTypes) : MedicineEvent
    data class SetAmount(val amount: String) : MedicineEvent
    data class SetPhKinetics(val phKinetics: String) : MedicineEvent
    data class SetComment(val comment: String) : MedicineEvent
    data class PickKit(val kitId: Long) : MedicineEvent
    data object ClearKit : MedicineEvent
    data class SetIcon(val icon: String) : MedicineEvent
    data object ShowKitDialog : MedicineEvent
    data object ShowDatePicker : MedicineEvent
    data object ShowIconPicker : MedicineEvent
    data object ShowDialogDelete : MedicineEvent
    data object ShowDoseMenu : MedicineEvent
}