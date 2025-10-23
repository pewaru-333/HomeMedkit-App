@file:OptIn(ExperimentalMaterial3Api::class)

package ru.application.homemedkit.ui.screens

import android.Manifest
import android.net.Uri
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts.PickMultipleVisualMedia
import androidx.activity.result.contract.ActivityResultContracts.PickVisualMedia
import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.Crossfade
import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.PressInteraction
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.relocation.BringIntoViewRequester
import androidx.compose.foundation.relocation.bringIntoViewRequester
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.text.input.InputTransformation
import androidx.compose.foundation.text.input.OutputTransformation
import androidx.compose.foundation.text.input.TextFieldLineLimits
import androidx.compose.foundation.text.input.rememberTextFieldState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.AlertDialogDefaults
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuAnchorType
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Snackbar
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.zIndex
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.collectLatest
import ru.application.homemedkit.R
import ru.application.homemedkit.data.dto.Kit
import ru.application.homemedkit.dialogs.DatePicker
import ru.application.homemedkit.dialogs.MonthYear
import ru.application.homemedkit.dialogs.dragHandle
import ru.application.homemedkit.dialogs.draggableItemsIndexed
import ru.application.homemedkit.dialogs.rememberDraggableListState
import ru.application.homemedkit.models.events.MedicineEvent
import ru.application.homemedkit.models.events.MedicineEvent.SetProductName
import ru.application.homemedkit.models.events.MedicineEvent.ToggleDialog
import ru.application.homemedkit.models.events.Response
import ru.application.homemedkit.models.states.MedicineDialogState
import ru.application.homemedkit.models.states.MedicineState
import ru.application.homemedkit.models.viewModels.MedicineViewModel
import ru.application.homemedkit.ui.elements.BoxLoading
import ru.application.homemedkit.ui.elements.DialogDelete
import ru.application.homemedkit.ui.elements.DialogKits
import ru.application.homemedkit.ui.elements.MedicineImage
import ru.application.homemedkit.ui.elements.NavigationIcon
import ru.application.homemedkit.ui.elements.TopBarActions
import ru.application.homemedkit.ui.elements.VectorIcon
import ru.application.homemedkit.utils.DecimalAmountInputTransformation
import ru.application.homemedkit.utils.DecimalAmountOutputTransformation
import ru.application.homemedkit.utils.camera.CameraConfig
import ru.application.homemedkit.utils.camera.ImageProcessing
import ru.application.homemedkit.utils.camera.rememberCameraConfig
import ru.application.homemedkit.utils.decimalFormat
import ru.application.homemedkit.utils.enums.DoseType
import ru.application.homemedkit.utils.enums.DrugType
import ru.application.homemedkit.utils.enums.ImageEditing
import ru.application.homemedkit.utils.extensions.medicine
import ru.application.homemedkit.utils.permissions.rememberPermissionState

@Composable
fun MedicineScreen(navigateBack: () -> Unit, navigateToIntake: (Long) -> Unit) {
    val context = LocalContext.current
    val filesDir = context.filesDir

    val model = viewModel<MedicineViewModel>()
    val state by model.state.collectAsStateWithLifecycle()
    val kits by model.kits.collectAsStateWithLifecycle()
    val response by model.response.collectAsStateWithLifecycle(
        initialValue = Response.Initial,
        context = Dispatchers.Main.immediate
    )

    val snackbarHost = remember(::SnackbarHostState)

    LaunchedEffect(Unit) {
        model.deleted.collectLatest {
            if (it) navigateBack()
        }
    }

    BackHandler {
        if (state.dialogState == MedicineDialogState.TakePhoto) {
            model.onEvent(ToggleDialog(MedicineDialogState.TakePhoto))
        } else if (!state.default) {
            model.onEvent(ToggleDialog(MedicineDialogState.DataLoss))
        } else {
            navigateBack()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {},
                navigationIcon = {
                    NavigationIcon {
                        if (state.default) navigateBack()
                        else model.onEvent(ToggleDialog(MedicineDialogState.DataLoss))
                    }
                },
                actions = {
                    TopBarActions(
                        isDefault = state.default,
                        setModifiable = model::setEditing,
                        onSave = if (state.adding) model::add else model::update,
                        onShowDialog = { model.onEvent(ToggleDialog(MedicineDialogState.Delete)) },
                        onNavigate = { navigateToIntake(state.id) }
                    )
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    navigationIconContentColor = MaterialTheme.colorScheme.onPrimaryContainer,
                    actionIconContentColor = MaterialTheme.colorScheme.onPrimaryContainer
                )
            )
        },
        snackbarHost = {
            SnackbarHost(snackbarHost) {
                Snackbar(
                    snackbarData = it,
                    containerColor = MaterialTheme.colorScheme.errorContainer,
                    contentColor = MaterialTheme.colorScheme.onErrorContainer,
                )
            }
        },
        floatingActionButton = {
            if (state.technical.scanned && !state.technical.verified) {
                ExtendedFloatingActionButton(
                    text = { Text(stringResource(R.string.text_update)) },
                    icon = { VectorIcon(R.drawable.vector_refresh) },
                    onClick = { model.fetch(filesDir) }
                )
            }
        }
    ) { values ->
        Crossfade(state.isLoading) { isLoading ->
            if (isLoading) {
                Box(Modifier.fillMaxSize())
            } else {
                LazyColumn(
                    modifier = Modifier.imePadding(),
                    contentPadding = values.medicine(),
                    verticalArrangement = Arrangement.spacedBy(32.dp)
                ) {
                    item {
                        Row(Modifier.height(256.dp), Arrangement.spacedBy(12.dp)) {
                            ProductImage(
                                isDefault = state.default,
                                images = state.images,
                                onShow = { model.onEvent(ToggleDialog(MedicineDialogState.FullImage(it))) },
                                onDismiss = { model.onEvent(ToggleDialog(MedicineDialogState.PictureGrid)) }
                            )

                            Summary(state, model::onEvent)
                        }
                    }
                    item {
                        InfoTextField(
                            isEditing = state.adding || state.editing,
                            title = stringResource(R.string.text_medicine_display_name),
                            value = state.nameAlias,
                            onValueChange = { model.onEvent(MedicineEvent.SetNameAlias(it)) },
                            emptyText = state.productName,
                            keyboardOptions = KeyboardOptions(
                                capitalization = KeyboardCapitalization.Sentences,
                                imeAction = ImeAction.Next
                            )
                        )
                    }
                    item {
                        InfoTextField(
                            isEditing = !state.default && !state.technical.verified,
                            title = stringResource(R.string.text_medicine_form),
                            value = state.prodFormNormName,
                            onValueChange = { model.onEvent(MedicineEvent.SetFormName(it)) },
                            modifier = Modifier.fillMaxWidth(),
                            emptyText = stringResource(R.string.text_empty),
                            keyboardOptions = KeyboardOptions(
                                capitalization = KeyboardCapitalization.Sentences,
                                keyboardType = KeyboardType.Text,
                                imeAction = ImeAction.Default
                            )
                        )
                    }
                    item {
                        Row(Modifier.fillMaxWidth(), Arrangement.spacedBy(16.dp)) {
                            InfoTextField(
                                isEditing = state.adding || state.editing && !state.technical.verified,
                                value = state.prodDNormName,
                                onValueChange = { model.onEvent(MedicineEvent.SetDoseName(it)) },
                                modifier = Modifier.weight(0.5f),
                                title = stringResource(R.string.text_medicine_dose),
                                placeholder = stringResource(R.string.placeholder_dose),
                                keyboardOptions = KeyboardOptions(KeyboardCapitalization.Sentences)
                            )

                            InfoTextField(
                                isEditing = !state.default,
                                title = stringResource(R.string.text_amount),
                                value = if (!state.default) state.prodAmount
                                else "${decimalFormat(state.prodAmount)} ${stringResource(state.doseType.title)}",
                                onValueChange = { model.onEvent(MedicineEvent.SetAmount(it)) },
                                modifier = Modifier.weight(0.5f),
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                                emptyText = stringResource(R.string.text_empty),
                                inputTransformation = DecimalAmountInputTransformation,
                                outputTransformation = DecimalAmountOutputTransformation,
                                suffix = {
                                    DoseDropdownMenu(
                                        doseTitle = stringResource(state.doseType.title),
                                        setDoseType = { model.onEvent(MedicineEvent.SetDoseType(it)) }
                                    )
                                }
                            )
                        }
                    }
                    if (state.default && state.structure.isNotEmpty()) {
                        item {
                            InfoTextField(
                                isEditing = false,
                                title = stringResource(R.string.text_medicine_composition),
                                value = state.structure,
                                onValueChange = {}
                            )
                        }
                    }
                    if (state.adding || state.editing || state.phKinetics.isNotEmpty()) {
                        item {
                            InfoTextField(
                                isEditing = !state.default,
                                title = stringResource(R.string.text_indications_for_use),
                                value = state.phKinetics,
                                onValueChange = { model.onEvent(MedicineEvent.SetPhKinetics(it)) },
                                keyboardOptions = KeyboardOptions(
                                    capitalization = KeyboardCapitalization.Sentences,
                                    keyboardType = KeyboardType.Text
                                )
                            )
                        }
                    }
                    if (state.default && state.recommendations.isNotEmpty()) {
                        item {
                            InfoTextField(
                                isEditing = false,
                                title = stringResource(R.string.text_medicine_recommendations),
                                value = state.recommendations,
                                onValueChange = {}
                            )
                        }
                    }
                    if (state.default && state.storageConditions.isNotEmpty()) {
                        item {
                            InfoTextField(
                                isEditing = false,
                                title = stringResource(R.string.text_medicine_storage_conditions),
                                value = state.storageConditions,
                                onValueChange = {}
                            )
                        }
                    }
                    if (state.adding || state.editing || state.comment.isNotEmpty()) {
                        item {
                            InfoTextField(
                                isEditing = !state.default,
                                title = stringResource(R.string.text_medicine_comment),
                                value = state.comment,
                                onValueChange = { model.onEvent(MedicineEvent.SetComment(it)) },
                                keyboardOptions = KeyboardOptions(
                                    capitalization = KeyboardCapitalization.Sentences,
                                    keyboardType = KeyboardType.Text,
                                    imeAction = ImeAction.Default
                                )
                            )
                        }
                    }
               }
            }
        }
    }

    when (val value = response) {
        Response.Loading -> BoxLoading(Modifier.zIndex(10f))

        Response.Duplicate -> LaunchedEffect(snackbarHost) {
            snackbarHost.showSnackbar(context.getString(R.string.text_duplicate))
        }

        is Response.Error -> LaunchedEffect(snackbarHost) {
            snackbarHost.showSnackbar(context.getString(value.message))
        }

        else -> Unit
    }

    when (state.dialogState) {
        MedicineDialogState.DataLoss -> DialogDataLoss(
            onDismiss = { model.onEvent(ToggleDialog(MedicineDialogState.DataLoss)) },
            onBack = navigateBack
        )

        MedicineDialogState.Kits -> DialogKits(
            kits = kits,
            isChecked = { it in state.kits },
            onPick = { model.onEvent(MedicineEvent.PickKit(it)) },
            onDismiss = { model.onEvent(ToggleDialog(MedicineDialogState.Kits)) },
            onClear = { model.onEvent(MedicineEvent.ClearKit) }
        )

        MedicineDialogState.Icons -> IconPicker(
            onDismiss = { model.onEvent(ToggleDialog(MedicineDialogState.Icons)) },
            onPick = { model.onEvent(MedicineEvent.SetIcon(it)) }
        )

        MedicineDialogState.PictureGrid -> DialogPictureGrid(state, model::onEvent)
        MedicineDialogState.PictureChoose -> DialogPictureChoose(state.images.size, model::onEvent, model::compressImage)

        is MedicineDialogState.FullImage -> DialogFullImage(
            images = state.images,
            initialPage = MedicineDialogState.getPage(state.dialogState),
            onDismiss = { model.onEvent(ToggleDialog(MedicineDialogState.FullImage(-1))) },
            onShow = { model.onEvent(ToggleDialog(MedicineDialogState.FullImage(it)))}
        )

        MedicineDialogState.TakePhoto -> CameraPhotoPreview(model::onEvent)

        MedicineDialogState.Date -> MonthYear(
            cancel = { model.onEvent(ToggleDialog(MedicineDialogState.Date)) },
            confirm = { month, year -> model.onEvent(MedicineEvent.SetExpDate(month, year)) }
        )

        MedicineDialogState.PackageDate -> DatePicker(
            onDismiss = { model.onEvent(ToggleDialog(MedicineDialogState.PackageDate)) },
            onSelect = { model.onEvent(MedicineEvent.SetPackageDate(it)) }
        )

        MedicineDialogState.Delete -> DialogDelete(
            text = R.string.text_confirm_deletion_med,
            onCancel = { model.onEvent(ToggleDialog(MedicineDialogState.Delete)) },
            onConfirm = { model.delete(filesDir) }
        )

        null -> Unit
    }
}

@Composable
private fun Summary(state: MedicineState, onEvent: (MedicineEvent) -> Unit) {

    @Composable
    fun LocalLabel(@StringRes text: Int) =
        Text(
            text = stringResource(text),
            style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.W400)
        )

    @Composable
    fun LocalText(text: String, style: TextStyle = MaterialTheme.typography.titleMedium) {
        Text(
            text = text.ifEmpty { stringResource(R.string.text_unspecified) },
            style = style,
            softWrap = false,
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState())
        )
    }

    @Composable
    fun LocalTextField(
        value: String,
        onEvent: () -> Unit,
        @StringRes label: Int,
        @StringRes placeholder: Int,
        modifier: Modifier = Modifier,
    ) {
        val interactionSource = remember(::MutableInteractionSource)

        LaunchedEffect(interactionSource) {
            interactionSource.interactions.collectLatest { interaction ->
                if (interaction is PressInteraction.Release) {
                    onEvent()
                }
            }
        }

        OutlinedTextField(
            value = value,
            onValueChange = {},
            modifier = modifier.fillMaxWidth(),
            interactionSource = interactionSource,
            readOnly = true,
            singleLine = true,
            placeholder = { Text(stringResource(placeholder)) },
            label = {
                Text(
                    text = stringResource(label),
                    modifier = Modifier.horizontalScroll(rememberScrollState()),
                    softWrap = false
                )
            }
        )
    }

    @Composable
    fun LocalAnimatedField(
        isDefault: Boolean,
        label: @Composable () -> Unit,
        text: @Composable () -> Unit,
        textField: @Composable () -> Unit,
    ) {
        AnimatedContent(isDefault) { isDefault ->
            if (isDefault) {
                Column {
                    label()
                    text()
                }
            } else {
                textField()
            }
        }
    }

    Column(
        verticalArrangement = Arrangement.SpaceBetween,
        modifier = Modifier
            .fillMaxHeight()
            .verticalScroll(rememberScrollState())
    ) {
        LocalAnimatedField(
            isDefault = state.default || state.technical.verified,
            label = { LocalLabel(R.string.text_medicine_product_name) },
            text = { LocalText(state.productName) },
            textField = {
                val textFieldState = rememberTextFieldState(state.productName)
                val focusRequester = remember(::FocusRequester)
                val viewRequester = remember(::BringIntoViewRequester)

                LaunchedEffect(textFieldState) {
                    snapshotFlow { textFieldState.text.toString() }.collectLatest {
                        onEvent(SetProductName(it))
                    }
                }

                LaunchedEffect(state.productNameError) {
                    if (state.productNameError != null) {
                        focusRequester.requestFocus()
                    }
                }

                LaunchedEffect(focusRequester) {
                    viewRequester.bringIntoView()
                }

                OutlinedTextField(
                    state = textFieldState,
                    label = { Text(stringResource(R.string.text_medicine_product_name)) },
                    supportingText = state.productNameError?.let { { Text(stringResource(it)) } },
                    isError = state.productNameError != null,
                    lineLimits = TextFieldLineLimits.SingleLine,
                    keyboardOptions = KeyboardOptions(
                        capitalization = KeyboardCapitalization.Sentences,
                        imeAction = ImeAction.Done
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .bringIntoViewRequester(viewRequester)
                        .focusRequester(focusRequester)
                )
            }
        )

        LocalAnimatedField(
            isDefault = state.default,
            label = { LocalLabel(R.string.text_medicine_group) },
            text = { LocalText(state.kits.joinToString(transform = Kit::title)) },
            textField = {
                LocalTextField(
                    value = state.kits.joinToString(transform = Kit::title),
                    label = R.string.text_medicine_group,
                    placeholder = R.string.text_empty,
                    onEvent = { onEvent(ToggleDialog(MedicineDialogState.Kits)) }
                )
            }
        )

        LocalAnimatedField(
            isDefault = state.default || !state.isOpened && state.technical.verified,
            label = { LocalLabel(R.string.text_exp_date) },
            text = { LocalText(state.expDateString) },
            textField = {
                LocalTextField(
                    value = state.expDateString,
                    label = R.string.text_exp_date,
                    placeholder = R.string.text_unspecified,
                    onEvent = { onEvent(ToggleDialog(MedicineDialogState.Date)) }
                )
            }
        )

        LocalAnimatedField(
            isDefault = state.default,
            label = { LocalLabel(R.string.text_package_opened_date) },
            text = { LocalText(state.dateOpenedString) },
            textField = {
                LocalTextField(
                    value = state.dateOpenedString,
                    label = R.string.text_package_opened_date,
                    placeholder = R.string.text_unspecified,
                    onEvent = { onEvent(ToggleDialog(MedicineDialogState.PackageDate)) }
                )
            }
        )

        if (state.default) {
            Column {
                LocalLabel(R.string.text_status)

                LocalText(
                    text = stringResource(
                        if (state.technical.verified) R.string.text_medicine_status_checked
                        else if (state.technical.scanned && !state.technical.verified) R.string.text_medicine_status_scanned
                        else R.string.text_medicine_status_self_added
                    ),
                    style = MaterialTheme.typography.titleMedium.copy(
                        color = if (state.technical.verified) MaterialTheme.colorScheme.primary
                        else if (state.technical.scanned && !state.technical.verified) MaterialTheme.colorScheme.onBackground
                        else MaterialTheme.colorScheme.error
                    )
                )
            }
        }
    }
}


@Composable
private fun ProductImage(images: List<String>, isDefault: Boolean, onShow: (Int) -> Unit, onDismiss: () -> Unit) {
    val pagerState = rememberPagerState(pageCount = images::count)

    Box(
        modifier = Modifier
            .width(128.dp)
            .fillMaxHeight()
            .border(1.dp, MaterialTheme.colorScheme.onSurface, MaterialTheme.shapes.medium)
            .clickable {
                if (isDefault) onShow(pagerState.currentPage)
                else onDismiss()
            }
    ) {
        if (images.isNotEmpty()) {
            HorizontalPager(pagerState) {
                MedicineImage(
                    image = images[it],
                    editable = !isDefault,
                    modifier = Modifier
                        .fillMaxHeight()
                        .padding(8.dp)
                )
            }
        } else {
            MedicineImage(
                image = null,
                editable = !isDefault,
                modifier = Modifier
                    .fillMaxHeight()
                    .padding(8.dp)
            )
        }

        PageIndicator(
            pageCount = pagerState.pageCount,
            currentPage = pagerState.currentPage,
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 8.dp)
        )
    }
}

@Composable
private fun InfoTextField(
    title: String,
    isEditing: Boolean,
    value: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    placeholder: String = stringResource(R.string.text_empty),
    emptyText: String = stringResource(R.string.text_empty),
    keyboardOptions: KeyboardOptions = KeyboardOptions.Default,
    inputTransformation: InputTransformation? = null,
    outputTransformation: OutputTransformation? = null,
    suffix: @Composable (() -> Unit)? = null
) = Column(modifier.animateContentSize(), Arrangement.spacedBy(8.dp)) {
    Text(
        text = title,
        style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.SemiBold)
    )

    AnimatedContent(isEditing) { editing ->
        if (editing) {
            val textFieldState = rememberTextFieldState(value)

            LaunchedEffect(textFieldState) {
                snapshotFlow { textFieldState.text.toString() }.collectLatest(onValueChange)
            }

            OutlinedTextField(
                state = textFieldState,
                modifier = Modifier.fillMaxWidth(),
                placeholder = { Text(placeholder) },
                inputTransformation = inputTransformation,
                outputTransformation = outputTransformation,
                keyboardOptions = keyboardOptions,
                suffix = suffix
            )
        } else {
            Text(value.ifEmpty { emptyText })
        }
    }
}

@Composable
private fun DialogFullImage(images: List<String>, initialPage: Int, onDismiss: () -> Unit, onShow: (Int) -> Unit) {
    val pagerState = rememberPagerState(initialPage = initialPage, pageCount = images::count)

    LaunchedEffect(pagerState) {
        snapshotFlow(pagerState::currentPage).collectLatest(onShow)
    }

    Dialog(onDismiss) {
        Surface(
            shape = MaterialTheme.shapes.medium,
            modifier = Modifier
                .fillMaxSize()
                .padding(vertical = 120.dp)
        ) {
            Column(Modifier.fillMaxSize(), Arrangement.Center, Alignment.CenterHorizontally) {
                if (images.isNotEmpty()) {
                    HorizontalPager(pagerState) { page ->
                        Box(Modifier.fillMaxWidth(), Alignment.Center) {
                            MedicineImage(images[page], Modifier.size(240.dp, 340.dp))
                        }
                    }
                } else {
                    MedicineImage(null, Modifier.size(240.dp, 340.dp))
                }

                Spacer(Modifier.height(16.dp))

                PageIndicator(pagerState.pageCount, pagerState.currentPage)
            }
        }
    }
}

@Composable
private fun DialogPictureGrid(state: MedicineState, event: (MedicineEvent) -> Unit) {
    val borderColor = MaterialTheme.colorScheme.onSurface

    AlertDialog(
        title = { Text(stringResource(R.string.text_images)) },
        onDismissRequest = { event(ToggleDialog(MedicineDialogState.PictureGrid)) },
        confirmButton = {
            TextButton(
                onClick = { event(ToggleDialog(MedicineDialogState.PictureGrid)) },
                content = { Text(stringResource(R.string.text_save)) }
            )
        },
        dismissButton = {
            TextButton(
                onClick = { event(MedicineEvent.EditImagesOrder) },
                content = {
                    Text(
                        text = stringResource(
                            when (state.imageEditing) {
                                ImageEditing.ADDING -> R.string.text_edit
                                ImageEditing.REORDERING -> R.string.text_add
                            }
                        )
                    )
                }
            )
        },
        text = {
            when (state.imageEditing) {
                ImageEditing.ADDING -> LazyVerticalGrid(
                    columns = GridCells.FixedSize(80.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(state.images) { image ->
                        MedicineImage(
                            image = image,
                            editable = false,
                            modifier = Modifier
                                .size(80.dp, 120.dp)
                                .border(1.dp, borderColor, MaterialTheme.shapes.medium)
                                .padding(4.dp)
                        )
                    }

                    if (state.images.size < 5) {
                        item {
                            Box(
                                contentAlignment = Alignment.Center,
                                modifier = Modifier
                                    .size(80.dp, 120.dp)
                                    .drawBehind {
                                        drawRoundRect(
                                            color = borderColor,
                                            cornerRadius = CornerRadius(16.dp.toPx()),
                                            style = Stroke(
                                                width = 4f,
                                                pathEffect = PathEffect.dashPathEffect(
                                                    intervals = floatArrayOf(10f, 10f),
                                                    phase = 0f
                                                )
                                            )
                                        )
                                    }
                                    .clickable {
                                        event(ToggleDialog(MedicineDialogState.PictureChoose))
                                    }
                            ) {
                                VectorIcon(R.drawable.vector_add)
                            }
                        }
                    }
                }

                ImageEditing.REORDERING -> {
                    val draggableState = rememberDraggableListState { fromIndex, toIndex ->
                        event(MedicineEvent.OnImageReodering(fromIndex, toIndex))
                    }

                    LazyColumn(state = draggableState.listState) {
                        draggableItemsIndexed(draggableState, state.images) { index, image, _ ->
                            ListItem(
                                headlineContent = {},
                                leadingContent = {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        if (state.images.size > 1) {
                                            IconButton(
                                                onClick = { event(MedicineEvent.RemoveImage(image)) },
                                                content = { VectorIcon(R.drawable.vector_delete) }
                                            )
                                        }

                                        MedicineImage(
                                            image = image,
                                            editable = false,
                                            modifier = Modifier.size(64.dp)
                                        )
                                    }
                                },
                                trailingContent = {
                                    VectorIcon(
                                        icon = R.drawable.vector_menu,
                                        modifier = Modifier.dragHandle(draggableState, index)
                                    )
                                },
                                colors = ListItemDefaults.colors(
                                    containerColor = AlertDialogDefaults.containerColor
                                )
                            )
                        }
                    }
                }
            }
        }
    )
}

@Composable
private fun DialogPictureChoose(
    imageCount: Int,
    event: (MedicineEvent) -> Unit,
    onPicked: (ImageProcessing, List<Uri>) -> Unit
) {
    val context = LocalContext.current
    val permissionState = rememberPermissionState(Manifest.permission.CAMERA)

    val maxItems = 5 - imageCount

    val contract = if (maxItems > 1) PickMultipleVisualMedia(maxItems)
    else PickVisualMedia()

    val picker = rememberLauncherForActivityResult(contract) { result ->
        when (val picked = result) {
            is List<*> -> {
                if (picked.isEmpty() || picked.size > maxItems) {
                    return@rememberLauncherForActivityResult
                }

                onPicked(ImageProcessing(context), picked as List<Uri>)
            }

            is Uri? -> picked?.let { uri ->
                onPicked(ImageProcessing(context), listOf(uri))
            }
        }
    }

    @Composable
    fun LocalButton(@StringRes text: Int, @DrawableRes icon: Int, onClick: () -> Unit) = ListItem(
        modifier = Modifier.clickable(onClick = onClick),
        leadingContent = { VectorIcon(icon, Modifier.size(24.dp)) },
        headlineContent = {
            Text(
                text = stringResource(text),
                style = MaterialTheme.typography.labelLarge
            )
        },
        colors = ListItemDefaults.colors().copy(
            containerColor = ButtonDefaults.textButtonColors().containerColor,
            headlineColor = ButtonDefaults.textButtonColors().contentColor,
            leadingIconColor = ButtonDefaults.textButtonColors().contentColor
        )
    )

    AlertDialog(
        onDismissRequest = { event(ToggleDialog(MedicineDialogState.PictureChoose)) },
        dismissButton = {},
        confirmButton = {},
        title = { Text(stringResource(R.string.text_set_image)) },
        text = {
            Column {
                LocalButton(R.string.text_take_picture, R.drawable.vector_add_photo) {
                    if (permissionState.isGranted) event(ToggleDialog(MedicineDialogState.TakePhoto))
                    else if (permissionState.showRationale) permissionState.openSettings()
                    else permissionState.launchRequest()
                }
                LocalButton(R.string.text_choose_from_gallery, R.drawable.vector_add_from_gallery) {
                    picker.launch(PickVisualMediaRequest(PickVisualMedia.ImageOnly))
                }
                LocalButton(R.string.text_pick_icon, R.drawable.vector_medicine) {
                    event(ToggleDialog(MedicineDialogState.Icons))
                }
            }
        }
    )
}

@Composable
private fun IconPicker(onDismiss: () -> Unit, onPick: (String) -> Unit) = Dialog(onDismiss) {
    ElevatedCard(Modifier.padding(vertical = 64.dp)) {
        LazyVerticalGrid(
            columns = GridCells.Adaptive(128.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            contentPadding = PaddingValues(16.dp)
        ) {
            items(DrugType.entries, DrugType::value) { type ->
                ElevatedCard(
                    onClick = { onPick(type.value) },
                    colors = CardDefaults.cardColors(MaterialTheme.colorScheme.secondaryContainer)
                ) {
                    MedicineImage(
                        image = type.icon,
                        modifier = Modifier
                            .size(128.dp)
                            .padding(16.dp)
                            .align(Alignment.CenterHorizontally)
                    )
                    Text(
                        text = stringResource(type.title),
                        color = MaterialTheme.colorScheme.onSecondaryContainer,
                        fontWeight = FontWeight.SemiBold,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.align(Alignment.CenterHorizontally)
                    )
                }
            }
        }
    }
}

@Composable
private fun CameraPhotoPreview(event: (MedicineEvent) -> Unit) {
    val context = LocalContext.current
    val controller = rememberCameraConfig(CameraConfig.UseCases.IMAGE_CAPTURE)

    Box {
        CameraPreview(controller, Modifier.fillMaxSize())

        Row(Modifier.fillMaxWidth(), Arrangement.SpaceBetween, Alignment.CenterVertically) {
            IconButton(
                onClick = { event(ToggleDialog(MedicineDialogState.TakePhoto)) },
                content = { VectorIcon(R.drawable.vector_arrow_back, Modifier, Color.White) } )

            IconButton(controller::toggleTorch) {
                VectorIcon(R.drawable.vector_flash, Modifier, Color.White)
            }
        }

        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
                .padding(bottom = 24.dp)
                .align(Alignment.BottomCenter)
                .size(80.dp)
                .clip(CircleShape)
                .background(Color.White, CircleShape)
                .border(4.dp, Color.LightGray, CircleShape)
                .clickable {
                    controller.takePicture(
                        onStart = { event(MedicineEvent.ShowLoading) },
                        onResult = { event(MedicineEvent.SetImage(ImageProcessing(context), it)) }
                    )
                }
        ) {
            VectorIcon(
                icon = R.drawable.vector_add_photo,
                modifier = Modifier.size(40.dp),
                tint = Color.Black
            )
        }
    }
}

@Composable
private fun DoseDropdownMenu(doseTitle: String, setDoseType: (DoseType) -> Unit) {
    var isExpanded by remember { mutableStateOf(false) }

    ExposedDropdownMenuBox(
        expanded = isExpanded,
        onExpandedChange = { isExpanded = !isExpanded },
        modifier = Modifier.width(64.dp)
    ) {
        Row(
            horizontalArrangement = Arrangement.End,
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .menuAnchor(ExposedDropdownMenuAnchorType.PrimaryNotEditable)
                .fillMaxWidth()
        ) {
            Text(doseTitle)
            ExposedDropdownMenuDefaults.TrailingIcon(isExpanded)
        }
        ExposedDropdownMenu(
            expanded = isExpanded,
            onDismissRequest = { isExpanded = false }
        ) {
            DoseType.entries.forEach { item ->
                DropdownMenuItem(
                    text = { Text(stringResource(item.title)) },
                    contentPadding = ExposedDropdownMenuDefaults.ItemContentPadding,
                    onClick = {
                        setDoseType(item)
                        isExpanded = false
                    }
                )
            }
        }
    }
}

@Composable
private fun PageIndicator(pageCount: Int, currentPage: Int, modifier: Modifier = Modifier) {
    if (pageCount > 1) {
        Row(modifier.fillMaxWidth(), Arrangement.Center) {
            repeat(pageCount) { index ->
                Box(
                    modifier = Modifier
                        .padding(2.dp)
                        .clip(CircleShape)
                        .size(12.dp)
                        .background(
                            color = MaterialTheme.colorScheme.onSurface.copy(
                                alpha = if (currentPage == index) 0.3f
                                else 0.7f
                            )
                        )
                )
            }
        }
    }
}

@Composable
private fun DialogDataLoss(onDismiss: () -> Unit, onBack: () -> Unit) = AlertDialog(
    onDismissRequest = onDismiss,
    confirmButton = { TextButton(onBack) { Text(stringResource(R.string.text_exit)) } },
    dismissButton = { TextButton(onDismiss) { Text(stringResource(R.string.text_stay)) } },
    text = {
        Text(
            text = stringResource(R.string.text_not_saved_medicine),
            style = MaterialTheme.typography.bodyLarge
        )
    }
)