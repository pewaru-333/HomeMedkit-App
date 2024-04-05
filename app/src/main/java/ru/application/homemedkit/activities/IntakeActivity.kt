package ru.application.homemedkit.activities

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.NotificationManager.IMPORTANCE_HIGH
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Build.VERSION
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
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
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.OffsetMapping
import androidx.compose.ui.text.input.TransformedText
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import kotlinx.coroutines.flow.collectLatest
import ru.application.homemedkit.R
import ru.application.homemedkit.alarms.AlarmSetter
import ru.application.homemedkit.databaseController.MedicineDatabase
import ru.application.homemedkit.dialogs.DateRangePicker
import ru.application.homemedkit.dialogs.TimePickerDialog
import ru.application.homemedkit.fragments.FragmentSettings
import ru.application.homemedkit.helpers.BLANK
import ru.application.homemedkit.helpers.DateHelper.FORMAT_D_MM_Y
import ru.application.homemedkit.helpers.DateHelper.FORMAT_S
import ru.application.homemedkit.helpers.DateHelper.ZONE
import ru.application.homemedkit.helpers.DateHelper.getDateTime
import ru.application.homemedkit.helpers.DateHelper.getPeriod
import ru.application.homemedkit.helpers.INTAKE_ID
import ru.application.homemedkit.helpers.MEDICINE_ID
import ru.application.homemedkit.helpers.NEW_INTAKE
import ru.application.homemedkit.helpers.decimalFormat
import ru.application.homemedkit.helpers.viewModelFactory
import ru.application.homemedkit.states.IntakeState
import ru.application.homemedkit.ui.theme.AppTheme
import ru.application.homemedkit.viewModels.IntakeEvent
import ru.application.homemedkit.viewModels.IntakeViewModel
import java.time.Instant
import java.time.LocalDate
import java.time.Period

class IntakeActivity : ComponentActivity() {

    private lateinit var database: MedicineDatabase

    @OptIn(ExperimentalMaterial3Api::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        database = MedicineDatabase.getInstance(this)
        val medicineDAO = database.medicineDAO()

        val intakeId = intent.getLongExtra(INTAKE_ID, 0L)
        val medicineId = if (intakeId == 0L) intent.getLongExtra(MEDICINE_ID, 0L)
        else database.intakeDAO().getByPK(intakeId)?.medicineId ?: 0L

        val newIntent = Intent(this, MainActivity::class.java)
            .setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK)
            .putExtra(NEW_INTAKE, true)

        setContent {
            val viewModel = viewModel<IntakeViewModel>(factory = viewModelFactory {
                IntakeViewModel(database.intakeDAO(), intakeId, AlarmSetter(this))
            })
            val state by viewModel.uiState.collectAsStateWithLifecycle()
            viewModel.onEvent(IntakeEvent.SetMedicineId(medicineId))

            var permissionGranted by remember {
                if (VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU)
                    mutableStateOf(checkNotificationPermission(this))
                else mutableStateOf(true)
            }
            var showRationale by remember { mutableStateOf(false) }

            val launcher = rememberLauncherForActivityResult(
                contract = ActivityResultContracts.RequestPermission(),
                onResult = { isGranted ->
                    if (isGranted) permissionGranted = true
                    else showRationale = true
                }
            )

            LaunchedEffect(Unit) { viewModel.events.collectLatest { startActivity(newIntent) } }

            DisposableEffect(LocalLifecycleOwner.current) {
                val observer = LifecycleEventObserver { _, event ->
                    if (event == Lifecycle.Event.ON_RESUME && VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                        permissionGranted = checkNotificationPermission(this@IntakeActivity)
                        showRationale = !permissionGranted
                    }
                }
                lifecycle.addObserver(observer)
                onDispose { lifecycle.removeObserver(observer) }
            }

            AppTheme {
                Scaffold(
                    topBar = {
                        TopAppBar(
                            title = {},
                            navigationIcon = {
                                IconButton({
                                    startActivity(
                                        Intent(this, MainActivity::class.java)
                                            .putExtra(NEW_INTAKE, true)
                                    )
                                }
                                ) { Icon(Icons.AutoMirrored.Filled.ArrowBack, null) }
                            },
                            actions = {
                                when {
                                    state.adding -> {
                                        IconButton(
                                            onClick = { viewModel.onEvent(IntakeEvent.Add) },
                                            enabled = viewModel.validate()
                                        )
                                        { Icon(Icons.Default.Check, null) }
                                    }

                                    state.editing -> {
                                        IconButton(
                                            onClick = { viewModel.onEvent(IntakeEvent.Update) },
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
                                                { Text(resources.getString(R.string.text_edit)) },
                                                { viewModel.onEvent(IntakeEvent.SetEditing) })
                                            DropdownMenuItem(
                                                { Text(resources.getString(R.string.text_delete)) },
                                                { viewModel.onEvent(IntakeEvent.Delete) }
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

                    CreateNotificationChannel()
                    if (!permissionGranted) {
                        LaunchedEffect(Unit) {
                            if (VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU)
                                launcher.launch(Manifest.permission.POST_NOTIFICATIONS)
                        }
                        if (showRationale) PermissionDialog(id = R.string.text_request_post)
                    }

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
                        Title(medicineDAO.getProductName(medicineId))
                        Amount(viewModel::onEvent, state, medicineDAO.getProdAmount(medicineId))
                        Interval(viewModel::onEvent, state)
                        Period(viewModel::onEvent, state)
                        Food(viewModel::onEvent, state)
                        Time(viewModel::onEvent, state)
                    }
                }
                BackHandler { startActivity(newIntent) }
            }
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
private fun Amount(onEvent: (IntakeEvent) -> Unit, state: IntakeState, prodAmount: Double) {
    var isError by rememberSaveable { mutableStateOf(false) }

    fun validate(text: String) {
        isError = text.isBlank()
    }

    Row(Modifier.padding(horizontal = 16.dp), Arrangement.spacedBy(16.dp)) {
        Column(Modifier.weight(0.5f), Arrangement.spacedBy(8.dp)) {
            LabelText(R.string.intake_text_amount)
            OutlinedTextField(
                value = state.amount,
                onValueChange = {if (!state.default) onEvent(IntakeEvent.SetAmount(it)); validate(it)},
                modifier = Modifier.fillMaxWidth(),
                readOnly = state.default,
                placeholder = { Text("0,25") },
                leadingIcon = { Icon(painterResource(R.drawable.vector_medicine), null) },
                isError = isError,
                visualTransformation = {
                    TransformedText(
                        AnnotatedString(it.text.replace('.', ',')),
                        OffsetMapping.Identity
                    )
                },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
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
                suffix = { Text(LocalContext.current.resources.getString(R.string.placeholder_pcs)) },
                shape = RoundedCornerShape(14.dp)
            )
        }
    }
}


@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun Interval(onEvent: (IntakeEvent) -> Unit, state: IntakeState) {
    val resources = LocalContext.current.resources
    val intervals = resources.getStringArray(R.array.interval_types_name)
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
                    placeholder = { Text(resources.getString(R.string.intake_text_select_interval)) },
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded) },
                    shape = RoundedCornerShape(14.dp)
                )
                ExposedDropdownMenu(expanded, { expanded = false }) {
                    intervals.forEachIndexed { index, selectionOption ->
                        DropdownMenuItem(
                            text = { Text(selectionOption) },
                            onClick = {
                                interval = selectionOption; expanded = false
                                onEvent(IntakeEvent.SetInterval(index))
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
                    onValueChange = { onEvent(IntakeEvent.SetInterval(it)); validate(it) },
                    modifier = Modifier.weight(0.5f),
                    readOnly = state.default,
                    placeholder = { Text("N") },
                    prefix = { Text(resources.getString(R.string.text_every)) },
                    suffix = { Text(resources.getString(R.string.text_days_short)) },
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
private fun Period(onEvent: (IntakeEvent) -> Unit, state: IntakeState) {
    val context = LocalContext.current
    val dateST = context.getString(R.string.text_start_date)
    val dateFT = context.getString(R.string.text_finish_date)

    Column(Modifier.padding(horizontal = 16.dp), Arrangement.spacedBy(8.dp)) {
        LabelText(R.string.intake_text_period)

        when {
            FragmentSettings().getLightPeriod() -> {
                val periods = context.resources.getStringArray(R.array.period_types_name)
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
                    onEvent(IntakeEvent.SetPeriod(getPeriod(state.startDate, state.finalDate)))

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
                            placeholder = { Text(context.getString(R.string.intake_text_pick_period)) },
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded) },
                            shape = RoundedCornerShape(14.dp)
                        )
                        ExposedDropdownMenu(expanded, { expanded = false }) {
                            periods.forEachIndexed { index, selectionOption ->
                                DropdownMenuItem(
                                    text = { Text(selectionOption) },
                                    onClick = {
                                        period = selectionOption; expanded = false
                                        onEvent(IntakeEvent.SetPeriod(index))
                                    },
                                    contentPadding = ExposedDropdownMenuDefaults.ItemContentPadding
                                )
                            }
                        }
                    }

                    if (period == periods[2] && (state.adding || state.editing)) {
                        if(state.editing) LaunchedEffect(Unit) {
                            onEvent(IntakeEvent.SetPeriod(getPeriod(state.startDate, state.finalDate)))
                        }
                        var isError by rememberSaveable { mutableStateOf(false) }


                        fun validate(text: String) {
                            isError = text.isEmpty()
                        }

                        OutlinedTextField(
                            value = state.period,
                            onValueChange = { onEvent(IntakeEvent.SetPeriod(it)); validate(it) },
                            modifier = Modifier.weight(1f),
                            readOnly = state.default,
                            textStyle = MaterialTheme.typography.titleMedium,
                            placeholder = { Text("10") },
                            leadingIcon = { Icon(Icons.Default.DateRange, null) },
                            suffix = { Text(context.getString(R.string.text_days_short)) },
                            isError = isError,
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            singleLine = true,
                            shape = RoundedCornerShape(14.dp)
                        )
                    }
                }

                if (period.isNotEmpty() && period != periods[3]) {
                    if ((state.adding || state.editing) && state.period.isNotEmpty()) {
                        onEvent(IntakeEvent.SetStart())
                        onEvent(IntakeEvent.SetFinal())
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
                        placeholder = { Text(context.getString(R.string.placeholder_date)) },
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
                        placeholder = { Text(context.getString(R.string.placeholder_date)) },
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
                            onEvent(IntakeEvent.SetPeriod(Period.between(milliS, milliF).days))
                    }

                    if (showPicker) DateRangePicker(
                        state = dateState,
                        start = selectS,
                        final = selectF,
                        onDismiss = { showPicker = false },
                        onConfirm = {
                            selectS?.let { onEvent(IntakeEvent.SetStart(it)) }
                            selectF?.let { onEvent(IntakeEvent.SetFinal(it)) }
                            showPicker = false
                        }
                    )
                }
            }
        }
    }
}

@Composable
private fun Food(onEvent: (IntakeEvent) -> Unit, state: IntakeState) {
    val context = LocalContext.current
    val icons = listOf(
        R.drawable.vector_before_food,
        R.drawable.vector_in_food,
        R.drawable.vector_after_food
    )
    val options = listOf(
        context.getString(R.string.intake_text_food_before),
        context.getString(R.string.intake_text_food_during),
        context.getString(R.string.intake_text_food_after)
    )
    var selected by remember { mutableIntStateOf(state.foodType) }

    Column(Modifier.padding(horizontal = 16.dp), Arrangement.spacedBy(8.dp)) {
        LabelText(R.string.intake_text_food)
        Row(Modifier.fillMaxWidth(), Arrangement.SpaceBetween) {
            options.forEachIndexed { index, label ->
                Column(
                    modifier = Modifier
                        .width(112.dp)
                        .height(96.dp)
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
                                onEvent(IntakeEvent.SetFoodType(selected))
                            }
                        },
                    verticalArrangement = Arrangement.SpaceEvenly,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Icon(
                        painter = painterResource(icons[index]),
                        contentDescription = null,
                        modifier = Modifier.size(32.dp)
                    )
                    Text(
                        text = label,
                        textAlign = TextAlign.Center,
                        maxLines = 2,
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontWeight = FontWeight.SemiBold
                        )
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
private fun Time(onEvent: (IntakeEvent) -> Unit, state: IntakeState) {
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
                                    String.format(
                                        LocalContext.current.getString(R.string.placeholder_time),
                                        index + 1
                                    )
                                )
                            },
                            leadingIcon = { Icon(painterResource(R.drawable.vector_time), null) },
                            shape = RoundedCornerShape(14.dp),
                            colors = fieldColorsInverted()
                        )

                        val showBox: Boolean
                        var color = Color(MaterialTheme.colorScheme.secondaryContainer.value)
                        var icon = R.drawable.vector_add
                        var tint = MaterialTheme.colorScheme.onSecondaryContainer
                        var onClick = {}

                        when (index + 1) {
                            1 -> {
                                showBox = true
                                color = Color(MaterialTheme.colorScheme.secondaryContainer.value)
                                icon = R.drawable.vector_add
                                tint = MaterialTheme.colorScheme.onSecondaryContainer
                                onClick = { onEvent(IntakeEvent.IncTime) }
                            }

                            state.time.size -> {
                                showBox = true
                                color = Color(MaterialTheme.colorScheme.errorContainer.value)
                                icon = R.drawable.vector_remove
                                tint = MaterialTheme.colorScheme.onErrorContainer
                                onClick = { onEvent(IntakeEvent.DecTime) }
                            }

                            else -> showBox = false
                        }

                        if (showBox) Box(
                            Modifier
                                .size(56.dp)
                                .background(color, RoundedCornerShape(12.dp))
                                .clickable { onClick() }, Alignment.Center
                        ) {
                            Icon(
                                painter = painterResource(icon),
                                contentDescription = null,
                                tint = tint
                            )
                        }
                    }
                }

                if (picker) TimePickerDialog(
                    onCancel = { picker = false },
                    onConfirm = { onEvent((IntakeEvent.SetTime(field))); picker = false },
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
private fun LabelText(id: Int, text: String = LocalContext.current.getString(id)) {
    Text(
        text = text,
        color = MaterialTheme.colorScheme.onSurface,
        fontWeight = FontWeight.SemiBold,
        style = MaterialTheme.typography.titleLarge
    )
}
@Composable
private fun CreateNotificationChannel(context: Context = LocalContext.current) {
    val channel = NotificationChannel(
        context.getString(R.string.notification_channel_name),
        context.getString(R.string.notification_channel_name),
        IMPORTANCE_HIGH
    )
    channel.description = context.getString(R.string.notification_channel_description)

    val manager = context.getSystemService(NotificationManager::class.java)
    manager.createNotificationChannel(channel)
}

@RequiresApi(Build.VERSION_CODES.TIRAMISU)
private fun checkNotificationPermission(
    context: ComponentActivity,
    permission: String = Manifest.permission.POST_NOTIFICATIONS
): Boolean = context.checkSelfPermission(permission) == PackageManager.PERMISSION_GRANTED