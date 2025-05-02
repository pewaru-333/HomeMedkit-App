package ru.application.homemedkit.ui.screens

import android.Manifest
import android.content.Context
import android.net.Uri
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts.PickMultipleVisualMedia
import androidx.activity.result.contract.ActivityResultContracts.PickVisualMedia
import androidx.camera.view.CameraController
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.gestures.waitForUpOrCancellation
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
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
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.relocation.BringIntoViewRequester
import androidx.compose.foundation.relocation.bringIntoViewRequester
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.toggleable
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material.icons.outlined.Check
import androidx.compose.material.icons.outlined.MoreVert
import androidx.compose.material.icons.outlined.Notifications
import androidx.compose.material.icons.outlined.Refresh
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MenuAnchorType
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Snackbar
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusDirection
import androidx.compose.ui.focus.FocusManager
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.focusTarget
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.PointerEventPass
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.OffsetMapping
import androidx.compose.ui.text.input.TransformedText
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import coil3.compose.AsyncImage
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import ru.application.homemedkit.R
import ru.application.homemedkit.R.drawable.vector_add_photo
import ru.application.homemedkit.R.drawable.vector_flash
import ru.application.homemedkit.R.string.placeholder_dose
import ru.application.homemedkit.R.string.preference_kits_group
import ru.application.homemedkit.R.string.text_amount
import ru.application.homemedkit.R.string.text_cancel
import ru.application.homemedkit.R.string.text_choose_from_gallery
import ru.application.homemedkit.R.string.text_clear
import ru.application.homemedkit.R.string.text_confirm_deletion_med
import ru.application.homemedkit.R.string.text_connection_error
import ru.application.homemedkit.R.string.text_delete
import ru.application.homemedkit.R.string.text_duplicate
import ru.application.homemedkit.R.string.text_edit
import ru.application.homemedkit.R.string.text_empty
import ru.application.homemedkit.R.string.text_error_not_medicine
import ru.application.homemedkit.R.string.text_exp_date
import ru.application.homemedkit.R.string.text_indications_for_use
import ru.application.homemedkit.R.string.text_medicine_comment
import ru.application.homemedkit.R.string.text_medicine_composition
import ru.application.homemedkit.R.string.text_medicine_description
import ru.application.homemedkit.R.string.text_medicine_display_name
import ru.application.homemedkit.R.string.text_medicine_dose
import ru.application.homemedkit.R.string.text_medicine_group
import ru.application.homemedkit.R.string.text_medicine_product_name
import ru.application.homemedkit.R.string.text_medicine_recommendations
import ru.application.homemedkit.R.string.text_medicine_status_checked
import ru.application.homemedkit.R.string.text_medicine_status_scanned
import ru.application.homemedkit.R.string.text_medicine_status_self_added
import ru.application.homemedkit.R.string.text_medicine_storage_conditions
import ru.application.homemedkit.R.string.text_package_opened_date
import ru.application.homemedkit.R.string.text_pick_icon
import ru.application.homemedkit.R.string.text_save
import ru.application.homemedkit.R.string.text_set_image
import ru.application.homemedkit.R.string.text_status
import ru.application.homemedkit.R.string.text_take_picture
import ru.application.homemedkit.R.string.text_try_again
import ru.application.homemedkit.R.string.text_unspecified
import ru.application.homemedkit.R.string.text_update
import ru.application.homemedkit.data.dto.Kit
import ru.application.homemedkit.dialogs.DatePicker
import ru.application.homemedkit.dialogs.MonthYear
import ru.application.homemedkit.helpers.decimalFormat
import ru.application.homemedkit.helpers.enums.DoseType
import ru.application.homemedkit.helpers.enums.DrugType
import ru.application.homemedkit.helpers.permissions.rememberPermissionState
import ru.application.homemedkit.models.events.MedicineEvent
import ru.application.homemedkit.models.events.Response
import ru.application.homemedkit.models.states.MedicineState
import ru.application.homemedkit.models.states.rememberCameraState
import ru.application.homemedkit.models.viewModels.MedicineViewModel
import java.io.File


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MedicineScreen(navigateBack: () -> Unit, navigateToIntake: (Long) -> Unit) {
    val filesDir = LocalContext.current.filesDir
    val focusManager = LocalFocusManager.current

    val model = viewModel<MedicineViewModel>()
    val state by model.state.collectAsStateWithLifecycle()
    val kits by model.kits.collectAsStateWithLifecycle()
    val response by model.response.collectAsStateWithLifecycle(
        initialValue = null,
        context = Dispatchers.Main.immediate
    )

    BackHandler { if (state.showTakePhoto) model.onEvent(MedicineEvent.ShowTakePhoto) else navigateBack() }
    Scaffold(
        topBar = {
            TopAppBar(
                title = {},
                navigationIcon = {
                    IconButton(navigateBack) {
                        Icon(Icons.AutoMirrored.Outlined.ArrowBack, null)
                    }
                },
                actions = {
                    if (state.default) {
                        focusManager.clearFocus(true)
                        var expanded by remember { mutableStateOf(false) }

                        IconButton({ navigateToIntake(state.id) }) {
                            Icon(Icons.Outlined.Notifications, null)
                        }

                        IconButton({ expanded = true }) {
                            Icon(Icons.Outlined.MoreVert, null)
                        }

                        DropdownMenu(expanded, { expanded = false }) {
                            DropdownMenuItem(
                                onClick = model::setEditing,
                                text = {
                                    Text(
                                        text = stringResource(text_edit),
                                        style = MaterialTheme.typography.bodyLarge
                                    )
                                }
                            )
                            DropdownMenuItem(
                                text = {
                                    Text(
                                        text = stringResource(text_delete),
                                        style = MaterialTheme.typography.bodyLarge
                                    )
                                },
                                onClick = {
                                    model.onEvent(MedicineEvent.ShowDialogDelete)
                                    expanded = false
                                }
                            )
                        }
                    } else IconButton(
                        onClick = {
                            if (state.adding) model.add() else model.update()
                        }
                    ) {
                        Icon(Icons.Outlined.Check, null)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    navigationIconContentColor = MaterialTheme.colorScheme.onPrimaryContainer,
                    actionIconContentColor = MaterialTheme.colorScheme.onPrimaryContainer
                )
            )
        },
        snackbarHost = {
            SnackbarHost(state.snackbarHostState) {
                Snackbar(
                    snackbarData = it,
                    containerColor = MaterialTheme.colorScheme.errorContainer,
                    contentColor = MaterialTheme.colorScheme.onErrorContainer,
                )
            }
        },
        floatingActionButton = {
            if (state.technical.scanned && !state.technical.verified)
                ExtendedFloatingActionButton(
                    text = { Text(stringResource(text_update)) },
                    icon = { Icon(Icons.Outlined.Refresh, null) },
                    onClick = { model.fetch(filesDir) }
                )
        }
    ) { values ->
        LazyColumn(
            modifier = Modifier.imePadding(),
            contentPadding = PaddingValues(16.dp, values.calculateTopPadding().plus(16.dp)),
            verticalArrangement = Arrangement.spacedBy(32.dp)
        ) {
            item {
                Row(Modifier.height(256.dp), Arrangement.spacedBy(12.dp)) {
                    ProductImage(state, model::onEvent)
                    ProductBrief(state, focusManager, model::onEvent)
                }
            }
            if (state.adding || state.editing || state.nameAlias.isNotEmpty()) {
                item { ProductAlias(state, focusManager, model::onEvent) }
            }
            item {
                ProductFormName(state, focusManager, model::onEvent)
            }
            item {
                ProductNormName(state, model::onEvent)
            }
            if (state.default && state.structure.isNotEmpty()) {
                item { Structure(state.structure) }
            }
            if (state.adding || state.editing || state.phKinetics.isNotEmpty()) {
                item { PhKinetics(state, focusManager, model::onEvent) }
            }
            if (state.default && state.recommendations.isNotEmpty()) {
                item { Recommendations(state.recommendations) }
            }
            if (state.default && state.storageConditions.isNotEmpty()) {
                item { StorageConditions(state.storageConditions) }
            }
            if (state.adding || state.editing || state.comment.isNotEmpty()) {
                item { Comment(state, model::onEvent) }
            }
        }
    }

    when (response) {
        null -> Unit
        Response.Loading -> LoadingDialog()
        Response.Duplicate -> model.showSnackbar(stringResource(text_duplicate))
        Response.IncorrectCode -> model.showSnackbar(stringResource(text_error_not_medicine))
        Response.UnknownError -> model.showSnackbar(stringResource(text_try_again))
        is Response.Success -> Unit
        is Response.NetworkError -> model.showSnackbar(stringResource(text_connection_error))
    }

    when {
        state.showDialogKits -> DialogKits(kits, state, model::onEvent)
        state.showDialogPictureChoose -> DialogPictureChoose(model::onEvent, model::compressImage)
        state.showDialogFullImage -> DialogFullImage(state, model::onEvent)
        state.showDialogIcons -> IconPicker(model::onEvent)
        state.showDialogDelete -> DialogDelete(
            text = text_confirm_deletion_med,
            cancel = { model.onEvent(MedicineEvent.ShowDialogDelete) },
            confirm = {
                model.delete(filesDir)
                navigateBack()
            }
        )

        state.showDialogDate -> MonthYear(
            cancel = { model.onEvent(MedicineEvent.ShowDatePicker) },
            confirm = { month, year ->
                model.onEvent(MedicineEvent.SetExpDate(month, year))
            }
        )

        state.showDialogPackageDate -> DatePicker(
            onDismiss = { model.onEvent(MedicineEvent.ShowPackageDatePicker) },
            onSelect = { model.onEvent(MedicineEvent.SetPackageDate(it)) }
        )

        state.showTakePhoto -> CameraPhotoPreview(model::onEvent)
    }
}

@Composable
private fun ProductBrief(
    state: MedicineState,
    focusManager: FocusManager,
    event: (MedicineEvent) -> Unit
) = Column(
    verticalArrangement = Arrangement.SpaceBetween,
    modifier = Modifier
        .fillMaxHeight()
        .verticalScroll(rememberScrollState())
) {
    ProductName(state, focusManager, event)
    ProductKit(state, event)
    ProductExp(state, event)
    ProductOpened(state, event)
    if (state.default) ProductStatus(state)
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun ProductName(
    state: MedicineState,
    focusManager: FocusManager,
    event: (MedicineEvent) -> Unit
) = Column {
    if (state.default || state.technical.verified) {
        Text(
            text = stringResource(text_medicine_product_name),
            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.W400)
        )

        Text(
            text = state.productName,
            softWrap = false,
            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold),
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState())
        )
    } else {
        val scope = rememberCoroutineScope()
        val focusRequester = remember(::FocusRequester)
        val viewRequester = remember(::BringIntoViewRequester)

        LaunchedEffect(state.productNameError) {
            if (state.productNameError != null) focusRequester.requestFocus()
        }

        OutlinedTextField(
            value = state.productName,
            onValueChange = { event(MedicineEvent.SetProductName(it)) },
            singleLine = true,
            isError = state.productNameError != null,
            label = { Text(stringResource(text_medicine_product_name)) },
            supportingText = state.productNameError?.let { { Text(stringResource(it)) } },
            keyboardOptions = KeyboardOptions(
                capitalization = KeyboardCapitalization.Sentences,
                imeAction = ImeAction.Next
            ),
            keyboardActions = KeyboardActions(
                onNext = { focusManager.moveFocus(FocusDirection.Down) }
            ),
            modifier = Modifier
                .fillMaxWidth()
                .bringIntoViewRequester(viewRequester)
                .focusRequester(focusRequester)
                .onFocusChanged { if (it.isFocused) scope.launch { viewRequester.bringIntoView() } }
                .focusTarget()
        )
    }
}

@Composable
private fun ProductOpened(state: MedicineState, event: (MedicineEvent) -> Unit) = Column {
    if (state.default) {
        Text(
            text = stringResource(text_package_opened_date),
            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.W400)
        )
        Text(
            text = state.dateOpenedString.ifEmpty { stringResource(text_unspecified) },
            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold)
        )
    } else OutlinedTextField(
        value = state.dateOpenedString,
        onValueChange = {},
        readOnly = true,
        label = { Text(stringResource(text_package_opened_date)) },
        modifier = Modifier
            .fillMaxWidth()
            .pointerInput(Unit) {
                awaitEachGesture {
                    awaitFirstDown(pass = PointerEventPass.Initial)
                    val up = waitForUpOrCancellation(pass = PointerEventPass.Initial)
                    up?.let { event(MedicineEvent.ShowPackageDatePicker) }
                }
            }
    )
}

@Composable
private fun ProductKit(state: MedicineState, event: (MedicineEvent) -> Unit) = Column {
    if (state.default) {
        Text(
            text = stringResource(text_medicine_group),
            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.W400)
        )
        Text(
            maxLines = 1,
            modifier = Modifier.horizontalScroll(rememberScrollState()),
            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold),
            text = if (state.kits.isEmpty()) stringResource(text_unspecified)
            else state.kits.joinToString(transform = Kit::title),
        )
    } else OutlinedTextField(
        value = state.kits.joinToString(transform = Kit::title),
        onValueChange = {},
        singleLine = true,
        readOnly = true,
        label = { Text(stringResource(text_medicine_group)) },
        placeholder = { Text(stringResource(text_empty)) },
        modifier = Modifier
            .fillMaxWidth()
            .pointerInput(Unit) {
                awaitEachGesture {
                    awaitFirstDown(pass = PointerEventPass.Initial)
                    val up = waitForUpOrCancellation(pass = PointerEventPass.Initial)
                    up?.let { event(MedicineEvent.ShowKitDialog) }
                }
            }
    )
}

@Composable
private fun ProductExp(state: MedicineState, event: (MedicineEvent) -> Unit) = Column {
    when {
        state.default || state.technical.verified -> {
            Text(
                text = stringResource(text_exp_date),
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.W400)
            )
            Text(
                text = state.expDateString.ifEmpty { stringResource(text_unspecified) },
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold)
            )
        }

        (state.adding || state.editing) && !state.technical.verified -> OutlinedTextField(
            value = state.expDateString,
            onValueChange = {},
            readOnly = true,
            label = { Text(stringResource(text_exp_date)) },
            modifier = Modifier
                .fillMaxWidth()
                .pointerInput(Unit) {
                    awaitEachGesture {
                        awaitFirstDown(pass = PointerEventPass.Initial)
                        val up = waitForUpOrCancellation(pass = PointerEventPass.Initial)
                        up?.let { event(MedicineEvent.ShowDatePicker) }
                    }
                }
        )
    }
}

@Composable
private fun ProductStatus(state: MedicineState) = Column {
    Text(
        text = stringResource(text_status),
        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.W400)
    )
    Text(
        text = stringResource(
            if (state.technical.verified) text_medicine_status_checked
            else if (state.technical.scanned && !state.technical.verified) text_medicine_status_scanned
            else text_medicine_status_self_added
        ),
        style = MaterialTheme.typography.titleMedium.copy(
            fontWeight = FontWeight.SemiBold,
            color = if (state.technical.verified) MaterialTheme.colorScheme.primary
            else if (state.technical.scanned && !state.technical.verified) MaterialTheme.colorScheme.onBackground
            else MaterialTheme.colorScheme.error
        )
    )
}

@Composable
private fun ProductImage(state: MedicineState, event: (MedicineEvent) -> Unit) {
    val pagerState = rememberPagerState(pageCount = state.images::count)

    Box(
        modifier = Modifier
            .width(128.dp)
            .fillMaxHeight()
            .border(1.dp, MaterialTheme.colorScheme.onSurface, MaterialTheme.shapes.medium)
            .clickable {
                if (state.default) event(MedicineEvent.ShowDialogFullImage(pagerState.currentPage))
                else event(MedicineEvent.ShowDialogPictureChoose)
            }
    ) {
        HorizontalPager(pagerState, Modifier.fillMaxSize()) {
            MedicineImage(
                image = state.images[it],
                editable = !state.default,
                modifier = Modifier
                    .fillMaxHeight()
                    .padding(8.dp)
            )
        }
        if (pagerState.pageCount > 1) {
            Row(
                horizontalArrangement = Arrangement.Center,
                modifier = Modifier
                    .fillMaxWidth()
                    .wrapContentHeight()
                    .align(Alignment.BottomCenter)
                    .padding(bottom = 8.dp)
            ) {
                repeat(pagerState.pageCount) { index ->
                    Box(
                        modifier = Modifier
                            .padding(2.dp)
                            .clip(CircleShape)
                            .background(if (pagerState.currentPage == index) Color.DarkGray else Color.LightGray)
                            .size(12.dp)
                    )
                }
            }
        }
    }
}

@Composable
private fun ProductAlias(
    state: MedicineState,
    focusManager: FocusManager,
    event: (MedicineEvent) -> Unit
) = Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
    Text(
        text = stringResource(text_medicine_display_name),
        style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold)
    )

    if (state.default && state.nameAlias.isNotEmpty()) Text(state.nameAlias)
    else OutlinedTextField(
        value = state.nameAlias,
        onValueChange = { event(MedicineEvent.SetNameAlias(it)) },
        modifier = Modifier.fillMaxWidth(),
        placeholder = { Text(stringResource(text_empty)) },
        keyboardOptions = KeyboardOptions(
            capitalization = KeyboardCapitalization.Sentences,
            imeAction = ImeAction.Next
        ),
        keyboardActions = KeyboardActions(
            onNext = { focusManager.moveFocus(FocusDirection.Down) }
        )
    )
}

@Composable
private fun ProductFormName(
    state: MedicineState,
    focusManager: FocusManager,
    event: (MedicineEvent) -> Unit
) = Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
    Text(
        text = stringResource(text_medicine_description),
        style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold)
    )

    if (state.default || state.technical.verified) Text(state.prodFormNormName)
    else OutlinedTextField(
        value = state.prodFormNormName,
        onValueChange = { event(MedicineEvent.SetFormName(it)) },
        modifier = Modifier.fillMaxWidth(),
        placeholder = { Text(stringResource(text_empty)) },
        keyboardOptions = KeyboardOptions(
            capitalization = KeyboardCapitalization.Sentences,
            imeAction = ImeAction.Next
        ),
        keyboardActions = KeyboardActions(
            onNext = { focusManager.moveFocus(FocusDirection.Down) }
        )
    )
}

@Composable
private fun Structure(structure: String) =
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(
            text = stringResource(text_medicine_composition),
            style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold)
        )
        Text(structure, Modifier.fillMaxWidth())
    }

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ProductNormName(state: MedicineState, event: (MedicineEvent) -> Unit) =
    Row(Modifier.fillMaxWidth(), Arrangement.spacedBy(16.dp)) {
        Column(Modifier.weight(0.5f), Arrangement.spacedBy(8.dp)) {
            Text(
                text = stringResource(text_medicine_dose),
                style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold)
            )

            if (state.adding || state.editing && !state.technical.verified) OutlinedTextField(
                value = state.prodDNormName,
                onValueChange = { event(MedicineEvent.SetDoseName(it)) },
                modifier = Modifier.fillMaxWidth(),
                placeholder = { Text(stringResource(placeholder_dose)) },
                keyboardOptions = KeyboardOptions(KeyboardCapitalization.Sentences)
            ) else Text(state.prodDNormName.ifEmpty { stringResource(text_unspecified) })
        }

        Column(Modifier.weight(0.5f), Arrangement.spacedBy(8.dp)) {
            Text(
                text = stringResource(text_amount),
                style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold)
            )

            if (state.default) Text(
                "${decimalFormat(state.prodAmount)} " + stringResource(state.doseType.title)
            )
            else OutlinedTextField(
                value = state.prodAmount,
                onValueChange = { event(MedicineEvent.SetAmount(it)) },
                modifier = Modifier.fillMaxWidth(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                placeholder = { Text(stringResource(text_empty)) },
                visualTransformation = {
                    TransformedText(
                        AnnotatedString(it.text.replace('.', ',')),
                        OffsetMapping.Identity
                    )
                },
                suffix = {
                    ExposedDropdownMenuBox(
                        expanded = state.showMenuDose,
                        onExpandedChange = { event(MedicineEvent.ShowDoseMenu) },
                        modifier = Modifier.width(64.dp)
                    ) {
                        Row(
                            horizontalArrangement = Arrangement.End,
                            modifier = Modifier
                                .menuAnchor(MenuAnchorType.PrimaryNotEditable)
                                .fillMaxWidth()
                        ) {
                            Text(stringResource(state.doseType.title))
                            ExposedDropdownMenuDefaults.TrailingIcon(state.showMenuDose)
                        }
                        ExposedDropdownMenu(
                            expanded = state.showMenuDose,
                            onDismissRequest = { event(MedicineEvent.ShowDoseMenu) }
                        ) {
                            DoseType.entries.forEach { item ->
                                DropdownMenuItem(
                                    text = { Text(stringResource(item.title)) },
                                    onClick = { event(MedicineEvent.SetDoseType(item)) },
                                    contentPadding = ExposedDropdownMenuDefaults.ItemContentPadding
                                )
                            }
                        }
                    }
                }
            )
        }
    }

@Composable
private fun PhKinetics(
    state: MedicineState,
    focusManager: FocusManager,
    event: (MedicineEvent) -> Unit
) = Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
    Text(
        text = stringResource(text_indications_for_use),
        style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold)
    )

    if (state.default) Text(state.phKinetics)
    else OutlinedTextField(
        value = state.phKinetics,
        onValueChange = { event(MedicineEvent.SetPhKinetics(it)) },
        modifier = Modifier.fillMaxWidth(),
        placeholder = { Text(stringResource(text_empty)) },
        keyboardOptions = KeyboardOptions(
            capitalization = KeyboardCapitalization.Sentences,
            imeAction = ImeAction.Next
        ),
        keyboardActions = KeyboardActions(
            onNext = { focusManager.moveFocus(FocusDirection.Down) }
        )
    )
}

@Composable
private fun Recommendations(recommendations: String) =
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(
            text = stringResource(text_medicine_recommendations),
            style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold)
        )
        Text(recommendations)
    }

@Composable
private fun StorageConditions(conditions: String) =
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(
            text = stringResource(text_medicine_storage_conditions),
            style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold)
        )
        Text(conditions)
    }

@Composable
private fun Comment(state: MedicineState, event: (MedicineEvent) -> Unit) =
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(
            text = stringResource(text_medicine_comment),
            style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold)
        )

        if (state.default) Text(state.comment)
        else OutlinedTextField(
            value = state.comment,
            onValueChange = { event(MedicineEvent.SetComment(it)) },
            modifier = Modifier.fillMaxWidth(),
            placeholder = { Text(stringResource(text_empty)) },
            keyboardOptions = KeyboardOptions(
                capitalization = KeyboardCapitalization.Sentences,
                imeAction = ImeAction.Done
            )
        )
    }

@Composable
private fun DialogKits(
    kits: List<Kit>,
    state: MedicineState,
    event: (MedicineEvent) -> Unit
) = AlertDialog(
    onDismissRequest = { event(MedicineEvent.ShowKitDialog) },
    title = { Text(stringResource(preference_kits_group)) },
    dismissButton = {
        TextButton(
            onClick = { event(MedicineEvent.ClearKit) }
        ) {
            Text(stringResource(text_clear))
        }
    },
    confirmButton = {
        TextButton(
            onClick = { event(MedicineEvent.ShowKitDialog) }
        ) {
            Text(stringResource(text_save))
        }
    },
    text = {
        LazyColumn {
            items(kits) { kit ->
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp)
                        .toggleable(
                            value = kit in state.kits,
                            onValueChange = { event(MedicineEvent.PickKit(kit)) },
                            role = Role.Checkbox
                        )
                ) {
                    Checkbox(kit in state.kits, null)
                    Text(
                        text = kit.title,
                        modifier = Modifier.padding(start = 16.dp),
                        style = MaterialTheme.typography.bodyLarge
                    )
                }
            }
        }
    }
)

@Composable
private fun DialogFullImage(state: MedicineState, event: (MedicineEvent) -> Unit) {
    val pagerState = rememberPagerState(initialPage = state.fullImage, pageCount = state.images::count)

    LaunchedEffect(pagerState) {
        snapshotFlow(pagerState::currentPage).collectLatest {
            event(MedicineEvent.SetFullImage(it))
        }
    }

    Dialog(onDismissRequest = { event(MedicineEvent.ShowDialogFullImage()) }) {
        HorizontalPager(pagerState, Modifier.fillMaxSize()) { page ->
            Column(Modifier.fillMaxSize(), Arrangement.Center, Alignment.CenterHorizontally) {
                MedicineImage(state.images[page], Modifier.size(240.dp, 340.dp))

                Spacer(Modifier.height(16.dp))

                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.Center) {
                    repeat(pagerState.pageCount) { index ->
                        Box(
                            modifier = Modifier
                                .padding(2.dp)
                                .clip(CircleShape)
                                .background(
                                    if (pagerState.currentPage == index) Color.White
                                    else Color.LightGray
                                )
                                .size(16.dp)
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun DialogPictureChoose(event: (MedicineEvent) -> Unit, onPicked: (Context, List<Uri>) -> Unit) {
    val context = LocalContext.current
    val cameraPermissionState = rememberPermissionState(Manifest.permission.CAMERA)

    val picker = rememberLauncherForActivityResult(PickMultipleVisualMedia(5)) { items ->
        if (items.isEmpty() || items.size > 5) return@rememberLauncherForActivityResult

        onPicked(context, items)
    }

    AlertDialog(
        onDismissRequest = { event(MedicineEvent.ShowDialogPictureChoose) },
        dismissButton = {},
        confirmButton = {},
        title = { Text(stringResource(text_set_image)) },
        text = {
            Column {
                TextButton(
                    onClick = {
                        if (cameraPermissionState.isGranted) event(MedicineEvent.ShowTakePhoto)
                        else if (cameraPermissionState.showRationale) cameraPermissionState.openSettings()
                        else cameraPermissionState.launchRequest()
                    }
                ) {
                    Text(
                        text = stringResource(text_take_picture),
                        textAlign = TextAlign.Start,
                        modifier = Modifier.fillMaxWidth(),
                        style = MaterialTheme.typography.labelLarge.copy(fontSize = 16.sp)
                    )
                }
                TextButton(
                    onClick = { picker.launch(PickVisualMediaRequest(PickVisualMedia.ImageOnly)) }
                ) {
                    Text(
                        text = stringResource(text_choose_from_gallery),
                        textAlign = TextAlign.Start,
                        modifier = Modifier.fillMaxWidth(),
                        style = MaterialTheme.typography.labelLarge.copy(fontSize = 16.sp)
                    )
                }
                TextButton(
                    onClick = { event(MedicineEvent.ShowIconPicker) }
                ) {
                    Text(
                        text = stringResource(text_pick_icon),
                        textAlign = TextAlign.Start,
                        modifier = Modifier.fillMaxWidth(),
                        style = MaterialTheme.typography.labelLarge.copy(fontSize = 16.sp)
                    )
                }
            }
        }
    )
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun IconPicker(event: (MedicineEvent) -> Unit) =
    Dialog(
        onDismissRequest = { event(MedicineEvent.ShowIconPicker) }
    ) {
        Surface(Modifier.padding(vertical = 64.dp), RoundedCornerShape(16.dp)) {
            LazyVerticalGrid(
                columns = GridCells.Adaptive(140.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                contentPadding = PaddingValues(16.dp)
            ) {
                items(DrugType.entries) {
                    ElevatedCard(
                        onClick = { event(MedicineEvent.SetIcon(it.value)) },
                        colors = CardDefaults.cardColors(MaterialTheme.colorScheme.secondaryContainer)
                    ) {
                        Image(
                            painter = painterResource(it.icon),
                            contentDescription = null,
                            modifier = Modifier
                                .size(128.dp)
                                .padding(16.dp)
                                .align(Alignment.CenterHorizontally)
                        )
                        Text(
                            text = stringResource(it.title),
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
fun DialogDelete(text: Int, cancel: () -> Unit, confirm: () -> Unit) = AlertDialog(
    onDismissRequest = cancel,
    confirmButton = { TextButton(confirm) { Text(stringResource(text_delete)) } },
    dismissButton = { TextButton(cancel) { Text(stringResource(text_cancel)) } },
    text = {
        Text(
            text = stringResource(text),
            style = MaterialTheme.typography.bodyLarge
        )
    }
)

@Composable
fun MedicineImage(image: String, modifier: Modifier = Modifier, editable: Boolean = false) {
    val context = LocalContext.current

    val model = remember(image, context) {
        DrugType.getIcon(image) ?: File(context.filesDir, image)
    }

    AsyncImage(
        model = model,
        modifier = modifier,
        contentDescription = null,
        error = painterResource(R.drawable.vector_type_unknown),
        alpha = if (editable) 0.4f else 1f
    )
}

@Composable
private fun CameraPhotoPreview(event: (MedicineEvent) -> Unit) {
    val controller = rememberCameraState(CameraController.IMAGE_CAPTURE)

    Box {
        CameraPreview(controller, Modifier.fillMaxSize())

        Row(Modifier.fillMaxWidth(), Arrangement.SpaceBetween, Alignment.CenterVertically) {
            IconButton(
                onClick = { event(MedicineEvent.ShowTakePhoto) }
            ) {
                Icon(Icons.AutoMirrored.Outlined.ArrowBack, null, Modifier, Color.White)
            }

            IconButton(controller::toggleTorch) {
                Icon(painterResource(vector_flash), null, Modifier, Color.White)
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
                    controller.takePicture {
                        event(MedicineEvent.SetImage(mutableStateListOf(it)))
                    }
                }
        ) {
            Icon(
                painter = painterResource(vector_add_photo),
                contentDescription = null,
                modifier = Modifier.size(40.dp),
                tint = Color.Black
            )
        }
    }
}