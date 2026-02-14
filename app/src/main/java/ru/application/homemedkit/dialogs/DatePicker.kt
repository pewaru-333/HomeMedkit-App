package ru.application.homemedkit.dialogs

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.width
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import ru.application.homemedkit.R

@Composable
fun DatePicker(onSelect: (Long) -> Unit, onDismiss: () -> Unit, onClear: (() -> Unit)? = null) {
    val state = rememberDatePickerState()

    DatePickerDialog(
        onDismissRequest = onDismiss,
        dismissButton = null,
        confirmButton = {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                onClear?.let { TextButton(it) { Text(stringResource(R.string.text_clear)) } }

                Spacer(Modifier.weight(1f))

                TextButton(onDismiss) { Text(stringResource(R.string.text_cancel)) }

                Spacer(Modifier.width(8.dp))

                TextButton(
                    enabled = state.selectedDateMillis != null,
                    onClick = { state.selectedDateMillis?.let { onSelect(it) } },
                    content = { Text(stringResource(R.string.text_save)) }
                )
            }
        },
    ) {
        androidx.compose.material3.DatePicker(
            state = state,
            showModeToggle = false
        )
    }
}