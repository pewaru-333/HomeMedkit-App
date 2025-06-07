package ru.application.homemedkit.models.viewModels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.awaitCancellation
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import ru.application.homemedkit.HomeMeds.Companion.database
import ru.application.homemedkit.utils.extensions.toMedicine
import ru.application.homemedkit.utils.getMedicineImages
import ru.application.homemedkit.models.events.Response
import ru.application.homemedkit.network.Network
import java.io.File

class ScannerViewModel : ViewModel() {
    private val dao = database.medicineDAO()
    private val mutex = Mutex()

    private val _response = Channel<Response>()
    val response = _response.receiveAsFlow()

    fun fetch(dir: File, code: String) {
        viewModelScope.launch {
            try {
                mutex.withLock(this@ScannerViewModel) {
                    dao.getIdByCis(code)?.let { duplicateId ->
                        _response.send(Response.Navigate(duplicateId, true))

                        awaitCancellation()
                    }

                    try {
                        _response.send(Response.Loading)

                        val response = withContext(Dispatchers.IO) {
                            Network.getMedicine(code)
                        }

                        when (val data = response) {
                            is Response.Error -> {
                                _response.send(data)
                                delay(2500L)
                            }

                            is Response.Success -> {
                                val medicine = data.model.run {
                                    drugsData?.toMedicine() ?: bioData?.toMedicine()
                                }?.copy(
                                    cis = code
                                )

                                if (medicine == null) {
                                    _response.send(Response.Error.UnknownError)
                                } else {
                                    val id = dao.insert(medicine)
                                    val images = getMedicineImages(
                                        medicineId = id,
                                        form = medicine.prodFormNormName,
                                        directory = dir,
                                        urls = data.model.drugsData?.vidalData?.images
                                    )

                                    dao.updateImages(images)
                                    _response.send(Response.Navigate(id))
                                }
                            }

                            else -> Unit
                        }
                    } catch (_: Throwable) {
                        _response.send(Response.Error.UnknownError)
                        delay(2500L)
                    }
                }
            } catch (_: IllegalStateException) {

            }
        }
    }

    fun setInitial() {
        viewModelScope.launch {
            _response.send(Response.Initial)
        }
    }
}