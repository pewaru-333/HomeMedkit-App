package ru.application.homemedkit.fragments

import android.app.AlarmManager
import android.content.Context
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.outlined.Clear
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
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
    val tabs = listOf(R.string.intakes_tab_list, R.string.intakes_tab_current, R.string.intakes_tab_taken)

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
                        label = { Text(stringResource(R.string.text_enter_product_name)) },
                        leadingIcon = { Icon(Icons.Default.Search, null) },
                        trailingIcon = {
                            if (text.isNotEmpty())
                                IconButton({ text = BLANK })
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
        Column(Modifier.padding(top = paddingValues.calculateTopPadding())) {
            TabRow(selectedIndex) {
                tabs.forEachIndexed { index, tab ->
                    Tab(
                        selected = selectedIndex == index,
                        onClick = { selectedIndex = index },
                        text = { Text(stringResource(tab)) }
                    )
                }
            }

            LazyColumn(
                modifier = Modifier.padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp),
                reverseLayout = selectedIndex == 2
            ) {
                when (selectedIndex) {
                    0 -> {
                        val filtered = FiltersHelper(context).intakes(text)

                        if (filtered.isEmpty())
                            item {
                                Box(
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .padding(
                                            start = 16.dp,
                                            top = paddingValues.calculateTopPadding(),
                                            end = 16.dp
                                        ),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = stringResource(R.string.text_no_intakes_found),
                                        textAlign = TextAlign.Center
                                    )
                                }
                            }
                        else items(filtered) { IntakeList(it, navigator, context) }
                    }

                    1 -> {
                        val result = getTriggers(text, false, context)
                        items(result.size) { IntakeSchedule(result.entries.elementAt(it)) }
                    }

                    2 -> {
                        val result = getTriggers(text, true, context)
                        items(result.size) { IntakeSchedule(result.entries.elementAt(it)) }
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
fun IntakeList(intake: Intake, navigator: DestinationsNavigator, context: Context) {
    val medicine = MedicineDatabase.getInstance(context).medicineDAO().getByPK(intake.medicineId)!!
    val shortName = shortName(medicine.productName)
    val image = medicine.image
    val icon = when {
        image.contains(TYPE) -> ICONS_MED[image]
        image.isEmpty() -> R.drawable.vector_type_unknown
        else -> File(context.filesDir, image).run {
            if (exists()) this else R.drawable.vector_type_unknown
        }
    }
    val startDate = stringResource(R.string.intake_card_text_from, intake.startDate)
    val count = intake.time.size
    val intervalName = if (count == 1) intervalName(context, intake.interval)
    else context.resources.getQuantityString(R.plurals.intakes_a_day, count, count)

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
        colors = ListItemDefaults.colors(containerColor = MaterialTheme.colorScheme.tertiaryContainer)
    )
}

@Composable
fun IntakeSchedule(data: Map.Entry<Long, List<Alarm>>, context: Context = LocalContext.current) {
    val database = MedicineDatabase.getInstance(context)
    val medicineDAO = database.medicineDAO()
    val intakeDAO = database.intakeDAO()

    val date = LocalDate.ofEpochDay(data.key)
    val textDate = date.format(
        if (date.year == LocalDate.now().year) DateHelper.FORMAT_D_M
        else DateHelper.FORMAT_D_M_Y
    )

    Column(Modifier.padding(vertical = 8.dp), Arrangement.spacedBy(12.dp)) {
        Text(
            text = textDate,
            style = MaterialTheme.typography.titleLarge.copy(
                color = MaterialTheme.colorScheme.onSurface,
                fontWeight = FontWeight.SemiBold
            )
        )
        data.value.forEach { alarm ->
            val intake = intakeDAO.getByPK(alarm.intakeId)!!
            val medicine = medicineDAO.getByPK(intake.medicineId)!!
            val shortName = shortName(medicine.productName)
            val image = medicine.image
            val form = medicine.prodFormNormName
            val formName = if (form.isEmpty()) stringResource(R.string.text_amount) else formName(form)
            val amount = intake.amount
            val doseType = medicine.doseType
            val icon = when {
                image.contains(TYPE) -> ICONS_MED[image]
                image.isEmpty() -> R.drawable.vector_type_unknown
                else -> File(context.filesDir, image).run {
                    if (exists()) this else R.drawable.vector_type_unknown
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
                        text = stringResource(
                            R.string.intake_text_quantity,
                            formName,
                            decimalFormat(amount),
                            doseType
                        ),
                        style = MaterialTheme.typography.bodyLarge
                    )
                },
                leadingContent = {
                    Image(rememberAsyncImagePainter(icon), null, Modifier.size(56.dp))
                },
                trailingContent = {
                    Text(
                        text = LocalDateTime.ofInstant(
                            Instant.ofEpochMilli(alarm.trigger),
                            DateHelper.ZONE
                        )
                            .format(DateHelper.FORMAT_H),
                        style = MaterialTheme.typography.titleLarge
                    )
                },
                colors = ListItemDefaults.colors(MaterialTheme.colorScheme.tertiaryContainer)
            )
        }
    }
}