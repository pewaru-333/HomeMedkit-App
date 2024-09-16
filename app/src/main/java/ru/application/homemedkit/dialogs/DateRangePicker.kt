package ru.application.homemedkit.dialogs

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Check
import androidx.compose.material.icons.outlined.Clear
import androidx.compose.material3.DateRangePickerState
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import ru.application.homemedkit.R
import ru.application.homemedkit.helpers.BLANK

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DateRangePicker(
    state: DateRangePickerState,
    start: String?,
    final: String?,
    onDismiss: () -> Unit,
    onConfirm: () -> Unit
) {
    Dialog(onDismiss, DialogProperties(usePlatformDefaultWidth = false)) {
        Surface {
            Column(Modifier.fillMaxSize(), Arrangement.Top) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(12.dp, 12.dp, 12.dp, 0.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(onDismiss) { Icon(Icons.Outlined.Clear, null) }
                    IconButton(onConfirm) { Icon(Icons.Outlined.Check, null) }
                }

                androidx.compose.material3.DateRangePicker(
                    state = state,
                    title = {
                        Text(
                            text = stringResource(R.string.intake_text_pick_period),
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(20.dp),
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            style = MaterialTheme.typography.labelLarge
                        )
                    },
                    headline = {
                        Row(
                            Modifier
                                .fillMaxWidth()
                                .padding(bottom = 12.dp),
                            Arrangement.spacedBy(8.dp, Alignment.CenterHorizontally),
                            Alignment.CenterVertically,
                        ) { Text("${start ?: BLANK} - ${final ?: BLANK}") }
                    },
                    showModeToggle = false
                )
            }
        }
    }
}