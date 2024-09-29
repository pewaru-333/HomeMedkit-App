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
import ru.application.homemedkit.HomeMeds.Companion.database
import ru.application.homemedkit.data.dto.Medicine
import ru.application.homemedkit.data.dto.Technical
import ru.application.homemedkit.helpers.BLANK
import ru.application.homemedkit.helpers.CATEGORY
import ru.application.homemedkit.helpers.DoseTypes
import ru.application.homemedkit.helpers.Types
import ru.application.homemedkit.helpers.toTimestamp
import ru.application.homemedkit.network.NetworkAPI
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
        dao.getById(medicineId)?.let { medicine ->
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

    fun add() {
        val medicine = Medicine(
            kitId = _state.value.kitId,
            cis = _state.value.cis,
            productName = _state.value.productName,
            expDate = _state.value.expDate,
            prodFormNormName = _state.value.prodFormNormName,
            prodDNormName = _state.value.prodDNormName,
            prodAmount = _state.value.prodAmount.ifEmpty { "0.0" }.toDouble(),
            doseType = _state.value.doseType,
            phKinetics = _state.value.phKinetics,
            comment = _state.value.comment,
            image = _state.value.image,
            technical = Technical(
                scanned = _state.value.cis.isNotBlank(),
                verified = _state.value.technical.verified
            )
        )

        viewModelScope.launch {
            val id = dao.add(medicine)
            _state.update { it.copy(adding = false, default = true, id = id) }
            _events.emit(ActivityEvents.Start)
        }
    }

    fun fetch() {
        viewModelScope.launch {
            _response.emit(Loading)

            try {
                NetworkAPI.client.requestData(_state.value.cis).apply {
                    if (category == CATEGORY && codeFounded && checkResult) {
                        val medicine = Medicine(
                            id = _state.value.id,
                            kitId = _state.value.kitId,
                            cis = cis,
                            productName = drugsData.prodDescLabel,
                            expDate = drugsData.expireDate,
                            prodFormNormName = drugsData.foiv.prodFormNormName,
                            prodDNormName = drugsData.foiv.prodDNormName ?: BLANK,
                            prodAmount = drugsData.foiv.prodPack1Size?.let {
                                it.toDouble() * (drugsData.foiv.prodPack12?.toDoubleOrNull() ?: 1.0)
                            } ?: 0.0,
                            phKinetics = drugsData.vidalData.phKinetics ?: BLANK,
                            comment = _state.value.comment.ifEmpty { BLANK },
                            technical = Technical(scanned = true, verified = true)
                        )

                        dao.update(medicine)
                        _response.emit(Success(medicineId))
                        _events.emit(ActivityEvents.Start)
                    } else {
                        _response.emit(Error)
                        delay(2000)
                        _events.emit(ActivityEvents.Start)
                    }
                }
            } catch (e: Throwable) {
                _response.emit(NoNetwork(_state.value.cis))
                delay(2000)
                _events.emit(ActivityEvents.Start)
            }
        }
    }

    fun update() {
        val medicine = Medicine(
            id = _state.value.id,
            kitId = _state.value.kitId,
            cis = _state.value.cis,
            productName = _state.value.productName,
            expDate = _state.value.expDate,
            prodFormNormName = _state.value.prodFormNormName,
            prodDNormName = _state.value.prodDNormName,
            prodAmount = _state.value.prodAmount.ifEmpty { "0.0" }.toDouble(),
            doseType = _state.value.doseType,
            phKinetics = _state.value.phKinetics,
            comment = _state.value.comment,
            image = _state.value.image,
            technical = Technical(
                scanned = _state.value.technical.scanned,
                verified = _state.value.technical.verified
            )
        )

        viewModelScope.launch {
            dao.update(medicine)
            _state.update { it.copy(adding = false, editing = false, default = true) }
        }
    }

    fun delete() = viewModelScope.launch {
        dao.delete(Medicine(id = _state.value.id))
        _events.emit(ActivityEvents.Close)
    }

    fun setAdding() = _state.update { it.copy(adding = true, default = false) }
    fun setEditing() = _state.update { it.copy(editing = true, default = false) }
    fun setCis(cis: String) = _state.update { it.copy(cis = cis) }
    fun setProductName(productName: String) = _state.update { it.copy(productName = productName) }
    fun setFormName(formName: String) = _state.update { it.copy(prodFormNormName = formName) }
    fun setDoseName(doseName: String) = _state.update { it.copy(prodDNormName = doseName) }
    fun setProdAmount(amount: String) = if (amount.isNotEmpty()) {
        if (amount.replace(',', '.').toDoubleOrNull() != null)
            _state.update { it.copy(prodAmount = amount.replace(',', '.')) } else {}
    } else _state.update { it.copy(prodAmount = BLANK) }
    fun setPhKinetics(phKinetics: String) = _state.update { it.copy(phKinetics = phKinetics) }
    fun setComment(comment: String) = _state.update { it.copy(comment = comment) }

    fun showKitDialog() = _state.update { it.copy(showDialogKits = true) }
    fun hideKitDialog() = _state.update { it.copy(showDialogKits = false) }
    fun pickKit(kitId: Long?) = _state.update { it.copy(kitId = kitId) }
    fun setKitId() = _state.update {
        it.copy(
            kitId = _state.value.kitId,
            kitTitle = dao.getKitTitle(_state.value.kitId) ?: BLANK,
            showDialogKits = false
        )
    }

    fun showDatePicker() = _state.update { it.copy(showDialogDate = true) }
    fun hideDatePicker() = _state.update { it.copy(showDialogDate = false) }
    fun setExpDate(month: Int, year: Int) = _state.update {
        it.copy(expDate = toTimestamp(month, year), showDialogDate = false)
    }

    fun showIconPicker() = _state.update { it.copy(showDialogIcons = true) }
    fun hideIconPicker() = _state.update { it.copy(showDialogIcons = false) }
    fun setIcon(icon: String) = _state.update { it.copy(image = icon, showDialogIcons = false) }

    fun showDoseMenu(flag: Boolean) = _state.update { it.copy(showMenuDose = flag) }
    fun hideDoseMenu() = _state.update { it.copy(showMenuDose = false) }
    fun setDoseType(type: DoseTypes) = _state.update {
        it.copy(doseType = type.value, doseTypeE = type, showMenuDose = false)
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
    val expDate: Long = -1L,
    val prodFormNormName: String = BLANK,
    val prodDNormName: String = BLANK,
    val prodAmount: String = BLANK,
    val doseType: String = BLANK,
    val doseTypeE: DoseTypes? = null,
    val phKinetics: String = BLANK,
    val comment: String = BLANK,
    val image: String = Types.entries[Random.nextInt(0, Types.entries.size)].value,
    val technical: TechnicalState = TechnicalState(),
    val showDialogKits: Boolean = false,
    val showDialogDate: Boolean = false,
    val showDialogIcons: Boolean = false,
    val showMenuDose: Boolean = false
)