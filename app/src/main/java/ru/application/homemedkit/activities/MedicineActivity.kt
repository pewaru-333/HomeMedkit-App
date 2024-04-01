package ru.application.homemedkit.activities

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
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
import androidx.compose.foundation.rememberScrollState
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
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
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
import coil.compose.AsyncImage
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.collectLatest
import ru.application.homemedkit.R
import ru.application.homemedkit.databaseController.MedicineDatabase
import ru.application.homemedkit.dialogs.MonthYear
import ru.application.homemedkit.helpers.BLANK
import ru.application.homemedkit.helpers.CIS
import ru.application.homemedkit.helpers.DUPLICATE
import ru.application.homemedkit.helpers.DateHelper.toExpDate
import ru.application.homemedkit.helpers.DateHelper.toTimestamp
import ru.application.homemedkit.helpers.ICONS_MED
import ru.application.homemedkit.helpers.ID
import ru.application.homemedkit.helpers.MEDICINE_ID
import ru.application.homemedkit.helpers.NEW_MEDICINE
import ru.application.homemedkit.helpers.decimalFormat
import ru.application.homemedkit.helpers.formName
import ru.application.homemedkit.helpers.fromHTML
import ru.application.homemedkit.helpers.viewModelFactory
import ru.application.homemedkit.states.MedicineState
import ru.application.homemedkit.ui.theme.AppTheme
import ru.application.homemedkit.viewModels.ActivityEvents
import ru.application.homemedkit.viewModels.MedicineEvent
import ru.application.homemedkit.viewModels.MedicineViewModel
import ru.application.homemedkit.viewModels.ResponseState

class MedicineActivity : ComponentActivity() {

    private lateinit var database: MedicineDatabase

    @OptIn(ExperimentalMaterial3Api::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        database = MedicineDatabase.getInstance(this)

        val id = intent.getLongExtra(ID, 0)
        val duplicate = intent.getBooleanExtra(DUPLICATE, false)
        val cis = intent.getStringExtra(CIS) ?: BLANK

        val intents = listOf(
            Intent(this, MainActivity::class.java).putExtra(NEW_MEDICINE, true),
            Intent(this, IntakeActivity::class.java).putExtra(MEDICINE_ID, id)
        )

        setContent {
            val viewModel = viewModel<MedicineViewModel>(factory = viewModelFactory {
                MedicineViewModel(database.medicineDAO(), id)
            })

            val state by viewModel.uiState.collectAsStateWithLifecycle()
            val response by viewModel.response.collectAsState(ResponseState.Default)

            if (id == 0L) {
                viewModel.onEvent(MedicineEvent.SetAdding)
                viewModel.onEvent(MedicineEvent.SetCis(cis))
            }

            if (duplicate) viewModel.show = true

            LaunchedEffect(Unit) {
                viewModel.events.collectLatest { event ->
                    when (event) {
                        ActivityEvents.Start -> startActivity(
                            intent.putExtra(ID, state.id)
                                .setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
                        )

                        ActivityEvents.Close -> startActivity(intents[0])
                    }
                }
            }

            AppTheme {
                Scaffold(
                    topBar = {
                        TopAppBar(
                            title = {},
                            navigationIcon = {
                                IconButton({ startActivity(intents[0]) }) {
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

                                        IconButton(onClick = { startActivity(intents[1]) }) {
                                            Icon(Icons.Default.Notifications, null)
                                        }

                                        IconButton({ expanded = true }) {
                                            Icon(Icons.Default.MoreVert, null)
                                        }

                                        DropdownMenu(expanded, { expanded = false }) {
                                            DropdownMenuItem(
                                                text = { Text(resources.getString(R.string.text_edit)) },
                                                onClick = { viewModel.onEvent(MedicineEvent.SetEditing) }
                                            )
                                            DropdownMenuItem(
                                                text = { Text(resources.getString(R.string.text_delete)) },
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
                            .padding(
                                top = paddingValues
                                    .calculateTopPadding()
                                    .plus(16.dp)
                            )
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

                if (viewModel.show) {
                    try {
                        if (!duplicate) ResponseState.Errors.valueOf(response.toString()).ordinal
                        else 0
                    } catch (e: IllegalArgumentException) {
                        ResponseState.Errors.FETCH_ERROR.ordinal
                    }.also { Snackbar(it) }

                    LaunchedEffect(Unit) {
                        delay(2000)
                        viewModel.show = false
                    }
                }

                when (response) {
                    ResponseState.Default -> {}
                    ResponseState.Loading -> LoadingDialog()
                    ResponseState.Success -> startActivity(intent)

                    else -> viewModel.show = true
                }

                BackHandler { startActivity(intents[0]) }
            }
        }
    }
}

@Composable
private fun ProductName(onEvent: (MedicineEvent) -> Unit, state: MedicineState) {
    val source = remember(::MutableInteractionSource)

    Column(Modifier.padding(horizontal = 16.dp), Arrangement.spacedBy(4.dp)) {

        if (state.adding)
            Text(
                text = LocalContext.current.getString(R.string.text_medicine_product_name),
                color = MaterialTheme.colorScheme.onSurface,
                style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold)
            )

        BasicTextField(
            value = state.productName,
            onValueChange = { onEvent(MedicineEvent.SetProductName(it)) },
            modifier = Modifier.fillMaxWidth(),
            readOnly = !state.adding,
            textStyle = MaterialTheme.typography.titleLarge.copy(
                color = MaterialTheme.colorScheme.onSurface,
                fontWeight = FontWeight.Bold
            ),
            decorationBox = decorationBox(
                state = state,
                text = state.productName,
                source = source
            )
        )

        if (state.default)
            Text(
                text = formName(state.prodFormNormName),
                style = MaterialTheme.typography.titleMedium.copy(
                    color = MaterialTheme.colorScheme.onSurface
                )
            )
    }
}

@Composable
private fun ProductImage(viewModel: MedicineViewModel, state: MedicineState) {
    var showPicker by remember { mutableStateOf(false) }
    val image = ICONS_MED[state.image] ?: R.drawable.vector_type_unknown

    ElevatedCard(
        modifier = Modifier
            .fillMaxWidth()
            .height(192.dp)
            .padding(horizontal = 16.dp),
        colors = CardDefaults.cardColors(MaterialTheme.colorScheme.secondaryContainer)
    ) {
        AsyncImage(
            model = image,
            contentDescription = null,
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
                .clickable { if(!state.default) showPicker = true },
            alignment = Alignment.Center,
            contentScale = ContentScale.Fit,
            alpha = if(state.default) 1f else 0.4f
        )
    }

    if (showPicker) IconPicker(onEvent = viewModel::onEvent, onCancel = { showPicker = false })
}

@Composable
private fun ExpirationDate(onEvent: (MedicineEvent) -> Unit, state: MedicineState) {
    val context = LocalContext.current
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
                text = context.getString(R.string.text_exp_date),
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
                        placeholder = { Text(context.getString(R.string.placeholder_exp_date)) },
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
    val source = remember { MutableInteractionSource() }

    Column(modifier = Modifier.padding(horizontal = 16.dp)) {
        Text(
            text = LocalContext.current.getString(R.string.text_medicine_description),
            style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold)
        )

        BasicTextField(
            value = state.prodFormNormName,
            onValueChange = { onEvent(MedicineEvent.SetProdFormNormName(it)) },
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp),
            readOnly = state.default,
            textStyle = MaterialTheme.typography.bodyLarge.copy(MaterialTheme.colorScheme.onSurface),
            decorationBox = decorationBox(
                state = state,
                text = state.prodFormNormName,
                id = R.string.placeholder_form_name,
                source = source
            )
        )
    }
}

@Composable
private fun ProductNormName(onEvent: (MedicineEvent) -> Unit, state: MedicineState) {
    val sourceA = remember(::MutableInteractionSource)
    val sourceB = remember(::MutableInteractionSource)

    Row(Modifier.padding(horizontal = 16.dp), Arrangement.spacedBy(16.dp)) {
        Column(Modifier.weight(0.5f), Arrangement.spacedBy(8.dp)) {
            Text(
                text = LocalContext.current.getString(R.string.text_medicine_dose),
                style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold)
            )

            BasicTextField(
                value = state.prodDNormName,
                onValueChange = { onEvent(MedicineEvent.SetProdDNormName(it)) },
                modifier = Modifier.fillMaxWidth(),
                readOnly = state.default,
                textStyle = MaterialTheme.typography.bodyLarge.copy(MaterialTheme.colorScheme.onSurface),
                decorationBox = decorationBox(
                    state = state,
                    text = state.prodDNormName,
                    id = R.string.placeholder_dose,
                    source = sourceA
                )
            )
        }

        Column(Modifier.weight(0.5f), Arrangement.spacedBy(8.dp)) {
            Text(
                text = LocalContext.current.getString(R.string.text_amount),
                style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold)
            )

            BasicTextField(
                value = if(state.default) decimalFormat(state.prodAmount) else state.prodAmount,
                onValueChange = { onEvent(MedicineEvent.SetProdAmount(it)) },
                modifier = Modifier.fillMaxWidth(),
                readOnly = state.default,
                textStyle = MaterialTheme.typography.bodyLarge.copy(MaterialTheme.colorScheme.onSurface),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                visualTransformation = {
                    TransformedText(
                        AnnotatedString(it.text.replace('.', ',')),
                        OffsetMapping.Identity
                    )
                },
                decorationBox = decorationBox(
                    state = state,
                    text = state.prodAmount,
                    id = R.string.placeholder_amount,
                    source = sourceB
                )
            )
        }
    }
}


@Composable
private fun PhKinetics(onEvent: (MedicineEvent) -> Unit, state: MedicineState) {
    val source = remember(::MutableInteractionSource)

    if (state.adding || state.editing || state.phKinetics.isNotEmpty())
        Column(modifier = Modifier.padding(horizontal = 16.dp)) {
            Text(
                text = LocalContext.current.getString(R.string.text_indications_for_use),
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
                decorationBox = decorationBox(
                    state = state,
                    text = state.phKinetics,
                    source = source
                )
            )
        }
}

@Composable
private fun Comment(onEvent: (MedicineEvent) -> Unit, state: MedicineState) {
    val source = remember(::MutableInteractionSource)

    if (state.adding || state.editing || state.comment.isNotEmpty())
        Column(modifier = Modifier.padding(16.dp, 0.dp, 16.dp, 16.dp)) {
            Text(
                text = LocalContext.current.getString(R.string.text_medicine_comment),
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
                decorationBox = decorationBox(
                    state = state,
                    text = state.comment,
                    source = source
                )
            )
        }
}

@Composable
@OptIn(ExperimentalMaterial3Api::class)
private fun decorationBox(
    state: MedicineState,
    text: String,
    id: Int = R.string.text_empty,
    source: MutableInteractionSource
): @Composable (innerTextField: @Composable () -> Unit) -> Unit {
    return {
        when {
            state.adding || state.editing && text == state.prodAmount ||
                    state.editing && text == state.comment ||
                    state.editing && !state.technical.verified ->
                OutlinedTextFieldDefaults.DecorationBox(
                    value = text,
                    innerTextField = it,
                    enabled = true,
                    singleLine = false,
                    visualTransformation = VisualTransformation.None,
                    interactionSource = source,
                    placeholder = { Text(LocalContext.current.getString(id)) },
                    container = {
                        OutlinedTextFieldDefaults.ContainerBox(
                            enabled = true,
                            isError = false,
                            interactionSource = source,
                            colors = OutlinedTextFieldDefaults.colors()
                        )
                    }
                )

            else -> OutlinedTextFieldDefaults.DecorationBox(
                value = text,
                innerTextField = it,
                enabled = true,
                singleLine = false,
                visualTransformation = VisualTransformation.None,
                interactionSource = source,
                contentPadding = PaddingValues(0.dp),
                container = {}
            )
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun IconPicker(onEvent: (MedicineEvent) -> Unit, onCancel: () -> Unit) {
    val context = LocalContext.current
    val names = context.resources.getStringArray(R.array.medicine_types)

    Dialog(onDismissRequest = onCancel) {
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