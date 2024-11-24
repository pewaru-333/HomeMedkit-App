package ru.application.homemedkit.models.viewModels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import ru.application.homemedkit.HomeMeds.Companion.database
import ru.application.homemedkit.helpers.BLANK
import ru.application.homemedkit.helpers.Preferences
import ru.application.homemedkit.helpers.toBio
import ru.application.homemedkit.helpers.toMedicine
import ru.application.homemedkit.models.events.Response
import ru.application.homemedkit.models.events.Response.Default
import ru.application.homemedkit.models.events.Response.Duplicate
import ru.application.homemedkit.models.events.Response.Error
import ru.application.homemedkit.models.events.Response.IncorrectCode
import ru.application.homemedkit.models.events.Response.Loading
import ru.application.homemedkit.models.events.Response.NoNetwork
import ru.application.homemedkit.models.events.Response.Success
import ru.application.homemedkit.network.Network
import java.io.File

class ScannerViewModel : ViewModel() {
    private val dao = database.medicineDAO()

    private val _response = MutableStateFlow<Response>(Default)
    val response = _response.asStateFlow()

    fun fetch(dir: File, code: String) = viewModelScope.launch(Dispatchers.IO) {
        _response.emit(Loading)

        if (code in dao.getAllCis()) _response.emit(Duplicate(dao.getIdByCis(code)))
        else try {
            Network.getMedicine(code).apply {
                if (codeFounded && checkResult) {
                    if (drugsData != null) {
                        val medicine = drugsData.toMedicine().copy(
                            cis = this.code,
                            image = if (Preferences.getDownloadNeeded())
                                Network.getImage(dir, drugsData.vidalData?.images) else BLANK
                        )
                        _response.emit(Success(dao.add(medicine)))
                    } else if (bioData != null) {
                        val medicine = bioData.toBio().copy(cis = this.code)

                        _response.emit(Success(dao.add(medicine)))
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
            _response.emit(NoNetwork(code))
        }
    }

    fun setDefault() = viewModelScope.launch { _response.emit(Default) }
}