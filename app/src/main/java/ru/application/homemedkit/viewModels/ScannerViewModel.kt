package ru.application.homemedkit.viewModels

import android.content.Context
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import ru.application.homemedkit.connectionController.NetworkAPI
import ru.application.homemedkit.databaseController.Medicine
import ru.application.homemedkit.databaseController.MedicineDAO
import ru.application.homemedkit.databaseController.Technical
import ru.application.homemedkit.fragments.FragmentSettings
import ru.application.homemedkit.helpers.BLANK
import ru.application.homemedkit.helpers.CATEGORY
import ru.application.homemedkit.states.MedicineState

class ScannerViewModel(private val dao: MedicineDAO) : ViewModel() {
    private val _uiState = MutableStateFlow(MedicineState())
    val uiState = _uiState.asStateFlow()

    private val _response = MutableSharedFlow<ResponseState>()
    val response = _response.asSharedFlow()

    var alert by mutableStateOf(false)
    var show by mutableStateOf(false)

    fun fetchData(context: Context, code: String) {
        viewModelScope.launch {
            _response.emit(ResponseState.Loading)

            try {
                NetworkAPI.client.requestData(code).apply {
                    if (isSuccessful) {
                        body()?.let { body ->
                            body.category?.let { category ->
                                if (category == CATEGORY) {
                                    if (body.codeFounded && body.checkResult) {
                                        if (dao.getAllCIS().contains(code)) {
                                            val id = dao.getIDbyCis(code)
                                            _uiState.update { it.copy(id = id) }
                                            _response.emit(ResponseState.Duplicate)
                                        } else {
                                            val medicine = Medicine(
                                                cis = body.cis,
                                                productName = body.drugsData.prodDescLabel,
                                                expDate = body.drugsData.expireDate,
                                                prodFormNormName = body.drugsData.foiv.prodFormNormName,
                                                prodDNormName = body.drugsData.foiv.prodDNormName ?: BLANK,
                                                prodAmount = body.drugsData.foiv.prodPack1Size?.toDoubleOrNull() ?: 0.0,
                                                phKinetics = body.drugsData.vidalData.phKinetics ?: BLANK,
                                                comment = _uiState.value.comment.ifEmpty { BLANK },
                                                image = getImage(context, body.drugsData.vidalData.images),
                                                technical = Technical(
                                                    scanned = true,
                                                    verified = true
                                                )
                                            )

                                            val id = dao.add(medicine)
                                            _uiState.update { it.copy(id = id) }
                                            _response.emit(ResponseState.Success)
                                        }
                                    } else _response.emit(ResponseState.Errors.CODE_NOT_FOUND)
                                } else _response.emit(ResponseState.Errors.WRONG_CATEGORY)
                            } ?: _response.emit(ResponseState.Errors.FETCH_ERROR)
                        } ?: _response.emit(ResponseState.Errors.WRONG_CODE_CATEGORY)
                    } else _response.emit(ResponseState.Errors.WRONG_CODE_CATEGORY)
                }
            } catch (throwable: Throwable) {
                if (dao.getAllCIS().contains(code)) {
                    val id = dao.getIDbyCis(code)
                    _uiState.update { it.copy(id = id) }
                    _response.emit(ResponseState.Duplicate)
                } else {
                    _uiState.update { it.copy(cis = code) }
                    _response.emit(ResponseState.Errors.NO_NETWORK)
                }
            }
        }
    }

    private suspend fun getImage(context: Context, url: List<String>?): String {
        if (url.isNullOrEmpty() || !FragmentSettings().getDownloadNeeded()) return BLANK
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
    data object Success : ResponseState
    data object Loading : ResponseState
    data object Duplicate : ResponseState
    enum class Errors : ResponseState {
        NO_ERROR, WRONG_CODE_CATEGORY, WRONG_CATEGORY, CODE_NOT_FOUND, FETCH_ERROR, NO_NETWORK
    }
}

