package ru.application.homemedkit.models.viewModels

import android.net.Uri
import androidx.compose.runtime.snapshots.SnapshotStateSet
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.toRoute
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.joinAll
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import ru.application.homemedkit.HomeMeds.Companion.database
import ru.application.homemedkit.data.dto.Image
import ru.application.homemedkit.data.dto.Kit
import ru.application.homemedkit.data.dto.MedicineKit
import ru.application.homemedkit.models.events.MedicineEvent
import ru.application.homemedkit.models.events.Response
import ru.application.homemedkit.models.states.MedicineState
import ru.application.homemedkit.models.validation.Validation
import ru.application.homemedkit.network.Network
import ru.application.homemedkit.ui.navigation.Screen.Medicine
import ru.application.homemedkit.utils.BLANK
import ru.application.homemedkit.utils.camera.ImageProcessing
import ru.application.homemedkit.utils.enums.ImageEditing
import ru.application.homemedkit.utils.extensions.toMedicine
import ru.application.homemedkit.utils.extensions.toState
import ru.application.homemedkit.utils.extensions.toggle
import ru.application.homemedkit.utils.getMedicineImages
import ru.application.homemedkit.utils.toExpDate
import ru.application.homemedkit.utils.toTimestamp
import java.io.File

class MedicineViewModel(saved: SavedStateHandle) : ViewModel() {
    private val dao = database.medicineDAO()
    private val daoK = database.kitDAO()
    private val args = saved.toRoute<Medicine>()

    private val _response = Channel<Response>()
    val response = _response.receiveAsFlow()

    private val _deleted = Channel<Boolean>()
    val deleted = _deleted.receiveAsFlow()

    private val _state = MutableStateFlow(MedicineState())
    val state = _state
        .onStart { _state.emit(dao.getById(args.id)?.toState() ?: MedicineState(cis = args.cis)) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000L), MedicineState())

    val kits = database.kitDAO().getFlow()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000L), emptyList())

    init {
        if (args.duplicate) viewModelScope.launch {
            _response.send(Response.Duplicate)
        }
    }

    fun add() {
        val currentValue = _state.value

        viewModelScope.launch {
            val checkProductName = Validation.textNotEmpty(currentValue.productName)

            if (checkProductName.successful) {
                val id = dao.insert(currentValue.toMedicine())
                val kits = currentValue.kits.map { MedicineKit(id, it.kitId) }
                val images = currentValue.images.mapIndexed { index, image ->
                    Image(
                        medicineId = id,
                        position = index,
                        image = image
                    )
                }

                val jobOne = launch { daoK.pinKit(kits) }
                val jobTwo = launch { dao.updateImages(images) }

                try {
                    joinAll(jobOne, jobTwo)
                } catch (_: CancellationException) {

                }

                _state.update {
                    it.copy(
                        id = id,
                        adding = false,
                        default = true,
                        productNameError = null
                    )
                }
            } else {
                _state.update { it.copy(productNameError = checkProductName.errorMessage) }
            }
        }
    }

    fun fetch(dir: File) {
        val currentState = _state.value

        viewModelScope.launch {
            _response.send(Response.Loading)

            try {
                val response = Network.getMedicine(currentState.cis)

                when (val data = response) {
                    is Response.Error -> {
                        _response.send(data)
                        delay(2500L)
                    }

                    is Response.Success -> {
                        val medicine = data.model.run {
                            drugsData?.toMedicine() ?: bioData?.toMedicine()
                        }?.copy(
                            id = currentState.id,
                            cis = currentState.cis,
                            comment = currentState.comment.ifEmpty { BLANK }
                        )

                        if (medicine == null) {
                            _response.send(Response.Error.UnknownError)
                        } else {
                            val images = async {
                                getMedicineImages(
                                    medicineId = currentState.id,
                                    form = medicine.prodFormNormName,
                                    directory = dir,
                                    urls = data.model.drugsData?.vidalData?.images
                                )
                            }

                            val jobOne = launch { dao.update(medicine) }
                            val jobTow = launch { dao.updateImages(images.await()) }

                            joinAll(jobOne, jobTow)

                            dao.getById(args.id)?.let { medicine ->
                                _state.update { medicine.toState() }
                            }

                            _response.send(Response.Success(data.model))
                        }
                    }

                    else -> Unit
                }
            } catch (_: Throwable) {
                _response.send(Response.Error.UnknownError)
            }
        }
    }

    fun update() {
        val currentValue = _state.value

        viewModelScope.launch {
            val checkProductName = Validation.textNotEmpty(currentValue.productName)

            if (checkProductName.successful) {
                val kits = currentValue.kits.map { MedicineKit(currentValue.id, it.kitId) }
                val images = currentValue.images.mapIndexed { index, image ->
                    Image(
                        medicineId = currentValue.id,
                        position = index,
                        image = image
                    )
                }

                daoK.deleteAll(currentValue.id)
                daoK.pinKit(kits)
                dao.updateImages(images)

                dao.update(currentValue.toMedicine())

                dao.getById(currentValue.id)?.let { medicine ->
                    _state.update { medicine.toState() }
                }
            } else {
                _state.update { it.copy(productNameError = checkProductName.errorMessage) }
            }
        }
    }

    fun delete(dir: File) {
        val currentState = _state.value

        viewModelScope.launch {
            val jobOne = launch { dao.delete(currentState.toMedicine())  }
            val jobTwo = launch(Dispatchers.IO) {
                currentState.images.forEach {
                    File(dir, it).delete()
                }
            }

            try {
                joinAll(jobOne, jobTwo)
            } catch (_: CancellationException) {

            }

            _state.update {
                it.copy(showDialogDelete = false)
            }

            _deleted.send(true)
        }
    }

    fun onEvent(event: MedicineEvent) {
        when (event) {
            is MedicineEvent.SetCis -> _state.update { it.copy(cis = event.cis) }
            is MedicineEvent.SetProductName -> _state.update { it.copy(productName = event.productName) }
            is MedicineEvent.SetNameAlias -> _state.update { it.copy(nameAlias = event.alias) }
            is MedicineEvent.SetExpDate -> _state.update {
                it.copy(
                    expDate = toTimestamp(event.month, event.year),
                    expDateString = toExpDate(toTimestamp(event.month, event.year)),
                    showDialogDate = false
                )
            }
            is MedicineEvent.SetPackageDate -> _state.update {
                it.copy(
                    dateOpened = event.timestamp,
                    dateOpenedString = toExpDate(event.timestamp),
                    showDialogPackageDate = false
                )
            }
            is MedicineEvent.SetFormName -> _state.update { it.copy(prodFormNormName = event.formName) }
            is MedicineEvent.SetDoseName -> _state.update { it.copy(prodDNormName = event.doseName) }
            is MedicineEvent.SetDoseType -> _state.update {
                it.copy(doseType = event.type, showMenuDose = false)
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
            is MedicineEvent.PickKit -> _state.update { it.copy(kits = it.kits.apply { toggle(event.kit) }) }

            MedicineEvent.ClearKit -> _state.update { it.copy(kits = it.kits.apply(SnapshotStateSet<Kit>::clear)) }

            is MedicineEvent.SetIcon -> _state.update {
                it.copy(
                    showDialogIcons = false,
                    showDialogPictureGrid = true,
                    images = it.images.apply { add(event.icon) },
                )
            }

            is MedicineEvent.SetFullImage -> _state.update { it.copy(fullImage = event.index) }

            is MedicineEvent.SetImage -> viewModelScope.launch {
                _state.update {
                    it.copy(
                        showDialogPictureGrid = true,
                        showDialogPictureChoose = false,
                        showTakePhoto = false,
                        showDialogIcons = false,
                        images = it.images.apply {
                            add(event.imageProcessing.compressImage(event.image))
                        }
                    )
                }

                _response.send(Response.Initial)
            }

            is MedicineEvent.OnImageReodering -> _state.update {
                it.copy(images = it.images.apply { add(event.toIndex, removeAt(event.fromIndex)) })
            }

            is MedicineEvent.RemoveImage -> _state.update {
                it.copy(images = it.images.apply { remove(event.image) })
            }

            MedicineEvent.EditImagesOrder -> _state.update {
                it.copy(imageEditing = ImageEditing.entries.getOrElse(it.imageEditing.ordinal + 1) { ImageEditing.ADDING })
            }

            MedicineEvent.ShowLoading -> viewModelScope.launch {
                _response.send(Response.Loading)
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

            MedicineEvent.ShowDialogPictureGrid -> _state.update { it.copy(showDialogPictureGrid = !it.showDialogPictureGrid) }

            MedicineEvent.ShowDialogPictureChoose -> _state.update {
                it.copy(
                    showDialogPictureGrid = !it.showDialogPictureGrid,
                    showDialogPictureChoose = !it.showDialogPictureChoose
                )
            }

            is MedicineEvent.ShowDialogFullImage -> _state.update {
                it.copy(
                    showDialogFullImage = !it.showDialogFullImage,
                    fullImage = event.index
                )
            }

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

    fun setEditing() = _state.update {
        it.copy(
            editing = true,
            default = false
        )
    }

    fun compressImage(imageProcessing: ImageProcessing, images: List<Uri>) {
        _state.update {
            it.copy(
                showDialogPictureChoose = false,
                showTakePhoto = false,
                showDialogIcons = false
            )
        }

        viewModelScope.launch {
            _response.send(Response.Loading)

            val compressed = images.map { uri ->
                async(Dispatchers.IO) {
                    imageProcessing.compressImage(uri)
                }
            }

            val compressedResult = compressed.mapNotNull { it.await() }

            withContext(Dispatchers.Main) {
                _state.update {
                    it.copy(
                        images = it.images.apply { addAll(compressedResult) },
                        showDialogPictureGrid = true
                    )
                }
            }

            _response.send(Response.Initial)
        }
    }
}