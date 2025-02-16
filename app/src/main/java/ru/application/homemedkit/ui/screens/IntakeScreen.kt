package ru.application.homemedkit.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Arrangement.spacedBy
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
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
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material.icons.automirrored.outlined.KeyboardArrowRight
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material.icons.outlined.Check
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material.icons.outlined.MoreVert
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Checkbox
import androidx.compose.material3.DropdownMenu
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
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
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
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.OffsetMapping
import androidx.compose.ui.text.input.TransformedText
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import ru.application.homemedkit.R
import ru.application.homemedkit.R.drawable.vector_remove
import ru.application.homemedkit.R.string.intake_text_amount
import ru.application.homemedkit.R.string.intake_text_extra
import ru.application.homemedkit.R.string.intake_text_food
import ru.application.homemedkit.R.string.intake_text_interval
import ru.application.homemedkit.R.string.intake_text_period
import ru.application.homemedkit.R.string.intake_text_time
import ru.application.homemedkit.R.string.placeholder_time
import ru.application.homemedkit.R.string.text_confirm_deletion_int
import ru.application.homemedkit.R.string.text_days_short
import ru.application.homemedkit.R.string.text_delete
import ru.application.homemedkit.R.string.text_dismiss
import ru.application.homemedkit.R.string.text_edit
import ru.application.homemedkit.R.string.text_empty
import ru.application.homemedkit.R.string.text_every
import ru.application.homemedkit.R.string.text_exit
import ru.application.homemedkit.R.string.text_exp_date
import ru.application.homemedkit.R.string.text_medicine_description
import ru.application.homemedkit.R.string.text_medicine_form
import ru.application.homemedkit.R.string.text_medicine_product_name
import ru.application.homemedkit.R.string.text_not_saved_intake
import ru.application.homemedkit.R.string.text_stay
import ru.application.homemedkit.R.string.text_unspecified
import ru.application.homemedkit.data.dto.Medicine
import ru.application.homemedkit.dialogs.DateRangePicker
import ru.application.homemedkit.dialogs.TimePickerDialog
import ru.application.homemedkit.helpers.FoodTypes
import ru.application.homemedkit.helpers.IntakeExtras
import ru.application.homemedkit.helpers.Intervals
import ru.application.homemedkit.helpers.LOCALE
import ru.application.homemedkit.helpers.Periods
import ru.application.homemedkit.helpers.SchemaTypes
import ru.application.homemedkit.helpers.canUseFullScreenIntent
import ru.application.homemedkit.helpers.decimalFormat
import ru.application.homemedkit.helpers.formName
import ru.application.homemedkit.helpers.toExpDate
import ru.application.homemedkit.models.events.IntakeEvent
import ru.application.homemedkit.models.states.IntakeState
import ru.application.homemedkit.models.viewModels.IntakeViewModel
import ru.application.homemedkit.receivers.AlarmSetter
import java.time.DayOfWeek
import java.time.format.TextStyle
import java.util.Locale


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun IntakeScreen(navigateBack: () -> Unit) {
    val model = viewModel<IntakeViewModel>()
    val state by model.state.collectAsStateWithLifecycle()

    model.setAlarmSetter(AlarmSetter(LocalContext.current))

    BackHandler(!state.isFirstLaunch) {
        if (state.adding || state.editing) model.onEvent(IntakeEvent.ShowDialogDataLoss(true))
        else navigateBack()
    }
    if (state.isFirstLaunch) PermissionsScreen(navigateBack, model::setExitFirstLaunch)
    else Scaffold(
        topBar = {
            TopAppBar(
                title = {},
                navigationIcon = {
                    IconButton(
                        onClick = {
                            if (state.adding || state.editing) model.onEvent(IntakeEvent.ShowDialogDataLoss(true))
                            else navigateBack()
                        }
                    ) {
                        Icon(Icons.AutoMirrored.Outlined.ArrowBack, null)
                    }
                },
                actions = {
                    if (state.adding || state.editing) IconButton(
                        onClick = if (state.adding) model::add else model::update
                    ) {
                        Icon(Icons.Outlined.Check, null)
                    }
                    else {
                        LocalFocusManager.current.clearFocus(true)
                        var expanded by remember { mutableStateOf(false) }

                        IconButton({ expanded = true }) {
                            Icon(Icons.Outlined.MoreVert, null)
                        }
                        DropdownMenu(expanded, { expanded = false }) {
                            DropdownMenuItem(
                                onClick = model::setEditing,
                                text = {
                                    Text(
                                        text = stringResource(text_edit),
                                        style = MaterialTheme.typography.bodyLarge
                                    )
                                },
                            )
                            DropdownMenuItem(
                                text = {
                                    Text(
                                        text = stringResource(text_delete),
                                        style = MaterialTheme.typography.bodyLarge
                                    )
                                },
                                onClick = {
                                    model.onEvent(IntakeEvent.ShowDialogDelete)
                                    expanded = false
                                }
                            )
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
        LazyColumn(
            verticalArrangement = spacedBy(24.dp),
            contentPadding = PaddingValues(8.dp, values.calculateTopPadding(), 8.dp, 8.dp),
            modifier = Modifier.padding(top = 16.dp)
        ) {
            item { MedicineInfo(state.medicine) }

            item { SchemaType(state, model::onEvent) }

            if (state.schemaType == SchemaTypes.BY_DAYS) item {
                DaysPicker(state, model::onEvent)
            }

            if (state.schemaType != SchemaTypes.BY_DAYS) item {
                Interval(state, model::onEvent)
            }

            if (state.schemaType != SchemaTypes.INDEFINITELY) item {
                Period(state, model::onEvent)
            }

            item { Amount(state, model::onEvent) }
            item { Food(state, model::onEvent) }
            item { Time(state, model::onEvent) }
            item { Extra(state, model::onEvent) }
        }
    }

    when {
        state.showDialogDescription -> DialogDescription(state, model::onEvent)
        state.showDialogDataLoss -> DialogDataLoss(model::onEvent, navigateBack)
        state.showDialogDelete -> DialogDelete(
            text = text_confirm_deletion_int,
            cancel = { model.onEvent(IntakeEvent.ShowDialogDelete) },
            confirm = { model.delete(); navigateBack() }
        )

        state.showDateRangePicker -> DateRangePicker(
            startDate = state.startDate,
            finalDate = state.finalDate,
            onDismiss = { model.onEvent(IntakeEvent.ShowDateRangePicker) },
            onRangeSelected = { model.onEvent(IntakeEvent.SetPeriod(it)) }
        )

        state.showTimePicker -> TimePickerDialog(
            onCancel = { model.onEvent(IntakeEvent.ShowTimePicker()) },
            onConfirm = { model.onEvent(IntakeEvent.SetPickedTime) }
        ) {
            TimePicker(state.pickedTime[state.timePickerIndex].picker)
        }
    }
}

@Composable
private fun MedicineInfo(medicine: Medicine) =
    Row(
        horizontalArrangement = spacedBy(12.dp),
        modifier = Modifier
            .fillMaxWidth()
            .height(144.dp)
    ) {
        MedicineImage(
            image = medicine.image,
            modifier = Modifier
                .fillMaxHeight()
                .width(128.dp)
                .border(1.dp, MaterialTheme.colorScheme.onSurface, MaterialTheme.shapes.medium)
                .padding(8.dp)
        )
        Column(Modifier.verticalScroll(rememberScrollState()), Arrangement.SpaceAround) {
            Column {
                Text(
                    text = stringResource(text_medicine_product_name),
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.W400)
                )

                Text(
                    text = medicine.nameAlias.ifEmpty { medicine.productName },
                    softWrap = false,
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.W600),
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState())
                )
            }
            Column {
                Text(
                    text = stringResource(text_medicine_form),
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.W400)
                )
                Text(
                    text = formName(medicine.prodFormNormName).ifEmpty { stringResource(text_unspecified) },
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.W600)
                )
            }
            Column {
                Text(
                    text = stringResource(text_exp_date),
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.W400)
                )
                Text(
                    text = toExpDate(medicine.expDate).ifEmpty { stringResource(text_unspecified) },
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.W600)
                )
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
                  if (!it) IconButton(
                        onClick = { event(IntakeEvent.ShowSchemaTypePicker) }
                    ) {
                        ExposedDropdownMenuDefaults.TrailingIcon(state.showSchemaTypePicker)
                    }
                }
            }
        )
        ExposedDropdownMenu(state.showSchemaTypePicker, {}) {
            SchemaTypes.entries.forEach {
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
private fun DaysPicker(state: IntakeState, event: (IntakeEvent) -> Unit, locale: Locale = LOCALE) =
    OutlinedCard {
        ListItem(
            headlineContent = { Text(stringResource(R.string.text_repeat)) },
            supportingContent = {
                Text(
                    if (state.pickedDays.size == DayOfWeek.entries.size) stringResource(R.string.text_every_day)
                    else state.pickedDays.joinToString { it.getDisplayName(TextStyle.SHORT, locale) }
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
                        text = day.getDisplayName(TextStyle.NARROW, locale),
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
            headlineContent = { Text(stringResource(intake_text_amount)) },
            supportingContent = {
                Text(
                    stringResource(
                        R.string.intake_text_in_stock,
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
                Text(
                    stringResource(if (state.sameAmount) R.string.text_on else R.string.text_off)
                )
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
                        placeholder = { Text(stringResource(text_empty)) },
                        suffix = { Text(stringResource(state.doseType)) },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        colors = TextFieldDefaults.colors().copy(
                            focusedContainerColor = ListItemDefaults.containerColor,
                            unfocusedContainerColor = ListItemDefaults.containerColor,
                            focusedIndicatorColor = Color.Transparent,
                            unfocusedIndicatorColor = Color.Transparent
                        ),
                        visualTransformation = {
                            TransformedText(
                                AnnotatedString(it.text.replace('.', ',')),
                                OffsetMapping.Identity
                            )
                        }
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
                headlineContent = {
                    Text(stringResource(intake_text_interval))
                },
                supportingContent = {
                    Text(stringResource(state.intervalType.title))
                },
                trailingContent = state.default.let {
                    {
                        if (!it) IconButton(
                            onClick = { event(IntakeEvent.ShowIntervalTypePicker) }
                        ) {
                            ExposedDropdownMenuDefaults.TrailingIcon(state.showIntervalTypePicker)
                        }
                    }
                }
            )
            ExposedDropdownMenu(state.showIntervalTypePicker, {}) {
                Intervals.entries.forEach {
                    DropdownMenuItem(
                        text = { Text(stringResource(it.title)) },
                        contentPadding = ExposedDropdownMenuDefaults.ItemContentPadding,
                        onClick = { event(IntakeEvent.SetInterval(it)) }
                    )
                }
            }
        }

        if (state.intervalType == Intervals.CUSTOM) {
            HorizontalDivider()
            ListItem(
                leadingContent = { Text(stringResource(text_every)) },
                headlineContent = {
                    OutlinedTextField(
                        value = state.interval,
                        onValueChange = { event(IntakeEvent.SetInterval(it)) },
                        readOnly = state.default,
                        placeholder = { Text(stringResource(text_empty)) },
                        suffix = { Text(stringResource(text_days_short)) },
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
    OutlinedCard(Modifier.animateContentSize()){
        Row(Modifier.fillMaxWidth(), Arrangement.Start, CenterVertically) {
            ExposedDropdownMenuBox(state.showPeriodTypePicker, {}, Modifier.weight(1f)) {
                ListItem(
                    modifier = Modifier.menuAnchor(MenuAnchorType.PrimaryNotEditable),
                    headlineContent = { Text(stringResource(intake_text_period)) },
                    supportingContent = { Text(stringResource(state.periodType.title)) },
                    trailingContent = state.default.let {
                        {
                            if (!it) IconButton(
                                onClick = { event(IntakeEvent.ShowPeriodTypePicker) }
                            ) {
                                ExposedDropdownMenuDefaults.TrailingIcon(state.showPeriodTypePicker)
                            }
                        }
                    }
                )
                ExposedDropdownMenu(state.showPeriodTypePicker, {}) {
                    Periods.entries.forEach {
                        DropdownMenuItem(
                            text = { Text(stringResource(it.title)) },
                            contentPadding = ExposedDropdownMenuDefaults.ItemContentPadding,
                            onClick = { event(IntakeEvent.SetPeriod(it)) }
                        )
                    }
                }
            }

            if (state.periodType == Periods.OTHER)
                TextField(
                    value = state.period,
                    onValueChange = { event(IntakeEvent.SetPeriod(it)) },
                    readOnly = state.default,
                    textStyle = MaterialTheme.typography.titleMedium,
                    placeholder = { Text(stringResource(text_empty)) },
                    leadingIcon = { Icon(painterResource(R.drawable.vector_period), null) },
                    suffix = { Text(stringResource(text_days_short)) },
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

        if (state.periodType != Periods.INDEFINITE) {
            HorizontalDivider()
            Row {
                ListItem(
                    headlineContent = { Text(stringResource(R.string.intake_text_start)) },
                    supportingContent = {
                        Text(
                            softWrap = false,
                            overflow = TextOverflow.Visible,
                            text = if (state.periodType == Periods.PICK)
                                state.startDate.ifEmpty { stringResource(R.string.text_not_selected) }
                            else state.startDate.ifEmpty { stringResource(R.string.text_today) }
                        )
                    },
                    trailingContent = state.let {
                        {
                            if (!it.default && it.periodType == Periods.PICK)
                                Icon(Icons.AutoMirrored.Outlined.KeyboardArrowRight, null)
                        }
                    },
                    modifier = Modifier
                        .weight(1f)
                        .clickable(
                            enabled = !state.default && state.periodType == Periods.PICK,
                            onClick = { event(IntakeEvent.ShowDateRangePicker) }
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
                            text = if (state.periodType == Periods.PICK)
                                state.finalDate.ifEmpty { stringResource(R.string.text_not_selected) }
                            else state.finalDate.ifEmpty { stringResource(R.string.text_tomorrow) }
                        )
                    },
                    trailingContent = state.let {
                        {
                            if (!it.default && it.periodType == Periods.PICK)
                                Icon(Icons.AutoMirrored.Outlined.KeyboardArrowRight, null)
                        }
                    },
                    modifier = Modifier
                        .weight(1f)
                        .clickable(
                            enabled = !state.default && state.periodType == Periods.PICK,
                            onClick = { event(IntakeEvent.ShowDateRangePicker) }
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
        headlineContent = { Text(stringResource(intake_text_food)) },
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
                FoodTypes.entries.forEach { type ->
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
            headlineContent = { Text(stringResource(intake_text_time)) },
            supportingContent = {
                Text(
                    pluralStringResource(
                        R.plurals.intake_times_a_day,
                        state.pickedTime.size,
                        state.pickedTime.size
                    )
                )
            }
        )

        HorizontalDivider()

        state.pickedTime.forEachIndexed { index, amountTime ->
            Row {
                ListItem(
                    headlineContent = { Text(stringResource(placeholder_time, index + 1)) },
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
                                    else ImageVector.vectorResource(vector_remove)
                                )
                            }
                        }
                    }
                )

                if (!state.sameAmount)
                    TextField(
                        value = amountTime.amount,
                        onValueChange = { event(IntakeEvent.SetAmount(it, index)) },
                        readOnly = state.default,
                        isError = state.amountError != null && amountTime.amount.isEmpty(),
                        placeholder = { Text(stringResource(text_empty)) },
                        suffix = { Text(stringResource(state.doseType)) },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        modifier = Modifier
                            .height(72.dp)
                            .weight(0.6f),
                        colors = TextFieldDefaults.colors().copy(
                            focusedIndicatorColor = Color.Transparent,
                            unfocusedIndicatorColor = Color.Transparent,
                            focusedContainerColor = ListItemDefaults.containerColor,
                            unfocusedContainerColor = ListItemDefaults.containerColor,

                        ),
                        visualTransformation = {
                            TransformedText(
                                AnnotatedString(it.text.replace('.', ',')),
                                OffsetMapping.Identity
                            )
                        }
                    )
            }
        }
    }

@Composable
private fun Extra(state: IntakeState, event: (IntakeEvent) -> Unit) = OutlinedCard {
    ListItem(
        headlineContent = { Text(stringResource(intake_text_extra)) },
        supportingContent = {
            Text(
                stringResource(
                    R.string.text_selected_of,
                    state.selectedExtras.size,
                    IntakeExtras.entries.size
                )
            )
        }
    )

    HorizontalDivider()

    IntakeExtras.entries
        .filter { !(it == IntakeExtras.FULLSCREEN && !LocalContext.current.canUseFullScreenIntent()) }
        .forEach { extra ->
            ListItem(
                headlineContent = { Text(stringResource(extra.title)) },
                leadingContent = {
                    Checkbox(
                        onCheckedChange = null,
                        checked = when (extra) {
                            IntakeExtras.CANCELLABLE -> state.cancellable
                            IntakeExtras.FULLSCREEN -> state.fullScreen
                            IntakeExtras.NO_SOUND -> state.noSound
                            IntakeExtras.PREALARM -> state.preAlarm
                        }
                    )
                },
                trailingContent = {
                    IconButton(
                        onClick = { event(IntakeEvent.ShowDialogDescription(extra.description)) }
                    ) {
                        Icon(Icons.Outlined.Info, null)
                    }
                },
                modifier = Modifier.toggleable(
                    enabled = !state.default,
                    role = Role.Checkbox,
                    onValueChange = { event(IntakeEvent.SetIntakeExtra(extra)) },
                    value = when (extra) {
                        IntakeExtras.CANCELLABLE -> state.cancellable
                        IntakeExtras.FULLSCREEN -> state.fullScreen
                        IntakeExtras.NO_SOUND -> state.noSound
                        IntakeExtras.PREALARM -> state.preAlarm
                    }
                )
            )
        }
}

@Composable
private fun DialogDescription(state: IntakeState, event: (IntakeEvent) -> Unit) = AlertDialog(
    onDismissRequest = { event(IntakeEvent.ShowDialogDescription()) },
    title = { Text(stringResource(text_medicine_description)) },
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
            onClick = { event(IntakeEvent.ShowDialogDescription()) }
        ) {
            Text(stringResource(text_dismiss))
        }
    }
)

@Composable
private fun DialogDataLoss(event: (IntakeEvent) -> Unit, navigateUp: () -> Unit) = AlertDialog(
    onDismissRequest = { event(IntakeEvent.ShowDialogDataLoss(false)) },
    confirmButton = { TextButton(navigateUp) { Text(stringResource(text_exit)) } },
    text = {
        Text(
            text = stringResource(text_not_saved_intake),
            style = MaterialTheme.typography.bodyLarge
        )
    },
    dismissButton = {
        TextButton(
            onClick = { event(IntakeEvent.ShowDialogDataLoss(false)) }
        ) {
            Text(stringResource(text_stay))
        }
    }
)