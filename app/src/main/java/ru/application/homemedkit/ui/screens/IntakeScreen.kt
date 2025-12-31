@file:OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterial3ExpressiveApi::class)

package ru.application.homemedkit.ui.screens

import androidx.activity.compose.BackHandler
import androidx.annotation.StringRes
import androidx.compose.animation.AnimatedVisibility
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
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.text.TextAutoSize
import androidx.compose.foundation.text.input.InputTransformation
import androidx.compose.foundation.text.input.TextFieldLineLimits
import androidx.compose.foundation.text.input.maxLength
import androidx.compose.foundation.text.input.rememberTextFieldState
import androidx.compose.foundation.text.input.then
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Checkbox
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.ExposedDropdownMenuAnchorType
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.FilledTonalIconButton
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.ListItem
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MenuDefaults
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
import androidx.compose.material3.TimePicker
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.intl.Locale
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import kotlinx.coroutines.flow.collectLatest
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
import ru.application.homemedkit.ui.elements.TextFieldListItemColors
import ru.application.homemedkit.ui.elements.TopBarActions
import ru.application.homemedkit.ui.elements.VectorIcon
import ru.application.homemedkit.utils.DaysInputTransformation
import ru.application.homemedkit.utils.DecimalAmountInputTransformation
import ru.application.homemedkit.utils.DecimalAmountOutputTransformation
import ru.application.homemedkit.utils.EmptyInteractionSource
import ru.application.homemedkit.utils.Formatter
import ru.application.homemedkit.utils.enums.FoodType
import ru.application.homemedkit.utils.enums.IntakeExtra
import ru.application.homemedkit.utils.enums.Interval
import ru.application.homemedkit.utils.enums.Period
import ru.application.homemedkit.utils.enums.SchemaType
import ru.application.homemedkit.utils.extensions.canUseFullScreenIntent
import ru.application.homemedkit.utils.extensions.defined
import ru.application.homemedkit.utils.extensions.intake
import java.time.DayOfWeek
import java.time.format.TextStyle

@Composable
fun IntakeScreen(model: IntakeViewModel, onBack: () -> Unit) {
    val state by model.state.collectAsStateWithLifecycle()

    BackHandler(!state.isFirstLaunch) {
        if (state.default) onBack()
        else model.onEvent(IntakeEvent.ShowDialogDataLoss(true))
    }
    if (state.isFirstLaunch) PermissionsScreen(onBack, model::setExitFirstLaunch)
    else Scaffold(
        topBar = {
            TopAppBar(
                title = {},
                subtitle = {},
                navigationIcon = {
                    NavigationIcon {
                        if (state.default) onBack()
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
        state.showDialogDataLoss -> DialogDataLoss(model::onEvent, onBack)
        state.showDialogDelete -> DialogDelete(
            text = R.string.text_confirm_deletion_int,
            onCancel = { model.onEvent(IntakeEvent.ShowDialogDelete) },
            onConfirm = { model.delete(onBack) }
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
            LocalInfo(R.string.text_medicine_form, Formatter.formFormat(medicine.prodFormNormName))
            LocalInfo(R.string.text_exp_date, Formatter.toExpDate(medicine.expDate))
        }
    }
}

@Composable
private fun SchemaType(state: IntakeState, event: (IntakeEvent) -> Unit) = OutlinedCard {
    ExposedDropdownMenuBox(state.showSchemaTypePicker, {}) {
        ListItem(
            modifier = Modifier.menuAnchor(ExposedDropdownMenuAnchorType.PrimaryNotEditable),
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
                    onClick = { event(IntakeEvent.SetSchemaType(it)) },
                    text = { Text(stringResource(it.title)) },
                    shape = MenuDefaults.standaloneItemShape
                )
            }
        }
    }
}

@Composable
private fun DaysPicker(state: IntakeState, event: (IntakeEvent) -> Unit) = OutlinedCard {
    val locale = Locale.current.platformLocale

    ListItem(
        headlineContent = { Text(stringResource(R.string.text_repeat)) },
        supportingContent = {
            Text(
                text = if (state.pickedDays.size == DayOfWeek.entries.size) stringResource(R.string.text_every_day)
                else state.pickedDays.joinToString {
                    it.getDisplayName(TextStyle.SHORT, locale)
                }
            )
        }
    )
    Row(
        horizontalArrangement = Arrangement.SpaceAround,
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .fillMaxWidth()
            .background(ListItemDefaults.containerColor)
    ) {
        DayOfWeek.entries.sorted().forEach { day ->
            FilterChip(
                shape = CircleShape,
                selected = day in state.pickedDays,
                onClick = { if (!state.default) event(IntakeEvent.SetPickedDay(day)) },
                label = {
                    Text(
                        text = day.getDisplayName(TextStyle.NARROW_STANDALONE, locale),
                        textAlign = TextAlign.Center
                    )
                }
            )
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
                    text = stringResource(
                        id = R.string.intake_text_in_stock_params,
                        formatArgs = arrayOf(
                            Formatter.decimalFormat(state.amountStock),
                            stringResource(state.doseType)
                        )
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
            val textFieldState = rememberTextFieldState(state.pickedTime.first().amount)

            LaunchedEffect(textFieldState) {
                snapshotFlow { textFieldState.text.toString() }.collectLatest {
                    event(IntakeEvent.SetAmount(it))
                }
            }

            HorizontalDivider()
            ListItem(
                leadingContent = { VectorIcon(R.drawable.vector_medicine) },
                headlineContent = {
                    TextField(
                        state = textFieldState,
                        readOnly = state.default,
                        isError = state.amountError != null && state.pickedTime.first().amount.isEmpty(),
                        placeholder = { Text(stringResource(R.string.text_empty)) },
                        suffix = { Text(stringResource(state.doseType)) },
                        lineLimits = TextFieldLineLimits.SingleLine,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        outputTransformation = DecimalAmountOutputTransformation,
                        colors = TextFieldListItemColors,
                        inputTransformation = InputTransformation
                            .maxLength(8)
                            .then(DecimalAmountInputTransformation),
                    )
                }
            )
        }
    }

@Composable
private fun Interval(state: IntakeState, event: (IntakeEvent) -> Unit) =
    OutlinedCard(Modifier.animateContentSize()) {
        ExposedDropdownMenuBox(state.showIntervalTypePicker, {}) {
            ListItem(
                modifier = Modifier.menuAnchor(ExposedDropdownMenuAnchorType.PrimaryNotEditable),
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
                        onClick = { event(IntakeEvent.SetInterval(it)) },
                        text = { Text(stringResource(it.title)) },
                        shape = MenuDefaults.standaloneItemShape
                    )
                }
            }
        }

        if (state.intervalType == Interval.CUSTOM) {
            val textFieldState = rememberTextFieldState(state.interval)

            LaunchedEffect(textFieldState) {
                snapshotFlow { textFieldState.text.toString() }.collectLatest {
                    event(IntakeEvent.SetInterval(it))
                }
            }

            HorizontalDivider()
            ListItem(
                leadingContent = { Text(stringResource(R.string.text_every)) },
                headlineContent = {
                    OutlinedTextField(
                        state = textFieldState,
                        readOnly = state.default,
                        isError = state.intervalError != null,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        placeholder = { Text(stringResource(R.string.text_empty)) },
                        suffix = { Text(stringResource(R.string.text_days_short)) },
                        lineLimits = TextFieldLineLimits.SingleLine,
                        colors = TextFieldListItemColors,
                        inputTransformation = InputTransformation
                            .maxLength(2)
                            .then(DaysInputTransformation),
                    )
                },
                colors = ListItemDefaults.colors(
                    containerColor = if (state.intervalError == null) ListItemDefaults.containerColor
                    else MaterialTheme.colorScheme.errorContainer
                )
            )
        }
    }

@Composable
private fun Period(state: IntakeState, event: (IntakeEvent) -> Unit) =
    OutlinedCard(Modifier.animateContentSize()) {
        Row(Modifier.height(IntrinsicSize.Max), verticalAlignment = Alignment.CenterVertically) {
            ExposedDropdownMenuBox(state.showPeriodTypePicker, {}, Modifier.weight(1f)) {
                ListItem(
                    modifier = Modifier.menuAnchor(ExposedDropdownMenuAnchorType.PrimaryNotEditable),
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
                            onClick = { event(IntakeEvent.SetPeriod(it)) },
                            text = { Text(stringResource(it.title)) },
                            shape = MenuDefaults.standaloneItemShape
                        )
                    }
                }
            }

            if (state.periodType == Period.OTHER) {
                val textFieldState = rememberTextFieldState(state.period)

                LaunchedEffect(textFieldState) {
                    snapshotFlow { textFieldState.text.toString() }.collectLatest {
                        event(IntakeEvent.SetPeriod(it))
                    }
                }

                TextField(
                    state = textFieldState,
                    readOnly = state.default,
                    isError = state.periodError != null,
                    lineLimits = TextFieldLineLimits.SingleLine,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    colors = TextFieldListItemColors,
                    suffix = { Text(stringResource(R.string.text_days_short)) },
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight(),
                    placeholder = {
                        Text(
                            text = stringResource(R.string.text_empty),
                            maxLines = 1,
                            autoSize = TextAutoSize.StepBased(
                                minFontSize = 8.sp,
                                maxFontSize = MaterialTheme.typography.bodyLarge.fontSize,
                                stepSize = (0.25).sp
                            )
                        )
                    },
                    prefix = {
                        VectorIcon(
                            icon = R.drawable.vector_period,
                            modifier = Modifier.padding(end = 12.dp)
                        )
                    },
                    inputTransformation = InputTransformation
                        .maxLength(3)
                        .then(DaysInputTransformation),
                )
            }
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
                            text = if (state.periodType == Period.PICK) {
                                state.startDate.ifEmpty { stringResource(R.string.text_not_selected) }
                            } else {
                                state.startDate.ifEmpty { stringResource(R.string.text_today) }
                            }
                        )
                    },
                    trailingContent = state.let {
                        {
                            if (!it.default && it.periodType in Period.entries.defined)
                                VectorIcon(R.drawable.vector_keyboard_arrow_right)
                        }
                    },
                    modifier = Modifier
                        .weight(1f)
                        .clickable(
                            enabled = !state.default && state.periodType in Period.entries.defined,
                            onClick = { event(IntakeEvent.ShowDatePicker) }
                        ),
                    colors = ListItemDefaults.colors(
                        containerColor = if (state.periodError != null && state.startDate.isEmpty()) {
                            MaterialTheme.colorScheme.errorContainer
                        } else {
                            ListItemDefaults.containerColor
                        }
                    )
                )
                ListItem(
                    headlineContent = { Text(stringResource(R.string.intake_text_finish)) },
                    supportingContent = {
                        Text(
                            softWrap = false,
                            overflow = TextOverflow.Visible,
                            text = if (state.periodType == Period.PICK) {
                                state.finalDate.ifEmpty { stringResource(R.string.text_not_selected) }
                            } else {
                                state.finalDate.ifEmpty { stringResource(R.string.text_tomorrow) }
                            }
                        )
                    },
                    trailingContent = state.let {
                        {
                            if (!it.default && it.periodType == Period.PICK)
                                VectorIcon(R.drawable.vector_keyboard_arrow_right)
                        }
                    },
                    modifier = Modifier
                        .weight(1f)
                        .clickable(
                            enabled = !state.default && state.periodType == Period.PICK,
                            onClick = { event(IntakeEvent.ShowDatePicker) }
                        ),
                    colors = ListItemDefaults.colors(
                        containerColor = if (state.periodError != null && state.finalDate.isEmpty()) {
                            MaterialTheme.colorScheme.errorContainer
                        } else {
                            ListItemDefaults.containerColor
                        }
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
                text = stringResource(
                    id = if (state.foodType == -1) R.string.text_not_selected
                    else R.string.text_selected
                )
            )
        }
    )
    ListItem(
        headlineContent = {
            Row(Modifier.fillMaxWidth(), Arrangement.SpaceAround, Alignment.CenterVertically) {
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
            supportingContent = {
                Text(
                    text = pluralStringResource(
                        id = R.plurals.intake_times_a_day,
                        count = state.pickedTime.size,
                        formatArgs = arrayOf(state.pickedTime.size)
                    )
                )
            },
            trailingContent = {
                AnimatedVisibility(!state.default) {
                    Row {
                        FilledTonalIconButton(
                            enabled = state.pickedTime.size > 1,
                            onClick = { event(IntakeEvent.DecTime) },
                            content = { VectorIcon(R.drawable.vector_remove) },
                            shapes = IconButtonDefaults.shapes()
                        )

                        FilledTonalIconButton(
                            onClick = { event(IntakeEvent.IncTime) },
                            content = { VectorIcon(R.drawable.vector_add) },
                            shapes = IconButtonDefaults.shapes()
                        )
                    }
                }
            }
        )

        HorizontalDivider()

        state.pickedTime.forEachIndexed { index, amountTime ->
            Row {
                ListItem(
                    onClick = { if (!state.default) event(IntakeEvent.ShowTimePicker(index)) },
                    modifier = Modifier.weight(1f),
                    interactionSource = if (state.default) EmptyInteractionSource else null,
                    verticalAlignment = Alignment.CenterVertically,
                    leadingContent = { VectorIcon(R.drawable.vector_time) },
                    content = {
                        Text(
                            text = stringResource(R.string.placeholder_time, index + 1),
                            overflow = TextOverflow.Ellipsis,
                            maxLines = 1,
                            autoSize = TextAutoSize.StepBased(
                                minFontSize = 8.sp,
                                maxFontSize = MaterialTheme.typography.bodyLarge.fontSize,
                                stepSize = (0.25).sp
                            )
                        )
                    },
                    supportingContent = {
                        Text(
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            autoSize = TextAutoSize.StepBased(
                                minFontSize = 8.sp,
                                maxFontSize = MaterialTheme.typography.bodyMedium.fontSize,
                                stepSize = (0.25).sp
                            ),
                            text = amountTime.time.ifEmpty {
                                stringResource(R.string.text_not_selected)
                            }
                        )
                    },
                    colors = ListItemDefaults.colors(
                        containerColor = if (state.timesError != null && amountTime.time.isEmpty()) {
                            MaterialTheme.colorScheme.errorContainer
                        } else {
                            ListItemDefaults.containerColor
                        }
                    )
                )

                if (!state.sameAmount) {
                    val textFieldState = rememberTextFieldState(amountTime.amount)

                    LaunchedEffect(textFieldState) {
                        snapshotFlow { textFieldState.text.toString() }.collectLatest {
                            event(IntakeEvent.SetAmount(it, index))
                        }
                    }

                    ListItem(
                        modifier = Modifier.weight(1f),
                        headlineContent = {
                            TextField(
                                state = textFieldState,
                                modifier = Modifier.fillMaxHeight(),
                                readOnly = state.default,
                                isError = state.amountError != null && amountTime.amount.isEmpty(),
                                placeholder = { Text(stringResource(R.string.text_empty)) },
                                suffix = { Text(stringResource(state.doseType)) },
                                lineLimits = TextFieldLineLimits.SingleLine,
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                                outputTransformation = DecimalAmountOutputTransformation,
                                colors = TextFieldListItemColors,
                                inputTransformation = InputTransformation
                                    .maxLength(8)
                                    .then(DecimalAmountInputTransformation)
                            )
                        },
                        colors = ListItemDefaults.colors(
                            containerColor = if (state.amountError != null && amountTime.amount.isEmpty()) {
                                MaterialTheme.colorScheme.errorContainer
                            } else {
                                ListItemDefaults.containerColor
                            }
                        )
                    )
                }
            }
        }
    }

@Composable
private fun Extra(state: IntakeState, event: (IntakeEvent) -> Unit) {
    val context = LocalContext.current

    val extrasFiltered = remember {
        IntakeExtra.entries.filter { extra ->
            !(extra == IntakeExtra.FULLSCREEN && !context.canUseFullScreenIntent())
        }
    }

    val extraAssociated = mapOf(
        IntakeExtra.CANCELLABLE to state.cancellable,
        IntakeExtra.FULLSCREEN to state.fullScreen,
        IntakeExtra.NO_SOUND to state.noSound,
        IntakeExtra.PREALARM to state.preAlarm,
    )

    OutlinedCard {
        ListItem(
            headlineContent = { Text(stringResource(R.string.intake_text_extra)) },
            supportingContent = {
                Text(
                    text = stringResource(
                        id = R.string.text_selected_of,
                        formatArgs = arrayOf(state.selectedExtras.size, extrasFiltered.size)
                    )
                )
            }
        )

        HorizontalDivider()

        extrasFiltered.forEach { extra ->
            ListItem(
                checked = extraAssociated.getOrDefault(extra, false),
                onCheckedChange = { if (!state.default) event(IntakeEvent.SetIntakeExtra(extra)) },
                shapes = ListItemDefaults.shapes(RectangleShape, RectangleShape),
                interactionSource = if (state.default) EmptyInteractionSource else null,
                content = {
                    Text(
                        text = stringResource(extra.title),
                        overflow = TextOverflow.Ellipsis,
                        maxLines = 2
                    )
                },
                leadingContent = {
                    Checkbox(
                        onCheckedChange = null,
                        checked = extraAssociated.getOrDefault(extra, false)
                    )
                },
                trailingContent = {
                    IconButton(
                        onClick = { event(IntakeEvent.ShowDialogDescription(extra.description)) },
                        content = { VectorIcon(R.drawable.vector_info) }
                    )
                }
            )
        }
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