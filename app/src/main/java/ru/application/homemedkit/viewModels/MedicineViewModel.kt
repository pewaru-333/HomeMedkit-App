package ru.application.homemedkit.viewModels

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import ru.application.homemedkit.connectionController.NetworkCall
import ru.application.homemedkit.connectionController.NetworkClient
import ru.application.homemedkit.connectionController.RequestUpdate
import ru.application.homemedkit.databaseController.Medicine
import ru.application.homemedkit.databaseController.MedicineDatabase
import ru.application.homemedkit.databaseController.Technical
import ru.application.homemedkit.helpers.ConstantsHelper.BLANK


sealed interface ResponseUiState {
    data object Default : ResponseUiState
    data object Success : ResponseUiState
    data object Loading : ResponseUiState
    enum class Errors : ResponseUiState {
        WRONG_CODE_CATEGORY, WRONG_CATEGORY, CODE_NOT_FOUND, FETCH_ERROR, NO_NETWORK
    }
}

class MedicineViewModel(val database: MedicineDatabase, medicineId: Long) : ViewModel() {
    private val _uiState = MutableStateFlow(Medicine())
    private val uiState = _uiState.asStateFlow()

    init {
        if (medicineId != 0L) _uiState.value = database.medicineDAO().getByPK(medicineId)
    }

    var responseUiState: ResponseUiState by mutableStateOf(ResponseUiState.Default)

    var add by mutableStateOf(false)
        private set

    var edit by mutableStateOf(false)
        private set

    var id by mutableLongStateOf(0L)
    var cis: String? = uiState.value.cis

    var productName: String by mutableStateOf(uiState.value.productName)
        private set

    var expDate: Long by mutableLongStateOf(uiState.value.expDate)
        private set

    var prodFormNormName: String by mutableStateOf(uiState.value.prodFormNormName)
        private set

    var prodDNormName: String by mutableStateOf(uiState.value.prodDNormName)
        private set

    var prodAmount by mutableStateOf(
        when (uiState.value.prodAmount) {
            -1.0 -> BLANK
            else -> uiState.value.prodAmount.toString()
        }
    )
        private set

    var phKinetics: String by mutableStateOf(
        when (uiState.value.phKinetics) {
            null -> BLANK
            else -> uiState.value.phKinetics
        }
    )
        private set

    var comment: String by mutableStateOf(
        when (uiState.value.comment) {
            null -> BLANK
            else -> uiState.value.comment
        }
    )
        private set

    var technical by mutableStateOf(
        Technical(
            uiState.value.technical.scanned,
            uiState.value.technical.verified
        )
    )
        private set


    fun setAdding(flag: Boolean) {
        add = flag
    }

    fun setEditing(flag: Boolean) {
        edit = flag
    }

    fun updateProductName(text: String) {
        productName = text
    }

    fun updateExpDate(milli: Long) {
        expDate = milli
    }

    fun updateFormName(text: String) {
        prodFormNormName = text
    }

    fun updateNormName(text: String) {
        prodDNormName = text
    }

    fun updateProdAmount(amount: String) {
        if (amount.isNotEmpty()) {
            when (amount.replace(',', '.').toDoubleOrNull()) {
                null -> {}
                else -> prodAmount = amount.trim()
            }
        } else prodAmount = amount
    }

    fun updatePhKinetics(text: String) {
        phKinetics = text
    }

    fun updateComment(text: String) {
        comment = text
    }

    fun fetchData() {
        viewModelScope.launch {
            responseUiState = ResponseUiState.Loading

            NetworkClient.getInstance()
                .create(NetworkCall::class.java)
                .requestInfo(cis)
                .enqueue(RequestUpdate(database, this@MedicineViewModel))
        }
    }
}