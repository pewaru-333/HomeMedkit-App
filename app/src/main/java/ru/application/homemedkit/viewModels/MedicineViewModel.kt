package ru.application.homemedkit.viewModels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import ru.application.homemedkit.connectionController.NetworkAPI
import ru.application.homemedkit.connectionController.models.MainModel
import ru.application.homemedkit.databaseController.Medicine
import ru.application.homemedkit.databaseController.MedicineDAO
import ru.application.homemedkit.databaseController.Technical
import ru.application.homemedkit.helpers.BLANK
import ru.application.homemedkit.helpers.CATEGORY
import ru.application.homemedkit.states.MedicineState
import ru.application.homemedkit.states.TechnicalState

class MedicineViewModel(private val dao: MedicineDAO, private val medicineId: Long) : ViewModel() {
    private val _uiState = MutableStateFlow(MedicineState())
    val uiState = _uiState.asStateFlow()

    private val _response = MutableStateFlow<ResponseState>(ResponseState.Default)
    val response = _response.asStateFlow()

    private val _events = MutableSharedFlow<ActivityEvents>()
    val events = _events.asSharedFlow()

    init {
        viewModelScope.launch {
            dao.getByPK(medicineId)?.let { medicine ->
                _uiState.update {
                    it.copy(
                        adding = false,
                        editing = false,
                        default = true,
                        fetch = ResponseState.Default,
                        id = medicine.id,
                        kitId = medicine.kitId,
                        kitTitle = dao.getKitTitle(medicine.kitId) ?: BLANK,
                        cis = medicine.cis,
                        productName = medicine.productName,
                        expDate = medicine.expDate,
                        prodFormNormName = medicine.prodFormNormName,
                        prodDNormName = medicine.prodDNormName,
                        prodAmount = medicine.prodAmount.toString(),
                        doseType = medicine.doseType,
                        phKinetics = medicine.phKinetics,
                        comment = medicine.comment,
                        image = medicine.image,
                        technical = TechnicalState(
                            scanned = medicine.technical.scanned,
                            verified = medicine.technical.verified
                        )
                    )
                }
            } ?: MedicineState()
        }
    }

    fun onEvent(event: MedicineEvent) {
        when (event) {
            MedicineEvent.Add -> {
                val kitId = _uiState.value.kitId
                val cis = _uiState.value.cis
                val productName = _uiState.value.productName
                val expDate = _uiState.value.expDate
                val prodFormNormName = _uiState.value.prodFormNormName
                val prodDNormName = _uiState.value.prodDNormName
                val prodAmount = _uiState.value.prodAmount.ifEmpty { "0.0" }
                val doseType = _uiState.value.doseType
                val phKinetics = _uiState.value.phKinetics
                val comment = _uiState.value.comment
                val image = _uiState.value.image
                val scanned = cis.isNotBlank()
                val verified = _uiState.value.technical.verified

                val medicine = Medicine(
                    kitId = kitId,
                    cis = cis,
                    productName = productName,
                    expDate = expDate,
                    prodFormNormName = prodFormNormName,
                    prodDNormName = prodDNormName,
                    prodAmount = prodAmount.toDouble(),
                    doseType = doseType,
                    phKinetics = phKinetics,
                    comment = comment,
                    image = image,
                    technical = Technical(
                        scanned = scanned,
                        verified = verified
                    )
                )

                viewModelScope.launch {
                    val id = dao.add(medicine)
                    _uiState.update { it.copy(adding = false, default = true, id = id) }
                    _events.emit(ActivityEvents.Start)
                }
            }

            MedicineEvent.Fetch -> {
                fetchData()
            }

            MedicineEvent.Update -> {
                val id = _uiState.value.id
                val kitId = _uiState.value.kitId
                val cis = _uiState.value.cis
                val productName = _uiState.value.productName
                val expDate = _uiState.value.expDate
                val prodFormNormName = _uiState.value.prodFormNormName
                val prodDNormName = _uiState.value.prodDNormName
                val prodAmount = _uiState.value.prodAmount.ifEmpty { "0.0" }
                val doseType = _uiState.value.doseType
                val phKinetics = _uiState.value.phKinetics
                val comment = _uiState.value.comment
                val image = _uiState.value.image
                val scanned = _uiState.value.technical.scanned
                val verified = _uiState.value.technical.verified

                val medicine = Medicine(
                    id = id,
                    kitId = kitId,
                    cis = cis,
                    productName = productName,
                    expDate = expDate,
                    prodFormNormName = prodFormNormName,
                    prodDNormName = prodDNormName,
                    prodAmount = prodAmount.toDouble(),
                    doseType = doseType,
                    phKinetics = phKinetics,
                    comment = comment,
                    image = image,
                    technical = Technical(
                        scanned = scanned,
                        verified = verified
                    )
                )

                viewModelScope.launch {
                    dao.update(medicine)
                    _uiState.update { it.copy(adding = false, editing = false, default = true) }
                }
            }

            MedicineEvent.Delete -> {
                val medicine = Medicine(id = _uiState.value.id)

                viewModelScope.launch {
                    dao.delete(medicine)
                    _events.emit(ActivityEvents.Close)
                }
            }

            MedicineEvent.SetAdding -> _uiState.update { it.copy(adding = true, default = false) }
            MedicineEvent.SetEditing -> _uiState.update { it.copy(editing = true, default = false) }
            is MedicineEvent.SetMedicineId -> _uiState.update { it.copy(id = event.medicineId) }

            is MedicineEvent.SetKitId -> {
                _uiState.update {
                    it.copy(kitId = event.kitId, kitTitle = dao.getKitTitle(event.kitId) ?: BLANK)
                }
            }

            is MedicineEvent.SetCis -> _uiState.update { it.copy(cis = event.cis) }
            is MedicineEvent.SetProductName -> _uiState.update { it.copy(productName = event.productName) }
            is MedicineEvent.SetExpDate -> _uiState.update { it.copy(expDate = event.expDate) }
            is MedicineEvent.SetProdFormNormName -> _uiState.update { it.copy(prodFormNormName = event.prodFormNormName) }
            is MedicineEvent.SetProdDNormName -> _uiState.update { it.copy(prodDNormName = event.prodDNormName) }

            is MedicineEvent.SetProdAmount -> {
                if (event.prodAmount.isNotEmpty()) {
                    when (event.prodAmount.replace(',', '.').toDoubleOrNull()) {
                        null -> {}
                        else -> _uiState.update {
                            it.copy(prodAmount = event.prodAmount.replace(',', '.'))
                        }
                    }
                } else _uiState.update { it.copy(prodAmount = BLANK) }
            }

            is MedicineEvent.SetDoseType -> _uiState.update { it.copy(doseType = event.doseType) }
            is MedicineEvent.SetPhKinetics -> _uiState.update { it.copy(phKinetics = event.phKinetics) }
            is MedicineEvent.SetComment -> _uiState.update { it.copy(comment = event.comment) }
            is MedicineEvent.SetImage -> _uiState.update { it.copy(image = event.image) }
        }
    }

    private fun fetchData() {
        viewModelScope.launch {
            _response.emit(ResponseState.Loading)

            try {
                NetworkAPI.client.requestData(_uiState.value.cis).apply {
                    when(category == CATEGORY && codeFounded && checkResult) {
                        true -> {
                            dao.update(fetchMedicine())
                            _response.emit(ResponseState.Success(medicineId))
                            _events.emit(ActivityEvents.Start)
                        }

                        false -> {
                            _response.emit(ResponseState.Error)
                            delay(2000)
                            _events.emit(ActivityEvents.Start)
                        }
                    }
                }
            } catch (e: Throwable) {
                _response.emit(ResponseState.NoNetwork(_uiState.value.cis))
                delay(2000)
                _events.emit(ActivityEvents.Start)
            }
        }
    }

    private fun MainModel.fetchMedicine() = Medicine(
        id = _uiState.value.id,
        kitId = _uiState.value.kitId,
        cis = cis,
        productName = drugsData.prodDescLabel,
        expDate = drugsData.expireDate,
        prodFormNormName = drugsData.foiv.prodFormNormName,
        prodDNormName = drugsData.foiv.prodDNormName ?: BLANK,
        prodAmount = drugsData.foiv.prodPack1Size?.toDoubleOrNull() ?: 0.0,
        phKinetics = drugsData.vidalData.phKinetics ?: BLANK,
        comment = _uiState.value.comment.ifEmpty { BLANK },
        technical = Technical(scanned = true, verified = true)
    )
}

sealed interface MedicineEvent {
    data object Add : MedicineEvent
    data object Fetch : MedicineEvent
    data object Update : MedicineEvent
    data object Delete : MedicineEvent
    data class SetMedicineId(val medicineId: Long) : MedicineEvent
    data class SetKitId(val kitId: Long?) : MedicineEvent
    data class SetCis(val cis: String) : MedicineEvent
    data class SetProductName(val productName: String) : MedicineEvent
    data class SetExpDate(val expDate: Long) : MedicineEvent
    data class SetProdFormNormName(val prodFormNormName: String) : MedicineEvent
    data class SetProdDNormName(val prodDNormName: String) : MedicineEvent
    data class SetProdAmount(val prodAmount: String) : MedicineEvent
    data class SetDoseType(val doseType: String) : MedicineEvent
    data class SetPhKinetics(val phKinetics: String) : MedicineEvent
    data class SetComment(val comment: String) : MedicineEvent
    data class SetImage(val image: String) : MedicineEvent
    data object SetAdding : MedicineEvent
    data object SetEditing : MedicineEvent
}

sealed interface ActivityEvents {
    data object Start : ActivityEvents
    data object Close : ActivityEvents
}