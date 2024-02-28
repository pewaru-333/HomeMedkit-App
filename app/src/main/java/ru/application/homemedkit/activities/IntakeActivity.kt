package ru.application.homemedkit.activities

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.NotificationManager.IMPORTANCE_HIGH
import android.content.Context
import android.content.Intent
import android.content.Intent.FLAG_ACTIVITY_CLEAR_TASK
import android.content.Intent.FLAG_ACTIVITY_NEW_TASK
import android.content.Intent.FLAG_RECEIVER_FOREGROUND
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.annotation.RequiresApi
import androidx.compose.foundation.background
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
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.DateRangePicker
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
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.material3.TimePicker
import androidx.compose.material3.TimePickerState
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberDateRangePickerState
import androidx.compose.material3.rememberTimePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.OffsetMapping
import androidx.compose.ui.text.input.TransformedText
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.lifecycle.viewmodel.compose.viewModel
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.rememberPermissionState
import ru.application.homemedkit.R
import ru.application.homemedkit.alarms.AlarmReceiver
import ru.application.homemedkit.alarms.AlarmSetter
import ru.application.homemedkit.databaseController.Intake
import ru.application.homemedkit.databaseController.MedicineDatabase
import ru.application.homemedkit.databaseController.repositories.IntakeRepository
import ru.application.homemedkit.dialogs.TimePickerDialog
import ru.application.homemedkit.helpers.ConstantsHelper.ADD
import ru.application.homemedkit.helpers.ConstantsHelper.BLANK
import ru.application.homemedkit.helpers.ConstantsHelper.INTAKE_ID
import ru.application.homemedkit.helpers.ConstantsHelper.INTERVALS
import ru.application.homemedkit.helpers.ConstantsHelper.MEDICINE_ID
import ru.application.homemedkit.helpers.ConstantsHelper.NEW_INTAKE
import ru.application.homemedkit.helpers.ConstantsHelper.PERIODS
import ru.application.homemedkit.helpers.ConstantsHelper.SEMICOLON
import ru.application.homemedkit.helpers.DateHelper.FORMAT_D_MM_Y
import ru.application.homemedkit.helpers.DateHelper.FORMAT_H
import ru.application.homemedkit.helpers.DateHelper.FORMAT_S
import ru.application.homemedkit.helpers.DateHelper.ZONE
import ru.application.homemedkit.helpers.DateHelper.getDateTime
import ru.application.homemedkit.helpers.DateHelper.getPeriod
import ru.application.homemedkit.helpers.SettingsHelper
import ru.application.homemedkit.helpers.decimalFormat
import ru.application.homemedkit.helpers.longSeconds
import ru.application.homemedkit.helpers.timesString
import ru.application.homemedkit.ui.theme.AppTheme
import ru.application.homemedkit.viewModels.IntakeViewModel
import ru.application.homemedkit.viewModels.factories.IntakeViewModelFactory
import java.time.Instant
import java.time.LocalDate
import java.time.LocalTime

class IntakeActivity : ComponentActivity() {

    private lateinit var settings: SettingsHelper
    private lateinit var database: MedicineDatabase

    @OptIn(ExperimentalMaterial3Api::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        settings = SettingsHelper(this)
        database = MedicineDatabase.getInstance(this)
        val medicineDAO = database.medicineDAO()

        val intakeId = intent.getLongExtra(INTAKE_ID, 0)
        var medicineId = intent.getLongExtra(MEDICINE_ID, 0)

        setContent {
            val factory = IntakeViewModelFactory(IntakeRepository(database.intakeDAO()), intakeId)
            val viewModel: IntakeViewModel = viewModel(factory = factory)

            viewModel.setAdding(intent.getBooleanExtra(ADD, false))

            AppTheme {
                Scaffold(
                    topBar = {
                        TopAppBar(
                            title = {},
                            navigationIcon = {
                                IconButton({
                                    val intent = Intent(this, MainActivity::class.java)
                                        .putExtra(NEW_INTAKE, true)
                                        .addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
                                    startActivity(intent)
                                }
                                ) {
                                    Icon(
                                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.onTertiaryContainer
                                    )
                                }
                            },
                            actions = {
                                when {
                                    viewModel.add -> {
                                        IconButton(
                                            onClick = addIntake(viewModel, medicineId),
                                            enabled = viewModel.validateAll()
                                        )
                                        { Icon(Icons.Default.Check, null) }
                                    }

                                    viewModel.edit -> {
                                        IconButton(
                                            onClick = updateIntake(viewModel, intakeId, medicineId),
                                            enabled = viewModel.validateAll()
                                        )
                                        { Icon(Icons.Default.Check, null) }
                                    }

                                    else -> {
                                        var expanded by remember { mutableStateOf(false) }
                                        LocalFocusManager.current.clearFocus(true)

                                        IconButton({ expanded = true }) {
                                            Icon(Icons.Default.MoreVert, null)
                                        }

                                        DropdownMenu(expanded, { expanded = false }) {
                                            DropdownMenuItem(
                                                { Text(resources.getString(R.string.text_edit)) },
                                                { viewModel.setEditing(true) })
                                            DropdownMenuItem(
                                                text = { Text(resources.getString(R.string.text_delete)) },
                                                onClick = deleteIntake(intakeId)
                                            )
                                        }
                                    }
                                }
                            },
                            colors = TopAppBarDefaults.topAppBarColors(
                                containerColor = MaterialTheme.colorScheme.tertiaryContainer,
                                titleContentColor = MaterialTheme.colorScheme.onTertiaryContainer,
                                navigationIconContentColor = MaterialTheme.colorScheme.onTertiaryContainer
                            )
                        )
                    }
                ) { paddingValues ->
                    val scrollState = rememberScrollState()

                    if (!viewModel.add)
                        medicineId = database.intakeDAO().getByPK(intakeId).medicineId

                    val productName = medicineDAO.getProductName(medicineId)
                    val prodAmount = medicineDAO.getByPK(medicineId).prodAmount

                    CreateNotificationChannel()
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                        CheckPostNotificationPermission()
                    }

                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(
                                top = paddingValues
                                    .calculateTopPadding()
                                    .plus(16.dp)
                            )
                            .verticalScroll(scrollState),
                        verticalArrangement = Arrangement.spacedBy(32.dp)
                    ) {
                        Title(productName)
                        Amount(viewModel, prodAmount)
                        Interval(viewModel)
                        Period(viewModel, settings)
                        Time(viewModel)
                    }
                }
            }
        }
    }

    @Composable
    private fun addIntake(viewModel: IntakeViewModel, medicineId: Long): () -> Unit = {
        val amount = decimalFormat(viewModel.amount)
        val time = timesString(viewModel.timesValues)

        val newIntake = Intake(
            medicineId, amount, viewModel.interval, time, viewModel.period,
            viewModel.startDate, viewModel.finalDate
        )

        val newIntakeId = database.intakeDAO().add(newIntake)
        val setter = AlarmSetter(this@IntakeActivity)
        val triggers = longSeconds(viewModel.startDate, time)

        setter.setAlarm(newIntakeId, triggers, viewModel.interval, viewModel.finalDate)

        val intent = Intent(this@IntakeActivity, MainActivity::class.java)
            .putExtra(NEW_INTAKE, true)
        startActivity(intent)
    }

    @Composable
    private fun updateIntake(
        viewModel: IntakeViewModel,
        intakeId: Long,
        medicineId: Long
    ): () -> Unit = {
        val amount = decimalFormat(viewModel.amount)
        viewModel.time = viewModel.timesValues.toList().joinToString(SEMICOLON)

        val newIntake = Intake(
            medicineId, amount, viewModel.interval, viewModel.time,
            viewModel.period, viewModel.startDate, viewModel.finalDate
        )

        newIntake.intakeId = intakeId

        database.intakeDAO().update(newIntake)

        val alarms = database.alarmDAO().getByIntakeId(newIntake.intakeId)
        val setter = AlarmSetter(this@IntakeActivity)

        alarms.forEach {
            val intent = Intent(this@IntakeActivity, AlarmReceiver::class.java)
                .addFlags(FLAG_RECEIVER_FOREGROUND)
                .setFlags(FLAG_ACTIVITY_NEW_TASK or FLAG_ACTIVITY_CLEAR_TASK)

            setter.removeAlarm(this@IntakeActivity, it.alarmId, intent)
        }

        val triggers = longSeconds(viewModel.startDate, viewModel.time)
        setter.setAlarm(newIntake.intakeId, triggers, viewModel.interval, viewModel.finalDate)

        viewModel.setEditing(false)
    }

    @Composable
    private fun deleteIntake(intakeId: Long): () -> Unit = {
        database.intakeDAO().delete(Intake(intakeId))
        val intent = Intent(this@IntakeActivity, MainActivity::class.java)
            .putExtra(NEW_INTAKE, true)
            .addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
        startActivity(intent)
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
private fun Amount(viewModel: IntakeViewModel, prodAmount: Double) {
    var isError by rememberSaveable { mutableStateOf(false) }

    fun validate(text: String) {
        isError = text.isBlank()
    }

    Row(Modifier.padding(horizontal = 16.dp), Arrangement.spacedBy(16.dp)) {
        Column(Modifier.weight(0.5f), Arrangement.spacedBy(8.dp)) {
            LabelText(R.string.text_intake_amount)
            OutlinedTextField(
                value = viewModel.amount,
                onValueChange = {
                    if (viewModel.add || viewModel.edit) viewModel.updateAmount(it)
                    validate(it)
                },
                modifier = Modifier.fillMaxWidth(),
                readOnly = !(viewModel.add || viewModel.edit),
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
            LabelText(R.string.text_intake_left_amount)
            OutlinedTextField(
                value = decimalFormat(prodAmount),
                onValueChange = {},
                modifier = Modifier.fillMaxWidth(),
                readOnly = true,
                leadingIcon = { Icon(Icons.Default.Home, null) },
                suffix = { Text(LocalContext.current.resources.getString(R.string.text_placeholder_pcs)) },
                shape = RoundedCornerShape(14.dp)
            )
        }
    }
}


@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun Interval(viewModel: IntakeViewModel) {
    val resources = LocalContext.current.resources
    val intervals = resources.getStringArray(R.array.interval_types_name)
    var interval by remember { mutableStateOf(BLANK) }
    var expanded by remember { mutableStateOf(false) }

    interval = when (viewModel.interval) {
        INTERVALS[0] -> intervals[0]
        INTERVALS[1] -> intervals[1]
        else -> if (viewModel.interval.contains(INTERVALS[2])) intervals[2] else BLANK
    }

    Column(Modifier.padding(horizontal = 16.dp), Arrangement.spacedBy(8.dp)) {
        LabelText(R.string.text_medicine_intake_interval)

        Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
            ExposedDropdownMenuBox(
                expanded = expanded,
                onExpandedChange = { if (viewModel.add || viewModel.edit) expanded = it },
                modifier = Modifier.weight(0.5f)
            ) {
                OutlinedTextField(
                    value = interval,
                    onValueChange = {},
                    modifier = Modifier
                        .fillMaxWidth()
                        .menuAnchor(),
                    readOnly = true,
                    placeholder = { Text(resources.getString(R.string.text_pick_interval)) },
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded) },
                    shape = RoundedCornerShape(14.dp)
                )
                ExposedDropdownMenu(expanded, { expanded = false }) {
                    intervals.forEach { selectionOption ->
                        DropdownMenuItem(
                            text = { Text(selectionOption) },
                            onClick = {
                                interval = selectionOption
                                expanded = false
                                when (interval) {
                                    intervals[0] -> viewModel.updateInterval(INTERVALS[0])
                                    intervals[1] -> viewModel.updateInterval(INTERVALS[1])
                                    intervals[2] -> viewModel.updateInterval(INTERVALS[2])
                                }
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
                    value = viewModel.intervalD,
                    onValueChange = { viewModel.updateInterval(INTERVALS[2], it); validate(it) },
                    modifier = Modifier.weight(0.5f),
                    readOnly = !(viewModel.add || viewModel.edit),
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
private fun Period(viewModel: IntakeViewModel, settings: SettingsHelper) {
    val current = LocalContext.current
    val dateST = current.getString(R.string.text_period_start_date)
    val dateFT = current.getString(R.string.text_period_finish_date)

    Column(Modifier.padding(horizontal = 16.dp), Arrangement.spacedBy(8.dp)) {
        LabelText(R.string.text_medicine_intake_period)

        when {
            settings.lightPeriod -> {
                val resources = current.resources
                val periods = resources.getStringArray(R.array.period_types_name)
                var period by rememberSaveable { mutableStateOf(BLANK) }
                var expanded by remember { mutableStateOf(false) }

                period = when (viewModel.period) {
                    PERIODS[0] -> periods[0]
                    PERIODS[1] -> periods[1]
                    PERIODS[2] -> periods[2]
                    PERIODS[3] -> periods[3]
                    else -> BLANK
                }

                if (viewModel.periodD == BLANK && period != periods[2] && viewModel.edit)
                    viewModel.periodD = getPeriod(viewModel.startDate, viewModel.finalDate)

                Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                    ExposedDropdownMenuBox(
                        expanded = expanded,
                        onExpandedChange = { if (viewModel.add || viewModel.edit) expanded = it },
                        modifier = Modifier.weight(1f)
                    ) {
                        OutlinedTextField(
                            value = period,
                            onValueChange = {},
                            modifier = Modifier
                                .fillMaxWidth()
                                .menuAnchor(),
                            readOnly = true,
                            placeholder = { Text(resources.getString(R.string.text_pick_period)) },
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded) },
                            shape = RoundedCornerShape(14.dp)
                        )
                        ExposedDropdownMenu(expanded, { expanded = false }) {
                            periods.forEach { selectionOption ->
                                DropdownMenuItem(
                                    text = { Text(selectionOption) },
                                    onClick = {
                                        period = selectionOption
                                        expanded = false
                                        when (period) {
                                            periods[0] -> viewModel.updatePeriod("7")
                                            periods[1] -> viewModel.updatePeriod("30")
                                            periods[2] -> viewModel.updatePeriod(BLANK)
                                            periods[3] -> viewModel.updatePeriod("38500")
                                        }
                                    },
                                    contentPadding = ExposedDropdownMenuDefaults.ItemContentPadding
                                )
                            }
                        }
                    }

                    if (period == periods[2] && (viewModel.add || viewModel.edit)) {
                        var isError by rememberSaveable { mutableStateOf(false) }

                        fun validate(text: String) {
                            isError = text.isBlank()
                        }

                        OutlinedTextField(
                            value = viewModel.periodD,
                            onValueChange = { viewModel.updatePeriod(it); validate(it) },
                            modifier = Modifier.weight(1f),
                            readOnly = !(viewModel.add || viewModel.edit),
                            textStyle = MaterialTheme.typography.titleMedium,
                            placeholder = { Text("10") },
                            leadingIcon = { Icon(Icons.Default.DateRange, null) },
                            suffix = { Text(resources.getString(R.string.text_days_short)) },
                            isError = isError,
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            singleLine = true,
                            shape = RoundedCornerShape(14.dp)
                        )
                    }
                }

                if (period.isNotEmpty() && period != periods[3]) {
                    if (viewModel.add && viewModel.periodD.isNotEmpty()) {
                        val today = System.currentTimeMillis()
                        viewModel.updateDateS(getDateTime(today).format(FORMAT_S))
                        viewModel.updateDateF(
                            getDateTime(today).toLocalDate().plusDays(viewModel.periodD.toLong())
                                .format(FORMAT_S)
                        )
                    }

                    if (viewModel.edit && viewModel.periodD != BLANK) {
                        val startDate = LocalDate.parse(viewModel.startDate, FORMAT_S)
                        viewModel.updateDateF(
                            startDate.plusDays(viewModel.periodD.toLong()).format(FORMAT_S)
                        )
                    }

                    Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                        OutlinedTextField(
                            value = viewModel.startDate,
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
                            value = viewModel.finalDate,
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
                    val state = rememberDateRangePickerState(selectableDates = dates())

                    var isError by rememberSaveable { mutableStateOf(false) }
                    var showPicker by remember { mutableStateOf(false) }
                    var selectS by remember { mutableStateOf(dateST) }
                    var selectF by remember { mutableStateOf(dateFT) }

                    if (viewModel.add) viewModel.period = PERIODS[2]

                    OutlinedTextField(
                        value = viewModel.startDate,
                        onValueChange = {},
                        modifier = Modifier
                            .weight(0.5f)
                            .clickable { showPicker = true },
                        enabled = false,
                        readOnly = true,
                        placeholder = { Text(current.getString(R.string.text_placeholder_date)) },
                        leadingIcon = { Icon(Icons.Default.DateRange, null) },
                        supportingText = { Text(dateST) },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true,
                        shape = RoundedCornerShape(14.dp),
                        colors = fieldColorsInverted(isError)
                    )
                    OutlinedTextField(
                        value = viewModel.finalDate,
                        onValueChange = {},
                        modifier = Modifier
                            .weight(0.5f)
                            .clickable { showPicker = true },
                        enabled = false,
                        readOnly = true,
                        placeholder = { Text(current.getString(R.string.text_placeholder_date)) },
                        leadingIcon = { Icon(Icons.Default.DateRange, null) },
                        supportingText = { Text(dateFT) },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true,
                        shape = RoundedCornerShape(14.dp),
                        colors = fieldColorsInverted(isError)
                    )

                    if (state.selectedStartDateMillis != null)
                        selectS = getDateTime(state.selectedStartDateMillis!!).format(FORMAT_D_MM_Y)

                    if (state.selectedEndDateMillis != null)
                        selectF = getDateTime(state.selectedEndDateMillis!!).format(FORMAT_D_MM_Y)

                    if (viewModel.startDate.isNotEmpty() && viewModel.finalDate.isNotEmpty()) {
                        val milliS = LocalDate.parse(viewModel.startDate, FORMAT_S)
                        val milliF = LocalDate.parse(viewModel.finalDate, FORMAT_S)

                        if (milliF < milliS) {
                            viewModel.periodD = BLANK
                            isError = true
                        } else {
                            viewModel.periodD = PERIODS[2]
                            isError = false
                        }
                    }

                    if (showPicker)
                        Dialog(
                            onDismissRequest = { showPicker = false },
                            properties = DialogProperties(usePlatformDefaultWidth = false)
                        ) {
                            Surface {
                                Column(Modifier.fillMaxSize(), Arrangement.Top) {
                                    Row(
                                        Modifier
                                            .fillMaxWidth()
                                            .padding(12.dp, 12.dp, 12.dp, 0.dp),
                                        Arrangement.SpaceBetween,
                                        Alignment.CenterVertically
                                    ) {
                                        IconButton(onClick = { showPicker = false }) {
                                            Icon(Icons.Default.Clear, null)
                                        }
                                        IconButton(onClick = {
                                            if (selectS != dateST) viewModel.updateDateS(selectS)
                                            if (selectF != dateFT) viewModel.updateDateF(selectF)
                                            showPicker = false
                                        }) { Icon(Icons.Default.Check, null) }
                                    }

                                    DateRangePicker(
                                        state = state,
                                        title = {
                                            Text(
                                                text = current.getString(R.string.text_pick_period),
                                                modifier = Modifier
                                                    .fillMaxWidth()
                                                    .padding(20.dp),
                                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                                style = MaterialTheme.typography.labelLarge
                                            )
                                        },
                                        headline = {
                                            Row(
                                                Modifier
                                                    .fillMaxWidth()
                                                    .padding(bottom = 12.dp),
                                                Arrangement.spacedBy(
                                                    8.dp,
                                                    Alignment.CenterHorizontally
                                                ),
                                                Alignment.CenterVertically,
                                            ) {
                                                Text(
                                                    stringResource(
                                                        R.string.text_period_dates,
                                                        selectS,
                                                        selectF
                                                    )
                                                )
                                            }
                                        },
                                        showModeToggle = false
                                    )
                                }
                            }
                        }
                }
            }
        }
    }
}

@Composable
@OptIn(ExperimentalMaterial3Api::class)
private fun dates() = object : SelectableDates {
    override fun isSelectableDate(utcTimeMillis: Long) =
        LocalDate.now() <= Instant.ofEpochMilli(utcTimeMillis).atZone(ZONE).toLocalDate()

    override fun isSelectableYear(year: Int) = LocalDate.now().year <= year
}


@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
private fun Time(viewModel: IntakeViewModel) {
    val state = rememberTimePickerState(12, 0, true)
    val timesStates = remember { mutableStateListOf(state) }
    while (timesStates.size != viewModel.timesAmount)
        timesStates.add(TimePickerState(12, 0, true))

    var showPicker by rememberSaveable { mutableStateOf(false) }
    var fieldIndex by rememberSaveable { mutableIntStateOf(0) }

    Column(
        modifier = Modifier.padding(start = 16.dp, end = 16.dp, bottom = 16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        LabelText(R.string.text_medicine_intake_time)

        when {
            viewModel.add || viewModel.edit -> {
                repeat(viewModel.timesAmount) { index ->
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        OutlinedTextField(
                            value = viewModel.timesValues[index],
                            onValueChange = {},
                            modifier = Modifier
                                .fillMaxWidth(0.8f)
                                .clickable { fieldIndex = index; showPicker = true },
                            enabled = false,
                            placeholder = { Text(timePlaceholder(index)) },
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
                                onClick = {
                                    viewModel.incAmount()
                                    viewModel.timesValues.add(BLANK)
                                    timesStates.add(TimePickerState(12, 0, true))
                                }
                            }

                            viewModel.timesAmount -> {
                                showBox = true
                                color = Color(MaterialTheme.colorScheme.errorContainer.value)
                                icon = R.drawable.vector_remove
                                tint = MaterialTheme.colorScheme.onErrorContainer
                                onClick = {
                                    if (viewModel.timesAmount > 1) {
                                        timesStates.removeLast()
                                        viewModel.timesValues.removeLast()
                                        viewModel.decAmount()
                                    }
                                }
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

                if (showPicker) {
                    TimePickerDialog(
                        onCancel = { showPicker = false },
                        onConfirm = {
                            val timeS = timesStates[fieldIndex]
                            val time = LocalTime.of(timeS.hour, timeS.minute)
                            viewModel.timesValues[fieldIndex] = time.format(FORMAT_H)
                            showPicker = false
                        },
                    ) { TimePicker(timesStates[fieldIndex]) }
                }
            }

            else -> {
                viewModel.updateTime()
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    repeat(viewModel.timesAmount) { index ->
                        OutlinedTextField(
                            value = viewModel.timesValues[index],
                            onValueChange = {},
                            modifier = Modifier.width(120.dp),
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
private fun timePlaceholder(index: Int) = String.format(
    LocalContext.current.resources.getString(R.string.intake_time_placeholder), index + 1
)

@Composable
private fun fieldColorsInverted(isError: Boolean = false) = TextFieldDefaults.colors(
    disabledTextColor = MaterialTheme.colorScheme.onSurfaceVariant,
    disabledContainerColor = when {
        isError -> MaterialTheme.colorScheme.errorContainer
        else -> MaterialTheme.colorScheme.surface
    },
    disabledPlaceholderColor = MaterialTheme.colorScheme.onSurfaceVariant,
    disabledLeadingIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
    disabledIndicatorColor = MaterialTheme.colorScheme.outline
)

@Composable
private fun LabelText(id: Int) {
    Text(
        text = LocalContext.current.resources.getString(id),
        color = MaterialTheme.colorScheme.onSurface,
        fontWeight = FontWeight.SemiBold,
        style = MaterialTheme.typography.titleLarge
    )
}

@RequiresApi(Build.VERSION_CODES.TIRAMISU)
@OptIn(ExperimentalPermissionsApi::class)
@Composable
private fun CheckPostNotificationPermission() {
    val state = rememberPermissionState(android.Manifest.permission.POST_NOTIFICATIONS)
    if (!state.status.isGranted) {
        Dialog(onDismissRequest = {}) { Surface { state.launchPermissionRequest() } }
    }
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