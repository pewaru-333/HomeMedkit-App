package ru.application.homemedkit.models.viewModels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import ru.application.homemedkit.HomeMeds.Companion.database
import ru.application.homemedkit.helpers.Preferences
import ru.application.homemedkit.helpers.Types
import ru.application.homemedkit.helpers.toBio
import ru.application.homemedkit.helpers.toMedicine
import ru.application.homemedkit.models.events.Response
import ru.application.homemedkit.models.states.ScannerState
import ru.application.homemedkit.network.Network
import java.io.File

class ScannerViewModel : ViewModel() {
    private val dao = database.medicineDAO()

    private val _state = MutableStateFlow(ScannerState())
    val state = _state.asStateFlow()

    private val _response = Channel<Response>()
    val response = _response.receiveAsFlow()

    fun fetch(dir: File, code: String) = viewModelScope.launch(Dispatchers.IO) {
        if (!_state.value.doImageAnalysis)
            return@launch

        setLoading()

        dao.getAllCis().filterNot(String::isBlank).find { it in code }?.let {
            _response.send(Response.Success(dao.getIdByCis(it), true))
        } ?: try {
            Network.getMedicine(code).apply {
                if (codeFounded) {
                    if (drugsData != null) {
                        val medicine = drugsData.toMedicine().copy(
                            cis = this.code,
                            image = if (Preferences.getImageFetch()) Network.getImage(dir, drugsData.vidalData?.images)
                            else Types.setIcon(drugsData.foiv.prodFormNormName)
                        )
                        _response.send(Response.Success(dao.add(medicine)))
                    } else if (bioData != null) {
                        val medicine = bioData.toBio().copy(
                            cis = this.code,
                            image = Types.setIcon(bioData.productProperty.releaseForm.orEmpty())
                        )

                        _response.send(Response.Success(dao.add(medicine)))
                    } else showIncorrectCodeError()
                } else showGeneralError()
            }
        } catch (e: Throwable) {
            showNetworkError(code)
        }
    }

    fun setInitial() = viewModelScope.launch { _state.emit(ScannerState()) }

    private fun setLoading() = _state.update {
        it.copy(
            doImageAnalysis = false,
            loading = true
        )
    }

    private suspend fun showIncorrectCodeError() {
        _state.update {
            it.copy(
                loading = false,
                incorrectCode = true
            )
        }
        delay(2500L)
        _state.emit(ScannerState())
    }

    private suspend fun showGeneralError() {
        _state.update {
            it.copy(
                loading = false,
                error = true
            )
        }
        delay(2500L)
        _state.emit(ScannerState())
    }

    private fun showNetworkError(code: String) = _state.update {
        it.copy(
            code = code,
            loading = false,
            noNetwork = true
        )
    }
}