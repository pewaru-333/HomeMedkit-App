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
import kotlinx.coroutines.withContext
import ru.application.homemedkit.HomeMeds.Companion.database
import ru.application.homemedkit.data.dto.Image
import ru.application.homemedkit.helpers.enums.Types
import ru.application.homemedkit.helpers.getMedicineImages
import ru.application.homemedkit.helpers.toMedicine
import ru.application.homemedkit.models.events.Response
import ru.application.homemedkit.models.states.ScannerState
import ru.application.homemedkit.network.Network
import java.io.File

class ScannerViewModel : ViewModel() {
    private val dao = database.medicineDAO()

    private val _state = MutableStateFlow(ScannerState())
    val state = _state.asStateFlow()

    private val _response = Channel<Response?>()
    val response = _response.receiveAsFlow()

    fun fetch(dir: File, code: String) {
        viewModelScope.launch {
            if (!_state.value.doImageAnalysis)
                return@launch

            setLoading()

            dao.getAllCis().filterNot(String::isBlank).find { it in code }?.let {
                val duplicateId = dao.getIdByCis(it)
                _response.send(Response.Success(duplicateId, true))

                return@launch
            }

            withContext(Dispatchers.IO) {
                try {
                    Network.getMedicine(code).run {
                        if (!codeFounded) {
                            showGeneralError()

                            return@withContext
                        }

                        drugsData?.let {
                            val medicine = it.toMedicine().copy(cis = this.code)
                            val id = dao.insert(medicine)
                            val images = getMedicineImages(
                                medicineId = id,
                                form = it.foiv.prodFormNormName,
                                directory = dir,
                                urls = it.vidalData?.images
                            )

                            dao.updateImages(*images)

                            _response.send(Response.Success(id))

                            return@withContext
                        }

                        bioData?.let {
                            val medicine = it.toMedicine().copy(cis = this.code)
                            val id = dao.insert(medicine)
                            val image = Image(
                                medicineId = id,
                                image = Types.setIcon(it.productProperty.releaseForm.orEmpty())
                            )

                            dao.addImage(image)

                            _response.send(Response.Success(id))

                            return@withContext
                        }

                        showIncorrectCodeError()
                    }
                } catch (_: Throwable) {
                    showNetworkError(code)
                }
            }
        }
    }

    fun setInitial() {
        viewModelScope.launch {
            _state.update {
                it.copy(
                    doImageAnalysis = true
                )
            }

            _response.send(null)
        }
    }

    private suspend fun setLoading() {
        _state.update {
            it.copy(
                doImageAnalysis = false
            )
        }

        _response.send(Response.Loading)
    }

    private suspend fun showIncorrectCodeError() {
        _response.send(Response.IncorrectCode)

        delay(2500L)

        _state.update {
            it.copy(
                doImageAnalysis = true
            )
        }
    }

    private suspend fun showGeneralError() {
        _response.send(Response.UnknownError)

        delay(2500L)

        _state.update {
            it.copy(
                doImageAnalysis = true
            )
        }
    }

    private suspend fun showNetworkError(code: String) {
        _response.send(Response.NetworkError(code))
    }

    fun showSnackbar(message: String) {
        viewModelScope.launch {
            _state.value.snackbarHostState.showSnackbar(message)
        }
    }
}