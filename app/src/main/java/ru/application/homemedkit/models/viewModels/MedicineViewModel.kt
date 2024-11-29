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
import ru.application.homemedkit.helpers.BLANK
import ru.application.homemedkit.helpers.DoseTypes
import ru.application.homemedkit.helpers.Preferences
import ru.application.homemedkit.helpers.Types
import ru.application.homemedkit.helpers.toBio
import ru.application.homemedkit.helpers.toMedicine
import ru.application.homemedkit.helpers.toState
import ru.application.homemedkit.helpers.toTimestamp
import ru.application.homemedkit.models.events.Response
import ru.application.homemedkit.models.events.Response.Default
import ru.application.homemedkit.models.events.Response.Error
import ru.application.homemedkit.models.events.Response.IncorrectCode
import ru.application.homemedkit.models.events.Response.Loading
import ru.application.homemedkit.models.events.Response.NoNetwork
import ru.application.homemedkit.models.states.MedicineState
import ru.application.homemedkit.network.Network
import java.io.File

class MedicineViewModel(private val medicineId: Long) : ViewModel() {
    private val dao = database.medicineDAO()

    private val _response = MutableStateFlow<Response>(Default)
    val response = _response.asStateFlow()

    private val _state = MutableStateFlow(MedicineState())
    val state = _state.asStateFlow()
        .onStart { dao.getById(medicineId)?.let { _state.value = it.toState() } }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000L), MedicineState())

    fun add() = viewModelScope.launch(Dispatchers.IO) {
        val id = dao.add(_state.value.toMedicine())
        _state.update { it.copy(adding = false, default = true, id = id) }
    }

    fun fetch(dir: File) = viewModelScope.launch(Dispatchers.IO) {
        _response.emit(Loading)

        try {
            Network.getMedicine(_state.value.cis).apply {
                if (codeFounded && checkResult) {
                    if (drugsData != null) {
                        val medicine = drugsData.toMedicine().copy(
                            id = _state.value.id,
                            cis = _state.value.cis,
                            kitId = _state.value.kitId,
                            comment = _state.value.comment.ifEmpty { BLANK },
                            image = if (Preferences.getImageFetch()) Network.getImage(dir, drugsData.vidalData?.images)
                            else Types.setIcon(drugsData.foiv.prodFormNormName)
                        )

                        dao.update(medicine)
                        _state.value = medicine.toState()
                        _response.emit(Default)
                    } else if (bioData != null) {
                        val medicine = bioData.toBio().copy(
                            id = _state.value.id,
                            cis = _state.value.cis,
                            kitId = _state.value.kitId,
                            comment = _state.value.comment.ifEmpty { BLANK }
                        )

                        dao.update(medicine)
                        _state.value = medicine.toState()
                        _response.emit(Default)
                    } else _response.apply {
                        emit(IncorrectCode)
                        delay(2500L)
                        emit(Default)
                    }
                } else _response.apply {
                    emit(Error)
                    delay(2500L)
                    emit(Default)
                }
            }
        } catch (e: Throwable) {
            _response.apply {
                emit(NoNetwork(_state.value.cis))
                delay(2500L)
                emit(Default)
            }
        }
    }

    fun update() = viewModelScope.launch(Dispatchers.IO) {
        dao.update(_state.value.toMedicine())
        _state.update { it.copy(adding = false, editing = false, default = true) }
    }

    fun delete(dir: File) = viewModelScope.launch(Dispatchers.IO) {
        dao.delete(_state.value.toMedicine())
    }.invokeOnCompletion { File(dir, _state.value.image).delete() }

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
    fun clearKit() = _state.update { it.copy(kitId = null, kitTitle = BLANK, showDialogKits = false) }
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