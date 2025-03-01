package ru.application.homemedkit.models.viewModels

import android.content.Context
import android.net.Uri
import android.webkit.MimeTypeMap
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.toRoute
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import ru.application.homemedkit.HomeMeds.Companion.database
import ru.application.homemedkit.data.dto.Image
import ru.application.homemedkit.data.dto.MedicineKit
import ru.application.homemedkit.helpers.BLANK
import ru.application.homemedkit.helpers.FileManager
import ru.application.homemedkit.helpers.ImageCompressor
import ru.application.homemedkit.helpers.Medicine
import ru.application.homemedkit.helpers.Preferences
import ru.application.homemedkit.helpers.Types
import ru.application.homemedkit.helpers.toBio
import ru.application.homemedkit.helpers.toMedicine
import ru.application.homemedkit.helpers.toState
import ru.application.homemedkit.helpers.toTimestamp
import ru.application.homemedkit.models.events.MedicineEvent
import ru.application.homemedkit.models.states.MedicineState
import ru.application.homemedkit.models.validation.Validation
import ru.application.homemedkit.network.Network
import java.io.File

class MedicineViewModel(saved: SavedStateHandle) : ViewModel() {
    private val dao = database.medicineDAO()
    private val daoK = database.kitDAO()
    private val args = saved.toRoute<Medicine>()

    private val _duplicate = Channel<Boolean>()
    val duplicate = _duplicate.receiveAsFlow()
        .onStart {
            if (args.duplicate) {
                emit(true); delay(2000L); emit(false)
            }
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000L), false)

    private val _state = MutableStateFlow(MedicineState())
    val state = _state.asStateFlow()
        .onStart {
            dao.getById(args.id)?.let { _state.value = it.toState() }
            if (args.id == 0L) _state.update { it.copy(cis = args.cis) }
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000L), MedicineState())

    fun add() = viewModelScope.launch(Dispatchers.IO) {
        val checkProductName = Validation.textNotEmpty(_state.value.productName)

        if (checkProductName.successful) {
            val id = dao.add(_state.value.toMedicine())
            _state.value.images.forEach { dao.addImage(Image(medicineId = id, image = it)) }
            _state.value.kits.forEach { daoK.pinKit(MedicineKit(id, it)) }
            _state.update { it.copy(adding = false, default = true, id = id, productNameError = null) }
        } else _state.update { it.copy(productNameError = checkProductName.errorMessage) }
    }

    fun fetch(dir: File) = viewModelScope.launch(Dispatchers.IO) {
        _state.update { it.copy(loading = true) }

        try {
            Network.getMedicine(_state.value.cis).apply {
                if (codeFounded && checkResult) {
                    if (drugsData != null) {
                        val medicine = drugsData.toMedicine().copy(
                            id = _state.value.id,
                            cis = _state.value.cis,
                            comment = _state.value.comment.ifEmpty { BLANK }
                        )

                        val image = Image(
                            medicineId = _state.value.id,
                            image = if (Preferences.getImageFetch()) Network.getImage(dir, drugsData.vidalData?.images)
                            else Types.setIcon(drugsData.foiv.prodFormNormName)
                        )

                        dao.update(medicine)
                        dao.updateImages(image)
                        _state.value = medicine.toState()
                    } else if (bioData != null) {
                        val medicine = bioData.toBio().copy(
                            id = _state.value.id,
                            cis = _state.value.cis,
                            comment = _state.value.comment.ifEmpty { BLANK }
                        )

                        dao.update(medicine)
                        _state.value = medicine.toState()
                    } else _state.apply {
                        update {
                            it.copy(
                                loading = false,
                                codeError = true
                            )
                        }
                        delay(2500L)
                        update { it.copy(codeError = false) }
                    }
                } else _state.apply {
                    update {
                        it.copy(
                            loading = false,
                            codeError = true
                        )
                    }
                    delay(2500L)
                    update { it.copy(codeError = false) }
                }
            }
        } catch (e: Throwable) {
            _state.apply {
                update {
                    it.copy(
                        loading = false,
                        noNetwork = true
                    )
                }
                delay(2500L)
                update { it.copy(noNetwork = false) }
            }
        }
    }

    fun update() = viewModelScope.launch(Dispatchers.IO) {
        val checkProductName = Validation.textNotEmpty(_state.value.productName)

        if (checkProductName.successful) {
            val images = _state.value.images.map { Image(medicineId = _state.value.id, image = it) }.toTypedArray()
            dao.update(_state.value.toMedicine())
            dao.updateImages(*images)
            daoK.deleteAll(args.id)
            _state.value.kits.forEach { daoK.pinKit(MedicineKit(args.id, it)) }
            _state.update { it.copy(adding = false, editing = false, default = true, productNameError = null) }
        } else _state.update { it.copy(productNameError = checkProductName.errorMessage) }
    }

    fun delete(dir: File) = viewModelScope.launch(Dispatchers.IO) {
        dao.delete(_state.value.toMedicine())
    }.invokeOnCompletion {
        _state.value.images.forEach {
            File(dir, it).deleteRecursively()
        }
    }

    fun onEvent(event: MedicineEvent) {
        when (event) {
            is MedicineEvent.SetCis -> _state.update { it.copy(cis = event.cis) }
            is MedicineEvent.SetProductName -> _state.update { it.copy(productName = event.productName) }
            is MedicineEvent.SetNameAlias -> _state.update { it.copy(nameAlias = event.alias) }
            is MedicineEvent.SetExpDate -> _state.update {
                it.copy(expDate = toTimestamp(event.month, event.year), showDialogDate = false)
            }
            is MedicineEvent.SetPackageDate -> _state.update {
                it.copy(dateOpened = event.timestamp, showDialogPackageDate = false)
            }
            is MedicineEvent.SetFormName -> _state.update { it.copy(prodFormNormName = event.formName) }
            is MedicineEvent.SetDoseName -> _state.update { it.copy(prodDNormName = event.doseName) }
            is MedicineEvent.SetDoseType -> _state.update {
                it.copy(doseType = event.type.value, doseTypeE = event.type, showMenuDose = false)
            }
            is MedicineEvent.SetAmount -> when {
                event.amount.isNotEmpty() -> {
                    if (event.amount.replace(',', '.').toDoubleOrNull() != null)
                        _state.update { it.copy(prodAmount = event.amount.replace(',', '.')) }
                }
                else -> _state.update { it.copy(prodAmount = BLANK) }
            }
            is MedicineEvent.SetPhKinetics -> _state.update { it.copy(phKinetics = event.phKinetics) }
            is MedicineEvent.SetComment -> _state.update { it.copy(comment = event.comment) }
            is MedicineEvent.PickKit -> _state.update {
                it.copy(
                    kits = it.kits.apply {
                        if (event.kitId in this) remove(event.kitId) else add(event.kitId)
                    }
                )
            }
            MedicineEvent.ClearKit -> _state.update { it.copy(kits = it.kits.apply(SnapshotStateList<Long>::clear)) }

            is MedicineEvent.SetIcon -> _state.update {
                it.copy(
                    images = mutableStateListOf(event.icon),
                    showDialogIcons = false)
            }

            is MedicineEvent.SetImage -> _state.update {
                it.copy(
                    images = event.images,
                    showDialogPictureChoose = false,
                    showTakePhoto = false,
                    showDialogIcons = false
                )
            }

            MedicineEvent.ShowKitDialog -> _state.update { it.copy(showDialogKits = !it.showDialogKits) }

            MedicineEvent.ShowDatePicker -> _state.update { it.copy(showDialogDate = !it.showDialogDate) }

            MedicineEvent.ShowPackageDatePicker -> _state.update { it.copy(showDialogPackageDate = !it.showDialogPackageDate) }

            MedicineEvent.ShowIconPicker -> _state.update {
                it.copy(
                    showDialogPictureChoose = false,
                    showDialogIcons = !it.showDialogIcons
                )
            }

            MedicineEvent.ShowDialogPictureChoose -> _state.update { it.copy(showDialogPictureChoose = !it.showDialogPictureChoose) }

            MedicineEvent.ShowDialogDelete -> _state.update { it.copy(showDialogDelete = !it.showDialogDelete) }

            MedicineEvent.ShowDoseMenu -> _state.update { it.copy(showMenuDose = !it.showMenuDose) }

            MedicineEvent.ShowTakePhoto -> _state.update {
                it.copy(
                    showTakePhoto = !it.showTakePhoto,
                    showDialogPictureChoose = false
                )
            }
        }
    }

    fun setEditing() = _state.update { it.copy(editing = true, default = false) }

    fun compressImage(context: Context, images: List<Uri>) {
         viewModelScope.launch {
            _state.update {
                it.copy(
                    showDialogPictureChoose = false,
                    showTakePhoto = false,
                    showDialogIcons = false,
                    loading = true,
                )
            }

            val fileManager = FileManager(context)
            val compressor = ImageCompressor(context)

            val names = mutableStateListOf<String>()

            images.forEach { uri ->
                val mimeType = context.contentResolver.getType(uri)
                val extension = MimeTypeMap.getSingleton().getExtensionFromMimeType(mimeType)
                val name = "${System.currentTimeMillis()}.$extension"

                val compressedImage = compressor.compressImage(uri, 300 * 1024L)
                fileManager.saveImage(
                    bytes = compressedImage ?: return@launch,
                    fileName = name
                )

                names.add(name)
            }

            _state.update {
                it.copy(
                    images = names,
                    loading = false
                )
            }
        }
    }
}