package ru.application.homemedkit.fragments

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
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
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.ViewCompositionStrategy
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.fragment.app.Fragment
import com.google.accompanist.drawablepainter.rememberDrawablePainter
import ru.application.homemedkit.R
import ru.application.homemedkit.activities.IntakeActivity
import ru.application.homemedkit.databaseController.Alarm
import ru.application.homemedkit.databaseController.Intake
import ru.application.homemedkit.databaseController.MedicineDatabase
import ru.application.homemedkit.helpers.ConstantsHelper.BLANK
import ru.application.homemedkit.helpers.ConstantsHelper.DAY
import ru.application.homemedkit.helpers.ConstantsHelper.DOWN_DASH
import ru.application.homemedkit.helpers.ConstantsHelper.INTAKE_ID
import ru.application.homemedkit.helpers.ConstantsHelper.INTERVALS
import ru.application.homemedkit.helpers.ConstantsHelper.SEMICOLON
import ru.application.homemedkit.helpers.ConstantsHelper.WEEK
import ru.application.homemedkit.helpers.DateHelper.FORMAT_D_H
import ru.application.homemedkit.helpers.DateHelper.FORMAT_D_M
import ru.application.homemedkit.helpers.DateHelper.FORMAT_D_M_Y
import ru.application.homemedkit.helpers.DateHelper.FORMAT_H
import ru.application.homemedkit.helpers.DateHelper.FORMAT_S
import ru.application.homemedkit.helpers.DateHelper.ZONE
import ru.application.homemedkit.helpers.FiltersHelper
import ru.application.homemedkit.helpers.decimalFormat
import ru.application.homemedkit.helpers.formName
import ru.application.homemedkit.helpers.getIconType
import ru.application.homemedkit.helpers.intervalName
import ru.application.homemedkit.helpers.shortName
import ru.application.homemedkit.ui.theme.AppTheme
import java.lang.System.currentTimeMillis
import java.time.Instant.ofEpochMilli
import java.time.LocalDate
import java.time.LocalDateTime.of
import java.time.LocalDateTime.ofInstant
import java.time.LocalDateTime.parse
import java.time.LocalTime


class FragmentIntakes : Fragment() {

    private lateinit var database: MedicineDatabase
    private lateinit var intakes: List<Intake>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        database = MedicineDatabase.getInstance(context)
        intakes = database.intakeDAO().all
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return ComposeView(requireContext()).apply {
            setViewCompositionStrategy(ViewCompositionStrategy.DisposeOnViewTreeLifecycleDestroyed)
            setContent {
                AppTheme {
                    var text by rememberSaveable { mutableStateOf(BLANK) }
                    var selectedIndex by remember { mutableIntStateOf(0) }

                    val stateOne = rememberLazyListState()
                    val stateTwo = rememberLazyListState()
                    val stateThr = rememberLazyListState()

                    Column {
                        Row(Modifier.fillMaxWidth(), Arrangement.Center) {
                            OutlinedTextField(
                                value = text,
                                onValueChange = { text = it },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(64.dp)
                                    .padding(start = 16.dp, top = 4.dp, end = 16.dp),
                                label = {
                                    Text(
                                        text = getString(R.string.text_enter_product_name),
                                        overflow = TextOverflow.Visible,
                                        softWrap = false
                                    )
                                },
                                leadingIcon = { Icon(Icons.Default.Search, null) },
                                trailingIcon = {
                                    if (text.isNotEmpty())
                                        IconButton(onClick = { text = BLANK })
                                        { Icon(Icons.Outlined.Clear, null) }
                                },
                                singleLine = true
                            )
                        }

                        Row(Modifier.padding(top = 4.dp)) {
                            Column {
                                TabRow(selectedTabIndex = selectedIndex) {
                                    Tab(
                                        selected = selectedIndex == 0,
                                        onClick = { selectedIndex = 0 },
                                        text = { Text(text = getString(R.string.intakes_tab_list)) }
                                    )
                                    Tab(
                                        selected = selectedIndex == 1,
                                        onClick = { selectedIndex = 1 },
                                        text = { Text(text = getString(R.string.intakes_tab_current)) }
                                    )
                                    Tab(selected = selectedIndex == 2,
                                        onClick = { selectedIndex = 2 },
                                        text = { Text(text = getString(R.string.intakes_tab_taken)) }
                                    )
                                }
                            }
                        }

                        when (selectedIndex) {
                            0 -> {
                                LazyColumn(
                                    modifier = Modifier.padding(top = 10.dp),
                                    state = stateOne,
                                    verticalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    val filtered = FiltersHelper(requireActivity()).intakes(text)
                                    items(filtered.size) { IntakeList(filtered[it]) }
                                }
                            }

                            1 -> {
                                val result = getTriggers(text, false)

                                LazyColumn(state = stateTwo) {
                                    items(result.size) {
                                        Row { Column { IntakeSchedule(result.entries.elementAt(it)) } }
                                    }
                                }
                            }

                            2 -> {
                                val result = getTriggers(text, true)

                                LazyColumn(state = stateThr, reverseLayout = true) {
                                    items(result.size) {
                                        Row { Column { IntakeSchedule(result.entries.elementAt(it)) } }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }


    private fun getTriggers(text: String, taken: Boolean): Map<Long, List<Alarm>> {
        val filtered = FiltersHelper(requireActivity()).intakes(text)
        val triggers = ArrayList<Alarm>(intakes.size)

        for (intake in filtered) {
            val times = intake.time.split(SEMICOLON)

            if (times.size == 1) {
                var milliS = parse("${intake.startDate} ${intake.time}", FORMAT_D_H)
                    .toInstant(ZONE).toEpochMilli()
                val milliF = parse("${intake.finalDate} ${intake.time}", FORMAT_D_H)
                    .toInstant(ZONE).toEpochMilli()

                val interval = when {
                    intake.interval.equals(INTERVALS[0]) -> DAY
                    intake.interval.equals(INTERVALS[1]) -> WEEK
                    else -> DAY * intake.interval.substringAfter(DOWN_DASH).toInt()
                }

                while (milliS <= milliF) {
                    triggers.add(Alarm(intake.intakeId, milliS))
                    milliS += interval
                }
            } else {
                val timesD = arrayOfNulls<LocalTime>(times.size)

                for (i in times.indices) timesD[i] = LocalTime.parse(times[i], FORMAT_H)

                var localS = LocalDate.parse(intake.startDate, FORMAT_S)
                val localF = LocalDate.parse(intake.finalDate, FORMAT_S)

                var milliS = of(localS, timesD.first())
                val milliF = of(localF, timesD.last())

                while (milliS <= milliF) {
                    for (time in timesD) {
                        val millis = of(localS, time).atOffset(ZONE).toInstant().toEpochMilli()
                        triggers.add(Alarm(intake.intakeId, millis))
                    }
                    localS = localS.plusDays(1)
                    milliS = milliS.plusDays(1)
                }
            }
        }

        if (taken) triggers.sortByDescending { it.trigger }
        else triggers.sortBy { it.trigger }

        return if (taken)
            triggers.filter { it.trigger < currentTimeMillis() }
                .groupBy { ofEpochMilli(it.trigger).atZone(ZONE).toLocalDate().toEpochDay() }
        else
            triggers.filter { it.trigger > currentTimeMillis() }
                .groupBy { ofEpochMilli(it.trigger).atZone(ZONE).toLocalDate().toEpochDay() }
    }

    @Composable
    fun IntakeList(intake: Intake) {
        val medicineDAO = database.medicineDAO()
        val productName = medicineDAO.getProductName(intake.medicineId)
        val shortName = shortName(productName)
        val form = medicineDAO.getByPK(intake.medicineId).prodFormNormName
        val icon = getIconType(requireContext(), form)
        val startDate = LocalContext.current.resources.getString(
            R.string.intake_card_text_from,
            intake.startDate
        )
        val count = intake.time.split(SEMICOLON).size
        val intervalName = if (count == 1) intervalName(requireContext(), intake.interval)
        else resources.getQuantityString(R.plurals.intakes_a_day, count, count)

        ElevatedCard(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
                .clickable {
                    val intent = Intent(context, IntakeActivity::class.java)
                    intent.putExtra(INTAKE_ID, intake.intakeId)
                    startActivity(intent)
                },
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.tertiaryContainer)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp, vertical = 4.dp),
                horizontalArrangement = Arrangement.spacedBy(16.dp, Alignment.Start),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Image(rememberDrawablePainter(drawable = icon), null, Modifier.size(64.dp))
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
                        text = stringResource(
                            R.string.intake_text_interval_from,
                            intervalName,
                            startDate
                        ),
                        style = MaterialTheme.typography.bodyLarge
                    )
                    Text(text = intake.time, style = MaterialTheme.typography.bodyMedium)
                }
            }
        }
    }

    @Composable
    fun IntakeSchedule(data: Map.Entry<Long, List<Alarm>>) {
        val medicineDAO = database.medicineDAO()
        val intakeDAO = database.intakeDAO()

        val date = LocalDate.ofEpochDay(data.key)
        val textDate = date.format(
            if (date.year == LocalDate.now().year) FORMAT_D_M
            else FORMAT_D_M_Y
        )

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .wrapContentHeight()
                .padding(horizontal = 16.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp, Alignment.Top),
            horizontalAlignment = Alignment.Start
        ) {
            Text(
                text = textDate,
                color = MaterialTheme.colorScheme.onSurface,
                fontWeight = FontWeight.SemiBold,
                style = MaterialTheme.typography.titleLarge
            )
            data.value.forEach {
                val medicineId = intakeDAO.getByPK(it.intakeId).medicineId
                val productName = medicineDAO.getProductName(medicineId)
                val shortName = shortName(productName)
                val form = medicineDAO.getByPK(medicineId).prodFormNormName
                val formName =
                    if (form.isEmpty()) resources.getString(R.string.text_amount) else formName(form)
                val amount = intakeDAO.getByPK(it.intakeId).amount
                val icon = getIconType(requireContext(), form)

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
                            rememberDrawablePainter(drawable = icon),
                            null,
                            Modifier
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
                                    decimalFormat(amount)
                                )
                            )
                        }
                        Text(
                            text = ofInstant(ofEpochMilli(it.trigger), ZONE).format(FORMAT_H),
                            modifier = Modifier.weight(0.25f),
                            color = MaterialTheme.colorScheme.onTertiaryContainer,
                            style = MaterialTheme.typography.headlineSmall
                        )
                    }
                }
            }
        }
    }
}



