package ru.application.homemedkit.screens

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Clear
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedCard
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
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringArrayResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight.Companion.SemiBold
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.ramcosta.composedestinations.annotation.Destination
import com.ramcosta.composedestinations.annotation.RootGraph
import com.ramcosta.composedestinations.generated.destinations.IntakeScreenDestination
import ru.application.homemedkit.HomeMeds.Companion.database
import ru.application.homemedkit.R
import ru.application.homemedkit.R.array.interval_types_name
import ru.application.homemedkit.R.string.intake_card_text_from
import ru.application.homemedkit.R.string.intake_text_date
import ru.application.homemedkit.R.string.intake_text_not_taken
import ru.application.homemedkit.R.string.intake_text_quantity
import ru.application.homemedkit.R.string.intake_text_taken
import ru.application.homemedkit.R.string.intake_text_time
import ru.application.homemedkit.R.string.text_amount
import ru.application.homemedkit.R.string.text_cancel
import ru.application.homemedkit.R.string.text_edit
import ru.application.homemedkit.R.string.text_enter_product_name
import ru.application.homemedkit.R.string.text_medicine_amount_not_enough
import ru.application.homemedkit.R.string.text_medicine_deleted
import ru.application.homemedkit.R.string.text_medicine_product_name
import ru.application.homemedkit.R.string.text_no_intakes_found
import ru.application.homemedkit.R.string.text_notification_pick_action_first
import ru.application.homemedkit.R.string.text_save
import ru.application.homemedkit.R.string.text_status
import ru.application.homemedkit.data.dto.Alarm
import ru.application.homemedkit.data.dto.Intake
import ru.application.homemedkit.data.dto.IntakeTaken
import ru.application.homemedkit.helpers.BLANK
import ru.application.homemedkit.helpers.FORMAT_D_M
import ru.application.homemedkit.helpers.FORMAT_D_M_Y
import ru.application.homemedkit.helpers.FORMAT_H
import ru.application.homemedkit.helpers.ZONE
import ru.application.homemedkit.helpers.decimalFormat
import ru.application.homemedkit.helpers.formName
import ru.application.homemedkit.helpers.shortName
import ru.application.homemedkit.viewModels.IntakesViewModel
import java.time.Instant
import java.time.LocalDate
import java.time.LocalDateTime
import com.ramcosta.composedestinations.navigation.DestinationsNavigator as Navigator

@OptIn(ExperimentalMaterial3Api::class)
@Destination<RootGraph>
@Composable
fun IntakesScreen(navigator: Navigator) {
    val model = viewModel<IntakesViewModel>()
    val state by model.state.collectAsStateWithLifecycle()
    val intakes by model.intakes.collectAsStateWithLifecycle()
    val schedule by model.schedule.collectAsStateWithLifecycle()
    val taken by model.taken.collectAsStateWithLifecycle()

    if (state.showDialog) DialogTaken(model)
    BackHandler{}
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
            )
        }
    ) { values ->
        Column(Modifier.padding(top = values.calculateTopPadding())) {
            TabRow(state.tab) {
                model.tabs.forEachIndexed { index, tab ->
                    Tab(
                        selected = state.tab == index,
                        onClick = { model.pickTab(index) },
                        text = { Text(stringResource(tab)) }
                    )
                }
            }

            when (state.tab) {
                0 -> intakes.let { list ->
                    if (list.isEmpty()) Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(horizontal = 16.dp),
                        contentAlignment = Alignment.Center
                    ) { Text(stringResource(text_no_intakes_found), textAlign = TextAlign.Center) }
                    else LazyColumn(
                        state = state.stateA,
                        contentPadding = PaddingValues(4.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp),
                    ) { items(list) { IntakeList(it, navigator) } }
                }

                1 -> schedule.let { list ->
                    LazyColumn(
                        state = state.stateB,
                        contentPadding = PaddingValues(4.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp),
                    ) { items(list.size) { IntakeSchedule(list.entries.elementAt(it)) } }
                }

                2 -> taken.let { list ->
                    LazyColumn(
                        state = state.stateC,
                        contentPadding = PaddingValues(4.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp),
                        reverseLayout = true
                    ) { items(list.size) { IntakeTaken(model, list.entries.elementAt(it)) } }
                }
            }
        }
    }
}

@Composable
fun IntakeList(intake: Intake, navigator: Navigator) {
    val medicine = database.medicineDAO().getById(intake.medicineId)
    val startDate = stringResource(intake_card_text_from, intake.startDate)
    val count = intake.time.size
    val intervalName = if (count == 1) stringArrayResource(interval_types_name).let {
        when (intake.interval) {
            1 -> it[0]
            7 -> it[1]
            else -> it[2]
        }
    } else pluralStringResource(R.plurals.intakes_a_day, count, count)

    ListItem(
        colors = ListItemDefaults.colors(MaterialTheme.colorScheme.tertiaryContainer),
        headlineContent = { Text("$intervalName $startDate") },
        supportingContent = { Text(intake.time.joinToString(", ")) },
        leadingContent = { MedicineImage(medicine?.image ?: BLANK, Modifier.size(56.dp)) },
        modifier = Modifier
            .clip(MaterialTheme.shapes.medium)
            .clickable { navigator.navigate(IntakeScreenDestination(intakeId = intake.intakeId)) },
        overlineContent = {
            Text(
                text = shortName(medicine?.productName),
                softWrap = false,
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = SemiBold)
            )
        }
    )
}

@Composable
fun IntakeSchedule(data: Map.Entry<Long, List<Alarm>>) = OutlinedCard(
    colors = CardDefaults.cardColors(MaterialTheme.colorScheme.surfaceContainerLowest)
) {
    TextDate(data.key)
    data.value.forEachIndexed { index, (_, intakeId, trigger) ->
        val intake = database.intakeDAO().getById(intakeId)
        val medicine = database.medicineDAO().getById(intake?.medicineId ?: 0L)

        MedicineItem(
            medicine?.productName, medicine?.prodFormNormName, medicine?.doseType, intake?.amount,
            medicine?.image, trigger
        )
        if (index < data.value.size - 1) HorizontalDivider()
    }
}

@Composable
fun IntakeTaken(model: IntakesViewModel, data: Map.Entry<Long, List<IntakeTaken>>) = OutlinedCard(
    colors = CardDefaults.cardColors(MaterialTheme.colorScheme.surfaceContainerLowest)
) {
    TextDate(data.key)
    data.value.sortedBy { it.trigger }.forEachIndexed { index, value ->
        MedicineItem(
            value.productName, value.formName, value.doseType, value.amount, value.image,
            value.trigger, value.taken
        ) { model.showDialog(value) }
        if (index < data.value.size - 1) HorizontalDivider()
    }
}

@Composable
private fun DialogTaken(model: IntakesViewModel) {
    val intake by model.takenState.collectAsStateWithLifecycle()
    val zoned = Instant.ofEpochMilli(intake.trigger).atZone(ZONE)
    val medicine = database.medicineDAO().getById(intake.medicineId)
    val items = listOf(stringResource(intake_text_not_taken), stringResource(intake_text_taken))

    var selected by remember { mutableIntStateOf(if (intake.taken) 1 else 0) }

    AlertDialog(
        onDismissRequest = model::hideDialog,
        confirmButton = {
            TextButton(
                onClick = { model.setTaken(intake.takenId, selected == 1) },
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
                    label = { Text(stringResource(text_medicine_product_name)) }
                )
                OutlinedTextField(
                    value = zoned.format(FORMAT_D_M_Y),
                    onValueChange = {},
                    readOnly = true,
                    label = { Text(stringResource(intake_text_date)) }
                )
                OutlinedTextField(
                    value = zoned.format(FORMAT_H),
                    onValueChange = {},
                    readOnly = true,
                    label = { Text(stringResource(intake_text_time)) }
                )

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
                                selected = index == selected,
                                onClick = { selected = index },
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
    text = LocalDate.ofEpochDay(timestamp).let {
        it.format(if (it.year == LocalDate.now().year) FORMAT_D_M else FORMAT_D_M_Y)
    },
    modifier = Modifier.padding(12.dp),
    style = MaterialTheme.typography.titleLarge.copy(fontWeight = SemiBold)
)

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun MedicineItem(
    title: String?,
    formName: String?,
    doseType: String?,
    amount: Double?,
    image: String?,
    trigger: Long,
    taken: Boolean = true,
    showDialog: ( () -> Unit)? = null
) {
    ListItem(
        headlineContent = { Text(text = shortName(title), softWrap = false) },
        modifier = Modifier.combinedClickable(
            onClick = {},
            onLongClick = { if (showDialog != null) showDialog() }
        ),
        supportingContent = {
            Text(
                text = stringResource(
                    intake_text_quantity,
                    formName?.let { if (it.isEmpty()) stringResource(text_amount) else formName(it) } ?: BLANK,
                    decimalFormat(amount), doseType ?: BLANK
                )
            )
        },
        leadingContent = { MedicineImage(image ?: BLANK, Modifier.size(40.dp)) },
        trailingContent = {
            Text(
                text = LocalDateTime.ofInstant(Instant.ofEpochMilli(trigger), ZONE).format(FORMAT_H),
                style = MaterialTheme.typography.labelLarge
            )
        },
        colors = ListItemDefaults.colors(
            if (taken) MaterialTheme.colorScheme.surfaceContainerLowest
            else MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.7f)
        )
    )
}