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
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
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
import ru.application.homemedkit.helpers.ConstantsHelper.MONTH_3
import ru.application.homemedkit.helpers.ConstantsHelper.SEMICOLON
import ru.application.homemedkit.helpers.ConstantsHelper.WEEK
import ru.application.homemedkit.helpers.DateHelper.FORMAT_D_H
import ru.application.homemedkit.helpers.DateHelper.FORMAT_D_M
import ru.application.homemedkit.helpers.DateHelper.FORMAT_H
import ru.application.homemedkit.helpers.DateHelper.FORMAT_S
import ru.application.homemedkit.helpers.DateHelper.ZONE
import ru.application.homemedkit.helpers.FiltersHelper
import ru.application.homemedkit.helpers.ImageHelper
import ru.application.homemedkit.helpers.StringHelper
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

                    Column {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.Center
                        ) {
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

                        Row {
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
                                        text = { Text(text = getString(R.string.intakes_tab_schedule)) }
                                    )
                                }
                            }
                        }

                        when (selectedIndex) {
                            0 -> {
                                LazyColumn(
                                    modifier = Modifier.padding(top = 10.dp),
                                    verticalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    val filtered = FiltersHelper(requireActivity()).intakes(text)
                                    items(filtered.size) { index -> IntakeList(filtered[index]) }
                                }
                            }

                            1 -> {
                                val result = getTriggers(text)

                                Column(modifier = Modifier.verticalScroll(rememberScrollState())) {
                                    result.forEach {
                                        Row {
                                            Column {
                                                DateText(text = it.key)
                                                IntakeTable(data = it)
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }


    private fun getTriggers(text: String): Map<Long, List<Alarm>> {
        val intervals = resources.getStringArray(R.array.interval_types)

        val filtered = FiltersHelper(requireActivity()).intakes(text)
        val triggers = ArrayList<Alarm>(intakes.size)

        for (intake in filtered) {
            val times = intake.time.split(SEMICOLON)

            if (times.size == 1) {
                var milliS = parse("${intake.startDate} ${intake.time}", FORMAT_D_H)
                    .toInstant(ZONE).toEpochMilli()
                val milliF = parse("${intake.finalDate} ${intake.time}", FORMAT_D_H)
                    .toInstant(ZONE).toEpochMilli()

                val interval = if (intake.interval.equals(intervals[1])) {
                    DAY
                } else if (intake.interval.equals(intervals[2])) {
                    WEEK
                } else {
                    DAY * intake.interval.substringAfter(DOWN_DASH).toInt()
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

        triggers.sortBy { it.trigger }
        if (triggers.isNotEmpty())
            while (triggers.last().trigger - currentTimeMillis() > MONTH_3) triggers.removeLast()

        return triggers.filter { it.trigger > currentTimeMillis() }
            .groupBy { ofEpochMilli(it.trigger).atZone(ZONE).toLocalDate().toEpochDay() }
    }

    @Composable
    fun IntakeList(intake: Intake) {
        val medicineDAO = database.medicineDAO()
        val productName = medicineDAO.getProductName(intake.medicineId)
        val shortName = StringHelper.shortName(productName)
        val form = medicineDAO.getByPK(intake.medicineId).prodFormNormName
        val icon = ImageHelper.getIconType(context, form)
        val startDate = LocalContext.current.resources.getString(
            R.string.text_from_date_card_intake,
            intake.startDate
        )
        val count = intake.time.split(SEMICOLON).size
        val intervalName = if (count == 1) StringHelper.intervalName(context, intake.interval)
        else resources.getQuantityString(R.plurals.intakes_amount, count, count)

        ElevatedCard(
            modifier = Modifier
                .fillMaxWidth()
                .wrapContentHeight()
                .padding(16.dp, 4.dp, 16.dp, 16.dp)
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
                    .padding(start = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Image(
                        painter = rememberDrawablePainter(drawable = icon),
                        contentDescription = stringResource(id = R.string.text_medicine_form_name),
                        modifier = Modifier.size(56.dp)
                    )
                }
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text(
                        text = shortName,
                        modifier = Modifier
                            .wrapContentSize()
                            .padding(top = 4.dp),
                        color = MaterialTheme.colorScheme.onTertiaryContainer,
                        fontWeight = FontWeight.Bold,
                        style = MaterialTheme.typography.headlineSmall
                    )
                    Text(
                        text = "$intervalName $startDate",
                        modifier = Modifier.wrapContentSize(),
                        color = MaterialTheme.colorScheme.onTertiaryContainer,
                        style = MaterialTheme.typography.bodyLarge
                    )
                    Text(
                        text = intake.time,
                        modifier = Modifier
                            .wrapContentSize()
                            .padding(bottom = 4.dp),
                        color = MaterialTheme.colorScheme.onTertiaryContainer,
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }
        }
    }

    @Composable
    fun DateText(text: Long) {
        Text(
            text = LocalDate.ofEpochDay(text).format(FORMAT_D_M),
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 16.dp),
            color = MaterialTheme.colorScheme.onSurface,
            fontWeight = FontWeight.SemiBold,
            style = MaterialTheme.typography.titleLarge
        )
    }

    @Composable
    fun IntakeTable(data: Map.Entry<Long, List<Alarm>>) {
        ElevatedCard(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.tertiaryContainer)
        ) {
            data.value.forEach {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(12.dp, 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = database.medicineDAO().getProductName(
                            database.intakeDAO().getByPK(it.intakeId).medicineId
                        ),
                        modifier = Modifier.fillMaxWidth(0.75f),
                        color = MaterialTheme.colorScheme.onTertiaryContainer,
                        fontWeight = FontWeight.W500,
                        overflow = TextOverflow.Ellipsis,
                        softWrap = false,
                        style = MaterialTheme.typography.headlineSmall
                    )
                    Text(
                        text = ofInstant(ofEpochMilli(it.trigger), ZONE).format(FORMAT_H),
                        color = MaterialTheme.colorScheme.onTertiaryContainer,
                        style = MaterialTheme.typography.headlineSmall
                    )
                }
            }
        }
    }
}



