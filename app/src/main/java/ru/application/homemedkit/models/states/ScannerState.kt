package ru.application.homemedkit.models.states

import ru.application.homemedkit.network.models.MainModel
import ru.application.homemedkit.utils.Code

sealed interface ScannerState {
    data object Ready : ScannerState
    data object Paused : ScannerState
    data object Loading : ScannerState
    data object Finished : ScannerState
    data object CheckingDuplicate : ScannerState

    data class DownloadingImages(val completed: Int, val total: Int, val percent: Int? = null) : ScannerState

    data class ConfirmProduct(val code: Code, val model: MainModel) : ScannerState

    data class ShowingDialog(val code: String? = null) : ScannerState
}