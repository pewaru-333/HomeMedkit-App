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
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Arrangement.spacedBy
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
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material.icons.outlined.Check
import androidx.compose.material.icons.outlined.DateRange
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.MoreVert
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MenuAnchorType
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.material3.TimePicker
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Alignment.Companion.CenterVertically
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.painterResource
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
import ru.application.homemedkit.R.drawable.vector_medicine
import ru.application.homemedkit.R.drawable.vector_period
import ru.application.homemedkit.R.drawable.vector_remove
import ru.application.homemedkit.R.drawable.vector_time
import ru.application.homemedkit.R.string.intake_text_amount
import ru.application.homemedkit.R.string.intake_text_food
import ru.application.homemedkit.R.string.intake_text_interval
import ru.application.homemedkit.R.string.intake_text_left
import ru.application.homemedkit.R.string.intake_text_period
import ru.application.homemedkit.R.string.intake_text_pick_period
import ru.application.homemedkit.R.string.intake_text_select_interval
import ru.application.homemedkit.R.string.intake_text_time
import ru.application.homemedkit.R.string.placeholder_time
import ru.application.homemedkit.R.string.text_days_short
import ru.application.homemedkit.R.string.text_delete
import ru.application.homemedkit.R.string.text_edit
import ru.application.homemedkit.R.string.text_empty
import ru.application.homemedkit.R.string.text_every
import ru.application.homemedkit.R.string.text_finish_date
import ru.application.homemedkit.R.string.text_medicine_product_name
import ru.application.homemedkit.R.string.text_request_post
import ru.application.homemedkit.R.string.text_start_date
import ru.application.homemedkit.dialogs.DateRangePicker
import ru.application.homemedkit.dialogs.TimePickerDialog
import ru.application.homemedkit.helpers.DoseTypes
import ru.application.homemedkit.helpers.FoodTypes
import ru.application.homemedkit.helpers.Intervals
import ru.application.homemedkit.helpers.Periods
import ru.application.homemedkit.helpers.decimalFormat
import ru.application.homemedkit.helpers.viewModelFactory
import ru.application.homemedkit.receivers.AlarmSetter
import ru.application.homemedkit.viewModels.IntakeState
import ru.application.homemedkit.viewModels.IntakeViewModel


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

    val model = viewModel<IntakeViewModel>(factory = viewModelFactory {
        IntakeViewModel(intakeId, AlarmSetter(context))
    })
    val state by model.state.collectAsStateWithLifecycle()
    model.setMedicineId(medId)

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
        if (showRationale) PermissionDialog(text_request_post)
    }

    LaunchedEffect(Unit) {
        model.events.collectLatest { navigator.navigate(IntakesScreenDestination) }
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
                    { Icon(Icons.AutoMirrored.Outlined.ArrowBack, null) }
                },
                actions = {
                    if (state.adding || state.editing) IconButton(
                        onClick = if (state.adding) model::add else model::update,
                        enabled = model.validate()
                    ) { Icon(Icons.Outlined.Check, null) }
                    else {
                        LocalFocusManager.current.clearFocus(true)
                        var expanded by remember { mutableStateOf(false) }

                        IconButton({ expanded = true }) { Icon(Icons.Outlined.MoreVert, null) }
                        DropdownMenu(expanded, { expanded = false }) {
                            DropdownMenuItem({ Text(stringResource(text_edit)) }, model::setEditing)
                            DropdownMenuItem({ Text(stringResource(text_delete)) }, model::delete)
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
    ) { values ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(top = values.calculateTopPadding().plus(16.dp))
                .verticalScroll(rememberScrollState()),
            verticalArrangement = spacedBy(32.dp)
        ) {
            Title(medicine.productName)
            Amount(model, state, medicine.prodAmount, medicine.doseType)
            Interval(model, state)
            Period(model, state)
            Food(model::setFoodType, state)
            Time(model, state)
        }
    }

    when {
        state.showPeriodD -> DateRangePicker(
            startDate = state.startDate,
            finalDate = state.finalDate,
            onDismiss = model::showPeriodD,
            onRangeSelected = model::setPeriod
        )

        state.showTimeP -> TimePickerDialog(model::showTimePicker, model::setTime)
        { TimePicker(state.times[state.timeF]) }
    }
}

@Composable
private fun Title(name: String) = Column(Modifier.padding(horizontal = 16.dp), spacedBy(8.dp)) {
    LabelText(text_medicine_product_name)
    OutlinedTextField(
        value = name,
        onValueChange = {},
        modifier = Modifier.fillMaxWidth(),
        readOnly = true,
        shape = RoundedCornerShape(14.dp)
    )
}

@Composable
private fun Amount(model: IntakeViewModel, state: IntakeState, prodAmount: Double, type: String) =
    Row(Modifier.padding(horizontal = 16.dp), spacedBy(16.dp)) {
        Column(Modifier.weight(0.5f), spacedBy(8.dp)) {
            LabelText(intake_text_amount)
            OutlinedTextField(
                value = state.amount,
                onValueChange = model::setAmount,
                modifier = Modifier.fillMaxWidth(),
                readOnly = state.default,
                placeholder = { Text(stringResource(text_empty)) },
                leadingIcon = { Icon(painterResource(vector_medicine), null) },
                suffix = { Text(stringResource(DoseTypes.getTitle(type))) },
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

        Column(Modifier.weight(0.5f), spacedBy(8.dp)) {
            LabelText(intake_text_left)
            OutlinedTextField(
                value = decimalFormat(prodAmount),
                onValueChange = {},
                modifier = Modifier.fillMaxWidth(),
                readOnly = true,
                leadingIcon = { Icon(Icons.Outlined.Home, null) },
                suffix = { Text(stringResource(DoseTypes.getTitle(type))) },
                maxLines = 1,
                shape = RoundedCornerShape(14.dp)
            )
        }
    }


@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun Interval(model: IntakeViewModel, state: IntakeState) =
    Column(Modifier.padding(horizontal = 16.dp), spacedBy(8.dp)) {
        LabelText(intake_text_interval)
        Row(horizontalArrangement = spacedBy(16.dp)) {
            ExposedDropdownMenuBox(
                expanded = state.showIntervalM,
                onExpandedChange = model::showIntervalM,
                modifier = Modifier.weight(0.5f)
            ) {
                OutlinedTextField(
                    value = stringResource(model.getIntervalTitle()),
                    onValueChange = {},
                    modifier = Modifier
                        .fillMaxWidth()
                        .menuAnchor(MenuAnchorType.PrimaryNotEditable),
                    readOnly = true,
                    placeholder = { Text(stringResource(intake_text_select_interval)) },
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(state.showIntervalM) },
                    shape = RoundedCornerShape(14.dp)
                )
                ExposedDropdownMenu(state.showIntervalM, { model.showIntervalM(false) }) {
                    Intervals.entries.forEach {
                        DropdownMenuItem(
                            text = { Text(stringResource(it.title)) },
                            contentPadding = ExposedDropdownMenuDefaults.ItemContentPadding,
                            onClick = { model.setInterval(it) }
                        )
                    }
                }
            }

            if (state.intervalE == Intervals.Custom) OutlinedTextField(
                value = state.interval,
                onValueChange = model::setInterval,
                modifier = Modifier.weight(0.5f),
                readOnly = state.default,
                placeholder = { Text("N") },
                prefix = { Text(stringResource(text_every)) },
                suffix = { Text(stringResource(text_days_short)) },
                isError = state.interval.isEmpty(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                singleLine = true,
                shape = RoundedCornerShape(12.dp)
            )
        }
    }

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun Period(model: IntakeViewModel, state: IntakeState) =
    Column(Modifier.padding(horizontal = 16.dp), spacedBy(8.dp)) {
        LabelText(intake_text_period)
        Row(horizontalArrangement = spacedBy(16.dp)) {
            ExposedDropdownMenuBox(
                expanded = state.showPeriodM,
                onExpandedChange = model::showPeriodM,
                modifier = Modifier.weight(1f)
            ) {
                OutlinedTextField(
                    value = stringResource(state.periodE.title),
                    onValueChange = {},
                    modifier = Modifier
                        .fillMaxWidth()
                        .menuAnchor(MenuAnchorType.PrimaryNotEditable),
                    readOnly = true,
                    placeholder = { Text(stringResource(intake_text_pick_period)) },
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(state.showPeriodM) },
                    shape = RoundedCornerShape(14.dp)
                )
                ExposedDropdownMenu(state.showPeriodM, { model.showPeriodM(false) }) {
                    Periods.entries.forEach {
                        DropdownMenuItem(
                            text = { Text(stringResource(it.title)) },
                            contentPadding = ExposedDropdownMenuDefaults.ItemContentPadding,
                            onClick = { model.setPeriod(it) }
                        )
                    }
                }
            }

            if (state.periodE == Periods.Other) OutlinedTextField(
                value = state.period,
                onValueChange = model::setPeriod,
                modifier = Modifier.weight(1f),
                readOnly = state.default,
                textStyle = MaterialTheme.typography.titleMedium,
                placeholder = { Text(stringResource(text_empty)) },
                leadingIcon = { Icon(painterResource(vector_period), null) },
                suffix = { Text(stringResource(text_days_short)) },
                isError = state.period.isEmpty(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                singleLine = true,
                shape = RoundedCornerShape(14.dp)
            )
        }

        if (state.periodE != Periods.Indefinite) Row(horizontalArrangement = spacedBy(16.dp)) {
            OutlinedTextField(
                value = state.startDate,
                onValueChange = {},
                modifier = Modifier
                    .weight(0.5f)
                    .clickable { model.showPeriodD(true) },
                enabled = false,
                readOnly = true,
                leadingIcon = { Icon(Icons.Outlined.DateRange, null) },
                placeholder = { Text(stringResource(text_empty)) },
                supportingText = { Text(stringResource(text_start_date)) },
                singleLine = true,
                shape = RoundedCornerShape(14.dp),
                colors = fieldColorsInverted
            )
            OutlinedTextField(
                value = state.finalDate,
                onValueChange = {},
                modifier = Modifier
                    .weight(0.5f)
                    .clickable { model.showPeriodD(true) },
                enabled = false,
                readOnly = true,
                leadingIcon = { Icon(Icons.Outlined.DateRange, null) },
                placeholder = { Text(stringResource(text_empty)) },
                supportingText = { Text(stringResource(text_finish_date)) },
                singleLine = true,
                shape = RoundedCornerShape(14.dp),
                colors = fieldColorsInverted
            )
        }
    }

@Composable
private fun Food(event: (Int) -> Unit, state: IntakeState) = Column(
    modifier = Modifier.padding(horizontal = 16.dp), verticalArrangement = spacedBy(8.dp)
) {
    LabelText(intake_text_food)
    Row(Modifier.fillMaxWidth(), Arrangement.SpaceBetween) {
        FoodTypes.entries.forEach {
            Column(
                verticalArrangement = Arrangement.SpaceEvenly,
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier
                    .clickable { if (!state.default) event(it.value) }
                    .size(112.dp, 96.dp)
                    .border(1.dp, MaterialTheme.colorScheme.onSurface, RoundedCornerShape(16.dp))
                    .background(
                        if (state.foodType != it.value) Color.Transparent
                        else MaterialTheme.colorScheme.secondaryContainer,
                        RoundedCornerShape(16.dp)
                    )
            ) {
                Icon(painterResource(it.icon), null, Modifier.size(32.dp))
                Text(
                    text = stringResource(it.title),
                    textAlign = TextAlign.Center,
                    maxLines = 2,
                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.SemiBold)
                )
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun Time(model: IntakeViewModel, state: IntakeState) = Column(
    modifier = Modifier.padding(start = 16.dp, end = 16.dp, bottom = 16.dp),
    verticalArrangement = spacedBy(8.dp)
) {
    LabelText(intake_text_time)

    if (state.adding || state.editing) {
        repeat(state.time.size) { index ->
            Row(horizontalArrangement = spacedBy(16.dp), verticalAlignment = CenterVertically) {
                OutlinedTextField(
                    value = state.time[index],
                    onValueChange = {},
                    modifier = Modifier
                        .fillMaxWidth(0.8f)
                        .clickable { model.showTimePicker(true, index) },
                    enabled = false,
                    placeholder = { Text(stringResource(placeholder_time, index + 1)) },
                    leadingIcon = { Icon(painterResource(vector_time), null) },
                    shape = RoundedCornerShape(14.dp),
                    colors = fieldColorsInverted
                )

                val showBox: Boolean
                var color = MaterialTheme.colorScheme.secondaryContainer
                var icon = Icons.Outlined.Add
                var tint = MaterialTheme.colorScheme.onSecondaryContainer
                var onClick = {}

                if (index + 1 == 1) {
                    showBox = true
                    color = MaterialTheme.colorScheme.secondaryContainer
                    icon = Icons.Outlined.Add
                    tint = MaterialTheme.colorScheme.onSecondaryContainer
                    onClick = model::incTime
                }
                else if (index + 1 == state.time.size) {
                    showBox = true
                    color = MaterialTheme.colorScheme.errorContainer
                    icon = ImageVector.vectorResource(vector_remove)
                    tint = MaterialTheme.colorScheme.onErrorContainer
                    onClick = model::decTime
                }
                else showBox = false

                if (showBox) Box(
                    Modifier
                        .size(56.dp)
                        .background(color, RoundedCornerShape(12.dp))
                        .clickable(onClick = onClick), Alignment.Center
                ) { Icon(icon, null, tint = tint) }
            }
        }
    }
    else FlowRow(horizontalArrangement = spacedBy(16.dp), verticalArrangement = spacedBy(8.dp)) {
        repeat(state.time.size) { index ->
            OutlinedTextField(
                value = state.time[index],
                onValueChange = {},
                modifier = Modifier.width(128.dp),
                enabled = false,
                leadingIcon = { Icon(painterResource(vector_time), null) },
                shape = RoundedCornerShape(14.dp),
                colors = fieldColorsInverted
            )
        }
    }
}

@Composable
private fun LabelText(id: Int) = Text(
    text = stringResource(id),
    color = MaterialTheme.colorScheme.onSurface,
    fontWeight = FontWeight.SemiBold,
    style = MaterialTheme.typography.titleLarge
)

@RequiresApi(Build.VERSION_CODES.TIRAMISU)
private fun checkNotificationPermission(context: Context) =
    context.checkSelfPermission(Manifest.permission.POST_NOTIFICATIONS) == PackageManager.PERMISSION_GRANTED

val fieldColorsInverted
    @Composable get() = TextFieldDefaults.colors(
        disabledTextColor = MaterialTheme.colorScheme.onSurface,
        disabledContainerColor = MaterialTheme.colorScheme.surface,
        disabledPlaceholderColor = MaterialTheme.colorScheme.onSurfaceVariant,
        disabledLeadingIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
        disabledIndicatorColor = MaterialTheme.colorScheme.outline
    )
