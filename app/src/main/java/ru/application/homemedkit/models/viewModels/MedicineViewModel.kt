package ru.application.homemedkit.models.viewModels

import android.net.Uri
import androidx.compose.ui.util.fastForEach
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.joinAll
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import ru.application.homemedkit.data.dto.Image
import ru.application.homemedkit.data.dto.MedicineKit
import ru.application.homemedkit.models.events.MedicineEvent
import ru.application.homemedkit.models.events.Response
import ru.application.homemedkit.models.states.MedicineDialogState
import ru.application.homemedkit.models.states.MedicineState
import ru.application.homemedkit.models.validation.Validation
import ru.application.homemedkit.network.Network
import ru.application.homemedkit.utils.BLANK
import ru.application.homemedkit.utils.Formatter
import ru.application.homemedkit.utils.camera.ImageProcessing
import ru.application.homemedkit.utils.di.Database
import ru.application.homemedkit.utils.enums.DrugType
import ru.application.homemedkit.utils.enums.ImageEditing
import ru.application.homemedkit.utils.extensions.concat
import ru.application.homemedkit.utils.extensions.toMedicine
import ru.application.homemedkit.utils.extensions.toState
import ru.application.homemedkit.utils.extensions.toggle
import ru.application.homemedkit.utils.getMedicineImages
import java.io.File

class MedicineViewModel(
    private val id: Long,
    private val cis: String,
    private val duplicate: Boolean
) : BaseViewModel<MedicineState, MedicineEvent>() {
    private val dao by lazy { Database.medicineDAO() }
    private val daoK by lazy { Database.kitDAO() }

    private val _response = Channel<Response>()
    val response = _response.receiveAsFlow()

    private val _deleted = Channel<Boolean>()
    val deleted = _deleted.receiveAsFlow()

    private val _duplicated = Channel<Unit>()
    val duplicated = _duplicated.receiveAsFlow()

    val kits by lazy {
        daoK.getFlow().stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000L), emptyList())
    }

    override fun initState() = MedicineState()

    override fun loadData() {
        viewModelScope.launch {
            val medicine = dao.getById(id)

            if (medicine != null) {
                val newState = withContext(Dispatchers.Default) { medicine.toState() }

                updateState { newState }
            } else {
                updateState {
                    it.copy(
                        adding = true,
                        isLoading = false,
                        code = cis,
                        images = listOf(DrugType.entries.random().value)
                    )
                }
            }

            if (duplicate) {
                _response.send(Response.Duplicate)
            }
        }
    }

    fun add() {
        viewModelScope.launch {
            val checkProductName = Validation.textNotEmpty(currentState.productName)

            if (checkProductName.successful) {
                val id = dao.insert(currentState.toMedicine())

                coroutineScope {
                    val jobOne = launch {
                        val kits = currentState.kits.map { MedicineKit(id, it.kitId) }

                        daoK.pinKit(kits)
                    }

                    val jobTwo = launch {
                        val images = currentState.images.mapIndexed { index, image ->
                            Image(
                                medicineId = id,
                                position = index,
                                image = image
                            )
                        }

                        dao.updateImages(images)
                    }

                    joinAll(jobOne, jobTwo)
                }

                updateState {
                    it.copy(
                        id = id,
                        adding = false,
                        default = true,
                        productNameError = null
                    )
                }
            } else {
                updateState { it.copy(productNameError = checkProductName.errorMessage) }
            }
        }
    }

    fun fetch(dir: File) {
        viewModelScope.launch {
            _response.send(Response.Loading)

            try {
                val response = Network.getMedicine(currentState.code)

                when (val data = response) {
                    is Response.Error -> {
                        _response.send(data)
                        delay(2500L)
                    }

                    is Response.Success -> {
                        val medicine = data.model.run {
                            drugsData?.toMedicine() ?: bioData?.toMedicine() ?: toMedicine()
                        }.copy(
                            id = currentState.id,
                            cis = currentState.code,
                            comment = currentState.comment.ifEmpty { BLANK }
                        )

                        val images = async {
                            getMedicineImages(
                                medicineId = currentState.id,
                                form = medicine.prodFormNormName,
                                directory = dir,
                                urls = data.model.imageUrls
                            )
                        }

                        coroutineScope {
                            val jobOne = launch { dao.update(medicine) }
                            val jobTow = launch { dao.updateImages(images.await()) }

                            joinAll(jobOne, jobTow)
                        }

                        dao.getById(id)?.let { medicine ->
                            updateState { medicine.toState() }
                        }

                        _response.send(Response.Success(data.model))
                    }

                    else -> Unit
                }
            } catch (_: Exception) {
                _response.send(Response.Error.UnknownError)
            }
        }
    }

    fun fetchImages(dir: File) {
        viewModelScope.launch {
            _response.send(Response.Loading)

            try {
                val response = Network.getMedicine(currentState.code)

                when (val data = response) {
                    is Response.Error -> {
                        _response.send(data)
                        delay(2500L)
                    }

                    is Response.Success -> {
                        val images = getMedicineImages(
                            medicineId = currentState.id,
                            form = currentState.prodFormNormName,
                            directory = dir,
                            urls = data.model.imageUrls
                        )

                        dao.updateImages(images)

                        dao.getById(id)?.let { medicine ->
                            updateState { medicine.toState() }
                        }

                        _response.send(Response.Success(data.model))
                    }

                    else -> Unit
                }
            } catch (_: Exception) {
                _response.send(Response.Error.UnknownError)
            }
        }
    }

    fun update() {
        viewModelScope.launch {
            val checkProductName = Validation.textNotEmpty(currentState.productName)

            if (checkProductName.successful) {
                val kits = currentState.kits.map { MedicineKit(currentState.id, it.kitId) }
                val images = currentState.images.mapIndexed { index, image ->
                    Image(
                        medicineId = currentState.id,
                        position = index,
                        image = image
                    )
                }

                daoK.deleteAll(currentState.id)
                daoK.pinKit(kits)
                dao.updateImages(images)

                dao.update(currentState.toMedicine())

                dao.getById(currentState.id)?.let { medicine ->
                    updateState { medicine.toState() }
                }
            } else {
                updateState { it.copy(productNameError = checkProductName.errorMessage) }
            }
        }
    }

    fun delete(dir: File) {
        viewModelScope.launch {
            coroutineScope {
                val jobOne = launch { dao.delete(currentState.toMedicine()) }
                val jobTwo = launch(Dispatchers.IO) {
                    currentState.images.fastForEach {
                        File(dir, it).delete()
                    }
                }


                joinAll(jobOne, jobTwo)
            }

            updateState {
                it.copy(dialogState = null)
            }

            _deleted.send(true)
        }
    }

     override fun onEvent(event: MedicineEvent) {
        when (event) {
            is MedicineEvent.SetProductName -> updateState { it.copy(productName = event.productName) }
            is MedicineEvent.SetNameAlias -> updateState { it.copy(nameAlias = event.alias) }
            is MedicineEvent.SetExpDate -> {
                val expDate = Formatter.toTimestamp(event.month, event.year)

                updateState {
                    it.copy(
                        expDate = expDate,
                        expDateString = Formatter.toExpDate(expDate),
                        dialogState = null
                    )
                }
            }
            is MedicineEvent.SetPackageDate -> updateState {
                it.copy(
                    dateOpened = event.timestamp,
                    dateOpenedString = Formatter.toExpDate(event.timestamp),
                    dialogState = null,
                    isOpened = event.timestamp > 0L
                )
            }
            is MedicineEvent.SetFormName -> updateState { it.copy(prodFormNormName = event.formName) }
            is MedicineEvent.SetDoseName -> updateState { it.copy(prodDNormName = event.doseName) }
            is MedicineEvent.SetDoseType -> updateState { it.copy(doseType = event.type) }
            is MedicineEvent.SetAmount -> updateState { it.copy(prodAmount = event.amount) }
            is MedicineEvent.SetPhKinetics -> updateState { it.copy(phKinetics = event.phKinetics) }
            is MedicineEvent.SetComment -> updateState { it.copy(comment = event.comment) }
            is MedicineEvent.PickKit -> updateState { it.copy(kits = it.kits.toggle(event.kit)) }

            MedicineEvent.ClearKit -> updateState { it.copy(kits = emptySet()) }

            is MedicineEvent.SetIcon -> updateState {
                it.copy(
                    dialogState = MedicineDialogState.PictureGrid,
                    images = it.images.concat(event.icon),
                )
            }

            is MedicineEvent.SetImage -> viewModelScope.launch {
                val compressedImage = event.imageProcessing.compressImage(event.image)

                updateState {
                    it.copy(
                        dialogState = MedicineDialogState.PictureGrid,
                        images = it.images.concat(compressedImage)
                    )
                }

                _response.send(Response.Initial)
            }

            is MedicineEvent.OnImageReodering -> {
                val imagesNameMutable = currentState.images.toMutableList()
                val removedName = imagesNameMutable.removeAt(event.fromIndex)

                imagesNameMutable.add(event.toIndex, removedName)

                updateState {
                    it.copy(images = imagesNameMutable)
                }
            }

            is MedicineEvent.RemoveImage -> {
                val imageNames = currentState.images.toMutableList().apply {
                    remove(event.image)
                }

                updateState {
                    it.copy(images = imageNames)
                }
            }

            MedicineEvent.EditImagesOrder -> updateState {
                it.copy(imageEditing = ImageEditing.entries.getOrElse(it.imageEditing.ordinal + 1) { ImageEditing.ADDING })
            }

            is MedicineEvent.ToggleDialog -> updateState {
                if (it.dialogState == event.dialog) {
                    it.copy(
                        dialogState = when (event.dialog) {
                            MedicineDialogState.PictureChoose -> MedicineDialogState.PictureGrid
                            MedicineDialogState.TakePhoto, MedicineDialogState.Icons -> MedicineDialogState.PictureChoose
                            is MedicineDialogState.FullImage -> if (event.dialog.page == -1) null
                            else MedicineDialogState.FullImage(event.dialog.page)
                            else -> null
                        }
                    )
                } else {
                    it.copy(
                        dialogState = if (event.dialog is MedicineDialogState.FullImage) {
                            if (event.dialog.page == -1) null
                            else MedicineDialogState.FullImage(event.dialog.page)
                        } else {
                            event.dialog
                        }
                    )
                }
            }

            MedicineEvent.ShowLoading -> viewModelScope.launch {
                _response.send(Response.Loading)
            }

            MedicineEvent.MakeDuplicate -> viewModelScope.launch {
                val duplicate = currentState.toMedicine().copy(id = 0L)
                val id = Database.medicineDAO().insert(duplicate)

                coroutineScope {
                    val jobOne = launch {
                        val kits = currentState.kits.map { MedicineKit(id, it.kitId) }

                        daoK.pinKit(kits)
                    }

                    val jobTwo = launch {
                        val images = currentState.images.mapIndexed { index, image ->
                            Image(
                                medicineId = id,
                                position = index,
                                image = image
                            )
                        }

                        dao.updateImages(images)
                    }

                    joinAll(jobOne, jobTwo)
                }

                _duplicated.send(Unit)
            }
        }
    }

    fun setEditing() = updateState {
        it.copy(
            editing = true,
            default = false
        )
    }

    fun compressImage(imageProcessing: ImageProcessing, images: List<Uri>) {
        updateState {
            it.copy(dialogState = null)
        }

        viewModelScope.launch {
            _response.send(Response.Loading)

            val compressed = images.map { uri ->
                async {
                    imageProcessing.compressImage(uri)
                }
            }

            val compressedResult = compressed.mapNotNull { it.await() }

            val images = currentState.images.toMutableList().apply {
                addAll(compressedResult)
            }

            updateState {
                it.copy(
                    images = images,
                    dialogState = MedicineDialogState.PictureGrid
                )
            }

            _response.send(Response.Initial)
        }
    }
}