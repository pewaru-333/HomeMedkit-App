package ru.application.homemedkit.models.states

sealed interface MedicineDialogState {
    object DataLoss : MedicineDialogState

    object Kits : MedicineDialogState

    object Icons : MedicineDialogState
    object PictureGrid : MedicineDialogState
    object PictureChoose : MedicineDialogState
    data class FullImage(val page: Int = 0) : MedicineDialogState

    object TakePhoto : MedicineDialogState

    object Date : MedicineDialogState
    object PackageDate : MedicineDialogState

    object Delete : MedicineDialogState

    companion object {
        fun getPage(state: MedicineDialogState?) = if (state is FullImage) state.page
        else -1
    }
}