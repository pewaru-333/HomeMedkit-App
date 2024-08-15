package ru.application.homemedkit.fragments

import android.app.AlarmManager
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Clear
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.rememberAsyncImagePainter
import com.ramcosta.composedestinations.annotation.Destination
import com.ramcosta.composedestinations.annotation.RootGraph
import com.ramcosta.composedestinations.generated.destinations.IntakeScreenDestination
import com.ramcosta.composedestinations.navigation.DestinationsNavigator
import ru.application.homemedkit.R
import ru.application.homemedkit.R.drawable.vector_type_unknown
import ru.application.homemedkit.R.string.intake_card_text_from
import ru.application.homemedkit.R.string.intake_text_quantity
import ru.application.homemedkit.R.string.text_amount
import ru.application.homemedkit.R.string.text_enter_product_name
import ru.application.homemedkit.R.string.text_no_intakes_found
import ru.application.homemedkit.activities.HomeMeds.Companion.database
import ru.application.homemedkit.databaseController.Alarm
import ru.application.homemedkit.databaseController.Intake
import ru.application.homemedkit.helpers.DateHelper.FORMAT_D_H
import ru.application.homemedkit.helpers.DateHelper.FORMAT_D_M
import ru.application.homemedkit.helpers.DateHelper.FORMAT_D_M_Y
import ru.application.homemedkit.helpers.DateHelper.FORMAT_H
import ru.application.homemedkit.helpers.DateHelper.FORMAT_S
import ru.application.homemedkit.helpers.DateHelper.ZONE
import ru.application.homemedkit.helpers.ICONS_MED
import ru.application.homemedkit.helpers.TYPE
import ru.application.homemedkit.helpers.decimalFormat
import ru.application.homemedkit.helpers.formName
import ru.application.homemedkit.helpers.intervalName
import ru.application.homemedkit.helpers.shortName
import ru.application.homemedkit.viewModels.IntakesViewModel
import java.io.File
import java.time.Instant
import java.time.LocalDate
import java.time.LocalDateTime

@OptIn(ExperimentalMaterial3Api::class)
@Destination<RootGraph>
@Composable
fun IntakesScreen(navigator: DestinationsNavigator) {
    val model = viewModel<IntakesViewModel>()
    val state by model.state.collectAsStateWithLifecycle()

    LaunchedEffect(Unit) { if (state.search.isEmpty()) model.getAll() }

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
                0 -> state.intakes.let { list ->
                    if (list.isNotEmpty()) LazyColumn(
                        state = state.stateOne,
                        contentPadding = PaddingValues(horizontal = 16.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp),
                    ) { items(list) { IntakeList(it, navigator) } }
                    else Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(horizontal = 16.dp),
                        contentAlignment = Alignment.Center
                    ) { Text(stringResource(text_no_intakes_found), textAlign = TextAlign.Center) }
                }

                1 -> getTriggers(state.intakes, false).let { list ->
                    LazyColumn(
                        state = state.stateTwo,
                        contentPadding = PaddingValues(horizontal = 16.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp),
                    ) { items(list.size) { IntakeSchedule(list.entries.elementAt(it)) } }
                }

                2 -> getTriggers(state.intakes, true).let { list ->
                    LazyColumn(
                        state = state.stateThree,
                        contentPadding = PaddingValues(horizontal = 16.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp),
                        reverseLayout = true
                    ) { items(list.size) { IntakeSchedule(list.entries.elementAt(it)) } }
                }
            }
        }
    }
}

private fun getTriggers(filtered: List<Intake>, taken: Boolean): Map<Long, List<Alarm>> {
    val triggers = ArrayList<Alarm>()

    for (intake in filtered) {
        if (intake.time.size == 1) {
            var milliS =
                LocalDateTime.parse("${intake.startDate} ${intake.time[0]}", FORMAT_D_H)
                    .toInstant(ZONE).toEpochMilli()
            val milliF =
                LocalDateTime.parse("${intake.finalDate} ${intake.time[0]}", FORMAT_D_H)
                    .toInstant(ZONE).toEpochMilli()

            while (milliS <= milliF) {
                triggers.add(Alarm(intakeId = intake.intakeId, trigger = milliS))
                milliS += intake.interval * AlarmManager.INTERVAL_DAY
            }
        } else {
            var localS = LocalDate.parse(intake.startDate, FORMAT_S)
            val localF = LocalDate.parse(intake.finalDate, FORMAT_S)

            var milliS = LocalDateTime.of(localS, intake.time.first())
            val milliF = LocalDateTime.of(localF, intake.time.last())

            while (milliS <= milliF) {
                for (time in intake.time) {
                    val millis =
                        LocalDateTime.of(localS, time).atOffset(ZONE).toInstant()
                            .toEpochMilli()
                    triggers.add(Alarm(intakeId = intake.intakeId, trigger = millis))
                }
                localS = localS.plusDays(1)
                milliS = milliS.plusDays(1)
            }
        }
    }

    if (taken) triggers.sortByDescending { it.trigger }
    else triggers.sortBy { it.trigger }

    return if (taken)
        triggers.filter { it.trigger < System.currentTimeMillis() }
            .groupBy {
                Instant.ofEpochMilli(it.trigger).atZone(ZONE).toLocalDate().toEpochDay()
            }
    else
        triggers.filter { it.trigger > System.currentTimeMillis() }
            .groupBy {
                Instant.ofEpochMilli(it.trigger).atZone(ZONE).toLocalDate().toEpochDay()
            }
}

@Composable
fun IntakeList(intake: Intake, navigator: DestinationsNavigator) {
    val medicine = database.medicineDAO().getByPK(intake.medicineId)!!
    val shortName = shortName(medicine.productName)
    val image = medicine.image
    val icon = when {
        image.contains(TYPE) -> ICONS_MED[image]
        image.isEmpty() -> vector_type_unknown
        else -> File(LocalContext.current.filesDir, image).run {
            if (exists()) this else vector_type_unknown
        }
    }
    val startDate = stringResource(intake_card_text_from, intake.startDate)
    val count = intake.time.size
    val intervalName = if (count == 1) intervalName(LocalContext.current, intake.interval)
    else pluralStringResource(R.plurals.intakes_a_day, count, count)

    ListItem(
        headlineContent = { Text("$intervalName $startDate") },
        modifier = Modifier
            .clickable { navigator.navigate(IntakeScreenDestination(intakeId = intake.intakeId)) }
            .padding(vertical = 8.dp)
            .clip(MaterialTheme.shapes.medium),
        overlineContent = {
            Text(
                text = shortName,
                fontWeight = FontWeight.Bold,
                overflow = TextOverflow.Clip,
                softWrap = false,
                style = MaterialTheme.typography.titleLarge
            )
        },
        supportingContent = { Text(intake.time.joinToString(", ")) },
        leadingContent = {
            Image(
                painter = rememberAsyncImagePainter(icon),
                contentDescription = null,
                modifier = Modifier
                    .size(64.dp)
                    .offset(y = 4.dp)
            )
        },
        colors = ListItemDefaults.colors(MaterialTheme.colorScheme.tertiaryContainer)
    )
}

@Composable
fun IntakeSchedule(data: Map.Entry<Long, List<Alarm>>) =
    Column(Modifier.padding(vertical = 8.dp), Arrangement.spacedBy(12.dp)) {
        Text(
            text = LocalDate.ofEpochDay(data.key).let {
                it.format(if (it.year == LocalDate.now().year) FORMAT_D_M else FORMAT_D_M_Y)
            },
            style = MaterialTheme.typography.titleLarge.copy(
                color = MaterialTheme.colorScheme.onSurface,
                fontWeight = FontWeight.SemiBold
            )
        )
        data.value.forEach { alarm ->
            val intake = database.intakeDAO().getByPK(alarm.intakeId)!!
            val medicine = database.medicineDAO().getByPK(intake.medicineId)!!
            val shortName = shortName(medicine.productName)
            val image = medicine.image
            val form = medicine.prodFormNormName
            val formName = if (form.isEmpty()) stringResource(text_amount) else formName(form)
            val amount = intake.amount
            val doseType = medicine.doseType
            val icon = when {
                image.contains(TYPE) -> ICONS_MED[image]
                image.isEmpty() -> vector_type_unknown
                else -> File(LocalContext.current.filesDir, image).run {
                    if (exists()) this else vector_type_unknown
                }
            }

            ListItem(
                headlineContent = {
                    Text(
                        text = shortName,
                        overflow = TextOverflow.Clip,
                        softWrap = false,
                        style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.SemiBold)
                    )
                },
                modifier = Modifier.clip(MaterialTheme.shapes.medium),
                supportingContent = {
                    Text(
                        stringResource(intake_text_quantity, formName, decimalFormat(amount), doseType),
                        style = MaterialTheme.typography.bodyLarge
                    )
                },
                leadingContent = {
                    Image(rememberAsyncImagePainter(icon), null, Modifier.size(56.dp))
                },
                trailingContent = {
                    Text(
                        LocalDateTime.ofInstant(Instant.ofEpochMilli(alarm.trigger), ZONE)
                            .format(FORMAT_H), style = MaterialTheme.typography.titleLarge
                    )
                },
                colors = ListItemDefaults.colors(MaterialTheme.colorScheme.tertiaryContainer)
            )
        }
    }