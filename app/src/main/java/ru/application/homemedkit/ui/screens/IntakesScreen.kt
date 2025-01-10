package ru.application.homemedkit.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Clear
import androidx.compose.material.icons.outlined.DateRange
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults.MinHeight
import androidx.compose.material3.OutlinedTextFieldDefaults.MinWidth
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.material3.TimePicker
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight.Companion.SemiBold
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import ru.application.homemedkit.HomeMeds.Companion.database
import ru.application.homemedkit.R
import ru.application.homemedkit.R.string.intake_card_text_from
import ru.application.homemedkit.R.string.intake_text_by_schedule
import ru.application.homemedkit.R.string.intake_text_date
import ru.application.homemedkit.R.string.intake_text_in_fact
import ru.application.homemedkit.R.string.intake_text_not_taken
import ru.application.homemedkit.R.string.intake_text_quantity
import ru.application.homemedkit.R.string.intake_text_taken
import ru.application.homemedkit.R.string.intakes_tab_current
import ru.application.homemedkit.R.string.intakes_tab_list
import ru.application.homemedkit.R.string.intakes_tab_past
import ru.application.homemedkit.R.string.text_amount
import ru.application.homemedkit.R.string.text_cancel
import ru.application.homemedkit.R.string.text_edit
import ru.application.homemedkit.R.string.text_enter_product_name
import ru.application.homemedkit.R.string.text_go_to
import ru.application.homemedkit.R.string.text_medicine_amount_not_enough
import ru.application.homemedkit.R.string.text_medicine_deleted
import ru.application.homemedkit.R.string.text_medicine_product_name
import ru.application.homemedkit.R.string.text_no_intakes_found
import ru.application.homemedkit.R.string.text_notification_pick_action_first
import ru.application.homemedkit.R.string.text_save
import ru.application.homemedkit.R.string.text_status
import ru.application.homemedkit.data.dto.Intake
import ru.application.homemedkit.dialogs.TimePickerDialog
import ru.application.homemedkit.helpers.BLANK
import ru.application.homemedkit.helpers.DoseTypes
import ru.application.homemedkit.helpers.FORMAT_DME
import ru.application.homemedkit.helpers.FORMAT_DMMMMY
import ru.application.homemedkit.helpers.FORMAT_H
import ru.application.homemedkit.helpers.Intervals
import ru.application.homemedkit.helpers.Preferences
import ru.application.homemedkit.helpers.ZONE
import ru.application.homemedkit.helpers.decimalFormat
import ru.application.homemedkit.helpers.formName
import ru.application.homemedkit.helpers.getDateTime
import ru.application.homemedkit.models.viewModels.IntakesViewModel
import java.time.Instant
import java.time.LocalDate
import java.time.LocalDateTime

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun IntakesScreen(navigateToIntake: (Long) -> Unit, backClick: () -> Unit) {
    val model = viewModel<IntakesViewModel>()
    val state by model.state.collectAsStateWithLifecycle()
    val intakes by model.intakes.collectAsStateWithLifecycle()
    val schedule by model.schedule.collectAsStateWithLifecycle()
    val taken by model.taken.collectAsStateWithLifecycle()

    if (state.showDialog) DialogTaken(model)
    BackHandler(onBack = backClick)
    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    TextField(
                        value = state.search,
                        onValueChange = model::setSearch,
                        modifier = Modifier.fillMaxWidth(),
                        label = { Text(stringResource(text_enter_product_name)) },
                        leadingIcon = { Icon(Icons.Outlined.Search, null) },
                        trailingIcon = {
                            if (state.search.isNotEmpty())
                                IconButton(model::clearSearch)
                                { Icon(Icons.Outlined.Clear, null) }
                        },
                        singleLine = true,
                        colors = TextFieldDefaults.colors(
                            focusedContainerColor = MaterialTheme.colorScheme.surface,
                            unfocusedContainerColor = MaterialTheme.colorScheme.surface,
                            focusedIndicatorColor = Color.Transparent,
                            unfocusedIndicatorColor = Color.Transparent,
                            disabledIndicatorColor = Color.Transparent,
                        )
                    )
                },
                modifier = Modifier.drawBehind {
                    drawLine(Color.LightGray, Offset(0f, size.height), Offset(size.width, size.height), 4f)
                },
                actions = {
                    if (state.tab != 0) IconButton(model::showDialogDate) {
                        Icon(Icons.Outlined.DateRange, null)
                    }
                }
            )
        }
    ) { values ->
        val initial = MaterialTheme.typography.bodyMedium

        var style by remember { mutableStateOf(initial) }
        var draw by remember { mutableStateOf(false) }

        Column(Modifier.padding(top = values.calculateTopPadding())) {
            TabRow(state.tab) {
                listOf(intakes_tab_list, intakes_tab_current, intakes_tab_past).forEachIndexed { index, tab ->
                    Tab(
                        selected = state.tab == index,
                        onClick = { model.pickTab(index) },
                        text = {
                            Text(
                                text = stringResource(tab),
                                softWrap = false,
                                modifier = Modifier.drawWithContent { if (draw) drawContent() },
                                style = style,
                                onTextLayout = {
                                    if (!it.didOverflowWidth) draw = true
                                    else style = style.copy(fontSize = style.fontSize * 0.95)
                                }
                            )
                        }
                    )
                }
            }

            when (state.tab) {
                0 -> intakes.let { list ->
                    if (list.isEmpty()) Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(horizontal = 16.dp)
                    ) { Text(stringResource(text_no_intakes_found), textAlign = TextAlign.Center) }
                    else if (Preferences.getSimpleView()) LazyColumn(state = state.stateA)
                    { items(list) { IntakeItem(it, navigateToIntake); HorizontalDivider() } }
                    else LazyColumn(
                        state = state.stateA,
                        contentPadding = PaddingValues(4.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp),
                    ) { items(list) { IntakeCard(it, navigateToIntake) } }
                }

                1 -> LazyColumn(state = state.stateB) {
                    schedule.forEach { future ->
                        item { TextDate(future.date) }
                        itemsIndexed(future.intakes) { index, value ->
                            val intake = database.intakeDAO().getById(value.intakeId)
                            val medicine = database.medicineDAO().getById(intake?.medicineId ?: 0L)

                            if (intake != null && medicine != null)
                                MedicineItem(
                                    title = medicine.nameAlias.ifEmpty { medicine.productName },
                                    formName = medicine.prodFormNormName,
                                    doseType = medicine.doseType,
                                    amount = intake.amount,
                                    image = medicine.image,
                                    trigger = value.trigger
                                )
                            if (index < future.intakes.lastIndex) HorizontalDivider()
                        }
                    }
                }

                2 -> LazyColumn(state = state.stateC) {
                    taken.forEach { past ->
                        item { TextDate(past.date) }
                        itemsIndexed(past.intakes.reversed()) { index, value->
                            MedicineItem(
                                title = value.productName,
                                formName = value.formName,
                                doseType = value.doseType,
                                amount = value.amount,
                                image = value.image,
                                trigger = value.trigger,
                                taken = value.taken
                            ) { model.showDialog(value) }
                            if (index < past.intakes.lastIndex) HorizontalDivider()
                        }
                    }
                }
            }

            if (state.showDialogDate) DialogGoToDate(model::showDialogDate, model::scrollToClosest)
        }
    }
}

@Composable
fun IntakeCard(intake: Intake, navigateToIntake:(Long) -> Unit) {
    val medicine = database.medicineDAO().getById(intake.medicineId)
    val startDate = stringResource(intake_card_text_from, intake.startDate)
    val count = intake.time.size
    val intervalName = if (count == 1) stringResource(Intervals.getTitle(intake.interval.toString()))
    else pluralStringResource(R.plurals.intakes_a_day, count, count)

    ListItem(
        colors = ListItemDefaults.colors(MaterialTheme.colorScheme.tertiaryContainer),
        headlineContent = { Text("$intervalName $startDate") },
        supportingContent = { Text(intake.time.joinToString()) },
        leadingContent = { MedicineImage(medicine?.image ?: BLANK, Modifier.size(56.dp)) },
        modifier = Modifier
            .clip(MaterialTheme.shapes.medium)
            .clickable { navigateToIntake(intake.intakeId) },
        overlineContent = {
            Text(
                text = medicine?.let { it.nameAlias.ifEmpty { it.productName } } ?: BLANK,
                softWrap = false,
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = SemiBold)
            )
        }
    )
}

@Composable
fun IntakeItem(intake: Intake, navigateToIntake:(Long) -> Unit) {
    val medicine = database.medicineDAO().getById(intake.medicineId)
    val count = intake.time.size
    val intervalName = if (count == 1) stringResource(Intervals.getTitle(intake.interval.toString()))
    else pluralStringResource(R.plurals.intakes_a_day, count, count)

    ListItem(
        trailingContent = { Text(intervalName) },
        supportingContent = { Text(intake.time.joinToString(), maxLines = 1) },
        leadingContent = { MedicineImage(medicine?.image ?: BLANK, Modifier.size(40.dp)) },
        modifier = Modifier.clickable { navigateToIntake(intake.intakeId) },
        headlineContent = { Text(medicine?.let { it.nameAlias.ifEmpty { it.productName } } ?: BLANK, softWrap = false) }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun DialogGoToDate(show: () -> Unit, scroll: (Long) -> Unit) {
    val pickerState = rememberDatePickerState()

    DatePickerDialog(
        onDismissRequest = show,
        dismissButton = { TextButton(show) { Text(stringResource(text_cancel)) } },
        confirmButton = {
            TextButton(
                enabled = pickerState.selectedDateMillis != null,
                onClick = { scroll(pickerState.selectedDateMillis!!) }
            ) { Text(stringResource(text_go_to)) }
        }
    ) {
        DatePicker(
            state = pickerState,
            showModeToggle = false
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun DialogTaken(model: IntakesViewModel) {
    val intake by model.takenState.collectAsStateWithLifecycle()
    val medicine = database.medicineDAO().getById(intake.medicineId)
    val items = listOf(stringResource(intake_text_not_taken), stringResource(intake_text_taken))

    if (intake.showPicker) TimePickerDialog(model::showPicker, model::setFactTime)
    { TimePicker(intake.pickerState) }

    AlertDialog(
        onDismissRequest = model::hideDialog,
        confirmButton = {
            TextButton(
                onClick = { model.saveTaken(intake.takenId, intake.selection == 1) },
                enabled = when {
                    medicine == null -> false
                    !intake.notified -> false
                    medicine.prodAmount < intake.amount && !intake.taken -> false
                    else -> true
                }
            ) { Text(stringResource(text_save)) }
        },
        dismissButton = { TextButton(model::hideDialog) { Text(stringResource(text_cancel)) } },
        title = { Text(stringResource(text_edit), Modifier.width(MinWidth)) },
        text = {
            Column(Modifier.verticalScroll(rememberScrollState()), Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(
                    value = intake.productName,
                    onValueChange = {},
                    readOnly = true,
                    singleLine = true,
                    label = { Text(stringResource(text_medicine_product_name)) }
                )
                OutlinedTextField(
                    value = getDateTime(intake.trigger).format(FORMAT_DMMMMY),
                    onValueChange = {},
                    readOnly = true,
                    label = { Text(stringResource(intake_text_date)) }
                )
                Row(Modifier.fillMaxWidth(), Arrangement.spacedBy(12.dp)) {
                    OutlinedTextField(
                        value = getDateTime(intake.trigger).format(FORMAT_H),
                        modifier = Modifier.weight(0.5f),
                        onValueChange = {},
                        readOnly = true,
                        label = { Text(stringResource(intake_text_by_schedule)) }
                    )
                    OutlinedTextField(
                        value = if (intake.selection == 1) getDateTime(intake.inFact).format(FORMAT_H)
                        else stringResource(intake_text_not_taken),
                        onValueChange = {},
                        enabled = false,
                        readOnly = intake.selection == 0,
                        label = { Text(stringResource(intake_text_in_fact)) },
                        colors = fieldColorsInverted.copy(
                            disabledContainerColor = Color.Transparent,
                            disabledLabelColor = MaterialTheme.colorScheme.onSurfaceVariant
                        ),
                        modifier = Modifier
                            .weight(0.5f)
                            .clickable(intake.selection == 1) { model.showPicker(true) }
                    )
                }

                when {
                    medicine == null -> OutlinedTextField(
                        value = stringResource(text_medicine_deleted),
                        onValueChange = {},
                        modifier = Modifier.width(MinWidth),
                        readOnly = true,
                        label = { Text(stringResource(text_status)) }
                    )

                    !intake.notified -> OutlinedTextField(
                        value = stringResource(text_notification_pick_action_first),
                        onValueChange = {},
                        modifier = Modifier.width(MinWidth),
                        readOnly = true,
                        label = { Text(items[0]) }
                    )

                    medicine.prodAmount < intake.amount && !intake.taken -> OutlinedTextField(
                        value = stringResource(text_medicine_amount_not_enough),
                        onValueChange = {},
                        modifier = Modifier.width(MinWidth),
                        readOnly = true,
                        label = { Text(items[0]) }
                    )

                    else -> SingleChoiceSegmentedButtonRow(Modifier.size(MinWidth, MinHeight)) {
                        items.forEachIndexed { index, label ->
                            SegmentedButton(
                                selected = index == intake.selection,
                                onClick = { model.setSelection(index) },
                                shape = MaterialTheme.shapes.extraSmall
                            ) { Text(label) }
                        }
                    }
                }
            }
        }
    )
}

@Composable
private fun TextDate(timestamp: Long) = Text(
    style = MaterialTheme.typography.titleLarge.copy(fontWeight = SemiBold),
    text = LocalDate.ofEpochDay(timestamp).let {
        it.format(if (it.year == LocalDate.now().year) FORMAT_DME else FORMAT_DMMMMY)
    },
    modifier = Modifier
        .fillMaxWidth()
        .background(MaterialTheme.colorScheme.background)
        .padding(12.dp, 24.dp)
)

@Composable
private fun MedicineItem(
    title: String,
    formName: String,
    doseType: String,
    amount: Double,
    image: String,
    trigger: Long,
    taken: Boolean = true,
    showDialog: (() -> Unit)? = null
) = ListItem(
    headlineContent = { Text(text = title, maxLines = 1, softWrap = false) },
    leadingContent = { MedicineImage(image, Modifier.size(40.dp)) },
    modifier = Modifier.clickable { showDialog?.invoke() },
    supportingContent = {
        Text(
            text = stringResource(
                intake_text_quantity,
                formName.let { if (it.isEmpty()) stringResource(text_amount) else formName(it) },
                decimalFormat(amount), stringResource(DoseTypes.getTitle(doseType)),
            )
        )
    },
    trailingContent = {
        Text(
            text = LocalDateTime.ofInstant(Instant.ofEpochMilli(trigger), ZONE).format(FORMAT_H),
            style = MaterialTheme.typography.labelLarge
        )
    },
    colors = ListItemDefaults.colors(
        if (taken) MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.35f)
        else MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.65f)
    )
)