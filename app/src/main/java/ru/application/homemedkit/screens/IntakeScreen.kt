package ru.application.homemedkit.screens

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import android.os.Build.VERSION
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SelectableDates
import androidx.compose.material3.Text
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.material3.TimePicker
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberDateRangePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringArrayResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.OffsetMapping
import androidx.compose.ui.text.input.TransformedText
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.ramcosta.composedestinations.annotation.Destination
import com.ramcosta.composedestinations.annotation.RootGraph
import com.ramcosta.composedestinations.generated.destinations.IntakesScreenDestination
import com.ramcosta.composedestinations.navigation.DestinationsNavigator
import kotlinx.coroutines.flow.collectLatest
import ru.application.homemedkit.HomeMeds.Companion.database
import ru.application.homemedkit.R
import ru.application.homemedkit.dialogs.DateRangePicker
import ru.application.homemedkit.dialogs.TimePickerDialog
import ru.application.homemedkit.helpers.BLANK
import ru.application.homemedkit.helpers.FORMAT_D_MM_Y
import ru.application.homemedkit.helpers.FORMAT_S
import ru.application.homemedkit.helpers.Preferences
import ru.application.homemedkit.helpers.ZONE
import ru.application.homemedkit.helpers.decimalFormat
import ru.application.homemedkit.helpers.getDateTime
import ru.application.homemedkit.helpers.getPeriod
import ru.application.homemedkit.helpers.viewModelFactory
import ru.application.homemedkit.receivers.AlarmSetter
import ru.application.homemedkit.viewModels.IntakeState
import ru.application.homemedkit.viewModels.IntakeViewModel
import ru.application.homemedkit.viewModels.IntakeViewModel.Event.Add
import ru.application.homemedkit.viewModels.IntakeViewModel.Event.DecTime
import ru.application.homemedkit.viewModels.IntakeViewModel.Event.Delete
import ru.application.homemedkit.viewModels.IntakeViewModel.Event.IncTime
import ru.application.homemedkit.viewModels.IntakeViewModel.Event.SetAmount
import ru.application.homemedkit.viewModels.IntakeViewModel.Event.SetEditing
import ru.application.homemedkit.viewModels.IntakeViewModel.Event.SetFinal
import ru.application.homemedkit.viewModels.IntakeViewModel.Event.SetFoodType
import ru.application.homemedkit.viewModels.IntakeViewModel.Event.SetInterval
import ru.application.homemedkit.viewModels.IntakeViewModel.Event.SetMedicineId
import ru.application.homemedkit.viewModels.IntakeViewModel.Event.SetPeriod
import ru.application.homemedkit.viewModels.IntakeViewModel.Event.SetStart
import ru.application.homemedkit.viewModels.IntakeViewModel.Event.SetTime
import ru.application.homemedkit.viewModels.IntakeViewModel.Event.Update
import java.time.Instant
import java.time.LocalDate
import java.time.Period


@OptIn(ExperimentalMaterial3Api::class)
@Destination<RootGraph>
@Composable
fun IntakeScreen(
    intakeId: Long = 0L,
    medicineId: Long = 0L,
    navigator: DestinationsNavigator,
    context: Context = LocalContext.current,
    lifecycle: Lifecycle = LocalLifecycleOwner.current.lifecycle
) {
    val medId = if (intakeId == 0L) medicineId else
        database.intakeDAO().getById(intakeId)?.medicineId ?: 0L
    val medicine = database.medicineDAO().getById(medId)!!

    val viewModel = viewModel<IntakeViewModel>(factory = viewModelFactory {
        IntakeViewModel(intakeId, AlarmSetter(context))
    })
    val state by viewModel.state.collectAsStateWithLifecycle()
    viewModel.onEvent(SetMedicineId(medId))

    var permissionGranted by remember {
        if (VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU)
            mutableStateOf(checkNotificationPermission(context))
        else mutableStateOf(true)
    }
    var showRationale by remember { mutableStateOf(false) }

    val launcher = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) {
        if (it) permissionGranted = true else showRationale = true
    }

    if (!permissionGranted) {
        LaunchedEffect(Unit) {
            if (VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU)
                launcher.launch(Manifest.permission.POST_NOTIFICATIONS)
        }
        if (showRationale) PermissionDialog(R.string.text_request_post)
    }

    LaunchedEffect(Unit) {
        viewModel.events.collectLatest { navigator.navigate(IntakesScreenDestination) }
    }

    DisposableEffect(LocalLifecycleOwner.current) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME && VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                permissionGranted = checkNotificationPermission(context)
                showRationale = !permissionGranted
            }
        }
        lifecycle.addObserver(observer)
        onDispose { lifecycle.removeObserver(observer) }
    }

    BackHandler { navigator.navigate(IntakesScreenDestination) }
    Scaffold(
        topBar = {
            TopAppBar(
                title = {},
                navigationIcon = {
                    IconButton({ navigator.navigate(IntakesScreenDestination) })
                    { Icon(Icons.AutoMirrored.Filled.ArrowBack, null) }
                },
                actions = {
                    when {
                        state.adding -> {
                            IconButton(
                                onClick = { viewModel.onEvent(Add) },
                                enabled = viewModel.validate()
                            )
                            { Icon(Icons.Default.Check, null) }
                        }

                        state.editing -> {
                            IconButton(
                                onClick = { viewModel.onEvent(Update) },
                                enabled = viewModel.validate()
                            )
                            { Icon(Icons.Default.Check, null) }
                        }

                        else -> {
                            LocalFocusManager.current.clearFocus(true)
                            var expanded by remember { mutableStateOf(false) }

                            IconButton({ expanded = true }) {
                                Icon(Icons.Default.MoreVert, null)
                            }

                            DropdownMenu(expanded, { expanded = false }) {
                                DropdownMenuItem(
                                    { Text(stringResource(R.string.text_edit)) },
                                    { viewModel.onEvent(SetEditing) })
                                DropdownMenuItem(
                                    { Text(stringResource(R.string.text_delete)) },
                                    { viewModel.onEvent(Delete) }
                                )
                            }
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.tertiaryContainer,
                    navigationIconContentColor = MaterialTheme.colorScheme.onTertiaryContainer,
                    actionIconContentColor = MaterialTheme.colorScheme.onTertiaryContainer
                )
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(top = paddingValues.calculateTopPadding().plus(16.dp))
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(32.dp)
        ) {
            Title(medicine.productName)
            Amount(viewModel::onEvent, state, medicine.prodAmount, medicine.doseType)
            Interval(viewModel::onEvent, state)
            Period(viewModel::onEvent, state)
            Food(viewModel::onEvent, state)
            Time(viewModel::onEvent, state)
        }
    }
}

@Composable
private fun Title(name: String) {
    Column(Modifier.padding(horizontal = 16.dp), Arrangement.spacedBy(8.dp)) {
        LabelText(R.string.text_medicine_product_name)
        OutlinedTextField(
            value = name,
            onValueChange = {},
            modifier = Modifier.fillMaxWidth(),
            readOnly = true,
            shape = RoundedCornerShape(14.dp)
        )
    }
}

@Composable
private fun Amount(onEvent: (IntakeViewModel.Event) -> Unit, state: IntakeState, prodAmount: Double, type: String) {
    var isError by rememberSaveable { mutableStateOf(false) }

    fun validate(text: String) {
        isError = text.isBlank()
    }

    Row(Modifier.padding(horizontal = 16.dp), Arrangement.spacedBy(16.dp)) {
        Column(Modifier.weight(0.5f), Arrangement.spacedBy(8.dp)) {
            LabelText(R.string.intake_text_amount)
            OutlinedTextField(
                value = state.amount,
                onValueChange = {if (!state.default) onEvent(SetAmount(it)); validate(it)},
                modifier = Modifier.fillMaxWidth(),
                readOnly = state.default,
                placeholder = { Text("0,25") },
                leadingIcon = { Icon(painterResource(R.drawable.vector_medicine), null) },
                suffix = { Text(type) },
                isError = isError,
                visualTransformation = {
                    TransformedText(
                        AnnotatedString(it.text.replace('.', ',')),
                        OffsetMapping.Identity
                    )
                },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                maxLines = 1,
                shape = RoundedCornerShape(14.dp)
            )
        }

        Column(Modifier.weight(0.5f), Arrangement.spacedBy(8.dp)) {
            LabelText(R.string.intake_text_left)
            OutlinedTextField(
                value = decimalFormat(prodAmount),
                onValueChange = {},
                modifier = Modifier.fillMaxWidth(),
                readOnly = true,
                leadingIcon = { Icon(Icons.Default.Home, null) },
                suffix = { Text(type) },
                maxLines = 1,
                shape = RoundedCornerShape(14.dp)
            )
        }
    }
}


@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun Interval(onEvent: (IntakeViewModel.Event) -> Unit, state: IntakeState) {
    val intervals = stringArrayResource(R.array.interval_types_name)
    var interval by remember {
        mutableStateOf(
            try {
                when (state.interval.toInt()) {
                    1 -> intervals[0]
                    7 -> intervals[1]
                    else -> if (state.intakeId != 0L) intervals[2] else BLANK
                }
            } catch (e: NumberFormatException) {
                BLANK
            }
        )
    }
    var expanded by remember { mutableStateOf(false) }


    Column(Modifier.padding(horizontal = 16.dp), Arrangement.spacedBy(8.dp)) {
        LabelText(R.string.intake_text_interval)

        Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
            ExposedDropdownMenuBox(
                expanded = expanded,
                onExpandedChange = { if (state.adding || state.editing) expanded = it },
                modifier = Modifier.weight(0.5f)
            ) {
                OutlinedTextField(
                    value = interval,
                    onValueChange = {},
                    modifier = Modifier
                        .fillMaxWidth()
                        .menuAnchor(),
                    readOnly = true,
                    placeholder = { Text(stringResource(R.string.intake_text_select_interval)) },
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded) },
                    shape = RoundedCornerShape(14.dp)
                )
                ExposedDropdownMenu(expanded, { expanded = false }) {
                    intervals.forEachIndexed { index, selectionOption ->
                        DropdownMenuItem(
                            text = { Text(selectionOption) },
                            onClick = {
                                interval = selectionOption; expanded = false
                                onEvent(SetInterval(index))
                            },
                            contentPadding = ExposedDropdownMenuDefaults.ItemContentPadding
                        )
                    }
                }
            }

            if (interval == intervals.last()) {
                var isError by rememberSaveable { mutableStateOf(false) }

                fun validate(text: String) {
                    isError = text.isBlank()
                }

                OutlinedTextField(
                    value = state.interval,
                    onValueChange = { onEvent(SetInterval(it)); validate(it) },
                    modifier = Modifier.weight(0.5f),
                    readOnly = state.default,
                    placeholder = { Text("N") },
                    prefix = { Text(stringResource(R.string.text_every)) },
                    suffix = { Text(stringResource(R.string.text_days_short)) },
                    isError = isError,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp)
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun Period(onEvent: (IntakeViewModel.Event) -> Unit, state: IntakeState) {
    val dateST = stringResource(R.string.text_start_date)
    val dateFT = stringResource(R.string.text_finish_date)

    Column(Modifier.padding(horizontal = 16.dp), Arrangement.spacedBy(8.dp)) {
        LabelText(R.string.intake_text_period)

        when {
            Preferences.getLightPeriod() -> {
                val periods = stringArrayResource(R.array.period_types_name)
                var period by rememberSaveable {
                    mutableStateOf(
                        when (state.periodD) {
                            7 -> periods[0]
                            30 -> periods[1]
                            38500 -> periods[3]
                            else -> if (state.intakeId != 0L) periods[2] else BLANK
                        }
                    )
                }
                var expanded by remember { mutableStateOf(false) }

                if (state.period.isBlank() && period != periods[2] && state.editing)
                    onEvent(SetPeriod(getPeriod(state.startDate, state.finalDate)))

                Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                    ExposedDropdownMenuBox(
                        expanded = expanded,
                        onExpandedChange = { if (state.adding || state.editing) expanded = it },
                        modifier = Modifier.weight(1f)
                    ) {
                        OutlinedTextField(
                            value = period,
                            onValueChange = {},
                            modifier = Modifier
                                .fillMaxWidth()
                                .menuAnchor(),
                            readOnly = true,
                            placeholder = { Text(stringResource(R.string.intake_text_pick_period)) },
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded) },
                            shape = RoundedCornerShape(14.dp)
                        )
                        ExposedDropdownMenu(expanded, { expanded = false }) {
                            periods.forEachIndexed { index, selectionOption ->
                                DropdownMenuItem(
                                    text = { Text(selectionOption) },
                                    onClick = {
                                        period = selectionOption; expanded = false
                                        onEvent(SetPeriod(index))
                                    },
                                    contentPadding = ExposedDropdownMenuDefaults.ItemContentPadding
                                )
                            }
                        }
                    }

                    if (period == periods[2] && (state.adding || state.editing)) {
                        if(state.editing) LaunchedEffect(Unit) {
                            onEvent(SetPeriod(getPeriod(state.startDate, state.finalDate)))
                        }
                        var isError by rememberSaveable { mutableStateOf(false) }


                        fun validate(text: String) {
                            isError = text.isEmpty()
                        }

                        OutlinedTextField(
                            value = state.period,
                            onValueChange = { onEvent(SetPeriod(it)); validate(it) },
                            modifier = Modifier.weight(1f),
                            readOnly = state.default,
                            textStyle = MaterialTheme.typography.titleMedium,
                            placeholder = { Text("10") },
                            leadingIcon = { Icon(Icons.Default.DateRange, null) },
                            suffix = { Text(stringResource(R.string.text_days_short)) },
                            isError = isError,
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            singleLine = true,
                            shape = RoundedCornerShape(14.dp)
                        )
                    }
                }

                if (period.isNotEmpty() && period != periods[3]) {
                    if ((state.adding || state.editing) && state.period.isNotEmpty()) {
                        onEvent(SetStart())
                        onEvent(SetFinal())
                    }

                    Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                        OutlinedTextField(
                            value = state.startDate,
                            onValueChange = {},
                            modifier = Modifier.weight(0.5f),
                            enabled = false,
                            readOnly = true,
                            leadingIcon = { Icon(Icons.Default.DateRange, null) },
                            supportingText = { Text(dateST) },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            singleLine = true,
                            shape = RoundedCornerShape(14.dp),
                            colors = fieldColorsInverted()
                        )
                        OutlinedTextField(
                            value = state.finalDate,
                            onValueChange = {},
                            modifier = Modifier.weight(0.5f),
                            enabled = false,
                            readOnly = true,
                            leadingIcon = { Icon(Icons.Default.DateRange, null) },
                            supportingText = { Text(dateFT) },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            singleLine = true,
                            shape = RoundedCornerShape(14.dp),
                            colors = fieldColorsInverted()
                        )
                    }
                }
            }

            else -> {
                Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                    val dateState =
                        rememberDateRangePickerState(selectableDates = object : SelectableDates {
                            override fun isSelectableDate(utcTimeMillis: Long) =
                                LocalDate.now() <= Instant.ofEpochMilli(utcTimeMillis)
                                    .atZone(ZONE)
                                    .toLocalDate()

                            override fun isSelectableYear(year: Int) = LocalDate.now().year <= year
                        })

                    var isError by rememberSaveable { mutableStateOf(false) }
                    var showPicker by remember { mutableStateOf(false) }
                    var selectS: String? by remember { mutableStateOf(null) }
                    var selectF: String? by remember { mutableStateOf(null) }

                    OutlinedTextField(
                        value = state.startDate,
                        onValueChange = {},
                        modifier = Modifier
                            .weight(0.5f)
                            .clickable { showPicker = true },
                        enabled = false,
                        readOnly = true,
                        placeholder = { Text(stringResource(R.string.placeholder_date)) },
                        leadingIcon = { Icon(Icons.Default.DateRange, null) },
                        supportingText = { Text(dateST) },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true,
                        shape = RoundedCornerShape(14.dp),
                        colors = fieldColorsInverted(isError)
                    )
                    OutlinedTextField(
                        value = state.finalDate,
                        onValueChange = {},
                        modifier = Modifier
                            .weight(0.5f)
                            .clickable { showPicker = true },
                        enabled = false,
                        readOnly = true,
                        placeholder = { Text(stringResource(R.string.placeholder_date)) },
                        leadingIcon = { Icon(Icons.Default.DateRange, null) },
                        supportingText = { Text(dateFT) },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true,
                        shape = RoundedCornerShape(14.dp),
                        colors = fieldColorsInverted(isError)
                    )

                    dateState.selectedStartDateMillis?.let { start ->
                        selectS = getDateTime(start).format(FORMAT_D_MM_Y)
                    }

                    dateState.selectedEndDateMillis?.let { end ->
                        selectF = getDateTime(end).format(FORMAT_D_MM_Y)
                    }

                    if (state.startDate.isNotEmpty() && state.finalDate.isNotEmpty()) {
                        val milliS = LocalDate.parse(state.startDate, FORMAT_S)
                        val milliF = LocalDate.parse(state.finalDate, FORMAT_S)

                        isError = milliF < milliS
                        if (!isError)
                            onEvent(SetPeriod(Period.between(milliS, milliF).days))
                    }

                    if (showPicker) DateRangePicker(
                        state = dateState,
                        start = selectS,
                        final = selectF,
                        onDismiss = { showPicker = false },
                        onConfirm = {
                            selectS?.let { onEvent(SetStart(it)) }
                            selectF?.let { onEvent(SetFinal(it)) }
                            showPicker = false
                        }
                    )
                }
            }
        }
    }
}

@Composable
private fun Food(onEvent: (IntakeViewModel.Event) -> Unit, state: IntakeState) {
    val icons = listOf(
        R.drawable.vector_before_food,
        R.drawable.vector_in_food,
        R.drawable.vector_after_food
    )
    val options = listOf(
        stringResource(R.string.intake_text_food_before),
        stringResource(R.string.intake_text_food_during),
        stringResource(R.string.intake_text_food_after)
    )
    var selected by remember { mutableIntStateOf(state.foodType) }

    Column(Modifier.padding(horizontal = 16.dp), Arrangement.spacedBy(8.dp)) {
        LabelText(R.string.intake_text_food)
        Row(Modifier.fillMaxWidth(), Arrangement.SpaceBetween) {
            options.forEachIndexed { index, label ->
                Column(
                    modifier = Modifier
                        .size(112.dp, 96.dp)
                        .border(
                            1.dp,
                            MaterialTheme.colorScheme.onSurface,
                            RoundedCornerShape(16.dp)
                        )
                        .background(
                            if (index == selected) MaterialTheme.colorScheme.secondaryContainer
                            else Color.Transparent,
                            RoundedCornerShape(16.dp)
                        )
                        .clickable {
                            if (!state.default) {
                                selected = if (selected == index) -1 else index
                                onEvent(SetFoodType(selected))
                            }
                        },
                    verticalArrangement = Arrangement.SpaceEvenly,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Icon(painterResource(icons[index]), null, Modifier.size(32.dp))
                    Text(
                        text = label,
                        textAlign = TextAlign.Center,
                        maxLines = 2,
                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.SemiBold)
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
private fun Time(onEvent: (IntakeViewModel.Event) -> Unit, state: IntakeState) {
    var picker by rememberSaveable { mutableStateOf(false) }
    var field by rememberSaveable { mutableIntStateOf(0) }

    Column(
        modifier = Modifier.padding(start = 16.dp, end = 16.dp, bottom = 16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        LabelText(R.string.intake_text_time)

        when {
            state.adding || state.editing -> {
                repeat(state.time.size) { index ->
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        OutlinedTextField(
                            value = state.time[index],
                            onValueChange = {},
                            modifier = Modifier
                                .fillMaxWidth(0.8f)
                                .clickable { field = index; picker = true },
                            enabled = false,
                            placeholder = {
                                Text(
                                    String.format(stringResource(R.string.placeholder_time), index + 1)
                                )
                            },
                            leadingIcon = { Icon(painterResource(R.drawable.vector_time), null) },
                            shape = RoundedCornerShape(14.dp),
                            colors = fieldColorsInverted()
                        )

                        val showBox: Boolean
                        var color = Color(MaterialTheme.colorScheme.secondaryContainer.value)
                        var icon = Icons.Default.Add
                        var tint = MaterialTheme.colorScheme.onSecondaryContainer
                        var onClick = {}

                        when (index + 1) {
                            1 -> {
                                showBox = true
                                color = Color(MaterialTheme.colorScheme.secondaryContainer.value)
                                icon = Icons.Default.Add
                                tint = MaterialTheme.colorScheme.onSecondaryContainer
                                onClick = { onEvent(IncTime) }
                            }

                            state.time.size -> {
                                showBox = true
                                color = Color(MaterialTheme.colorScheme.errorContainer.value)
                                icon = ImageVector.vectorResource(R.drawable.vector_remove)
                                tint = MaterialTheme.colorScheme.onErrorContainer
                                onClick = { onEvent(DecTime) }
                            }

                            else -> showBox = false
                        }

                        if (showBox) Box(
                            Modifier
                                .size(56.dp)
                                .background(color, RoundedCornerShape(12.dp))
                                .clickable { onClick() }, Alignment.Center
                        ) {
                            Image(
                                painter = rememberVectorPainter(icon),
                                contentDescription = null,
                                colorFilter = ColorFilter.tint(tint)
                            )
                        }
                    }
                }

                if (picker) TimePickerDialog(
                    onCancel = { picker = false },
                    onConfirm = { onEvent((SetTime(field))); picker = false },
                ) { TimePicker(state.times[field]) }
            }

            else -> {
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    repeat(state.time.size) { index ->
                        OutlinedTextField(
                            value = state.time[index],
                            onValueChange = {},
                            modifier = Modifier.width(128.dp),
                            enabled = false,
                            leadingIcon = { Icon(painterResource(R.drawable.vector_time), null) },
                            shape = RoundedCornerShape(14.dp),
                            colors = fieldColorsInverted()
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun fieldColorsInverted(isError: Boolean = false) = TextFieldDefaults.colors(
    disabledTextColor = MaterialTheme.colorScheme.onSurface,
    disabledContainerColor = when {
        isError -> MaterialTheme.colorScheme.errorContainer
        else -> MaterialTheme.colorScheme.surface
    },
    disabledPlaceholderColor = MaterialTheme.colorScheme.onSurfaceVariant,
    disabledLeadingIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
    disabledIndicatorColor = MaterialTheme.colorScheme.outline
)

@Composable
private fun LabelText(id: Int) = Text(
    text = stringResource(id),
    color = MaterialTheme.colorScheme.onSurface,
    fontWeight = FontWeight.SemiBold,
    style = MaterialTheme.typography.titleLarge
)

@RequiresApi(Build.VERSION_CODES.TIRAMISU)
private fun checkNotificationPermission(
    context: Context,
    permission: String = Manifest.permission.POST_NOTIFICATIONS
): Boolean = context.checkSelfPermission(permission) == PackageManager.PERMISSION_GRANTED