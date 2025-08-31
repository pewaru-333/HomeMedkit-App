package ru.application.homemedkit.dialogs

import android.icu.text.DateFormatSymbols
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.KeyboardArrowLeft
import androidx.compose.material.icons.automirrored.rounded.KeyboardArrowRight
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import ru.application.homemedkit.R.string.text_cancel
import ru.application.homemedkit.R.string.text_save
import java.time.LocalDate

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun MonthYear(
    confirm: (Int, Int) -> Unit,
    cancel: () -> Unit,
    currentMonth: Int = LocalDate.now().month.value - 1,
    currentYear: Int = LocalDate.now().year
) {
    val months = DateFormatSymbols.getInstance().shortMonths

    var month by remember { mutableStateOf(months[currentMonth]) }
    var year by remember { mutableIntStateOf(currentYear) }

    Dialog(cancel) {
        Surface(shape = CardDefaults.shape) {
            Column(Modifier.padding(16.dp), Arrangement.spacedBy(16.dp)) {
                Row(Modifier.fillMaxWidth(), Arrangement.Center, Alignment.CenterVertically) {
                    IconButton(
                        onClick = { year-- },
                        content = {
                            Icon(
                                imageVector = Icons.AutoMirrored.Rounded.KeyboardArrowLeft,
                                contentDescription = null,
                                modifier = Modifier.size(36.dp)
                            )
                        }
                    )

                    Text(
                        modifier = Modifier.padding(horizontal = 12.dp),
                        color = MaterialTheme.colorScheme.onSurface,
                        text = year.toString(),
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Bold
                    )

                    IconButton(
                        onClick = { year++ },
                        content = {
                            Icon(
                                imageVector = Icons.AutoMirrored.Rounded.KeyboardArrowRight,
                                contentDescription = null,
                                modifier = Modifier.size(36.dp)
                            )
                        }
                    )
                }

                Card(Modifier.fillMaxWidth()) {
                    FlowRow(Modifier.fillMaxWidth(), Arrangement.Center) {
                        months.forEach {
                            Box(
                                contentAlignment = Alignment.Center,
                                modifier = Modifier
                                    .clip(CircleShape)
                                    .size(76.dp)
                                    .clickable { month = it }
                            ) {
                                val boxSize by animateDpAsState(if (month == it) 60.dp else 0.dp)

                                Box(
                                    modifier = Modifier
                                        .size(boxSize)
                                        .background(
                                            shape = CircleShape,
                                            color = if (month == it) MaterialTheme.colorScheme.secondary
                                            else Color.Transparent
                                        )
                                )

                                Text(
                                    text = it.uppercase().removeSuffix("."),
                                    fontWeight = FontWeight.Medium,
                                    color = if (month == it) MaterialTheme.colorScheme.onSecondary
                                    else MaterialTheme.colorScheme.onPrimaryContainer,
                                )
                            }
                        }
                    }
                }

                Row(Modifier.fillMaxWidth(), Arrangement.End) {
                    TextButton(cancel) {
                        Text(stringResource(text_cancel))
                    }

                    TextButton({ confirm(months.indexOf(month) + 1, year) }) {
                        Text(stringResource(text_save))
                    }
                }
            }
        }
    }
}