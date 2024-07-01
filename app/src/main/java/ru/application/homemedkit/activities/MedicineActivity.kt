package ru.application.homemedkit.activities

import android.content.Context
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Scaffold
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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringArrayResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.OffsetMapping
import androidx.compose.ui.text.input.TransformedText
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.rememberAsyncImagePainter
import com.ramcosta.composedestinations.annotation.Destination
import com.ramcosta.composedestinations.annotation.RootGraph
import com.ramcosta.composedestinations.generated.destinations.IntakeScreenDestination
import com.ramcosta.composedestinations.generated.destinations.MedicineScreenDestination
import com.ramcosta.composedestinations.generated.destinations.MedicinesScreenDestination
import com.ramcosta.composedestinations.navigation.DestinationsNavigator
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.collectLatest
import ru.application.homemedkit.R
import ru.application.homemedkit.databaseController.MedicineDatabase
import ru.application.homemedkit.dialogs.MonthYear
import ru.application.homemedkit.helpers.BLANK
import ru.application.homemedkit.helpers.DateHelper.toExpDate
import ru.application.homemedkit.helpers.DateHelper.toTimestamp
import ru.application.homemedkit.helpers.ICONS_MED
import ru.application.homemedkit.helpers.TYPE
import ru.application.homemedkit.helpers.decimalFormat
import ru.application.homemedkit.helpers.formName
import ru.application.homemedkit.helpers.fromHTML
import ru.application.homemedkit.helpers.viewModelFactory
import ru.application.homemedkit.states.MedicineState
import ru.application.homemedkit.viewModels.ActivityEvents
import ru.application.homemedkit.viewModels.MedicineEvent
import ru.application.homemedkit.viewModels.MedicineViewModel
import ru.application.homemedkit.viewModels.ResponseState
import java.io.File


@OptIn(ExperimentalMaterial3Api::class)
@Destination<RootGraph>
@Composable
fun MedicineScreen(
    id: Long = 0L,
    cis: String = BLANK,
    duplicate: Boolean = false,
    navigator: DestinationsNavigator,
    context: Context = LocalContext.current
) {
    val database = MedicineDatabase.getInstance(context)
    val viewModel = viewModel<MedicineViewModel>(factory = viewModelFactory {
        MedicineViewModel(database.medicineDAO(), id)
    })

    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val response by viewModel.response.collectAsStateWithLifecycle()
    val path = context.filesDir

    if (id == 0L) {
        viewModel.onEvent(MedicineEvent.SetAdding)
        viewModel.onEvent(MedicineEvent.SetCis(cis))
    }

    if (duplicate) {
        var show by remember { mutableStateOf(true) }
        if (show) Snackbar(R.string.text_duplicate)

        LaunchedEffect(Unit) { delay(2000); show = false }
    }

    LaunchedEffect(Unit) {
        viewModel.events.collectLatest { event ->
            when (event) {
                ActivityEvents.Start -> navigator.navigate(MedicineScreenDestination(state.id))
                ActivityEvents.Close -> {
                    File(path, state.image).delete()
                    navigator.navigate(MedicinesScreenDestination)
                }
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {},
                navigationIcon = {
                    IconButton({ navigator.navigate(MedicinesScreenDestination) }) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    }
                },
                actions = {
                    when {
                        state.adding -> {
                            IconButton(
                                onClick = { viewModel.onEvent(MedicineEvent.Add) },
                                enabled = state.productName.isNotEmpty()
                            )
                            { Icon(Icons.Default.Check, null) }
                        }

                        state.editing -> {
                            IconButton(
                                onClick = { viewModel.onEvent(MedicineEvent.Update) },
                                enabled = state.productName.isNotEmpty()
                            )
                            { Icon(Icons.Default.Check, null) }
                        }

                        else -> {
                            LocalFocusManager.current.clearFocus(true)
                            var expanded by remember { mutableStateOf(false) }

                            IconButton({
                                navigator.navigate(IntakeScreenDestination(medicineId = state.id))
                            }) { Icon(Icons.Default.Notifications, null) }

                            IconButton({ expanded = true }) {
                                Icon(Icons.Default.MoreVert, null)
                            }

                            DropdownMenu(expanded, { expanded = false }) {
                                DropdownMenuItem(
                                    text = { Text(stringResource(R.string.text_edit)) },
                                    onClick = { viewModel.onEvent(MedicineEvent.SetEditing) }
                                )
                                DropdownMenuItem(
                                    text = { Text(stringResource(R.string.text_delete)) },
                                    onClick = { viewModel.onEvent(MedicineEvent.Delete) }
                                )
                            }
                        }
                    }
                },

                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    navigationIconContentColor = MaterialTheme.colorScheme.onPrimaryContainer,
                    actionIconContentColor = MaterialTheme.colorScheme.onPrimaryContainer
                )
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(top = paddingValues.calculateTopPadding())
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(32.dp)
        ) {
            ProductName(viewModel::onEvent, state)
            ProductImage(viewModel, state)
            ExpirationDate(viewModel::onEvent, state)
            ProductFormName(viewModel::onEvent, state)
            ProductNormName(viewModel::onEvent, state)
            PhKinetics(viewModel::onEvent, state)
            Comment(viewModel::onEvent, state)
        }
    }

    when (val data = response) {
        ResponseState.Default -> {}
        ResponseState.Loading -> LoadingDialog()
        is ResponseState.Success -> navigator.navigate(MedicineScreenDestination(data.id))
        is ResponseState.NoNetwork -> Snackbar(R.string.text_connection_error)

        else -> Snackbar(R.string.text_try_again)
    }

    BackHandler { navigator.navigate(MedicinesScreenDestination) }
}

@Composable
private fun ProductName(onEvent: (MedicineEvent) -> Unit, state: MedicineState) {
    Column(Modifier.padding(16.dp, 16.dp, 16.dp, 0.dp), Arrangement.spacedBy(4.dp)) {

        if (state.adding)
            Text(
                text = stringResource(R.string.text_medicine_product_name),
                color = MaterialTheme.colorScheme.onSurface,
                style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold)
            )

        BasicTextField(
            value = state.productName,
            onValueChange = { onEvent(MedicineEvent.SetProductName(it)) },
            modifier = Modifier.fillMaxWidth(),
            readOnly = state.default || state.technical.verified,
            textStyle = MaterialTheme.typography.titleLarge.copy(
                color = MaterialTheme.colorScheme.onSurface,
                fontWeight = FontWeight.Bold
            ),
            decorationBox = { innerTextField ->
                when {
                    state.default || state.technical.verified
                    -> NoDecorationBox(state.productName, innerTextField)

                    else -> DecorationBox(state.productName, innerTextField)
                }
            }
        )

        if (state.default || state.technical.verified) Text(
            text = formName(state.prodFormNormName),
            style = MaterialTheme.typography.titleMedium.copy(MaterialTheme.colorScheme.onSurface)
        )

        when {
            state.default -> {
                if (state.kitTitle.isNotBlank()) Text(
                    text = state.kitTitle,
                    style = MaterialTheme.typography.titleMedium.copy(MaterialTheme.colorScheme.onSurface)
                )
            }

            state.adding || state.editing -> {
                val kits = MedicineDatabase.getInstance(LocalContext.current).kitDAO().getAll()
                var show by remember { mutableStateOf(false) }
                var kitId by remember { mutableStateOf(state.kitId) }

                OutlinedTextField(
                    value = state.kitTitle,
                    onValueChange = {},
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 8.dp)
                        .clickable { show = true },
                    enabled = false,
                    readOnly = true,
                    placeholder = { Text(stringResource(R.string.placeholder_kitchen)) },
                    colors = fieldColorsInverted()
                )

                if (show) AlertDialog(
                    onDismissRequest = { show = false },
                    confirmButton = {
                        TextButton(
                            onClick = { onEvent(MedicineEvent.SetKitId(kitId)); show = false },
                            enabled = kitId != null,
                        ) {
                            Text(stringResource(R.string.text_save))
                        }
                    },
                    dismissButton = {
                        TextButton({ onEvent(MedicineEvent.SetKitId(null)); show = false }) {
                            Text(stringResource(R.string.text_clear))
                        }
                    },
                    title = { Text(stringResource(R.string.preference_kits_group)) },
                    text = {
                        Column(Modifier.selectableGroup()) {
                            kits.forEach { kit ->
                                Row(
                                    Modifier
                                        .fillMaxWidth()
                                        .height(56.dp)
                                        .selectable(
                                            selected = kitId == kit.kitId,
                                            onClick = { kitId = kit.kitId },
                                            role = Role.RadioButton
                                        ),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    RadioButton(kitId == kit.kitId, null)
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
            }
        }
    }
}

@Composable
private fun ProductImage(viewModel: MedicineViewModel, state: MedicineState) {
    val isIcon = state.image.contains(TYPE)
    val noIcon = state.image.isEmpty()
    val image = when {
        isIcon -> ICONS_MED[state.image]
        noIcon -> R.drawable.vector_type_unknown
        else -> File(LocalContext.current.filesDir, state.image).run {
            if (exists()) this else R.drawable.vector_type_unknown
        }
    }
    var showPicker by remember { mutableStateOf(false) }

    ElevatedCard(
        modifier = Modifier
            .fillMaxWidth()
            .height(192.dp)
            .padding(horizontal = 16.dp),
        colors = CardDefaults.cardColors(
            if (isIcon || noIcon) MaterialTheme.colorScheme.secondaryContainer
            else MaterialTheme.colorScheme.background
        )
    ) {
        Image(
            painter = rememberAsyncImagePainter(image),
            contentDescription = null,
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
                .clickable(!state.default && (isIcon || noIcon)) { showPicker = true },
            alignment = Alignment.Center,
            contentScale = ContentScale.Fit,
            alpha = when {
                state.default -> 1f
                isIcon || noIcon -> 0.4f
                else -> 1f
            }
        )
    }

    if (showPicker) IconPicker(onEvent = viewModel::onEvent, onCancel = { showPicker = false })
}

@Composable
private fun ExpirationDate(onEvent: (MedicineEvent) -> Unit, state: MedicineState) {
    var size by remember { mutableStateOf(IntSize.Zero) }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(Modifier.onGloballyPositioned { size = it.size }) {
            Text(
                text = stringResource(R.string.text_exp_date),
                color = MaterialTheme.colorScheme.onSurface,
                style = if (state.default) MaterialTheme.typography.titleLarge
                else MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold)
            )

            when {
                state.default || state.technical.verified -> {
                    Text(
                        text = toExpDate(state.expDate),
                        color = MaterialTheme.colorScheme.onSurface,
                        style = MaterialTheme.typography.titleLarge,
                    )
                }

                (state.adding || state.editing) && !state.technical.verified -> {
                    var showPicker by remember { mutableStateOf(false) }

                    OutlinedTextField(
                        value = toExpDate(state.expDate),
                        onValueChange = {},
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 8.dp)
                            .clickable { showPicker = true },
                        enabled = false,
                        readOnly = true,
                        placeholder = { Text(stringResource(R.string.placeholder_exp_date)) },
                        leadingIcon = { Icon(Icons.Default.DateRange, null) },
                        colors = fieldColorsInverted()
                    )

                    if (showPicker) MonthYear(
                        onConfirm = { month, year ->
                            onEvent(MedicineEvent.SetExpDate(toTimestamp(month, year)))
                            showPicker = false
                        },
                        onCancel = { showPicker = false })
                }
            }
        }

        if (state.default) {
            val icon: ImageVector
            val color: Color
            val onColor: Color
            val onClick: () -> Unit

            when {
                state.technical.verified -> {
                    color = MaterialTheme.colorScheme.primaryContainer
                    onColor = MaterialTheme.colorScheme.onPrimaryContainer
                    icon = Icons.Default.Check
                    onClick = {}
                }

                state.technical.scanned && !state.technical.verified -> {
                    color = MaterialTheme.colorScheme.errorContainer
                    onColor = MaterialTheme.colorScheme.onErrorContainer
                    icon = Icons.Default.Refresh
                    onClick = { onEvent(MedicineEvent.Fetch) }
                }

                else -> {
                    color = MaterialTheme.colorScheme.error
                    onColor = MaterialTheme.colorScheme.onError
                    icon = Icons.Default.Info
                    onClick = {}
                }
            }

            Box(Modifier.background(color, RoundedCornerShape(16.dp))) {
                IconButton(
                    onClick = onClick,
                    modifier = Modifier.size(with(LocalDensity.current) { size.height.toDp() })
                ) { Icon(imageVector = icon, contentDescription = null, tint = onColor) }
            }
        }
    }
    HorizontalDivider(Modifier.padding(16.dp, 0.dp), 2.dp, MaterialTheme.colorScheme.outline)
}

@Composable
private fun ProductFormName(onEvent: (MedicineEvent) -> Unit, state: MedicineState) {
    Column(Modifier.padding(horizontal = 16.dp)) {
        Text(
            text = stringResource(R.string.text_medicine_description),
            style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold)
        )

        BasicTextField(
            value = state.prodFormNormName,
            onValueChange = { onEvent(MedicineEvent.SetProdFormNormName(it)) },
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp),
            readOnly = state.default || state.technical.verified,
            textStyle = MaterialTheme.typography.bodyLarge.copy(MaterialTheme.colorScheme.onSurface),
            decorationBox = { innerTextField ->
                when {
                    state.default || state.technical.verified
                        -> NoDecorationBox(state.prodFormNormName, innerTextField)

                    else -> DecorationBox(state.prodFormNormName, innerTextField)
                }
            }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ProductNormName(onEvent: (MedicineEvent) -> Unit, state: MedicineState) {
    Row(Modifier.padding(horizontal = 16.dp), Arrangement.spacedBy(16.dp)) {
        Column(Modifier.weight(0.5f), Arrangement.spacedBy(8.dp)) {
            Text(
                text = stringResource(R.string.text_medicine_dose),
                style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold)
            )

            BasicTextField(
                value = state.prodDNormName,
                onValueChange = { onEvent(MedicineEvent.SetProdDNormName(it)) },
                modifier = Modifier.fillMaxWidth(),
                readOnly = state.default || state.technical.verified,
                textStyle = MaterialTheme.typography.bodyLarge.copy(MaterialTheme.colorScheme.onSurface),
                decorationBox = { innerTextField ->
                    when {
                        state.adding || state.editing && !state.technical.verified ->
                            OutlinedTextFieldDefaults.DecorationBox(
                                value = state.prodDNormName,
                                innerTextField = innerTextField,
                                enabled = true,
                                singleLine = false,
                                visualTransformation = VisualTransformation.None,
                                interactionSource = remember(::MutableInteractionSource),
                                placeholder = { Text(stringResource(R.string.placeholder_dose)) }
                            )

                        else -> OutlinedTextFieldDefaults.DecorationBox(
                            value = state.prodDNormName,
                            innerTextField = innerTextField,
                            enabled = true,
                            singleLine = false,
                            visualTransformation = VisualTransformation.None,
                            interactionSource = remember(::MutableInteractionSource),
                            contentPadding = PaddingValues(0.dp),
                            container = {}
                        )
                    }
                }
            )
        }

        Column(Modifier.weight(0.5f), Arrangement.spacedBy(8.dp)) {
            Text(
                text = stringResource(R.string.text_amount),
                style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold)
            )

            BasicTextField(
                value = if (state.default) "${decimalFormat(state.prodAmount)} ${state.doseType}"
                else state.prodAmount,
                onValueChange = { onEvent(MedicineEvent.SetProdAmount(it)) },
                modifier = Modifier.wrapContentSize(),
                readOnly = state.default,
                textStyle = MaterialTheme.typography.bodyLarge.copy(MaterialTheme.colorScheme.onSurface),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                visualTransformation = {
                    TransformedText(
                        AnnotatedString(it.text.replace('.', ',')),
                        OffsetMapping.Identity
                    )
                },
                interactionSource = remember(::MutableInteractionSource),
                decorationBox = { innerTextField ->
                    when {
                        state.default -> NoDecorationBox(state.prodAmount, innerTextField)
                        else -> OutlinedTextFieldDefaults.DecorationBox(
                            value = state.prodAmount,
                            innerTextField = innerTextField,
                            enabled = true,
                            singleLine = false,
                            visualTransformation = VisualTransformation.None,
                            interactionSource = remember(::MutableInteractionSource),
                            placeholder = { Text("50") },
                            trailingIcon = {
                                val list = stringArrayResource(R.array.medicine_dose_types)

                                var expanded by remember { mutableStateOf(false) }
                                var selected by remember { mutableStateOf(state.doseType) }

                                ExposedDropdownMenuBox(
                                    expanded = expanded,
                                    onExpandedChange = { expanded = it },
                                    modifier = Modifier.fillMaxWidth(0.6f)
                                ) {
                                    OutlinedTextField(
                                        value = selected,
                                        onValueChange = { selected = it },
                                        modifier = Modifier.menuAnchor(),
                                        readOnly = true,
                                        trailingIcon = {
                                            ExposedDropdownMenuDefaults.TrailingIcon(expanded)
                                        }
                                    )
                                    ExposedDropdownMenu(
                                        expanded = expanded,
                                        onDismissRequest = { expanded = false }
                                    ) {
                                        list.forEach { item ->
                                            DropdownMenuItem(
                                                text = { Text(item) },
                                                onClick = {
                                                    selected = item
                                                    onEvent(MedicineEvent.SetDoseType(selected))
                                                    expanded = false
                                                }
                                            )
                                        }
                                    }
                                }
                            }
                        )
                    }
                }
            )
        }
    }
}


@Composable
private fun PhKinetics(onEvent: (MedicineEvent) -> Unit, state: MedicineState) {
    if (state.adding || state.editing || state.phKinetics.isNotEmpty())
        Column(Modifier.padding(horizontal = 16.dp)) {
            Text(
                text = stringResource(R.string.text_indications_for_use),
                style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold)
            )

            BasicTextField(
                value = fromHTML(state.phKinetics),
                onValueChange = { onEvent(MedicineEvent.SetPhKinetics(it)) },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                readOnly = state.default,
                textStyle = MaterialTheme.typography.bodyLarge.copy(MaterialTheme.colorScheme.onSurface),
                decorationBox = { innerTextField ->
                    when {
                        state.default -> NoDecorationBox(state.phKinetics, innerTextField)
                        else -> DecorationBox(state.phKinetics, innerTextField)
                    }
                }
            )
        }
}

@Composable
private fun Comment(onEvent: (MedicineEvent) -> Unit, state: MedicineState) {
    if (state.adding || state.editing || state.comment.isNotEmpty())
        Column(Modifier.padding(16.dp, 0.dp, 16.dp, 16.dp)) {
            Text(
                text = stringResource(R.string.text_medicine_comment),
                style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold)
            )

            BasicTextField(
                value = state.comment,
                onValueChange = { onEvent(MedicineEvent.SetComment(it)) },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                readOnly = state.default,
                textStyle = MaterialTheme.typography.bodyLarge.copy(MaterialTheme.colorScheme.onSurface),
                decorationBox = { innerTextField ->
                    when {
                        state.default -> NoDecorationBox(state.comment, innerTextField)
                        else -> DecorationBox(state.comment, innerTextField)
                    }
                }
            )
        }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun DecorationBox(text: String, innerTextField: @Composable () -> Unit) {
    OutlinedTextFieldDefaults.DecorationBox(
        value = text,
        innerTextField = innerTextField,
        enabled = true,
        singleLine = false,
        visualTransformation = VisualTransformation.None,
        interactionSource = remember(::MutableInteractionSource),
        placeholder = { Text(stringResource(R.string.text_empty)) }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun NoDecorationBox(text: String, innerTextField: @Composable () -> Unit) {
    OutlinedTextFieldDefaults.DecorationBox(
        value = text,
        innerTextField = innerTextField,
        enabled = true,
        singleLine = false,
        visualTransformation = VisualTransformation.None,
        interactionSource = remember(::MutableInteractionSource),
        contentPadding = PaddingValues(0.dp),
        container = {}
    )
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun IconPicker(onEvent: (MedicineEvent) -> Unit, onCancel: () -> Unit) {
    val names = stringArrayResource(R.array.medicine_types)

    Dialog(onCancel) {
        Surface(Modifier.padding(vertical = 64.dp), RoundedCornerShape(16.dp)) {
            FlowRow(
                modifier = Modifier
                    .padding(16.dp)
                    .verticalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.Center,
                verticalArrangement = Arrangement.Center,
                maxItemsInEachRow = 4
            ) {
                ICONS_MED.entries.forEachIndexed { index, (name, icon) ->
                    ElevatedCard(
                        modifier = Modifier
                            .padding(8.dp)
                            .clickable { onEvent(MedicineEvent.SetImage(name)); onCancel() },
                        colors = CardDefaults.cardColors().copy(
                            containerColor = MaterialTheme.colorScheme.secondaryContainer
                        )
                    ) {
                        Image(
                            painter = painterResource(icon),
                            contentDescription = null,
                            modifier = Modifier
                                .size(128.dp)
                                .padding(16.dp)
                        )
                        Text(
                            text = names[index],
                            modifier = Modifier
                                .padding(vertical = 4.dp)
                                .align(Alignment.CenterHorizontally),
                            color = MaterialTheme.colorScheme.onSecondaryContainer,
                            fontWeight = FontWeight.SemiBold,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }
            }
        }
    }
}