package ru.application.homemedkit.models.viewModels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import ru.application.homemedkit.HomeMeds.Companion.database
import ru.application.homemedkit.data.dto.Medicine
import ru.application.homemedkit.data.dto.Technical
import ru.application.homemedkit.helpers.BLANK
import ru.application.homemedkit.helpers.CATEGORY
import ru.application.homemedkit.helpers.DoseTypes
import ru.application.homemedkit.helpers.toMedicine
import ru.application.homemedkit.helpers.toTimestamp
import ru.application.homemedkit.models.events.Response
import ru.application.homemedkit.models.events.Response.Default
import ru.application.homemedkit.models.events.Response.Error
import ru.application.homemedkit.models.events.Response.Loading
import ru.application.homemedkit.models.events.Response.NoNetwork
import ru.application.homemedkit.models.events.Response.Success
import ru.application.homemedkit.models.states.MedicineState
import ru.application.homemedkit.models.states.TechnicalState
import ru.application.homemedkit.network.Network
import java.io.File

class MedicineViewModel(private val medicineId: Long) : ViewModel() {
    private val dao = database.medicineDAO()

    private val _response = MutableStateFlow<Response>(Default)
    val response = _response.asStateFlow()

    private val _state = MutableStateFlow(MedicineState())
    val state = _state.asStateFlow()
        .onStart {
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
            }
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(), MedicineState())

    fun add() = viewModelScope.launch(Dispatchers.IO) {
        val id = dao.add(_state.value.toMedicine())
        _state.update { it.copy(adding = false, default = true, id = id) }
    }

    fun fetch() {
        viewModelScope.launch(Dispatchers.IO) {
            _response.emit(Loading)

            try {
                Network.client.requestData(_state.value.cis).apply {
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
                    } else {
                        _response.emit(Error)
                        delay(2000)
                        _response.emit(Default)
                    }
                }
            } catch (e: Throwable) {
                _response.emit(NoNetwork(_state.value.cis))
                delay(2000)
                _response.emit(Default)
            }
        }
    }

    fun update() = viewModelScope.launch {
        dao.update(_state.value.toMedicine())
        _state.update { it.copy(adding = false, editing = false, default = true) }
    }

    fun delete(dir: File) = viewModelScope.launch(Dispatchers.IO) {
        dao.delete(_state.value.toMedicine())
    }.invokeOnCompletion { File(dir, _state.value.image).delete() }

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
}