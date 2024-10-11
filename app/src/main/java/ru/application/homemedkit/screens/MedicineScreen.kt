package ru.application.homemedkit.screens

import android.content.Context
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.Image
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material.icons.outlined.Check
import androidx.compose.material.icons.outlined.DateRange
import androidx.compose.material.icons.outlined.MoreVert
import androidx.compose.material.icons.outlined.Notifications
import androidx.compose.material.icons.outlined.Refresh
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CardDefaults
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
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.OffsetMapping
import androidx.compose.ui.text.input.TransformedText
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.core.text.HtmlCompat.FROM_HTML_OPTION_USE_CSS_COLORS
import androidx.core.text.HtmlCompat.fromHtml
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.rememberAsyncImagePainter
import com.ramcosta.composedestinations.annotation.Destination
import com.ramcosta.composedestinations.annotation.RootGraph
import com.ramcosta.composedestinations.annotation.parameters.DeepLink
import com.ramcosta.composedestinations.generated.destinations.IntakeScreenDestination
import com.ramcosta.composedestinations.generated.destinations.MedicineScreenDestination
import com.ramcosta.composedestinations.generated.destinations.MedicinesScreenDestination
import com.ramcosta.composedestinations.navigation.DestinationsNavigator
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.collectLatest
import ru.application.homemedkit.HomeMeds.Companion.database
import ru.application.homemedkit.R.drawable.vector_type_unknown
import ru.application.homemedkit.R.string.placeholder_dose
import ru.application.homemedkit.R.string.preference_kits_group
import ru.application.homemedkit.R.string.text_amount
import ru.application.homemedkit.R.string.text_clear
import ru.application.homemedkit.R.string.text_connection_error
import ru.application.homemedkit.R.string.text_delete
import ru.application.homemedkit.R.string.text_duplicate
import ru.application.homemedkit.R.string.text_edit
import ru.application.homemedkit.R.string.text_empty
import ru.application.homemedkit.R.string.text_exp_date
import ru.application.homemedkit.R.string.text_indications_for_use
import ru.application.homemedkit.R.string.text_medicine_comment
import ru.application.homemedkit.R.string.text_medicine_description
import ru.application.homemedkit.R.string.text_medicine_dose
import ru.application.homemedkit.R.string.text_medicine_form
import ru.application.homemedkit.R.string.text_medicine_group
import ru.application.homemedkit.R.string.text_medicine_product_name
import ru.application.homemedkit.R.string.text_medicine_status_checked
import ru.application.homemedkit.R.string.text_medicine_status_scanned
import ru.application.homemedkit.R.string.text_medicine_status_self_added
import ru.application.homemedkit.R.string.text_save
import ru.application.homemedkit.R.string.text_status
import ru.application.homemedkit.R.string.text_try_again
import ru.application.homemedkit.R.string.text_unspecified
import ru.application.homemedkit.R.string.text_update
import ru.application.homemedkit.dialogs.MonthYear
import ru.application.homemedkit.helpers.BLANK
import ru.application.homemedkit.helpers.DoseTypes
import ru.application.homemedkit.helpers.FORMAT_DMMMMY
import ru.application.homemedkit.helpers.TYPE
import ru.application.homemedkit.helpers.Types
import ru.application.homemedkit.helpers.decimalFormat
import ru.application.homemedkit.helpers.formName
import ru.application.homemedkit.helpers.toExpDate
import ru.application.homemedkit.helpers.viewModelFactory
import ru.application.homemedkit.viewModels.MedicineState
import ru.application.homemedkit.viewModels.MedicineViewModel
import ru.application.homemedkit.viewModels.MedicineViewModel.ActivityEvents.Close
import ru.application.homemedkit.viewModels.MedicineViewModel.ActivityEvents.Start
import ru.application.homemedkit.viewModels.ScannerViewModel.Response.Default
import ru.application.homemedkit.viewModels.ScannerViewModel.Response.Loading
import ru.application.homemedkit.viewModels.ScannerViewModel.Response.NoNetwork
import ru.application.homemedkit.viewModels.ScannerViewModel.Response.Success
import java.io.File
import java.time.LocalDate


@OptIn(ExperimentalMaterial3Api::class)
@Destination<RootGraph>(deepLinks = [DeepLink(uriPattern = "app://medicines/{id}")])
@Composable
fun MedicineScreen(
    id: Long = 0L,
    cis: String = BLANK,
    duplicate: Boolean = false,
    navigator: DestinationsNavigator,
    context: Context = LocalContext.current
) {
    val model = viewModel<MedicineViewModel>(factory = viewModelFactory { MedicineViewModel(id) })
    val state by model.state.collectAsStateWithLifecycle()
    val response by model.response.collectAsStateWithLifecycle()

    if (id == 0L) {
        model.setAdding()
        model.setCis(cis)
    }

    if (duplicate) {
        var show by remember { mutableStateOf(true) }
        if (show) Snackbar(text_duplicate)

        LaunchedEffect(Unit) { delay(2000); show = false }
    }

    LaunchedEffect(Unit) {
        model.events.collectLatest {
            when (it) {
                Start -> navigator.navigate(MedicineScreenDestination(state.id))
                Close -> {
                    File(context.filesDir, state.image).delete()
                    navigator.navigate(MedicinesScreenDestination) {
                        popUpTo(MedicinesScreenDestination) { inclusive = true }
                    }
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
                            imageVector = Icons.AutoMirrored.Outlined.ArrowBack,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    }
                },
                actions = {
                    if (state.default) {
                        LocalFocusManager.current.clearFocus(true)
                        var expanded by remember { mutableStateOf(false) }

                        IconButton({
                            navigator.navigate(IntakeScreenDestination(medicineId = state.id))
                        }) { Icon(Icons.Outlined.Notifications, null) }

                        IconButton({ expanded = true }) {
                            Icon(Icons.Outlined.MoreVert, null)
                        }

                        DropdownMenu(expanded, { expanded = false }) {
                            DropdownMenuItem(
                                text = { Text(stringResource(text_edit)) },
                                onClick = model::setEditing
                            )
                            DropdownMenuItem(
                                text = { Text(stringResource(text_delete)) },
                                onClick = model::delete
                            )
                        }
                    } else IconButton(
                        onClick = { if (state.adding) model.add() else model.update() },
                        enabled = state.productName.isNotEmpty()
                    ) { Icon(Icons.Outlined.Check, null) }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    navigationIconContentColor = MaterialTheme.colorScheme.onPrimaryContainer,
                    actionIconContentColor = MaterialTheme.colorScheme.onPrimaryContainer
                )
            )
        },
        floatingActionButton = {
            if (state.technical.scanned && !state.technical.verified)
                ExtendedFloatingActionButton(
                    text = { Text(stringResource(text_update)) },
                    icon = { Icon(Icons.Outlined.Refresh, null) },
                    onClick = model::fetch
                )
        }
    ) { values ->
        LazyColumn(
            contentPadding = PaddingValues(16.dp, values.calculateTopPadding().plus(16.dp)),
            verticalArrangement = Arrangement.spacedBy(32.dp)
        ) {
            item {
                Row(Modifier.height(256.dp), Arrangement.spacedBy(12.dp)) {
                    ProductImage(model, state)
                    ProductBrief(model, state)
                }
            }
            item { ProductFormName(model, state) }
            item { ProductNormName(model, state) }
            if (state.adding || state.editing || state.phKinetics.isNotEmpty())
                item { PhKinetics(model, state) }
            if (state.adding || state.editing || state.comment.isNotEmpty())
                item { Comment(model, state) }
        }
    }

    when {
        state.showDialogKits -> DialogKits(model, state)
        state.showDialogDate -> MonthYear(model::setExpDate, model::hideDatePicker)
        state.showDialogIcons -> IconPicker(model)
    }

    when (val data = response) {
        Default -> {}
        Loading -> LoadingDialog()
        is Success -> navigator.navigate(MedicineScreenDestination(data.id))
        is NoNetwork -> Snackbar(text_connection_error)

        else -> Snackbar(text_try_again)
    }

    BackHandler { navigator.navigate(MedicinesScreenDestination) }
}

@Composable
private fun ProductBrief(model: MedicineViewModel, state: MedicineState) = Column(
    modifier = Modifier
        .fillMaxHeight()
        .verticalScroll(rememberScrollState()),
    verticalArrangement = Arrangement.SpaceBetween
) {
    ProductName(model, state)
    if (state.default || state.technical.verified) ProductForm(state)
    ProductKit(model, state)
    ProductExp(model, state)
    if (state.default) ProductStatus(state)
}

@Composable
private fun ProductName(model: MedicineViewModel, state: MedicineState) = Column {
    Text(
        stringResource(text_medicine_product_name),
        style = MaterialTheme.typography.titleMedium.copy(
            color = MaterialTheme.colorScheme.onSurface,
            fontWeight = FontWeight.W400
        )
    )
    BasicTextField(
        value = state.productName,
        onValueChange = model::setProductName,
        modifier = Modifier.fillMaxWidth(),
        readOnly = state.default || state.technical.verified,
        singleLine = true,
        textStyle = MaterialTheme.typography.titleMedium.copy(
            color = MaterialTheme.colorScheme.onSurface,
            fontWeight = FontWeight.SemiBold
        ),
        decorationBox = {
            if (state.default || state.technical.verified) NoDecorationBox(state.productName, it)
            else DecorationBox(state.productName, it)
        }
    )
}

@Composable
private fun ProductForm(state: MedicineState) = Column {
    Text(
        stringResource(text_medicine_form), style = MaterialTheme.typography.titleMedium.copy(
            color = MaterialTheme.colorScheme.onSurface,
            fontWeight = FontWeight.W400
        )
    )
    Text(
        text = formName(state.prodFormNormName).ifEmpty { stringResource(text_unspecified) },
        style = MaterialTheme.typography.titleMedium.copy(
            color = MaterialTheme.colorScheme.onSurface,
            fontWeight = FontWeight.SemiBold
        )
    )
}

@Composable
private fun ProductKit(model: MedicineViewModel, state: MedicineState) = Column {
    Text(
        stringResource(text_medicine_group), style = MaterialTheme.typography.titleMedium.copy(
            color = MaterialTheme.colorScheme.onSurface,
            fontWeight = FontWeight.W400
        )
    )
    if (state.default) Text(
        text = state.kitTitle.ifBlank { stringResource(text_unspecified) },
        style = MaterialTheme.typography.titleMedium.copy(
            color = MaterialTheme.colorScheme.onSurface,
            fontWeight = FontWeight.SemiBold
        )
    )
    else OutlinedTextField(
        value = state.kitTitle,
        onValueChange = {},
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = model::showKitDialog),
        enabled = false,
        readOnly = true,
        placeholder = { Text(stringResource(text_empty)) },
        colors = fieldColorsInverted
    )
}

@Composable
private fun ProductExp(model: MedicineViewModel, state: MedicineState) = Column {
    Text(
        stringResource(text_exp_date), style = MaterialTheme.typography.titleMedium.copy(
            color = MaterialTheme.colorScheme.onSurface,
            fontWeight = FontWeight.W400
        )
    )
    when {
        state.default || state.technical.verified ->
            Text(
                text = toExpDate(state.expDate).ifEmpty { stringResource(text_unspecified) },
                style = MaterialTheme.typography.titleMedium.copy(
                    color = MaterialTheme.colorScheme.onSurface,
                    fontWeight = FontWeight.SemiBold
                )
            )

        (state.adding || state.editing) && !state.technical.verified -> {
            OutlinedTextField(
                value = toExpDate(state.expDate),
                onValueChange = {},
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable(onClick = model::showDatePicker),
                enabled = false,
                readOnly = true,
                placeholder = { Text(LocalDate.now().plusDays(555).format(FORMAT_DMMMMY)) },
                leadingIcon = { Icon(Icons.Outlined.DateRange, null) },
                colors = fieldColorsInverted
            )
        }
    }
}

@Composable
private fun ProductStatus(state: MedicineState) = Column {
    Text(
        stringResource(text_status), style = MaterialTheme.typography.titleMedium.copy(
            color = MaterialTheme.colorScheme.onSurface,
            fontWeight = FontWeight.W400
        )
    )
    Row(verticalAlignment = Alignment.CenterVertically) {
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
}

@Composable
private fun ProductImage(model: MedicineViewModel, state: MedicineState) = MedicineImage(
    image = state.image,
    state = state,
    modifier = Modifier
        .width(128.dp)
        .fillMaxHeight()
        .border(1.dp, MaterialTheme.colorScheme.onSurface, MaterialTheme.shapes.medium)
        .padding(8.dp)
        .clickable(
            enabled = !state.default && (state.image.contains(TYPE) || state.image.isEmpty()),
            onClick = model::showIconPicker
        )
)


@Composable
private fun ProductFormName(model: MedicineViewModel, state: MedicineState) =
    Column(verticalArrangement = Arrangement.spacedBy(8.dp))  {
        Text(
            text = stringResource(text_medicine_description),
            style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold)
        )

        BasicTextField(
            value = state.prodFormNormName,
            onValueChange = model::setFormName,
            modifier = Modifier.fillMaxWidth(),
            readOnly = state.default || state.technical.verified,
            textStyle = MaterialTheme.typography.bodyLarge.copy(MaterialTheme.colorScheme.onSurface),
            decorationBox = {
                if (state.default || state.technical.verified) NoDecorationBox(state.prodFormNormName, it)
                else DecorationBox(state.prodFormNormName, it)
            }
        )
    }

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ProductNormName(model: MedicineViewModel, state: MedicineState) =
    Row(horizontalArrangement =  Arrangement.spacedBy(16.dp)) {
        Column(Modifier.weight(0.5f), Arrangement.spacedBy(8.dp)) {
            Text(
                text = stringResource(text_medicine_dose),
                style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold)
            )

            BasicTextField(
                value = state.prodDNormName,
                onValueChange = model::setDoseName,
                modifier = Modifier.fillMaxWidth(),
                readOnly = state.default || state.technical.verified,
                textStyle = MaterialTheme.typography.bodyLarge.copy(MaterialTheme.colorScheme.onSurface),
                decorationBox = {
                    if (state.adding || state.editing && !state.technical.verified) OutlinedTextFieldDefaults.DecorationBox(
                        value = state.prodDNormName,
                        innerTextField = it,
                        enabled = true,
                        singleLine = false,
                        visualTransformation = VisualTransformation.None,
                        interactionSource = remember(::MutableInteractionSource),
                        placeholder = { Text(stringResource(placeholder_dose)) }
                    )
                    else OutlinedTextFieldDefaults.DecorationBox(
                        value = state.prodDNormName.ifEmpty { stringResource(text_unspecified) },
                        innerTextField = it,
                        enabled = true,
                        singleLine = false,
                        visualTransformation = VisualTransformation.None,
                        interactionSource = remember(::MutableInteractionSource),
                        contentPadding = PaddingValues(0.dp),
                        container = {}
                    )
                }
            )
        }

        Column(Modifier.weight(0.5f), Arrangement.spacedBy(8.dp)) {
            Text(
                text = stringResource(text_amount),
                style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold)
            )

            BasicTextField(
                value = if (state.default) "${decimalFormat(state.prodAmount)} ${
                    stringResource(DoseTypes.getTitle(state.doseType))}"
                else state.prodAmount,
                onValueChange = model::setProdAmount,
                readOnly = state.default,
                singleLine = true,
                textStyle = MaterialTheme.typography.bodyLarge.copy(color = MaterialTheme.colorScheme.onSurface),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                interactionSource = remember(::MutableInteractionSource),
                visualTransformation = {
                    TransformedText(
                        AnnotatedString(it.text.replace('.', ',')),
                        OffsetMapping.Identity
                    )
                }
            ) { field ->
                if (state.default) NoDecorationBox(state.prodAmount, field)
                else OutlinedTextFieldDefaults.DecorationBox(
                    value = state.prodAmount,
                    innerTextField = field,
                    enabled = true,
                    singleLine = true,
                    visualTransformation = VisualTransformation.None,
                    interactionSource = remember(::MutableInteractionSource),
                    placeholder = { Text(stringResource(text_empty)) },
                    suffix = {
                        ExposedDropdownMenuBox(state.showMenuDose, model::showDoseMenu) {
                            Row(Modifier.menuAnchor(MenuAnchorType.PrimaryNotEditable)) {
                                Text(stringResource(DoseTypes.getTitle(state.doseType)))
                                ExposedDropdownMenuDefaults.TrailingIcon(state.showMenuDose)
                            }
                            ExposedDropdownMenu(state.showMenuDose, model::hideDoseMenu, Modifier.width(48.dp)) {
                                DoseTypes.entries.forEach { item ->
                                    DropdownMenuItem(
                                        text = { Text(stringResource(item.title)) },
                                        onClick = { model.setDoseType(item) }
                                    )
                                }
                            }
                        }
                    }
                )
            }
        }
    }

@Composable
private fun PhKinetics(model: MedicineViewModel, state: MedicineState) = Column(
    verticalArrangement = Arrangement.spacedBy(8.dp)
)
{
    Text(
        text = stringResource(text_indications_for_use),
        style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold)
    )

    BasicTextField(
        value = fromHtml(state.phKinetics, FROM_HTML_OPTION_USE_CSS_COLORS).toString(),
        onValueChange = model::setPhKinetics,
        modifier = Modifier.fillMaxWidth(),
        readOnly = state.default,
        textStyle = MaterialTheme.typography.bodyLarge.copy(MaterialTheme.colorScheme.onSurface)
    ) {
        if (state.default) NoDecorationBox(state.phKinetics, it)
        else DecorationBox(state.phKinetics, it)
    }
}

@Composable
private fun Comment(model: MedicineViewModel, state: MedicineState) = Column(
    verticalArrangement = Arrangement.spacedBy(8.dp)
) {
    Text(
        text = stringResource(text_medicine_comment),
        style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold)
    )

    BasicTextField(
        value = state.comment,
        onValueChange = model::setComment,
        modifier = Modifier.fillMaxWidth(),
        readOnly = state.default,
        textStyle = MaterialTheme.typography.bodyLarge.copy(MaterialTheme.colorScheme.onSurface),
        decorationBox = {
            if (state.default) NoDecorationBox(state.comment, it)
            else DecorationBox(state.comment, it)
        }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun DecorationBox(text: String, field: @Composable () -> Unit) =
    OutlinedTextFieldDefaults.DecorationBox(
        value = text,
        innerTextField = field,
        enabled = true,
        singleLine = false,
        visualTransformation = VisualTransformation.None,
        interactionSource = remember(::MutableInteractionSource),
        placeholder = { Text(stringResource(text_empty)) }
    )

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun NoDecorationBox(text: String, field: @Composable () -> Unit) =
    OutlinedTextFieldDefaults.DecorationBox(
        value = text,
        innerTextField = field,
        enabled = true,
        singleLine = false,
        visualTransformation = VisualTransformation.None,
        interactionSource = remember(::MutableInteractionSource),
        contentPadding = PaddingValues(0.dp),
        container = {}
    )

@Composable
private fun DialogKits(model: MedicineViewModel, state: MedicineState) = AlertDialog(
    onDismissRequest = model::hideKitDialog,
    dismissButton = { TextButton(model::setKitId) { Text(stringResource(text_clear)) } },
    title = { Text(stringResource(preference_kits_group)) },
    confirmButton = {
        TextButton(model::setKitId, enabled = state.kitId != null)
        { Text(stringResource(text_save)) }
    },
    text = {
        Column(Modifier.selectableGroup()) {
            database.kitDAO().getAll().forEach { kit ->
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp)
                        .selectable(
                            selected = state.kitId == kit.kitId,
                            onClick = { model.pickKit(kit.kitId) },
                            role = Role.RadioButton
                        )
                ) {
                    RadioButton(state.kitId == kit.kitId, null)
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

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun IconPicker(model: MedicineViewModel) = Dialog(model::hideIconPicker) {
    Surface(Modifier.padding(vertical = 64.dp), RoundedCornerShape(16.dp)) {
        FlowRow(
            modifier = Modifier
                .padding(16.dp)
                .verticalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.Center,
            verticalArrangement = Arrangement.Center,
            maxItemsInEachRow = 4
        ) {
            Types.entries.forEach {
                ElevatedCard(
                    modifier = Modifier
                        .padding(8.dp)
                        .clickable { model.setIcon(it.value) },
                    colors = CardDefaults.cardColors().copy(
                        containerColor = MaterialTheme.colorScheme.secondaryContainer
                    )
                ) {
                    Image(
                        painter = painterResource(it.icon),
                        contentDescription = null,
                        modifier = Modifier
                            .size(128.dp)
                            .padding(16.dp)
                    )
                    Text(
                        text = stringResource(it.title),
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

@Composable
fun MedicineImage(image: String, modifier: Modifier = Modifier, state: MedicineState? = null) {
    val isIcon = image.contains(TYPE)
    val noIcon = image.isEmpty()
    val icon = when {
        isIcon -> Types.entries.find { it.value == image }?.icon ?: vector_type_unknown
        noIcon -> vector_type_unknown
        else -> File(LocalContext.current.filesDir, image).run { if (exists()) this else vector_type_unknown }
    }

    Image(
        painter = rememberAsyncImagePainter(icon),
        contentDescription = null,
        modifier = modifier,
        alignment = Alignment.Center,
        contentScale = ContentScale.Fit,
        colorFilter = if (noIcon) ColorFilter.tint(MaterialTheme.colorScheme.onSurface) else null,
        alpha = when {
            state?.default == true -> 1f
            state?.default == false && (isIcon || noIcon) -> 0.4f
            else -> 1f
        }
    )
}