package ru.application.homemedkit.dialogs

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.DatePickerDefaults
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.DateRangePickerDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SelectableDates
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberDateRangePickerState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment.Companion.CenterHorizontally
import androidx.compose.ui.Alignment.Companion.CenterVertically
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import ru.application.homemedkit.R.string.text_cancel
import ru.application.homemedkit.R.string.text_finish_date
import ru.application.homemedkit.R.string.text_save
import ru.application.homemedkit.R.string.text_start_date
import ru.application.homemedkit.helpers.FORMAT_S
import ru.application.homemedkit.helpers.LOCALE
import ru.application.homemedkit.helpers.ZONE
import ru.application.homemedkit.helpers.getDateTime
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DateRangePicker(
    startDate: String,
    finalDate: String,
    onRangeSelected: (Pair<Long?, Long?>) -> Unit,
    onDismiss: () -> Unit
) {
    val initialStart = startDate.let {
        if (it.isNotEmpty()) LocalDateTime.of(
            LocalDate.parse(it, FORMAT_S), LocalTime.of(12, 0, 0)
        ).toInstant(ZONE).toEpochMilli() else null
    }

    val initialFinal = finalDate.let {
        if (it.isNotEmpty()) LocalDateTime.of(
            LocalDate.parse(it, FORMAT_S), LocalTime.of(12, 0, 0)
        ).toInstant(ZONE).toEpochMilli() else null
    }

    val state = rememberDateRangePickerState(
        initialSelectedStartDateMillis = initialStart,
        initialSelectedEndDateMillis = initialFinal,
        yearRange = if (initialStart != null) DatePickerDefaults.YearRange
        else IntRange(LocalDate.now().year, LocalDate.now().year + 10),
        selectableDates = if (initialStart != null) DatePickerDefaults.AllDates
        else object : SelectableDates {
            override fun isSelectableDate(utcTimeMillis: Long) =
                getDateTime(utcTimeMillis).toLocalDate() >= LocalDate.now()

            override fun isSelectableYear(year: Int) = LocalDate.now().year <= year
        }
    )

    DatePickerDialog(
        onDismissRequest = onDismiss,
        dismissButton = { TextButton(onDismiss) { Text(stringResource(text_cancel)) } },
        confirmButton = {
            TextButton(
                enabled = state.selectedStartDateMillis != null && state.selectedEndDateMillis != null,
                onClick = {
                    onRangeSelected(state.selectedStartDateMillis to state.selectedEndDateMillis)
                    onDismiss()
                }
            ) { Text(stringResource(text_save)) }
        }
    ) {
        androidx.compose.material3.DateRangePicker(
            state = state,
            showModeToggle = false,
            title = {
                DateRangePickerDefaults.DateRangePickerTitle(
                    displayMode = state.displayMode,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp)
                )
            },
            headline = {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(8.dp),
                    horizontalArrangement = Arrangement.spacedBy(4.dp, CenterHorizontally),
                    verticalAlignment = CenterVertically
                ) {
                    val formattedStart = DatePickerDefaults.dateFormatter()
                        .formatDate(state.selectedStartDateMillis, LOCALE)

                    val formattedFinal = DatePickerDefaults.dateFormatter()
                        .formatDate(state.selectedEndDateMillis, LOCALE)

                    Text(
                        text = formattedStart ?: stringResource(text_start_date),
                        style = MaterialTheme.typography.titleLarge.copy(fontSize = 20.sp)
                    )

                    Text("-")

                    Text(
                        text = formattedFinal ?: stringResource(text_finish_date),
                        style = MaterialTheme.typography.titleLarge.copy(fontSize = 20.sp)
                    )
                }
            }
        )
    }
}