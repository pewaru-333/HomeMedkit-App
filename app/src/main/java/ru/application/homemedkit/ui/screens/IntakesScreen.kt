package ru.application.homemedkit.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
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
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import ru.application.homemedkit.R
import ru.application.homemedkit.R.string.intake_text_by_schedule
import ru.application.homemedkit.R.string.intake_text_date
import ru.application.homemedkit.R.string.intake_text_in_fact
import ru.application.homemedkit.R.string.intake_text_not_taken
import ru.application.homemedkit.R.string.intake_text_taken
import ru.application.homemedkit.R.string.text_cancel
import ru.application.homemedkit.R.string.text_edit
import ru.application.homemedkit.R.string.text_enter_product_name
import ru.application.homemedkit.R.string.text_go_to
import ru.application.homemedkit.R.string.text_medicine_amount_not_enough
import ru.application.homemedkit.R.string.text_medicine_deleted
import ru.application.homemedkit.R.string.text_medicine_product_name
import ru.application.homemedkit.R.string.text_save
import ru.application.homemedkit.R.string.text_status
import ru.application.homemedkit.data.model.Intake
import ru.application.homemedkit.data.model.IntakeModel
import ru.application.homemedkit.data.model.ScheduleModel
import ru.application.homemedkit.data.model.TakenModel
import ru.application.homemedkit.dialogs.TimePickerDialog
import ru.application.homemedkit.helpers.enums.IntakeTab
import ru.application.homemedkit.models.events.TakenEvent
import ru.application.homemedkit.models.states.TakenState
import ru.application.homemedkit.models.viewModels.IntakesViewModel
import ru.application.homemedkit.ui.elements.BoxWithEmptyListText

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun IntakesScreen(navigateToIntake: (Long) -> Unit, backClick: () -> Unit) {
    val model = viewModel<IntakesViewModel>()
    val state by model.state.collectAsStateWithLifecycle()
    val intakes by model.intakes.collectAsStateWithLifecycle()
    val schedule by model.schedule.collectAsStateWithLifecycle()
    val taken by model.taken.collectAsStateWithLifecycle()
    val takenState by model.takenState.collectAsStateWithLifecycle()

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
                    if (state.tab != IntakeTab.LIST) IconButton(model::showDialogDate) {
                        Icon(Icons.Outlined.DateRange, null)
                    }
                }
            )
        }
    ) { values ->
        val initial = MaterialTheme.typography.bodyMedium

        var style by remember { mutableStateOf(initial) }
        var draw by remember { mutableStateOf(false) }

        Column(Modifier.padding(values)) {
            TabRow(state.tab.ordinal) {
                IntakeTab.entries.forEach { tab ->
                    Tab(
                        selected = state.tab.ordinal == tab.ordinal,
                        onClick = { model.pickTab(tab) },
                        text = {
                            Text(
                                text = stringResource(tab.title),
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
                IntakeTab.LIST -> if (intakes.isNotEmpty())
                    LazyColumn(state = model.listState) {
                        items(intakes, Intake::intakeId) {
                            ItemIntake(it, Modifier.animateItem(), navigateToIntake)
                            HorizontalDivider()
                        }
                    }
                else BoxWithEmptyListText(
                    text = R.string.text_no_intakes_found,
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(values)
                )

                IntakeTab.CURRENT -> LazyColumn(state = model.listState) {
                    schedule.forEach {
                        item {
                            TextDate(it.date)
                        }

                        itemsIndexed(
                            items = it.intakes,
                            key = { _, item -> item.id },
                            contentType = { _, item -> ScheduleModel::class }
                        ) { index, item ->
                            ItemSchedule(
                                item = item,
                                modifier = Modifier.animateItem()
                            )

                            if (index < it.intakes.lastIndex) HorizontalDivider()
                        }
                    }
                }

                IntakeTab.PAST -> LazyColumn(state = model.listState) {
                    taken.forEach {
                        item {
                            TextDate(it.date)
                        }

                        itemsIndexed(
                            items = it.intakes.reversed(),
                            key = { _, item -> item.id },
                            contentType = { _, item -> TakenModel::class }
                        ) { index, item ->
                            ItemSchedule(
                                item = item,
                                modifier = Modifier.animateItem(),
                                showDialog = model::showDialog,
                                showDialogDelete = model::showDialogDelete
                            )

                            if (index < it.intakes.lastIndex) HorizontalDivider()
                        }
                    }
                }
            }
        }

        when {
            state.showDialogDelete -> DialogDeleteTaken(model::showDialogDelete, model::deleteTaken)
            state.showDialogDate -> DialogGoToDate(model::showDialogDate, model::scrollToClosest)
            state.showDialog -> DialogTaken(takenState, model::onTakenEvent)
        }
    }
}

@Composable
fun ItemIntake(intake: Intake, modifier: Modifier, navigateToIntake: (Long) -> Unit) =
    ListItem(
        overlineContent = {
            Text(
                text = intake.title,
                softWrap = false,
                overflow = TextOverflow.Ellipsis,
                maxLines = 1,
                style = MaterialTheme.typography.bodyLarge,
                color = ListItemDefaults.colors().headlineColor
            )
        },
        headlineContent = {
            Text(
                text = intake.days.asString(),
                softWrap = false,
                overflow = TextOverflow.Ellipsis,
                maxLines = 1,
                style = MaterialTheme.typography.bodyMedium
            )
        },
        leadingContent = {
            MedicineImage(
                image = intake.image,
                modifier = Modifier.size(56.dp)
            )
        },
        supportingContent = {
            Text(
                text = intake.time,
                maxLines = 1
            )
        },
        trailingContent = {
            Text(
                text = intake.interval.asString()
            )
        },
        colors = ListItemDefaults.colors(
            containerColor = if (intake.active) ListItemDefaults.containerColor
            else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.15f)
        ),
        modifier = modifier.clickable {
            navigateToIntake(intake.intakeId)
        }
    )

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
private fun DialogTaken(intake: TakenState, onEvent: (TakenEvent) -> Unit) {
    val context = LocalContext.current
    val items = listOf(stringResource(intake_text_not_taken), stringResource(intake_text_taken))

    if (intake.showPicker)
        TimePickerDialog(
            onCancel = { onEvent(TakenEvent.ShowTimePicker(false)) },
            onConfirm = { onEvent(TakenEvent.SetFactTime) }
        ) {
            TimePicker(intake.pickerState)
        }

    AlertDialog(
        onDismissRequest = { onEvent(TakenEvent.HideDialog) },
        title = {
            Text(
                text = stringResource(text_edit),
                modifier = Modifier.width(MinWidth)
            )
        },
        confirmButton = {
            TextButton(
                onClick = { onEvent(TakenEvent.SaveTaken(context)) },
                enabled = when {
                    intake.medicine == null -> false
                    intake.medicine.prodAmount < intake.amount && !intake.taken -> false
                    else -> true
                }
            ) {
                Text(
                    text = stringResource(text_save)
                )
            }
        },
        dismissButton = {
            TextButton(
                onClick = { onEvent(TakenEvent.HideDialog) }
            ) {
                Text(
                    text = stringResource(text_cancel)
                )
            }
        },
        text = {
            Column(
                modifier = Modifier.verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedTextField(
                    value = intake.productName,
                    onValueChange = {},
                    readOnly = true,
                    singleLine = true,
                    label = { Text(stringResource(text_medicine_product_name)) }
                )
                OutlinedTextField(
                    value = intake.date,
                    onValueChange = {},
                    readOnly = true,
                    label = { Text(stringResource(intake_text_date)) }
                )
                Row(Modifier.fillMaxWidth(), Arrangement.spacedBy(12.dp)) {
                    OutlinedTextField(
                        value = intake.scheduled,
                        modifier = Modifier.weight(0.5f),
                        onValueChange = {},
                        readOnly = true,
                        label = { Text(stringResource(intake_text_by_schedule)) }
                    )
                    OutlinedTextField(
                        value = intake.actual.asString(),
                        onValueChange = {},
                        enabled = false,
                        readOnly = intake.selection == 0,
                        label = { Text(stringResource(intake_text_in_fact)) },
                        colors = TextFieldDefaults.colors(
                            disabledTextColor = MaterialTheme.colorScheme.onSurface,
                            disabledContainerColor = Color.Transparent,
                            disabledLabelColor = MaterialTheme.colorScheme.onSurfaceVariant,
                            disabledPlaceholderColor = MaterialTheme.colorScheme.onSurfaceVariant,
                            disabledLeadingIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                            disabledIndicatorColor = MaterialTheme.colorScheme.outline,
                        ),
                        modifier = Modifier
                            .weight(0.5f)
                            .clickable(intake.selection == 1) {
                                onEvent(TakenEvent.ShowTimePicker(true))
                            }
                    )
                }

                when {
                    intake.medicine == null -> OutlinedTextField(
                        value = stringResource(text_medicine_deleted),
                        onValueChange = {},
                        modifier = Modifier.width(MinWidth),
                        readOnly = true,
                        label = { Text(stringResource(text_status)) }
                    )

                    intake.medicine.prodAmount < intake.amount && !intake.taken -> OutlinedTextField(
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
                                shape = MaterialTheme.shapes.extraSmall,
                                onClick = { onEvent(TakenEvent.SetSelection(index)) },
                            ) { Text(label) }
                        }
                    }
                }
            }
        }
    )
}

@Composable
private fun DialogDeleteTaken(onDismiss: () -> Unit, onDelete: () -> Unit) = AlertDialog(
    onDismissRequest = onDismiss,
    dismissButton = { TextButton(onDismiss) { Text(stringResource(text_cancel)) } },
    confirmButton = { TextButton(onDelete) { Text(stringResource(R.string.text_confirm)) } },
    title = { Text(stringResource(R.string.text_attention)) },
    text = {
        Text(
            text = stringResource(R.string.text_confirm_deletion_int),
            style = MaterialTheme.typography.bodyLarge
        )
    }
)

@Composable
private fun TextDate(date: String) = Text(
    text = date,
    style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.W500),
    modifier = Modifier
        .fillMaxWidth()
        .background(MaterialTheme.colorScheme.background)
        .padding(12.dp, 24.dp)
)

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun ItemSchedule(
    item: IntakeModel,
    modifier: Modifier,
    showDialog: ((Long) -> Unit)? = null,
    showDialogDelete: ((Long) -> Unit)? = null
) {
    ListItem(
        headlineContent = {
            Text(
                text = item.title,
                softWrap = false,
                maxLines = 1
            )
        },
        leadingContent = {
            MedicineImage(
                image = item.image,
                modifier = Modifier.size(40.dp)
            )
        },
        modifier = modifier.combinedClickable(
            onClick = { showDialog?.invoke(item.id) },
            onLongClick = { if (!item.taken) showDialogDelete?.invoke(item.id) }
        ),
        supportingContent = {
            Text(
                text = item.doseAmount.asString()
            )
        },
        trailingContent = {
            Text(
                text = item.time,
                style = MaterialTheme.typography.labelLarge
            )
        },
        colors = ListItemDefaults.colors(
            if (item.taken) MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.35f)
            else MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.65f)
        )
    )
}