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
import ru.application.homemedkit.activities.HomeMeds.Companion.database
import ru.application.homemedkit.connectionController.NetworkAPI
import ru.application.homemedkit.connectionController.models.MainModel
import ru.application.homemedkit.databaseController.Medicine
import ru.application.homemedkit.databaseController.Technical
import ru.application.homemedkit.helpers.BLANK
import ru.application.homemedkit.helpers.CATEGORY
import ru.application.homemedkit.helpers.ICONS_MED
import ru.application.homemedkit.viewModels.MedicineViewModel.Event.Add
import ru.application.homemedkit.viewModels.MedicineViewModel.Event.Delete
import ru.application.homemedkit.viewModels.MedicineViewModel.Event.Fetch
import ru.application.homemedkit.viewModels.MedicineViewModel.Event.SetAdding
import ru.application.homemedkit.viewModels.MedicineViewModel.Event.SetCis
import ru.application.homemedkit.viewModels.MedicineViewModel.Event.SetComment
import ru.application.homemedkit.viewModels.MedicineViewModel.Event.SetDoseType
import ru.application.homemedkit.viewModels.MedicineViewModel.Event.SetEditing
import ru.application.homemedkit.viewModels.MedicineViewModel.Event.SetExpDate
import ru.application.homemedkit.viewModels.MedicineViewModel.Event.SetId
import ru.application.homemedkit.viewModels.MedicineViewModel.Event.SetImage
import ru.application.homemedkit.viewModels.MedicineViewModel.Event.SetKitId
import ru.application.homemedkit.viewModels.MedicineViewModel.Event.SetPhKinetics
import ru.application.homemedkit.viewModels.MedicineViewModel.Event.SetProdAmount
import ru.application.homemedkit.viewModels.MedicineViewModel.Event.SetProdDNormName
import ru.application.homemedkit.viewModels.MedicineViewModel.Event.SetProdFormNormName
import ru.application.homemedkit.viewModels.MedicineViewModel.Event.SetProductName
import ru.application.homemedkit.viewModels.MedicineViewModel.Event.Update
import ru.application.homemedkit.viewModels.ScannerViewModel.Response.Default
import ru.application.homemedkit.viewModels.ScannerViewModel.Response.Error
import ru.application.homemedkit.viewModels.ScannerViewModel.Response.Loading
import ru.application.homemedkit.viewModels.ScannerViewModel.Response.NoNetwork
import ru.application.homemedkit.viewModels.ScannerViewModel.Response.Success
import kotlin.random.Random

class MedicineViewModel(private val medicineId: Long) : ViewModel() {
    private val dao = database.medicineDAO()

    private val _state = MutableStateFlow(MedicineState())
    val state = _state.asStateFlow()

    private val _response = MutableStateFlow<ScannerViewModel.Response>(Default)
    val response = _response.asStateFlow()

    private val _events = MutableSharedFlow<ActivityEvents>()
    val events = _events.asSharedFlow()

    init {
        viewModelScope.launch {
            dao.getByPK(medicineId)?.let { medicine ->
                _state.update {
                    it.copy(
                        adding = false,
                        editing = false,
                        default = true,
                        fetch = Default,
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

    fun onEvent(event: Event) {
        when (event) {
            Add -> {
                val kitId = _state.value.kitId
                val cis = _state.value.cis
                val productName = _state.value.productName
                val expDate = _state.value.expDate
                val prodFormNormName = _state.value.prodFormNormName
                val prodDNormName = _state.value.prodDNormName
                val prodAmount = _state.value.prodAmount.ifEmpty { "0.0" }
                val doseType = _state.value.doseType
                val phKinetics = _state.value.phKinetics
                val comment = _state.value.comment
                val image = _state.value.image
                val scanned = cis.isNotBlank()
                val verified = _state.value.technical.verified

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
                    _state.update { it.copy(adding = false, default = true, id = id) }
                    _events.emit(ActivityEvents.Start)
                }
            }

            Fetch -> fetchData()

            Update -> {
                val id = _state.value.id
                val kitId = _state.value.kitId
                val cis = _state.value.cis
                val productName = _state.value.productName
                val expDate = _state.value.expDate
                val prodFormNormName = _state.value.prodFormNormName
                val prodDNormName = _state.value.prodDNormName
                val prodAmount = _state.value.prodAmount.ifEmpty { "0.0" }
                val doseType = _state.value.doseType
                val phKinetics = _state.value.phKinetics
                val comment = _state.value.comment
                val image = _state.value.image
                val scanned = _state.value.technical.scanned
                val verified = _state.value.technical.verified

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
                    _state.update { it.copy(adding = false, editing = false, default = true) }
                }
            }

            Delete -> {
                val medicine = Medicine(id = _state.value.id)

                viewModelScope.launch {
                    dao.delete(medicine)
                    _events.emit(ActivityEvents.Close)
                }
            }

            SetAdding -> _state.update { it.copy(adding = true, default = false) }
            SetEditing -> _state.update { it.copy(editing = true, default = false) }
            is SetId -> _state.update { it.copy(id = event.medicineId) }

            is SetKitId -> {
                _state.update {
                    it.copy(kitId = event.kitId, kitTitle = dao.getKitTitle(event.kitId) ?: BLANK)
                }
            }

            is SetCis -> _state.update { it.copy(cis = event.cis) }
            is SetProductName -> _state.update { it.copy(productName = event.productName) }
            is SetExpDate -> _state.update { it.copy(expDate = event.expDate) }
            is SetProdFormNormName -> _state.update { it.copy(prodFormNormName = event.prodFormNormName) }
            is SetProdDNormName -> _state.update { it.copy(prodDNormName = event.prodDNormName) }

            is SetProdAmount -> {
                if (event.prodAmount.isNotEmpty()) {
                    when (event.prodAmount.replace(',', '.').toDoubleOrNull()) {
                        null -> {}
                        else -> _state.update {
                            it.copy(prodAmount = event.prodAmount.replace(',', '.'))
                        }
                    }
                } else _state.update { it.copy(prodAmount = BLANK) }
            }

            is SetDoseType -> _state.update { it.copy(doseType = event.doseType) }
            is SetPhKinetics -> _state.update { it.copy(phKinetics = event.phKinetics) }
            is SetComment -> _state.update { it.copy(comment = event.comment) }
            is SetImage -> _state.update { it.copy(image = event.image) }
        }
    }

    private fun fetchData() {
        viewModelScope.launch {
            _response.emit(Loading)

            try {
                NetworkAPI.client.requestData(_state.value.cis).apply {
                    when(category == CATEGORY && codeFounded && checkResult) {
                        true -> {
                            dao.update(fetchMedicine())
                            _response.emit(Success(medicineId))
                            _events.emit(ActivityEvents.Start)
                        }

                        false -> {
                            _response.emit(Error)
                            delay(2000)
                            _events.emit(ActivityEvents.Start)
                        }
                    }
                }
            } catch (e: Throwable) {
                _response.emit(NoNetwork(_state.value.cis))
                delay(2000)
                _events.emit(ActivityEvents.Start)
            }
        }
    }

    private fun MainModel.fetchMedicine() = Medicine(
        id = _state.value.id,
        kitId = _state.value.kitId,
        cis = cis,
        productName = drugsData.prodDescLabel,
        expDate = drugsData.expireDate,
        prodFormNormName = drugsData.foiv.prodFormNormName,
        prodDNormName = drugsData.foiv.prodDNormName ?: BLANK,
        prodAmount = drugsData.foiv.prodPack1Size?.toDoubleOrNull() ?: 0.0,
        phKinetics = drugsData.vidalData.phKinetics ?: BLANK,
        comment = _state.value.comment.ifEmpty { BLANK },
        technical = Technical(scanned = true, verified = true)
    )

    sealed interface Event {
        data object Add : Event
        data object Fetch : Event
        data object Update : Event
        data object Delete : Event
        data class SetId(val medicineId: Long) : Event
        data class SetKitId(val kitId: Long?) : Event
        data class SetCis(val cis: String) : Event
        data class SetProductName(val productName: String) : Event
        data class SetExpDate(val expDate: Long) : Event
        data class SetProdFormNormName(val prodFormNormName: String) : Event
        data class SetProdDNormName(val prodDNormName: String) : Event
        data class SetProdAmount(val prodAmount: String) : Event
        data class SetDoseType(val doseType: String) : Event
        data class SetPhKinetics(val phKinetics: String) : Event
        data class SetComment(val comment: String) : Event
        data class SetImage(val image: String) : Event
        data object SetAdding : Event
        data object SetEditing : Event
    }

    sealed interface ActivityEvents {
        data object Start : ActivityEvents
        data object Close : ActivityEvents
    }
}

data class MedicineState(
    val adding: Boolean = true,
    val editing: Boolean = false,
    val default: Boolean = false,
    val fetch: ScannerViewModel.Response = Default,
    val id: Long = 0L,
    val kitId: Long? = null,
    val kitTitle: String = BLANK,
    val cis: String = BLANK,
    val productName: String = BLANK,
    val expDate: Long = 0L,
    val prodFormNormName: String = BLANK,
    val prodDNormName: String = BLANK,
    val prodAmount: String = BLANK,
    val doseType: String = BLANK,
    val phKinetics: String = BLANK,
    val comment: String = BLANK,
    val image: String = ICONS_MED.keys.elementAt(Random.nextInt(0, ICONS_MED.keys.size)),
    val technical: TechnicalState = TechnicalState()
)