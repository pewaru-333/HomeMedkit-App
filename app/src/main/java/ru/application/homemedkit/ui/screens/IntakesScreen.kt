package ru.application.homemedkit.ui.screens

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.gestures.waitForUpOrCancellation
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
import androidx.compose.foundation.text.BasicText
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.text.TextAutoSize
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material.icons.outlined.Clear
import androidx.compose.material.icons.outlined.DateRange
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MenuAnchorType
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults.MinHeight
import androidx.compose.material3.OutlinedTextFieldDefaults.MinWidth
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SelectableDates
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
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
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.PointerEventPass
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.OffsetMapping
import androidx.compose.ui.text.input.TransformedText
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.app.NotificationManagerCompat
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
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
import ru.application.homemedkit.receivers.AlarmSetter
import ru.application.homemedkit.ui.elements.BoxWithEmptyListText
import ru.application.homemedkit.ui.navigation.Screen
import ru.application.homemedkit.utils.Preferences
import ru.application.homemedkit.utils.enums.IntakeTab
import ru.application.homemedkit.utils.getDateTime
import java.time.LocalDate

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun IntakesScreen(onNavigate: (Long) -> Unit) {
    val context = LocalContext.current
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

    LaunchedEffect(pagerState) {
        snapshotFlow(pagerState::currentPage).collectLatest {
            if (it != pagerState.settledPage) {
                pagerState.animateScrollToPage(it)
            }
        }
    }

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
                    if (IntakeTab.entries[pagerState.currentPage] != IntakeTab.LIST) {
                        IconButton(model::showDialogDate) {
                            Icon(Icons.Outlined.DateRange, null)
                        }
                    }
                }
            )
        },
        floatingActionButton = {
            if (IntakeTab.entries[pagerState.currentPage] == IntakeTab.PAST) {
                FloatingActionButton(model::showDialogAddTaken) {
                    Icon(Icons.Outlined.Add, null)
                }
            }
        }
    ) { values ->
        val style = MaterialTheme.typography.titleSmall
        var maxFontSize by remember {
            mutableStateOf(style.fontSize)
        }

        Column(Modifier.padding(values)) {
            TabRow(pagerState.currentPage) {
                IntakeTab.entries.forEach { tab ->
                    Tab(
                        selected = pagerState.currentPage == tab.ordinal,
                        onClick = {
                            scope.launch {
                                pagerState.animateScrollToPage(tab.ordinal)
                            }
                        },
                        text = {
                            BasicText(
                                text = stringResource(tab.title),
                                softWrap = false,
                                style = LocalTextStyle.current.copy(
                                    color = LocalContentColor.current,
                                    fontSize = maxFontSize
                                ),
                                autoSize = TextAutoSize.StepBased(
                                    minFontSize = 10.sp,
                                    maxFontSize = maxFontSize,
                                    stepSize = (0.1).sp
                                ),
                                onTextLayout = {
                                    maxFontSize = it.layoutInput.style.fontSize
                                }
                            )
                        }
                    )
                }
            }

            HorizontalPager(pagerState) { index ->
                when (index) {
                    0 -> if (intakes.isNotEmpty())
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

                    1 -> LazyColumn(Modifier.fillMaxSize(),listStates[1]) {
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

                    2 -> LazyColumn(state = listStates[2]) {
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
                onConfirm = { model.scheduleToTaken(AlarmSetter(context)) }
            )

            state.showDialogAddTaken -> DialogAddTaken(
                medicines = medicines,
                newTaken = newTaken,
                onEvent = model::onNewTakenEvent,
                hide = model::showDialogAddTaken,
            )
        }
    }
}

@Composable
fun ItemIntake(intake: Intake, modifier: Modifier, onNavigate: (Long) -> Unit) =
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
private fun DialogGoToDate(show: () -> Unit, scroll: (Long) -> Unit) {
    val pickerState = rememberDatePickerState()

    DatePickerDialog(
        onDismissRequest = show,
        dismissButton = { TextButton(show) { Text(stringResource(text_cancel)) } },
        confirmButton = {
            TextButton(
                enabled = pickerState.selectedDateMillis != null,
                onClick = { scroll(pickerState.selectedDateMillis!!) },
                content = { Text(stringResource(text_go_to)) }
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
    onDismissRequest = onDismiss,
    title = { Text(stringResource(R.string.text_edit)) },
    dismissButton = {
        TextButton(onDismiss) { Text(stringResource(R.string.text_cancel)) }
    },
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
    hide: () -> Unit
) {
    val currentTime = getDateTime(System.currentTimeMillis())

    val datePickerState = rememberDatePickerState(
        initialSelectedDateMillis = currentTime.toInstant().toEpochMilli(),
        selectableDates = object : SelectableDates {
            override fun isSelectableDate(utcTimeMillis: Long) =
                getDateTime(utcTimeMillis).toLocalDate() <= LocalDate.now()
        }
    )

    val timePickerState = rememberTimePickerState(
        initialHour = currentTime.hour,
        initialMinute = currentTime.minute,
        is24Hour = true
    )

    LaunchedEffect(Unit) {
        onEvent(NewTakenEvent.SetDate(datePickerState))
        onEvent(NewTakenEvent.SetTime(timePickerState))
    }

    var expanded by remember { mutableStateOf(false) }
    var showDate by remember { mutableStateOf(false) }
    var showTime by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = hide,
        dismissButton = {
            TextButton(hide) { Text(stringResource(R.string.text_cancel)) }
        },
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
                    OutlinedTextField(
                        value = newTaken.title,
                        modifier = Modifier.menuAnchor(MenuAnchorType.PrimaryNotEditable),
                        onValueChange = {},
                        readOnly = true,
                        label = { Text(stringResource(R.string.text_medicine_product_name)) }
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
                    OutlinedTextField(
                        value = newTaken.amount,
                        modifier = Modifier.weight(0.5f),
                        onValueChange = { onEvent(NewTakenEvent.SetAmount(it)) },
                        label = { Text(stringResource(R.string.text_amount)) },
                        placeholder = { Text(stringResource(R.string.text_empty)) },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        visualTransformation = {
                            TransformedText(
                                AnnotatedString(it.text.replace('.', ',')),
                                OffsetMapping.Identity
                            )
                        }
                    )
                    OutlinedTextField(
                        value = newTaken.inStock,
                        modifier = Modifier.weight(0.5f),
                        onValueChange = {},
                        label = { Text(stringResource(R.string.intake_text_in_stock)) },
                        placeholder = { Text(stringResource(R.string.text_empty)) },
                        suffix = { Text(newTaken.doseType.asString()) },
                        readOnly = true
                    )
                }

                Row(Modifier.fillMaxWidth(), Arrangement.spacedBy(12.dp)) {
                    OutlinedTextField(
                        value = newTaken.date,
                        onValueChange = {},
                        readOnly = true,
                        label = { Text(stringResource(R.string.intake_text_date)) },
                        placeholder = { Text(stringResource(R.string.text_empty)) },
                        modifier = Modifier
                            .weight(0.5f)
                            .pointerInput(Unit) {
                                awaitEachGesture {
                                    awaitFirstDown(pass = PointerEventPass.Initial)
                                    val upEvent =
                                        waitForUpOrCancellation(pass = PointerEventPass.Initial)
                                    if (upEvent != null) {
                                        showDate = true
                                    }
                                }
                            }
                    )

                    OutlinedTextField(
                        value = newTaken.time,
                        onValueChange = {},
                        readOnly = true,
                        label = { Text(stringResource(R.string.intake_text_time)) },
                        placeholder = { Text(stringResource(R.string.text_empty)) },
                        modifier = Modifier
                            .weight(0.5f)
                            .pointerInput(Unit) {
                                awaitEachGesture {
                                    awaitFirstDown(pass = PointerEventPass.Initial)
                                    val upEvent =
                                        waitForUpOrCancellation(pass = PointerEventPass.Initial)
                                    if (upEvent != null) {
                                        showTime = true
                                    }
                                }
                            }
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
                    content = { Text(stringResource(text_cancel)) }
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
    val items = listOf(stringResource(intake_text_not_taken), stringResource(intake_text_taken))

    if (intake.showPicker) {
        TimePickerDialog(
            onCancel = { onEvent(TakenEvent.ShowTimePicker(false)) },
            onConfirm = { onEvent(TakenEvent.SetFactTime) }
        ) {
            TimePicker(intake.pickerState)
        }
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
                content = { Text(stringResource(text_save)) },
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
                content = { Text(stringResource(text_cancel)) }
            )
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
                                label = { Text(label) }
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
fun TextDate(date: String) = Text(
    text = date,
    style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.W500),
    modifier = Modifier
        .fillMaxWidth()
        .background(MaterialTheme.colorScheme.background)
        .padding(16.dp)
)

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun ItemSchedule(
    item: IntakeModel,
    modifier: Modifier,
    showDialog: ((Long) -> Unit)? = null,
    showDialogDelete: ((Long) -> Unit)? = null,
    showDialogScheduleToTaken: ((IntakeModel) -> Unit)? = null
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
            if (item.taken) MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.35f)
            else MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.65f)
        )
    )
}