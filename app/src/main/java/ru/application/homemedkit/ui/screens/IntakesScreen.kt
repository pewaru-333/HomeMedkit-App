@file:OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterial3ExpressiveApi::class)

package ru.application.homemedkit.ui.screens

import androidx.annotation.StringRes
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.PressInteraction
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.text.input.InputTransformation
import androidx.compose.foundation.text.input.OutputTransformation
import androidx.compose.foundation.text.input.TextFieldLineLimits
import androidx.compose.foundation.text.input.rememberTextFieldState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ButtonGroupDefaults
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.ExposedDropdownMenuAnchorType
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.ListItem
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MenuDefaults
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults.MinWidth
import androidx.compose.material3.PrimaryTabRow
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SegmentedListItem
import androidx.compose.material3.SelectableDates
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TimePicker
import androidx.compose.material3.ToggleButton
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.material3.rememberTimePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.util.fastForEach
import androidx.core.app.NotificationManagerCompat
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import ru.application.homemedkit.R
import ru.application.homemedkit.data.model.Intake
import ru.application.homemedkit.data.model.IntakeListScheme
import ru.application.homemedkit.data.model.IntakeModel
import ru.application.homemedkit.data.model.MedicineMain
import ru.application.homemedkit.dialogs.TimePickerDialog
import ru.application.homemedkit.models.events.IntakesEvent
import ru.application.homemedkit.models.events.NewTakenEvent
import ru.application.homemedkit.models.events.TakenEvent
import ru.application.homemedkit.models.states.IntakesDialogState
import ru.application.homemedkit.models.states.NewTakenState
import ru.application.homemedkit.models.states.ScheduledState
import ru.application.homemedkit.models.states.TakenState
import ru.application.homemedkit.models.viewModels.IntakesViewModel
import ru.application.homemedkit.ui.elements.BoxWithEmptyListText
import ru.application.homemedkit.ui.elements.IconButton
import ru.application.homemedkit.ui.elements.MedicineImage
import ru.application.homemedkit.ui.elements.SearchAppBar
import ru.application.homemedkit.ui.elements.TextDate
import ru.application.homemedkit.ui.elements.VectorIcon
import ru.application.homemedkit.utils.DecimalAmountInputTransformation
import ru.application.homemedkit.utils.DecimalAmountOutputTransformation
import ru.application.homemedkit.utils.Formatter
import ru.application.homemedkit.utils.di.Preferences
import ru.application.homemedkit.utils.enums.IntakeTab
import java.time.ZoneOffset

@Composable
fun IntakesScreen(onNavigate: (Long) -> Unit) {
    val model = viewModel<IntakesViewModel>()

    val state by model.state.collectAsStateWithLifecycle()

    val medicines by model.medicines.collectAsStateWithLifecycle()
    val intakes by model.intakes.collectAsStateWithLifecycle()
    val schedule by model.schedule.collectAsStateWithLifecycle()
    val taken by model.taken.collectAsStateWithLifecycle()

    val scheduledState by model.scheduledManager.state.collectAsStateWithLifecycle()
    val takenState by model.takenManager.state.collectAsStateWithLifecycle()
    val newTakenState by model.newTakenManager.state.collectAsStateWithLifecycle()

    val scope = rememberCoroutineScope()
    val pagerState = rememberPagerState(
        pageCount = IntakeTab.entries::size,
        initialPage = Preferences.startPage.extras.getOrElse(0) { 0 } as Int
    )
    val listStates = IntakeTab.entries.map { rememberLazyListState() }

    fun toggleDialog(state: IntakesDialogState) {
        model.onEvent(IntakesEvent.ToggleDialog(state))
    }

    Scaffold(
        topBar = {
            SearchAppBar(
                search = state.search,
                onSearch = { model.onEvent(IntakesEvent.SetSearch(it)) },
                actions = {
                    AnimatedVisibility(IntakeTab.entries[pagerState.currentPage] != IntakeTab.LIST) {
                        IconButton(
                            onClick = { toggleDialog(IntakesDialogState.DatePicker) },
                            content = { VectorIcon(R.drawable.vector_date_range) }
                        )
                    }
                }
            )
        },
        floatingActionButton = {
            AnimatedVisibility(IntakeTab.entries[pagerState.currentPage] == IntakeTab.PAST) {
                FloatingActionButton(
                    onClick = { toggleDialog(IntakesDialogState.TakenAdd) },
                    content = { VectorIcon(R.drawable.vector_add) }
                )
            }
        }
    ) { values ->
        Column(Modifier.padding(values)) {
            PrimaryTabRow(pagerState.targetPage) {
                IntakeTab.entries.forEach { tab ->
                    Tab(
                        selected = pagerState.targetPage == tab.ordinal,
                        onClick = { scope.launch { pagerState.animateScrollToPage(tab.ordinal) } },
                        text = {
                            Text(
                                text = stringResource(tab.title),
                                overflow = TextOverflow.Visible,
                                softWrap = false
                            )
                        }
                    )
                }
            }

            HorizontalPager(pagerState) { index ->
                when (IntakeTab.entries[index]) {
                    IntakeTab.LIST -> if (intakes.isNotEmpty())
                        LazyColumn(Modifier.fillMaxSize(), listStates[0]) {
                            items(intakes, Intake::intakeId) {
                                ItemIntake(it, Modifier.animateItem(), onNavigate)
                                HorizontalDivider()
                            }
                        }
                    else BoxWithEmptyListText(
                        text = R.string.text_no_intakes_found,
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(values)
                    )

                    IntakeTab.CURRENT -> IntakeList(
                        items = schedule,
                        modifier = Modifier.fillMaxSize(),
                        state = listStates[1],
                        showDialogScheduleToTaken = { toggleDialog(IntakesDialogState.ScheduleToTaken(it)) }
                    )

                    IntakeTab.PAST -> IntakeList(
                        items = taken,
                        modifier = Modifier.fillMaxSize(),
                        state = listStates[2],
                        showDialog = { toggleDialog(IntakesDialogState.TakenInfo(it)) },
                        showDialogDelete = { toggleDialog(IntakesDialogState.TakenDelete(it)) }
                    )
                }
            }
        }
    }

    when (state.dialogState) {
        IntakesDialogState.TakenAdd -> DialogAddTaken(
            medicines = medicines,
            newTaken = newTakenState,
            onEvent = model.newTakenManager::onEvent,
            onDismiss = { model.onEvent(IntakesEvent.ToggleDialog()) },
        )

        is IntakesDialogState.TakenDelete -> DialogDeleteTaken(
            onDelete = { model.takenManager.onEvent(TakenEvent.Delete) },
            onDismiss = { model.onEvent(IntakesEvent.ToggleDialog()) }
        )

        is IntakesDialogState.TakenInfo -> DialogTaken(
            intake = takenState,
            onEvent = model.takenManager::onEvent,
            onDismiss = { model.onEvent(IntakesEvent.ToggleDialog()) }
        )

        is IntakesDialogState.ScheduleToTaken -> DialogScheduleToTaken(
            item = scheduledState,
            onConfirm = model.scheduledManager::scheduleToTaken,
            onDismiss = { model.onEvent(IntakesEvent.ToggleDialog()) }
        )

        IntakesDialogState.DatePicker -> DialogGoToDate(
            onDismiss = { model.onEvent(IntakesEvent.ToggleDialog()) },
            onScroll = { time ->
                model.onEvent(
                    event = IntakesEvent.ScrollToDate(
                        tab = IntakeTab.entries[pagerState.currentPage],
                        listState = listStates[pagerState.currentPage],
                        time = time
                    )
                )
            }
        )

        null -> Unit
    }
}

@Composable
private fun <T : IntakeModel> IntakeList(
    items: List<IntakeListScheme<T>>,
    modifier: Modifier,
    state: LazyListState,
    showDialog: ((Long) -> Unit)? = null,
    showDialogDelete: ((Long) -> Unit)? = null,
    showDialogScheduleToTaken: ((IntakeModel) -> Unit)? = null
) = LazyColumn(modifier, state) {
    items.fastForEach { group ->
        item(group.date) {
            TextDate(group.date)
        }

        itemsIndexed(
            items = group.intakes,
            key = { _, item -> item.id }
        ) { index, item ->
            ItemSchedule(
                item = item,
                index = index,
                count = group.intakes.size,
                modifier = Modifier.animateItem(),
                showDialog = showDialog,
                showDialogDelete = showDialogDelete,
                showDialogScheduleToTaken = showDialogScheduleToTaken
            )
        }
    }
}

@Composable
private fun ItemIntake(intake: Intake, modifier: Modifier, onNavigate: (Long) -> Unit) =
    ListItem(
        modifier = modifier,
        onClick = { onNavigate(intake.intakeId) },
        overlineContent = {
            Text(
                text = intake.title,
                softWrap = false,
                overflow = TextOverflow.Ellipsis,
                maxLines = 1,
                style = MaterialTheme.typography.bodyLarge,
                color = ListItemDefaults.colors().contentColor
            )
        },
        content = {
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
        )
    )

@Composable
private fun DialogGoToDate(onDismiss: () -> Unit, onScroll: (Long) -> Unit) {
    val pickerState = rememberDatePickerState()

    DatePickerDialog(
        onDismissRequest = onDismiss,
        dismissButton = { TextButton(onDismiss) { Text(stringResource(R.string.text_cancel)) } },
        confirmButton = {
            TextButton(
                enabled = pickerState.selectedDateMillis != null,
                onClick = { onScroll(pickerState.selectedDateMillis!!) },
                content = { Text(stringResource(R.string.text_go_to)) }
            )
        }
    ) {
        DatePicker(
            state = pickerState,
            showModeToggle = false
        )
    }
}

@Composable
private fun DialogScheduleToTaken(
    item: ScheduledState,
    onDismiss: () -> Unit,
    onConfirm: () -> Unit
) = AlertDialog(
    title = { Text(stringResource(R.string.text_edit)) },
    onDismissRequest = onDismiss,
    dismissButton = { TextButton(onDismiss) { Text(stringResource(R.string.text_cancel)) } },
    confirmButton = {
        TextButton(
            onClick = onConfirm,
            enabled = item.taken,
            content = { Text(stringResource(R.string.text_confirm)) })
    },
    text = {
        Text(
            text = if (item.taken) stringResource(R.string.text_confirm_schedule_to_taken, item.title, item.date, item.time)
            else stringResource(R.string.text_medicine_not_in_stock, item.title),
            style = MaterialTheme.typography.bodyLarge
        )
    }
)

@Composable
private fun DialogAddTaken(
    medicines: List<MedicineMain>,
    newTaken: NewTakenState,
    onEvent: (NewTakenEvent) -> Unit,
    onDismiss: () -> Unit
) {
    val now = remember { Formatter.getDateTime(System.currentTimeMillis()) }
    val today = remember { now.toLocalDate() }

    val datePickerState = rememberDatePickerState(
        initialSelectedDateMillis = today.atStartOfDay(ZoneOffset.UTC).toInstant().toEpochMilli(),
        selectableDates = object : SelectableDates {
            override fun isSelectableDate(utcTimeMillis: Long) =
                !Formatter.getDateTime(utcTimeMillis).toLocalDate().isAfter(today)
        }
    )

    val timePickerState = rememberTimePickerState(
        initialHour = now.hour,
        initialMinute = now.minute,
        is24Hour = true
    )

    LaunchedEffect(Unit) {
        onEvent(NewTakenEvent.SetDate(datePickerState))
        onEvent(NewTakenEvent.SetTime(timePickerState))
    }

    @Composable
    fun LocalTextField(
        @StringRes label: Int,
        value: String,
        onValueChange: (String) -> Unit = {},
        onEvent: () -> Unit = {},
        modifier: Modifier = Modifier,
        readOnly: Boolean = false,
        suffix: @Composable (() -> Unit)? = null,
        trailingIcon: @Composable (() -> Unit)? = null,
        keyboardOptions: KeyboardOptions = KeyboardOptions.Default,
        inputTransformation: InputTransformation? = null,
        outputTransformation: OutputTransformation? = null,
    ) {
        val textFieldState = rememberTextFieldState(value)
        val interactionSource = remember(::MutableInteractionSource)

        LaunchedEffect(textFieldState) {
            snapshotFlow { textFieldState.text.toString() }.collectLatest(onValueChange)
        }

        LaunchedEffect(value) {
            if (value != textFieldState.text) {
                textFieldState.edit { replace(0, length, value) }
            }
        }

        LaunchedEffect(interactionSource) {
            if (readOnly) {
                interactionSource.interactions.collectLatest { interaction ->
                    if (interaction is PressInteraction.Release) {
                        onEvent()
                    }
                }
            }
        }

        OutlinedTextField(
            state = textFieldState,
            modifier = modifier,
            readOnly = readOnly,
            lineLimits = TextFieldLineLimits.SingleLine,
            label = { Text(stringResource(label)) },
            keyboardOptions = keyboardOptions,
            interactionSource = interactionSource,
            inputTransformation = inputTransformation,
            outputTransformation = outputTransformation,
            suffix = suffix,
            trailingIcon = trailingIcon
        )
    }

    var expanded by remember { mutableStateOf(false) }
    var showDate by remember { mutableStateOf(false) }
    var showTime by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        dismissButton = { TextButton(onDismiss) { Text(stringResource(R.string.text_cancel)) } },
        confirmButton = {
            TextButton(
                enabled = newTaken.medicine != null && newTaken.amount.isNotEmpty(),
                onClick = { onEvent(NewTakenEvent.AddNewTaken) },
                content = { Text(stringResource(R.string.text_save)) }
            )
        },
        title = {
            Text(
                text = stringResource(R.string.text_add),
                modifier = Modifier.width(MinWidth)
            )
        },
        text = {
            Column(
                modifier = Modifier.verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                ExposedDropdownMenuBox(
                    expanded = expanded,
                    onExpandedChange = { expanded = it }
                ) {
                    LocalTextField(
                        value = newTaken.title,
                        label = R.string.text_medicine_product_name,
                        modifier = Modifier.menuAnchor(ExposedDropdownMenuAnchorType.PrimaryNotEditable),
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded) },
                        readOnly = true
                    )
                    ExposedDropdownMenu(
                        expanded = expanded,
                        onDismissRequest = { expanded = false }
                    ) {
                        medicines.fastForEach { item ->
                            DropdownMenuItem(
                                shape = MenuDefaults.shape,
                                onClick = {
                                    onEvent(NewTakenEvent.PickMedicine(item))
                                    expanded = false
                                },
                                text = {
                                    Text(
                                        text = item.nameAlias.ifEmpty(item::productName),
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                }
                            )
                        }
                    }
                }

                Row(Modifier.fillMaxWidth(), Arrangement.spacedBy(12.dp)) {
                    LocalTextField(
                        value = newTaken.amount,
                        onValueChange = { onEvent(NewTakenEvent.SetAmount(it)) },
                        label = R.string.text_amount,
                        modifier = Modifier.weight(0.5f),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        inputTransformation = DecimalAmountInputTransformation,
                        outputTransformation = DecimalAmountOutputTransformation
                    )
                    LocalTextField(
                        value = newTaken.inStock,
                        label = R.string.intake_text_in_stock,
                        modifier = Modifier.weight(0.5f),
                        suffix = { Text(newTaken.doseType.asString()) },
                        readOnly = true
                    )
                }

                Row(Modifier.fillMaxWidth(), Arrangement.spacedBy(12.dp)) {
                    LocalTextField(
                        value = newTaken.date,
                        label = R.string.intake_text_date,
                        modifier = Modifier.weight(0.5f),
                        readOnly = true,
                        onEvent = { showDate = true }
                    )
                    LocalTextField(
                        value = newTaken.time,
                        label = R.string.intake_text_time,
                        modifier = Modifier.weight(0.5f),
                        readOnly = true,
                        onEvent = { showTime = true }
                    )
                }
            }
        }
    )

    when {
        showDate -> DatePickerDialog(
            onDismissRequest = { showDate = false },
            dismissButton = {
                TextButton(
                    onClick = { showDate = false },
                    content = { Text(stringResource(R.string.text_cancel)) }
                )
            },
            confirmButton = {
                TextButton(
                    content = { Text(stringResource(R.string.text_save)) },
                    enabled = datePickerState.selectedDateMillis != null,
                    onClick = {
                        onEvent(NewTakenEvent.SetDate(datePickerState))
                        showDate = false
                    }
                )
            }
        ) {
            DatePicker(
                state = datePickerState,
                showModeToggle = false
            )
        }

        showTime -> TimePickerDialog(
            content = { TimePicker(timePickerState) },
            onCancel = { showTime = false },
            onConfirm = {
                onEvent(NewTakenEvent.SetTime(timePickerState))
                showTime = false
            }
        )
    }
}

@Composable
private fun DialogTaken(
    intake: TakenState,
    onDismiss: () -> Unit,
    onEvent: (TakenEvent) -> Unit
) {
    val context = LocalContext.current
    val items = listOf(R.string.intake_text_not_taken, R.string.intake_text_taken)

    @Composable
    fun LocalTextField(
        value: String,
        @StringRes label: Int,
        modifier: Modifier = Modifier,
        eventEnabled: Boolean = false,
        onEvent: () -> Unit = {}
    ) {
        val textFieldState = rememberTextFieldState(value)
        val interactionSource = remember(::MutableInteractionSource)

        LaunchedEffect(value) {
            textFieldState.edit { replace(0, length, value) }
        }

        LaunchedEffect(interactionSource, eventEnabled) {
            if (eventEnabled) {
                interactionSource.interactions.collectLatest { interaction ->
                    if (interaction is PressInteraction.Release) {
                        onEvent()
                    }
                }
            }
        }

        OutlinedTextField(
            state = textFieldState,
            modifier = modifier,
            interactionSource = interactionSource,
            lineLimits = TextFieldLineLimits.SingleLine,
            readOnly = true,
            label = { Text(stringResource(label)) }
        )
    }

    if (intake.showPicker) {
        TimePickerDialog(
            onCancel = { onEvent(TakenEvent.ShowTimePicker(false)) },
            onConfirm = { onEvent(TakenEvent.SetFactTime) },
            content = { TimePicker(intake.pickerState) }
        )
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = stringResource(R.string.text_edit),
                modifier = Modifier.width(MinWidth)
            )
        },
        confirmButton = {
            TextButton(
                content = { Text(stringResource(R.string.text_save)) },
                onClick = { onEvent(TakenEvent.Save(NotificationManagerCompat.from(context))) },
                enabled = when {
                    intake.medicine == null -> false
                    intake.medicine.prodAmount < intake.amount && !intake.taken -> false
                    else -> true
                }
            )
        },
        dismissButton = {
            TextButton(onDismiss) { Text(stringResource(R.string.text_cancel)) }
        },
        text = {
            Column(Modifier.verticalScroll(rememberScrollState()), Arrangement.spacedBy(8.dp)) {
                LocalTextField(
                    value = intake.productName,
                    label = R.string.text_medicine_product_name
                )
                LocalTextField(
                    value = intake.date,
                    label = R.string.intake_text_date
                )

                Row(Modifier.fillMaxWidth(), Arrangement.spacedBy(12.dp)) {
                    LocalTextField(
                        value = intake.scheduled,
                        label = R.string.intake_text_by_schedule,
                        modifier = Modifier.weight(0.5f)
                    )
                    LocalTextField(
                        value = intake.actual.asString(),
                        label = R.string.intake_text_in_fact,
                        modifier = Modifier.weight(0.5f),
                        eventEnabled = intake.selection == 1,
                        onEvent = { onEvent(TakenEvent.ShowTimePicker(true)) }
                    )
                }

                when {
                    intake.medicine == null -> {
                        LocalTextField(
                            value = stringResource(R.string.text_medicine_deleted),
                            label = R.string.text_status,
                            modifier = Modifier.width(MinWidth)
                        )
                    }

                    intake.medicine.prodAmount < intake.amount && !intake.taken -> {
                        LocalTextField(
                            value = stringResource(R.string.text_medicine_amount_not_enough),
                            label = items[0],
                            modifier = Modifier.width(MinWidth)
                        )
                    }

                    else -> Row(horizontalArrangement = Arrangement.spacedBy(ButtonGroupDefaults.ConnectedSpaceBetween)) {
                        items.forEachIndexed { index, label ->
                            ToggleButton(
                                checked = index == intake.selection,
                                onCheckedChange = { onEvent(TakenEvent.SetSelection(index)) },
                                modifier = Modifier.weight(1f),
                                content = { Text(stringResource(label)) },
                                shapes = if (index == 0) ButtonGroupDefaults.connectedLeadingButtonShapes()
                                else ButtonGroupDefaults.connectedTrailingButtonShapes()
                            )
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
    dismissButton = { TextButton(onDismiss) { Text(stringResource(R.string.text_cancel)) } },
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
private fun ItemSchedule(
    item: IntakeModel,
    index: Int,
    count: Int,
    modifier: Modifier,
    showDialog: ((Long) -> Unit)? = null,
    showDialogDelete: ((Long) -> Unit)? = null,
    showDialogScheduleToTaken: ((IntakeModel) -> Unit)? = null
) = SegmentedListItem(
    modifier = modifier.padding(ListItemDefaults.SegmentedGap),
    shapes = ListItemDefaults.segmentedShapes(index, count),
    onClick = { showDialog?.invoke(item.id) },
    onLongClick = {
        showDialogScheduleToTaken?.invoke(item)

        if (!item.taken) {
            showDialogDelete?.invoke(item.id)
        }
    },
    content = {
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
        containerColor = if (item.taken) MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f)
        else MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.9f)
    )
)