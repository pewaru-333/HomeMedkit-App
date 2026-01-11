package ru.application.homemedkit.models.viewModels

import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import ru.application.homemedkit.models.events.Response
import ru.application.homemedkit.network.Network
import ru.application.homemedkit.utils.di.Database
import ru.application.homemedkit.utils.extensions.toMedicine
import ru.application.homemedkit.utils.getMedicineImages
import java.io.File

class ScannerViewModel : BaseViewModel<Response, Unit>() {
    private val dao by lazy { Database.medicineDAO() }
    private val mutex = Mutex()

    override fun initState() = Response.Initial

    override fun loadData() = Unit

    override fun onEvent(event: Unit) = Unit

    fun fetch(dir: File, code: String) {
        viewModelScope.launch {
            mutex.withLock {
                val duplicateId = dao.getIdByCis(code)
                if (duplicateId != null) {
                    updateState { Response.Navigate(duplicateId, true) }
                    return@launch
                }

                try {
                    updateState { Response.Loading }

                    when (val response = Network.getMedicine(code)) {
                        is Response.Error -> updateState { response }

                        is Response.Success -> {
                            val medicine = response.model.run {
                                drugsData?.toMedicine() ?: bioData?.toMedicine() ?: toMedicine()
                            }.copy(
                                cis = code
                            )

                            val id = dao.insert(medicine)
                            val images = getMedicineImages(
                                medicineId = id,
                                form = medicine.prodFormNormName,
                                directory = dir,
                                urls = response.model.imageUrls
                            )

                            dao.updateImages(images)
                            updateState { Response.Navigate(id) }
                        }

                        else -> Unit
                    }
                } catch (_: Exception) {
                    updateState { Response.Error.UnknownError }
                }
            }
        }
    }

    fun setInitial() = updateState { Response.Initial }
}