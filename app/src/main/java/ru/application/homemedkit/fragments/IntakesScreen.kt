package ru.application.homemedkit.fragments

import android.app.AlarmManager
import android.content.Context
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.outlined.Clear
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import coil.compose.rememberAsyncImagePainter
import com.ramcosta.composedestinations.annotation.Destination
import com.ramcosta.composedestinations.annotation.RootGraph
import com.ramcosta.composedestinations.generated.destinations.IntakeScreenDestination
import com.ramcosta.composedestinations.navigation.DestinationsNavigator
import ru.application.homemedkit.R
import ru.application.homemedkit.databaseController.Alarm
import ru.application.homemedkit.databaseController.Intake
import ru.application.homemedkit.databaseController.MedicineDatabase
import ru.application.homemedkit.helpers.BLANK
import ru.application.homemedkit.helpers.DateHelper
import ru.application.homemedkit.helpers.FiltersHelper
import ru.application.homemedkit.helpers.ICONS_MED
import ru.application.homemedkit.helpers.TYPE
import ru.application.homemedkit.helpers.decimalFormat
import ru.application.homemedkit.helpers.formName
import ru.application.homemedkit.helpers.intervalName
import ru.application.homemedkit.helpers.shortName
import java.io.File
import java.time.Instant
import java.time.LocalDate
import java.time.LocalDateTime

@OptIn(ExperimentalMaterial3Api::class)
@Destination<RootGraph>
@Composable
fun IntakesScreen(navigator: DestinationsNavigator, context: Context = LocalContext.current) {
    val database = MedicineDatabase.getInstance(context)
    val tabs = listOf(R.string.intakes_tab_list, R.string.intakes_tab_current, R.string.intakes_tab_taken)

    val stateOne = rememberLazyListState()
    val stateTwo = rememberLazyListState()
    val stateThr = rememberLazyListState()

    var text by rememberSaveable { mutableStateOf(BLANK) }
    var selectedIndex by remember { mutableIntStateOf(0) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    TextField(
                        value = text,
                        onValueChange = { text = it },
                        modifier = Modifier.fillMaxWidth(),
                        label = { Text(context.getString(R.string.text_enter_product_name)) },
                        leadingIcon = { Icon(Icons.Default.Search, null) },
                        trailingIcon = {
                            if (text.isNotEmpty())
                                IconButton(onClick = { text = BLANK })
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
    ) { paddingValues ->
        Column(modifier = Modifier.padding(top = paddingValues.calculateTopPadding())) {
            Row {
                Column {
                    TabRow(selectedTabIndex = selectedIndex) {
                        tabs.forEachIndexed { index, tab ->
                            Tab(
                                selected = selectedIndex == index,
                                onClick = { selectedIndex = index },
                                text = { Text(context.getString(tab)) }
                            )
                        }
                    }
                }
            }

            when (selectedIndex) {
                0 -> {
                    LazyColumn(
                        state = stateOne,
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        val filtered = FiltersHelper(context).intakes(text)
                        items(filtered.size) { IntakeList(filtered[it], database, navigator) }
                    }
                }

                1 -> {
                    val result = getTriggers(text, false, context)

                    LazyColumn(state = stateTwo) {
                        items(result.size) {
                            Column {
                                IntakeSchedule(result.entries.elementAt(it), database, context)
                            }
                        }
                    }
                }

                2 -> {
                    val result = getTriggers(text, true, context)

                    LazyColumn(state = stateThr, reverseLayout = true) {
                        items(result.size) {
                            Column {
                                IntakeSchedule(result.entries.elementAt(it), database, context)
                            }
                        }
                    }
                }
            }
        }
    }
}

private fun getTriggers(text: String, taken: Boolean, context: Context): Map<Long, List<Alarm>> {
    val filtered = FiltersHelper(context).intakes(text)
    val triggers = ArrayList<Alarm>()

    for (intake in filtered) {
        if (intake.time.size == 1) {
            var milliS =
                LocalDateTime.parse("${intake.startDate} ${intake.time[0]}", DateHelper.FORMAT_D_H)
                    .toInstant(DateHelper.ZONE).toEpochMilli()
            val milliF =
                LocalDateTime.parse("${intake.finalDate} ${intake.time[0]}", DateHelper.FORMAT_D_H)
                    .toInstant(DateHelper.ZONE).toEpochMilli()

            while (milliS <= milliF) {
                triggers.add(Alarm(intakeId = intake.intakeId, trigger = milliS))
                milliS += intake.interval * AlarmManager.INTERVAL_DAY
            }
        } else {
            var localS = LocalDate.parse(intake.startDate, DateHelper.FORMAT_S)
            val localF = LocalDate.parse(intake.finalDate, DateHelper.FORMAT_S)

            var milliS = LocalDateTime.of(localS, intake.time.first())
            val milliF = LocalDateTime.of(localF, intake.time.last())

            while (milliS <= milliF) {
                for (time in intake.time) {
                    val millis =
                        LocalDateTime.of(localS, time).atOffset(DateHelper.ZONE).toInstant()
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
                Instant.ofEpochMilli(it.trigger).atZone(DateHelper.ZONE).toLocalDate().toEpochDay()
            }
    else
        triggers.filter { it.trigger > System.currentTimeMillis() }
            .groupBy {
                Instant.ofEpochMilli(it.trigger).atZone(DateHelper.ZONE).toLocalDate().toEpochDay()
            }
}

@Composable
fun IntakeList(
    intake: Intake,
    database: MedicineDatabase,
    navigator: DestinationsNavigator,
    context: Context = LocalContext.current
) {
    val medicine = database.medicineDAO().getByPK(intake.medicineId)
    val productName = medicine?.productName ?: BLANK
    val shortName = shortName(productName)
    val image = medicine?.image ?: BLANK
    val icon = when {
        image.contains(TYPE) -> ICONS_MED[image]
        image.isEmpty() -> R.drawable.vector_type_unknown
        else -> File(context.filesDir, image)
    }
    val startDate = LocalContext.current.resources.getString(
        R.string.intake_card_text_from,
        intake.startDate
    )
    val count = intake.time.size
    val intervalName = if (count == 1) intervalName(context, intake.interval)
    else context.resources.getQuantityString(R.plurals.intakes_a_day, count, count)

    ElevatedCard(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
            .clickable { navigator.navigate(IntakeScreenDestination(intakeId = intake.intakeId)) },
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.tertiaryContainer)
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp, vertical = 4.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp, Alignment.Start),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Image(rememberAsyncImagePainter(icon), null, Modifier.size(64.dp))
            Column(
                verticalArrangement = Arrangement.spacedBy(8.dp, Alignment.CenterVertically),
                horizontalAlignment = Alignment.Start
            ) {
                Text(
                    text = shortName,
                    fontWeight = FontWeight.SemiBold,
                    overflow = TextOverflow.Ellipsis,
                    style = MaterialTheme.typography.headlineSmall
                )
                Text(
                    text = "$intervalName $startDate",
                    style = MaterialTheme.typography.bodyLarge
                )
                Text(
                    text = intake.time.joinToString(", "),
                    style = MaterialTheme.typography.bodyMedium
                )
            }
        }
    }
}

@Composable
fun IntakeSchedule(
    data: Map.Entry<Long, List<Alarm>>,
    database: MedicineDatabase,
    context: Context = LocalContext.current
) {
    val medicineDAO = database.medicineDAO()
    val intakeDAO = database.intakeDAO()

    val date = LocalDate.ofEpochDay(data.key)
    val textDate = date.format(
        if (date.year == LocalDate.now().year) DateHelper.FORMAT_D_M
        else DateHelper.FORMAT_D_M_Y
    )

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .wrapContentHeight()
            .padding(16.dp, 8.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp, Alignment.Top),
        horizontalAlignment = Alignment.Start
    ) {
        Text(
            text = textDate,
            color = MaterialTheme.colorScheme.onSurface,
            fontWeight = FontWeight.SemiBold,
            style = MaterialTheme.typography.titleLarge
        )
        data.value.forEach { alarm ->
            val intake = intakeDAO.getByPK(alarm.intakeId)
            val medicine = medicineDAO.getByPK(intake?.medicineId ?: 0L)
            val productName = medicine?.productName ?: BLANK
            val shortName = shortName(productName)
            val image = medicine?.image ?: BLANK
            val form = medicine?.prodFormNormName ?: BLANK
            val formName = if (form.isEmpty()) context.resources.getString(R.string.text_amount) else formName(form)
            val amount = intake?.amount ?: 0.0
            val doseType = medicine?.doseType ?: BLANK
            val icon = when {
                image.contains(TYPE) -> ICONS_MED[image]
                image.isEmpty() -> R.drawable.vector_type_unknown
                else -> File(context.filesDir, image)
            }

            ElevatedCard(
                modifier = Modifier.height(100.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.tertiaryContainer)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 12.dp),
                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Image(
                        painter = rememberAsyncImagePainter(icon),
                        contentDescription = null,
                        modifier = Modifier
                            .size(64.dp)
                            .weight(0.2f)
                    )
                    Column(
                        modifier = Modifier
                            .height(64.dp)
                            .weight(0.55f),
                        verticalArrangement = Arrangement.SpaceBetween,
                        horizontalAlignment = Alignment.Start
                    ) {
                        Text(
                            text = shortName,
                            fontWeight = FontWeight.SemiBold,
                            overflow = TextOverflow.Ellipsis,
                            softWrap = false,
                            style = MaterialTheme.typography.headlineSmall
                        )
                        Text(
                            text = stringResource(
                                R.string.intake_text_quantity,
                                formName,
                                decimalFormat(amount),
                                doseType
                            )
                        )
                    }
                    Text(
                        text = LocalDateTime.ofInstant(
                            Instant.ofEpochMilli(alarm.trigger),
                            DateHelper.ZONE
                        )
                            .format(DateHelper.FORMAT_H),
                        modifier = Modifier.weight(0.25f),
                        color = MaterialTheme.colorScheme.onTertiaryContainer,
                        style = MaterialTheme.typography.headlineSmall
                    )
                }
            }
        }
    }
}