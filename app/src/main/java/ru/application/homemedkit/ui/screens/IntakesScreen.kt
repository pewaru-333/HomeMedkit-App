package ru.application.homemedkit.ui.screens

import androidx.annotation.StringRes
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuAnchorType
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults.MinHeight
import androidx.compose.material3.OutlinedTextFieldDefaults.MinWidth
import androidx.compose.material3.PrimaryTabRow
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SelectableDates
import androidx.compose.material3.ShapeDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TimePicker
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
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.core.app.NotificationManagerCompat
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import ru.application.homemedkit.R
import ru.application.homemedkit.data.model.Intake
import ru.application.homemedkit.data.model.IntakeModel
import ru.application.homemedkit.data.model.MedicineMain
import ru.application.homemedkit.data.model.ScheduleModel
import ru.application.homemedkit.data.model.TakenModel
import ru.application.homemedkit.dialogs.TimePickerDialog
import ru.application.homemedkit.models.events.NewTakenEvent
import ru.application.homemedkit.models.events.TakenEvent
import ru.application.homemedkit.models.states.NewTakenState
import ru.application.homemedkit.models.states.ScheduledState
import ru.application.homemedkit.models.states.TakenState
import ru.application.homemedkit.models.viewModels.IntakesViewModel
import ru.application.homemedkit.ui.elements.BoxWithEmptyListText
import ru.application.homemedkit.ui.elements.MedicineImage
import ru.application.homemedkit.ui.elements.SearchAppBar
import ru.application.homemedkit.ui.elements.TextDate
import ru.application.homemedkit.ui.elements.VectorIcon
import ru.application.homemedkit.ui.navigation.Screen
import ru.application.homemedkit.utils.DotCommaReplacer
import ru.application.homemedkit.utils.di.Preferences
import ru.application.homemedkit.utils.enums.IntakeTab
import ru.application.homemedkit.utils.getDateTime
import java.time.ZoneOffset

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun IntakesScreen(onNavigate: (Long) -> Unit) {
    val model = viewModel<IntakesViewModel>()

    val state by model.state.collectAsStateWithLifecycle()
    val medicines by model.medicines.collectAsStateWithLifecycle()
    val intakes by model.intakes.collectAsStateWithLifecycle()
    val schedule by model.schedule.collectAsStateWithLifecycle()
    val scheduledState by model.scheduleState.collectAsStateWithLifecycle()
    val taken by model.taken.collectAsStateWithLifecycle()
    val takenState by model.takenState.collectAsStateWithLifecycle()
    val newTaken by model.newTakenState.collectAsStateWithLifecycle()

    val scope = rememberCoroutineScope()
    val pagerState = rememberPagerState(
        pageCount = IntakeTab.entries::count,
        initialPage = when (val route = Preferences.startPage.route) {
            is Screen.Intakes -> route.tab.ordinal
            else -> 0
        }
    )
    val listStates = IntakeTab.entries.map { rememberLazyListState() }

    fun onScroll(page: Int) {
        scope.launch {
            pagerState.animateScrollToPage(page)
        }
    }

    LaunchedEffect(pagerState) {
        snapshotFlow(pagerState::currentPage).collectLatest {
            if (it != pagerState.settledPage) {
                onScroll(it)
            }
        }
    }

    Scaffold(
        topBar = {
            SearchAppBar(
                search = state.search,
                onSearch = model::setSearch,
                onClear = model::clearSearch,
                actions = {
                    AnimatedVisibility(IntakeTab.entries[pagerState.currentPage] != IntakeTab.LIST) {
                        IconButton(model::showDialogDate) {
                            VectorIcon(R.drawable.vector_date_range)
                        }
                    }
                }
            )
        },
        floatingActionButton = {
            AnimatedVisibility(IntakeTab.entries[pagerState.currentPage] == IntakeTab.PAST) {
                FloatingActionButton(model::showDialogAddTaken) {
                    VectorIcon(R.drawable.vector_add)
                }
            }
        }
    ) { values ->
        Column(Modifier.padding(values)) {
            PrimaryTabRow(pagerState.currentPage) {
                IntakeTab.entries.forEach { tab ->
                    Tab(
                        selected = pagerState.currentPage == tab.ordinal,
                        onClick = { onScroll(tab.ordinal) },
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

                    IntakeTab.CURRENT -> LazyColumn(Modifier.fillMaxSize(),listStates[1]) {
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
                                    modifier = Modifier.animateItem(),
                                    showDialogScheduleToTaken = model::showDialogScheduleToTaken
                                )

                                if (index < it.intakes.lastIndex) {
                                    HorizontalDivider()
                                }
                            }
                        }
                    }

                    IntakeTab.PAST -> LazyColumn(Modifier.fillMaxSize(), listStates[2]) {
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

                                if (index < it.intakes.lastIndex) {
                                    HorizontalDivider()
                                }
                            }
                        }
                    }
                }
            }
        }

        when {
            state.showDialog -> DialogTaken(takenState, model::onTakenEvent)
            state.showDialogDelete -> DialogDeleteTaken(model::showDialogDelete, model::deleteTaken)
            state.showDialogDate -> DialogGoToDate(model::showDialogDate) { time ->
                model.scrollToClosest(
                    tab = IntakeTab.entries[pagerState.currentPage],
                    listState = listStates[pagerState.currentPage],
                    time = time
                )
            }

            state.showDialogScheduleToTaken -> DialogScheduleToTaken(
                item = scheduledState,
                onDismiss = model::showDialogScheduleToTaken,
                onConfirm = model::scheduleToTaken
            )

            state.showDialogAddTaken -> DialogAddTaken(
                medicines = medicines,
                newTaken = newTaken,
                onEvent = model::onNewTakenEvent,
                onHide = model::showDialogAddTaken,
            )
        }
    }
}

@Composable
private fun ItemIntake(intake: Intake, modifier: Modifier, onNavigate: (Long) -> Unit) =
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
            onNavigate(intake.intakeId)
        }
    )

@OptIn(ExperimentalMaterial3Api::class)
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
            text = if (item.taken) stringResource(R.string.text_confirm_schedule_to_taken, item.title, item.time)
            else stringResource(R.string.text_medicine_not_in_stock, item.title),
            style = MaterialTheme.typography.bodyLarge
        )
    }
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun DialogAddTaken(
    medicines: List<MedicineMain>,
    newTaken: NewTakenState,
    onEvent: (NewTakenEvent) -> Unit,
    onHide: () -> Unit
) {
    val now = remember { getDateTime(System.currentTimeMillis()) }
    val today = remember { now.toLocalDate() }

    val datePickerState = rememberDatePickerState(
        initialSelectedDateMillis = today.atStartOfDay(ZoneOffset.UTC).toInstant().toEpochMilli(),
        selectableDates = object : SelectableDates {
            override fun isSelectableDate(utcTimeMillis: Long) =
                !getDateTime(utcTimeMillis).toLocalDate().isAfter(today)
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
        value: String,
        onValueChange: (String) -> Unit = {},
        onEvent: () -> Unit = {},
        modifier: Modifier = Modifier,
        readOnly: Boolean = false,
        singleLine: Boolean = true,
        suffix: @Composable (() -> Unit)? = null,
        trailingIcon: @Composable (() -> Unit)? = null,
        @StringRes label: Int,
        @StringRes placeholder: Int = R.string.text_empty,
        keyboardOptions: KeyboardOptions = KeyboardOptions.Default,
        visualTransformation: VisualTransformation = VisualTransformation.None
    ) {
        val interactionSource = remember(::MutableInteractionSource)

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
            value = value,
            onValueChange = onValueChange,
            modifier = modifier,
            readOnly = readOnly,
            singleLine = singleLine,
            label = { Text(stringResource(label)) },
            placeholder = { Text(stringResource(placeholder)) },
            keyboardOptions = keyboardOptions,
            visualTransformation = visualTransformation,
            interactionSource = interactionSource,
            suffix = suffix,
            trailingIcon = trailingIcon
        )
    }

    var expanded by remember { mutableStateOf(false) }
    var showDate by remember { mutableStateOf(false) }
    var showTime by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onHide,
        dismissButton = { TextButton(onHide) { Text(stringResource(R.string.text_cancel)) } },
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
                        medicines.forEach { item ->
                            DropdownMenuItem(
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
                        visualTransformation = DotCommaReplacer
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun DialogTaken(intake: TakenState, onEvent: (TakenEvent) -> Unit) {
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
        val interactionSource = remember(::MutableInteractionSource)

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
            value = value,
            onValueChange = {},
            modifier = modifier,
            label = { Text(stringResource(label)) },
            readOnly = true,
            singleLine = true,
            interactionSource = interactionSource
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
        onDismissRequest = { onEvent(TakenEvent.HideDialog) },
        title = {
            Text(
                text = stringResource(R.string.text_edit),
                modifier = Modifier.width(MinWidth)
            )
        },
        confirmButton = {
            TextButton(
                content = { Text(stringResource(R.string.text_save)) },
                onClick = { onEvent(TakenEvent.SaveTaken(NotificationManagerCompat.from(context))) },
                enabled = when {
                    intake.medicine == null -> false
                    intake.medicine.prodAmount < intake.amount && !intake.taken -> false
                    else -> true
                }
            )
        },
        dismissButton = {
            TextButton(
                onClick = { onEvent(TakenEvent.HideDialog) },
                content = { Text(stringResource(R.string.text_cancel)) }
            )
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

                    else -> SingleChoiceSegmentedButtonRow(Modifier.size(MinWidth, MinHeight)) {
                        items.forEachIndexed { index, label ->
                            SegmentedButton(
                                selected = index == intake.selection,
                                shape = SegmentedButtonDefaults.itemShape(index, items.size, ShapeDefaults.ExtraSmall),
                                onClick = { onEvent(TakenEvent.SetSelection(index)) },
                                label = { Text(stringResource(label)) }
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

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun ItemSchedule(
    item: IntakeModel,
    modifier: Modifier,
    showDialog: ((Long) -> Unit)? = null,
    showDialogDelete: ((Long) -> Unit)? = null,
    showDialogScheduleToTaken: ((IntakeModel) -> Unit)? = null
) = ListItem(
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
        onLongClick = {
            showDialogScheduleToTaken?.invoke(item)

            if (!item.taken) {
                showDialogDelete?.invoke(item.id)
            }
        }
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
        containerColor = if (item.taken) MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.35f)
        else MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.65f)
    )
)