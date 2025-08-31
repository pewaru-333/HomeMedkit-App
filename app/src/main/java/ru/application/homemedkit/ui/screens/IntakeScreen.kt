package ru.application.homemedkit.ui.screens

import androidx.activity.compose.BackHandler
import androidx.annotation.StringRes
import androidx.compose.animation.Crossfade
import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Arrangement.spacedBy
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.toggleable
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.KeyboardArrowRight
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Checkbox
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MenuAnchorType
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.material3.TimePicker
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Alignment.Companion.CenterVertically
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.intl.Locale
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import ru.application.homemedkit.R
import ru.application.homemedkit.data.model.MedicineIntake
import ru.application.homemedkit.dialogs.DatePicker
import ru.application.homemedkit.dialogs.DateRangePicker
import ru.application.homemedkit.dialogs.TimePickerDialog
import ru.application.homemedkit.models.events.IntakeEvent
import ru.application.homemedkit.models.states.IntakeState
import ru.application.homemedkit.models.viewModels.IntakeViewModel
import ru.application.homemedkit.ui.elements.DialogDelete
import ru.application.homemedkit.ui.elements.MedicineImage
import ru.application.homemedkit.ui.elements.NavigationIcon
import ru.application.homemedkit.ui.elements.TopBarActions
import ru.application.homemedkit.utils.DotCommaReplacer
import ru.application.homemedkit.utils.decimalFormat
import ru.application.homemedkit.utils.enums.FoodType
import ru.application.homemedkit.utils.enums.IntakeExtra
import ru.application.homemedkit.utils.enums.Interval
import ru.application.homemedkit.utils.enums.Period
import ru.application.homemedkit.utils.enums.SchemaType
import ru.application.homemedkit.utils.extensions.canUseFullScreenIntent
import ru.application.homemedkit.utils.extensions.intake
import ru.application.homemedkit.utils.formName
import ru.application.homemedkit.utils.toExpDate
import java.time.DayOfWeek
import java.time.format.TextStyle


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun IntakeScreen(navigateBack: () -> Unit) {
    val focusManager = LocalFocusManager.current

    val model = viewModel<IntakeViewModel>()
    val state by model.state.collectAsStateWithLifecycle()

    LaunchedEffect(state.default) {
        if (state.default) {
            focusManager.clearFocus(true)
        }
    }

    BackHandler(!state.isFirstLaunch) {
        if (state.default) navigateBack()
        else model.onEvent(IntakeEvent.ShowDialogDataLoss(true))
    }
    if (state.isFirstLaunch) PermissionsScreen(navigateBack, model::setExitFirstLaunch)
    else Scaffold(
        topBar = {
            TopAppBar(
                title = {},
                navigationIcon = {
                    NavigationIcon {
                        if (state.default) navigateBack()
                        else model.onEvent(IntakeEvent.ShowDialogDataLoss(true))
                    }
                },
                actions = {
                    TopBarActions(
                        isDefault = state.default,
                        setModifiable = model::setEditing,
                        onSave = if (state.adding) model::add else model::update,
                        onShowDialog = { model.onEvent(IntakeEvent.ShowDialogDelete) }
                    )
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.tertiaryContainer,
                    navigationIconContentColor = MaterialTheme.colorScheme.onTertiaryContainer,
                    actionIconContentColor = MaterialTheme.colorScheme.onTertiaryContainer
                )
            )
        }
    ) { values ->
        Crossfade(state.isLoading) { isLoading ->
            if (isLoading) {
                Box(Modifier.fillMaxSize())
            } else {
                LazyColumn(
                    verticalArrangement = spacedBy(24.dp),
                    contentPadding = values.intake()
                ) {
                    item { MedicineInfo(state.medicine, state.image) }

                    item { SchemaType(state, model::onEvent) }

                    if (state.schemaType == SchemaType.BY_DAYS) item {
                        DaysPicker(state, model::onEvent)
                    }

                    if (state.schemaType != SchemaType.BY_DAYS) item {
                        Interval(state, model::onEvent)
                    }

                    if (state.schemaType != SchemaType.INDEFINITELY) item {
                        Period(state, model::onEvent)
                    }

                    item { Amount(state, model::onEvent) }
                    item { Food(state, model::onEvent) }
                    item { Time(state, model::onEvent) }
                    item { Extra(state, model::onEvent) }
                }
            }
        }
    }

    when {
        state.showDialogDescription -> DialogDescription(state, model::onEvent)
        state.showDialogDataLoss -> DialogDataLoss(model::onEvent, navigateBack)
        state.showDialogDelete -> DialogDelete(
            text = R.string.text_confirm_deletion_int,
            onCancel = { model.onEvent(IntakeEvent.ShowDialogDelete) },
            onConfirm = { model.delete(navigateBack) }
        )

        state.showDatePicker -> DatePicker(
            onDismiss = { model.onEvent(IntakeEvent.ShowDatePicker) },
            onSelect = { model.onEvent(IntakeEvent.SetStartDate(it)) }
        )

        state.showDateRangePicker -> DateRangePicker(
            startDate = state.startDate,
            finalDate = state.finalDate,
            onDismiss = { model.onEvent(IntakeEvent.ShowDatePicker) },
            onRangeSelected = { model.onEvent(IntakeEvent.SetPeriod(it)) }
        )

        state.showTimePicker -> TimePickerDialog(
            onCancel = { model.onEvent(IntakeEvent.ShowTimePicker()) },
            onConfirm = { model.onEvent(IntakeEvent.SetPickedTime) },
            content = { TimePicker(state.pickedTime[state.timePickerIndex].picker) }
        )
    }
}

@Composable
private fun MedicineInfo(medicine: MedicineIntake, image: String) {

    @Composable
    fun LocalInfo(@StringRes label: Int, text: String) = Column {
        Text(
            text = stringResource(label),
            style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.W400)
        )

        Text(
            text = text.ifEmpty { stringResource(R.string.text_unspecified) },
            style = MaterialTheme.typography.titleMedium,
            softWrap = false,
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState())
        )
    }

    Row(
        horizontalArrangement = spacedBy(12.dp),
        modifier = Modifier
            .fillMaxWidth()
            .height(156.dp)
    ) {
        MedicineImage(
            image = image,
            modifier = Modifier
                .fillMaxHeight()
                .width(128.dp)
                .border(1.dp, MaterialTheme.colorScheme.onSurface, MaterialTheme.shapes.medium)
                .padding(8.dp)
        )
        Column(
            verticalArrangement = Arrangement.SpaceBetween,
            modifier = Modifier
                .fillMaxHeight()
                .verticalScroll(rememberScrollState())
        ) {
            LocalInfo(R.string.text_medicine_product_name, medicine.nameAlias.ifEmpty(medicine::productName))
            LocalInfo(R.string.text_medicine_form, formName(medicine.prodFormNormName))
            LocalInfo(R.string.text_exp_date, toExpDate(medicine.expDate))
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SchemaType(state: IntakeState, event: (IntakeEvent) -> Unit) = OutlinedCard {
    ExposedDropdownMenuBox(state.showSchemaTypePicker, {}) {
        ListItem(
            modifier = Modifier.menuAnchor(MenuAnchorType.PrimaryNotEditable),
            headlineContent = { Text(stringResource(R.string.intake_text_schema_type)) },
            supportingContent = { Text(stringResource(state.schemaType.title)) },
            trailingContent = state.default.let {
                {
                  if (!it) {
                      IconButton(
                          onClick = { event(IntakeEvent.ShowSchemaTypePicker) },
                          content = { ExposedDropdownMenuDefaults.TrailingIcon(state.showSchemaTypePicker) }
                      )
                  }
                }
            }
        )
        ExposedDropdownMenu(state.showSchemaTypePicker, {}) {
            SchemaType.entries.forEach {
                DropdownMenuItem(
                    text = { Text(stringResource(it.title)) },
                    contentPadding = ExposedDropdownMenuDefaults.ItemContentPadding,
                    onClick = { event(IntakeEvent.SetSchemaType(it)) }
                )
            }
        }
    }
}

@Composable
private fun DaysPicker(state: IntakeState, event: (IntakeEvent) -> Unit) = OutlinedCard {
    ListItem(
        headlineContent = { Text(stringResource(R.string.text_repeat)) },
        supportingContent = {
            Text(
                if (state.pickedDays.size == DayOfWeek.entries.size) stringResource(R.string.text_every_day)
                else state.pickedDays.joinToString {
                    it.getDisplayName(TextStyle.SHORT, Locale.current.platformLocale)
                }
            )
        },
    )
    Row(
        horizontalArrangement = Arrangement.SpaceAround,
        verticalAlignment = CenterVertically,
        modifier = Modifier
            .fillMaxWidth()
            .background(ListItemDefaults.containerColor)
            .padding(vertical = 8.dp)
    ) {
        DayOfWeek.entries.forEach { day ->
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .size(32.dp)
                    .clip(CircleShape)
                    .border(1.dp, MaterialTheme.colorScheme.onSurface, CircleShape)
                    .background(
                        if (day !in state.pickedDays) MaterialTheme.colorScheme.surface
                        else MaterialTheme.colorScheme.tertiaryContainer
                    )
                    .clickable(
                        enabled = !state.default,
                        onClick = { event(IntakeEvent.SetPickedDay(day)) }
                    )
            ) {
                Text(
                    text = day.getDisplayName(TextStyle.NARROW, Locale.current.platformLocale),
                    color = if (day !in state.pickedDays) MaterialTheme.colorScheme.onSurface
                    else MaterialTheme.colorScheme.onTertiaryContainer
                )
            }
        }
    }
}

@Composable
private fun Amount(state: IntakeState, event: (IntakeEvent) -> Unit) =
    OutlinedCard(Modifier.animateContentSize()) {
        ListItem(
            headlineContent = { Text(stringResource(R.string.intake_text_amount)) },
            supportingContent = {
                Text(
                    stringResource(
                        R.string.intake_text_in_stock_params,
                        decimalFormat(state.amountStock),
                        stringResource(state.doseType)
                    )
                )
            }
        )

        HorizontalDivider()

        ListItem(
            headlineContent = { Text(stringResource(R.string.text_same_amount)) },
            supportingContent = {
                Text(stringResource(if (state.sameAmount) R.string.text_on else R.string.text_off))
            },
            trailingContent = {
                Switch(
                    checked = state.sameAmount,
                    onCheckedChange = { if (!state.default) event(IntakeEvent.SetSameAmount(it)) }
                )
            }
        )

        if (state.sameAmount) {
            HorizontalDivider()
            ListItem(
                leadingContent = { Icon(painterResource(R.drawable.vector_medicine), null) },
                headlineContent = {
                    TextField(
                        value = state.pickedTime.first().amount,
                        onValueChange = { event(IntakeEvent.SetAmount(it)) },
                        readOnly = state.default,
                        isError = state.amountError != null && state.pickedTime.first().amount.isEmpty(),
                        placeholder = { Text(stringResource(R.string.text_empty)) },
                        suffix = { Text(stringResource(state.doseType)) },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        visualTransformation = DotCommaReplacer,
                        colors = TextFieldDefaults.colors().copy(
                            focusedContainerColor = ListItemDefaults.containerColor,
                            unfocusedContainerColor = ListItemDefaults.containerColor,
                            focusedIndicatorColor = Color.Transparent,
                            unfocusedIndicatorColor = Color.Transparent
                        )
                    )
                }
            )
        }
    }


@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun Interval(state: IntakeState, event: (IntakeEvent) -> Unit) =
    OutlinedCard(Modifier.animateContentSize()) {
        ExposedDropdownMenuBox(state.showIntervalTypePicker, {}) {
            ListItem(
                modifier = Modifier.menuAnchor(MenuAnchorType.PrimaryNotEditable),
                headlineContent = { Text(stringResource(R.string.intake_text_interval)) },
                supportingContent = { Text(stringResource(state.intervalType.title)) },
                trailingContent = state.default.let {
                    {
                        if (!it) {
                            IconButton(
                                onClick = { event(IntakeEvent.ShowIntervalTypePicker) },
                                content = { ExposedDropdownMenuDefaults.TrailingIcon(state.showIntervalTypePicker) }
                            )
                        }
                    }
                }
            )
            ExposedDropdownMenu(state.showIntervalTypePicker, {}) {
                Interval.entries.forEach {
                    DropdownMenuItem(
                        text = { Text(stringResource(it.title)) },
                        contentPadding = ExposedDropdownMenuDefaults.ItemContentPadding,
                        onClick = { event(IntakeEvent.SetInterval(it)) }
                    )
                }
            }
        }

        if (state.intervalType == Interval.CUSTOM) {
            HorizontalDivider()
            ListItem(
                leadingContent = { Text(stringResource(R.string.text_every)) },
                headlineContent = {
                    OutlinedTextField(
                        value = state.interval,
                        onValueChange = { event(IntakeEvent.SetInterval(it)) },
                        readOnly = state.default,
                        placeholder = { Text(stringResource(R.string.text_empty)) },
                        suffix = { Text(stringResource(R.string.text_days_short)) },
                        isError = state.intervalError != null,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true,
                        colors = TextFieldDefaults.colors().copy(
                            focusedContainerColor = ListItemDefaults.containerColor,
                            unfocusedContainerColor = ListItemDefaults.containerColor,
                            focusedIndicatorColor = Color.Transparent,
                            unfocusedIndicatorColor = Color.Transparent
                        )
                    )
                }
            )
        }
    }

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun Period(state: IntakeState, event: (IntakeEvent) -> Unit) =
    OutlinedCard(Modifier.animateContentSize()) {
        Row(Modifier.fillMaxWidth(), Arrangement.Start, CenterVertically) {
            ExposedDropdownMenuBox(state.showPeriodTypePicker, {}, Modifier.weight(1f)) {
                ListItem(
                    modifier = Modifier.menuAnchor(MenuAnchorType.PrimaryNotEditable),
                    headlineContent = { Text(stringResource(R.string.intake_text_period)) },
                    supportingContent = { Text(stringResource(state.periodType.title)) },
                    trailingContent = state.default.let {
                        {
                            if (!it) {
                                IconButton(
                                    onClick = { event(IntakeEvent.ShowPeriodTypePicker) },
                                    content = { ExposedDropdownMenuDefaults.TrailingIcon(state.showPeriodTypePicker) }
                                )
                            }
                        }
                    }
                )
                ExposedDropdownMenu(state.showPeriodTypePicker, {}) {
                    Period.entries.forEach {
                        DropdownMenuItem(
                            text = { Text(stringResource(it.title)) },
                            contentPadding = ExposedDropdownMenuDefaults.ItemContentPadding,
                            onClick = { event(IntakeEvent.SetPeriod(it)) }
                        )
                    }
                }
            }

            if (state.periodType == Period.OTHER)
                TextField(
                    value = state.period,
                    onValueChange = { event(IntakeEvent.SetPeriod(it)) },
                    readOnly = state.default,
                    textStyle = MaterialTheme.typography.titleMedium,
                    placeholder = { Text(stringResource(R.string.text_empty)) },
                    leadingIcon = { Icon(painterResource(R.drawable.vector_period), null) },
                    suffix = { Text(stringResource(R.string.text_days_short)) },
                    isError = state.periodError != null,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true,
                    modifier = Modifier
                        .height(72.dp)
                        .weight(1f),
                    colors = TextFieldDefaults.colors().copy(
                        focusedContainerColor = ListItemDefaults.containerColor,
                        unfocusedContainerColor = ListItemDefaults.containerColor,
                        focusedIndicatorColor = Color.Transparent,
                        unfocusedIndicatorColor = Color.Transparent
                    )
                )
        }

        if (state.periodType != Period.INDEFINITE) {
            HorizontalDivider()
            Row {
                ListItem(
                    headlineContent = { Text(stringResource(R.string.intake_text_start)) },
                    supportingContent = {
                        Text(
                            softWrap = false,
                            overflow = TextOverflow.Visible,
                            text = if (state.periodType == Period.PICK)
                                state.startDate.ifEmpty { stringResource(R.string.text_not_selected) }
                            else state.startDate.ifEmpty { stringResource(R.string.text_today) }
                        )
                    },
                    trailingContent = state.let {
                        {
                            if (!it.default && it.periodType in Period.entries.dropLast(1))
                                Icon(Icons.AutoMirrored.Outlined.KeyboardArrowRight, null)
                        }
                    },
                    modifier = Modifier
                        .weight(1f)
                        .clickable(
                            enabled = !state.default && state.periodType in Period.entries.dropLast(
                                1
                            ),
                            onClick = { event(IntakeEvent.ShowDatePicker) }
                        ),
                    colors = ListItemDefaults.colors(
                        containerColor = if (state.periodError != null && state.startDate.isEmpty())
                            MaterialTheme.colorScheme.errorContainer
                        else ListItemDefaults.containerColor
                    )
                )
                ListItem(
                    headlineContent = { Text(stringResource(R.string.intake_text_finish)) },
                    supportingContent = {
                        Text(
                            softWrap = false,
                            overflow = TextOverflow.Visible,
                            text = if (state.periodType == Period.PICK)
                                state.finalDate.ifEmpty { stringResource(R.string.text_not_selected) }
                            else state.finalDate.ifEmpty { stringResource(R.string.text_tomorrow) }
                        )
                    },
                    trailingContent = state.let {
                        {
                            if (!it.default && it.periodType == Period.PICK)
                                Icon(Icons.AutoMirrored.Outlined.KeyboardArrowRight, null)
                        }
                    },
                    modifier = Modifier
                        .weight(1f)
                        .clickable(
                            enabled = !state.default && state.periodType == Period.PICK,
                            onClick = { event(IntakeEvent.ShowDatePicker) }
                        ),
                    colors = ListItemDefaults.colors(
                        containerColor = if (state.periodError != null && state.finalDate.isEmpty())
                            MaterialTheme.colorScheme.errorContainer
                        else ListItemDefaults.containerColor
                    )
                )
            }
        }
    }

@Composable
private fun Food(state: IntakeState, event: (IntakeEvent) -> Unit) = OutlinedCard {
    ListItem(
        headlineContent = { Text(stringResource(R.string.intake_text_food)) },
        supportingContent = {
            Text(
                stringResource(
                    if (state.foodType == -1) R.string.text_not_selected
                    else R.string.text_selected
                )
            )
        }
    )
    ListItem(
        headlineContent = {
            Row(Modifier.fillMaxWidth(), Arrangement.SpaceAround, CenterVertically) {
                FoodType.entries.forEach { type ->
                    FilterChip(
                        modifier = Modifier.width(100.dp),
                        selected = type.value == state.foodType,
                        onClick = { if (!state.default) event(IntakeEvent.SetFoodType(type.value)) },
                        label = {
                            Text(
                                text = stringResource(type.title),
                                textAlign = TextAlign.Center,
                                minLines = 2,
                                maxLines = 2
                            )
                        }
                    )
                }
            }
        }
    )
}

@Composable
private fun Time(state: IntakeState, event: (IntakeEvent) -> Unit) =
    OutlinedCard(Modifier.animateContentSize()) {
        ListItem(
            headlineContent = { Text(stringResource(R.string.intake_text_time)) },
            supportingContent = { Text(pluralStringResource(R.plurals.intake_times_a_day, state.pickedTime.size, state.pickedTime.size)) },
        )

        HorizontalDivider()

        state.pickedTime.forEachIndexed { index, amountTime ->
            Row {
                ListItem(
                    headlineContent = { Text(stringResource(R.string.placeholder_time, index + 1)) },
                    supportingContent = { Text(amountTime.time.ifEmpty { stringResource(R.string.text_not_selected) }) },
                    leadingContent = { Icon(painterResource(R.drawable.vector_time), null) },
                    colors = ListItemDefaults.colors(
                        containerColor = if (state.timesError != null && amountTime.time.isEmpty())
                            MaterialTheme.colorScheme.errorContainer
                        else ListItemDefaults.containerColor
                    ),
                    modifier = Modifier
                        .weight(1f)
                        .clickable(
                            enabled = !state.default,
                            onClick = { event(IntakeEvent.ShowTimePicker(index)) }
                        ),
                    trailingContent = index.let {
                        {
                            if (!state.default && (it == 0 || it == state.pickedTime.lastIndex)) IconButton(
                                onClick = {
                                    if (index == 0) event(IntakeEvent.IncTime)
                                    else event(IntakeEvent.DecTime)
                                }
                            ) {
                                Icon(
                                    contentDescription = null,
                                    imageVector = if (index == 0) Icons.Outlined.Add
                                    else ImageVector.vectorResource(R.drawable.vector_remove)
                                )
                            }
                        }
                    }
                )

                if (!state.sameAmount) {
                    TextField(
                        value = amountTime.amount,
                        onValueChange = { event(IntakeEvent.SetAmount(it, index)) },
                        readOnly = state.default,
                        isError = state.amountError != null && amountTime.amount.isEmpty(),
                        placeholder = { Text(stringResource(R.string.text_empty)) },
                        suffix = { Text(stringResource(state.doseType)) },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        visualTransformation = DotCommaReplacer,
                        modifier = Modifier
                            .height(72.dp)
                            .weight(0.6f),
                        colors = TextFieldDefaults.colors().copy(
                            focusedIndicatorColor = Color.Transparent,
                            unfocusedIndicatorColor = Color.Transparent,
                            focusedContainerColor = ListItemDefaults.containerColor,
                            unfocusedContainerColor = ListItemDefaults.containerColor,
                        )
                    )
                }
            }
        }
    }

@Composable
private fun Extra(state: IntakeState, event: (IntakeEvent) -> Unit) = OutlinedCard {
    ListItem(
        headlineContent = { Text(stringResource(R.string.intake_text_extra)) },
        supportingContent = { Text(stringResource(R.string.text_selected_of, state.selectedExtras.size, IntakeExtra.entries.size)) }
    )

    HorizontalDivider()

    IntakeExtra.entries
        .filter { !(it == IntakeExtra.FULLSCREEN && !LocalContext.current.canUseFullScreenIntent()) }
        .forEach { extra ->
            ListItem(
                headlineContent = { Text(stringResource(extra.title)) },
                leadingContent = {
                    Checkbox(
                        onCheckedChange = null,
                        checked = when (extra) {
                            IntakeExtra.CANCELLABLE -> state.cancellable
                            IntakeExtra.FULLSCREEN -> state.fullScreen
                            IntakeExtra.NO_SOUND -> state.noSound
                            IntakeExtra.PREALARM -> state.preAlarm
                        }
                    )
                },
                trailingContent = {
                    IconButton(
                        onClick = { event(IntakeEvent.ShowDialogDescription(extra.description)) },
                        content = { Icon(Icons.Outlined.Info, null) }
                    )
                },
                modifier = Modifier.toggleable(
                    enabled = !state.default,
                    role = Role.Checkbox,
                    onValueChange = { event(IntakeEvent.SetIntakeExtra(extra)) },
                    value = when (extra) {
                        IntakeExtra.CANCELLABLE -> state.cancellable
                        IntakeExtra.FULLSCREEN -> state.fullScreen
                        IntakeExtra.NO_SOUND -> state.noSound
                        IntakeExtra.PREALARM -> state.preAlarm
                    }
                )
            )
        }
}

@Composable
private fun DialogDescription(state: IntakeState, event: (IntakeEvent) -> Unit) = AlertDialog(
    onDismissRequest = { event(IntakeEvent.ShowDialogDescription()) },
    title = { Text(stringResource(R.string.text_medicine_description)) },
    confirmButton = {},
    text = state.extraDesc?.let {
        {
            Text(
                text = stringResource(it),
                style = MaterialTheme.typography.bodyLarge
            )
        }
    },
    dismissButton = {
        TextButton(
            onClick = { event(IntakeEvent.ShowDialogDescription()) },
            content = { Text(stringResource(R.string.text_dismiss)) }
        )
    }
)

@Composable
private fun DialogDataLoss(event: (IntakeEvent) -> Unit, navigateUp: () -> Unit) = AlertDialog(
    onDismissRequest = { event(IntakeEvent.ShowDialogDataLoss(false)) },
    confirmButton = { TextButton(navigateUp) { Text(stringResource(R.string.text_exit)) } },
    text = {
        Text(
            text = stringResource(R.string.text_not_saved_intake),
            style = MaterialTheme.typography.bodyLarge
        )
    },
    dismissButton = {
        TextButton(
            onClick = { event(IntakeEvent.ShowDialogDataLoss(false)) },
            content = { Text(stringResource(R.string.text_stay)) }
        )
    }
)