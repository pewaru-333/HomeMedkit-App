package ru.application.homemedkit.models.viewModels

import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.awaitCancellation
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import ru.application.homemedkit.models.events.Response
import ru.application.homemedkit.models.events.ScannerEvent
import ru.application.homemedkit.models.states.ScannerState
import ru.application.homemedkit.network.Network
import ru.application.homemedkit.utils.di.Database
import ru.application.homemedkit.utils.extensions.asMedicine
import ru.application.homemedkit.utils.getMedicineImages
import java.io.File

class ScannerViewModel : BaseViewModel<ScannerState, Unit>() {
    override fun initState() = ScannerState.Default

    override fun loadData() = Unit

    override fun onEvent(event: Unit) = Unit

    private val dao by lazy { Database.medicineDAO() }
    private val mutex = Mutex()

    private val _event = Channel<ScannerEvent>()
    val event = _event.receiveAsFlow()

    fun fetch(dir: File, code: String) {
        if (currentState != ScannerState.Default) return

        viewModelScope.launch {
            mutex.withLock {
                if (currentState != ScannerState.Default) return@launch

                val duplicateId = dao.getIdByCis(code)
                if (duplicateId != null) {
                    _event.send(ScannerEvent.Navigate(duplicateId, null, true))
                    awaitCancellation()
                }

                updateState { ScannerState.Loading }
                try {
                    when (val response = Network.getMedicine(code)) {
                        is Response.Success -> {
                            val model = response.model

                            if (model.category == "drugs" || model.category == "bio") {
                                val medicine = model.asMedicine().copy(cis = code)

                                val id = dao.insert(medicine)
                                val images = getMedicineImages(
                                    medicineId = id,
                                    form = medicine.prodFormNormName,
                                    directory = dir,
                                    urls = response.model.imageUrls
                                )

                                dao.updateImages(images)

                                _event.send(ScannerEvent.Navigate(id))
                                awaitCancellation()
                            } else {
                                _event.send(ScannerEvent.ShowSnackbar.IncorrectCode)
                            }
                        }

                        is Response.Error.NetworkError -> {
                            updateState { ScannerState.ShowDialog(response.code) }
                        }

                        is Response.Error -> {
                            _event.send(ScannerEvent.ShowSnackbar.UnknownError(response.message))
                        }
                    }
                } catch (_: Exception) {
                    _event.send(ScannerEvent.ShowSnackbar.UnknownError())
                } finally {
                    if (currentState !is ScannerState.ShowDialog) {
                        updateState { ScannerState.Idle }
                    }
                }
            }
        }
    }

    fun setDefault() = updateState { ScannerState.Default }
}