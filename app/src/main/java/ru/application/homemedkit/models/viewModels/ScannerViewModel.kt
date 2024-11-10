package ru.application.homemedkit.models.viewModels

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import ru.application.homemedkit.HomeMeds.Companion.database
import ru.application.homemedkit.data.dto.Medicine
import ru.application.homemedkit.data.dto.Technical
import ru.application.homemedkit.helpers.BLANK
import ru.application.homemedkit.helpers.CATEGORY
import ru.application.homemedkit.helpers.Preferences
import ru.application.homemedkit.models.events.Response
import ru.application.homemedkit.models.events.Response.Default
import ru.application.homemedkit.models.events.Response.Duplicate
import ru.application.homemedkit.models.events.Response.Error
import ru.application.homemedkit.models.events.Response.Loading
import ru.application.homemedkit.models.events.Response.NoNetwork
import ru.application.homemedkit.models.events.Response.Success
import ru.application.homemedkit.network.Network
import ru.application.homemedkit.network.models.MainModel

class ScannerViewModel : ViewModel() {
    private val dao = database.medicineDAO()

    private val _response = MutableStateFlow<Response>(Default)
    val response = _response.asStateFlow()

    fun fetchData(context: Context, code: String) {
        viewModelScope.launch(Dispatchers.IO) {
            _response.emit(Loading)

            try {
                Network.client.requestData(code).apply {
                    if (category == CATEGORY && codeFounded && checkResult) _response.emit(
                        if (code in dao.getAllCis()) Duplicate(dao.getIdByCis(code))
                        else Success(dao.add(mapMedicine(this, context)))
                    ) else throwError()
                }
            } catch (e: Throwable) {
                _response.emit(
                    if (code in dao.getAllCis()) Duplicate(dao.getIdByCis(code))
                    else NoNetwork(code)
                )
            }
        }
    }

    private fun throwError() = viewModelScope.launch {
        _response.apply {
            emit(Error)
            delay(2500)
            emit(Default)
        }
    }

    fun setDefault() = viewModelScope.launch { _response.emit(Default) }

    private suspend fun mapMedicine(model: MainModel, context: Context) = Medicine(
        cis = model.cis,
        productName = model.drugsData.prodDescLabel,
        expDate = model.drugsData.expireDate,
        prodFormNormName = model.drugsData.foiv.prodFormNormName,
        prodDNormName = model.drugsData.foiv.prodDNormName ?: BLANK,
        prodAmount = model.drugsData.foiv.prodPack1Size?.let {
            it.toDouble() * (model.drugsData.foiv.prodPack12?.toDoubleOrNull() ?: 1.0)
        } ?: 0.0,
        phKinetics = model.drugsData.vidalData.phKinetics ?: BLANK,
        image = getImage(context, model.drugsData.vidalData.images),
        technical = Technical(scanned = true, verified = true)
    )

    private suspend fun getImage(context: Context, url: List<String>?): String {
        if (url.isNullOrEmpty() || !Preferences.getDownloadNeeded()) return BLANK
        else try {
            Network.client.getImage(url.first()).apply {
                if (isSuccessful) body()?.let { body ->
                    val name = url.first().substringAfterLast("/").substringBefore(".")
                    context.openFileOutput(name, Context.MODE_PRIVATE)
                        .use { it.write(body.bytes()) }
                    return name
                } else return BLANK
            }
        } catch (throwable: Throwable) {
            return BLANK
        }
        return BLANK
    }
}