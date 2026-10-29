package ru.application.homemedkit.models.viewModels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.*
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import ru.application.homemedkit.models.events.Response
import ru.application.homemedkit.models.events.ScannerEvent
import ru.application.homemedkit.models.states.ScannerState
import ru.application.homemedkit.network.Network
import ru.application.homemedkit.network.models.MainModel
import ru.application.homemedkit.utils.Code
import ru.application.homemedkit.utils.di.Database
import ru.application.homemedkit.utils.extensions.asMedicine
import ru.application.homemedkit.utils.getMedicineImages
import java.io.File

class ScannerViewModel : ViewModel() {
    val state: StateFlow<ScannerState>
        field = MutableStateFlow<ScannerState>(ScannerState.Ready)

    private val _event = Channel<ScannerEvent>(Channel.BUFFERED)
    val event = _event.receiveAsFlow()

    fun fetch(dir: File, code: Code?) {
        if (!state.compareAndSet(ScannerState.Ready, ScannerState.CheckingDuplicate)) {
            return
        }

        search {
            if (code == null) {
                showError(ScannerEvent.ShowSnackbar.IncorrectCode)
                return@search
            }

            val duplicate = Database.medicineDAO().findDuplicate(code.identity)
            if (duplicate != null) {
                navigate(duplicate, true)
                return@search
            }

            state.value = ScannerState.Loading
            when (val response = Network.getMedicine(code)) {
                is Response.Success -> {
                    val model = response.model
                    if (!model.isValidCategory && !(code.isEan13 && model.category == "gtin")) {
                        showError(ScannerEvent.ShowSnackbar.IncorrectCode)
                    } else if (code.isEan13) {
                        state.value = ScannerState.ConfirmProduct(code, model)
                    } else {
                        save(dir, code, model)
                    }
                }
                is Response.Error.NetworkError -> state.value = ScannerState.ShowingDialog(code.value)
                is Response.Error -> showError(ScannerEvent.ShowSnackbar.UnknownError(response.message))
            }
        }
    }

    fun confirmAddition(dir: File) {
        val product = state.value as? ScannerState.ConfirmProduct ?: return
        if (!state.compareAndSet(product, ScannerState.Loading)) return

        search {
            save(dir, product.code, product.model)
        }
    }

    private fun search(block: suspend () -> Unit) {
        viewModelScope.launch {
            try {
                block()
            } catch (e: CancellationException) {
                throw e
            } catch (_: Exception) {
                showError(ScannerEvent.ShowSnackbar.UnknownError())
            } finally {
                state.update {
                    when (it) {
                        ScannerState.CheckingDuplicate, ScannerState.Loading, is ScannerState.DownloadingImages -> ScannerState.Ready
                        else -> it
                    }
                }
            }
        }
    }

    private suspend fun save(dir: File, code: Code, model: MainModel) {
        val medicine = withContext(Dispatchers.Default) {
            model.asMedicine().copy(
                cis = code.value,
                verified = !code.isEan13 && model.isValidCategory
            )
        }
        val images = getMedicineImages(
            medicineId = 0L,
            form = medicine.prodFormNormName,
            directory = dir,
            urls = model.imageUrls,
            onProgress = { completed, total, percent ->
                state.value = ScannerState.DownloadingImages(completed, total, percent)
            }
        )

        val id = Database.medicineDAO().insertScanned(medicine, images)
        navigate(id, false)
    }

    private suspend fun showError(error: ScannerEvent.ShowSnackbar) {
        state.value = ScannerState.Paused
        _event.send(error)
    }

    private suspend fun navigate(id: Long, duplicate: Boolean) {
        state.value = ScannerState.Finished
        _event.send(ScannerEvent.Navigate(id, duplicate))
    }

    fun setDefault() = state.update {
        when (it) {
            ScannerState.Paused, is ScannerState.ShowingDialog, is ScannerState.ConfirmProduct -> ScannerState.Ready
            else -> it
        }
    }

    fun cancel() = viewModelScope.coroutineContext.cancelChildren()
}