package ru.application.homemedkit.viewModels

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import ru.application.homemedkit.connectionController.NetworkAPI
import ru.application.homemedkit.connectionController.models.MainModel
import ru.application.homemedkit.databaseController.Medicine
import ru.application.homemedkit.databaseController.MedicineDAO
import ru.application.homemedkit.databaseController.Technical
import ru.application.homemedkit.helpers.BLANK
import ru.application.homemedkit.helpers.CATEGORY
import ru.application.homemedkit.helpers.Preferences

class ScannerViewModel(private val dao: MedicineDAO) : ViewModel() {
    private val _response = MutableStateFlow<ResponseState>(ResponseState.Default)
    val response = _response.asStateFlow()

    fun fetchData(context: Context, code: String) {
        viewModelScope.launch {
            _response.emit(ResponseState.Loading)

            try {
                NetworkAPI.client.requestData(code).apply {
                    if (category == CATEGORY && codeFounded && checkResult) _response.emit(
                        if (code in dao.getAllCIS()) ResponseState.Duplicate(dao.getIDbyCis(code))
                        else ResponseState.Success(dao.add(mapMedicine(this, context)))
                    ) else throwError()
                }
            } catch (e: Throwable) {
                _response.emit(
                    if (code in dao.getAllCIS()) ResponseState.Duplicate(dao.getIDbyCis(code))
                    else ResponseState.NoNetwork(code)
                )
            }
        }
    }

    fun throwError() {
        viewModelScope.launch {
            _response.apply {
                emit(ResponseState.Error)
                delay(2000)
                emit(ResponseState.AfterError)
            }
        }
    }

    private suspend fun mapMedicine(model: MainModel, context: Context) = Medicine(
        cis = model.cis,
        productName = model.drugsData.prodDescLabel,
        expDate = model.drugsData.expireDate,
        prodFormNormName = model.drugsData.foiv.prodFormNormName,
        prodDNormName = model.drugsData.foiv.prodDNormName ?: BLANK,
        prodAmount = model.drugsData.foiv.prodPack1Size?.toDoubleOrNull() ?: 0.0,
        phKinetics = model.drugsData.vidalData.phKinetics ?: BLANK,
        image = getImage(context, model.drugsData.vidalData.images),
        technical = Technical(scanned = true, verified = true)
    )

    private suspend fun getImage(context: Context, url: List<String>?): String {
        if (url.isNullOrEmpty() || !Preferences(context).getDownloadNeeded()) return BLANK
        else try {
            NetworkAPI.client.getImage(url.first()).apply {
                if (isSuccessful) body()?.let { body ->
                    val name = url.first().substringAfterLast("/").substringBefore(".")
                    context.openFileOutput(name, Context.MODE_PRIVATE).use { it.write(body.bytes()) }
                    return name
                } else return BLANK
            }
        } catch (throwable: Throwable) { return BLANK }; return BLANK
    }
}

sealed interface ResponseState {
    data object Default : ResponseState
    data object Loading : ResponseState
    data class Duplicate(val id: Long) : ResponseState
    data class Success(val id: Long) : ResponseState
    data class NoNetwork(val cis: String) : ResponseState
    data object Error: ResponseState
    data object AfterError: ResponseState
}

