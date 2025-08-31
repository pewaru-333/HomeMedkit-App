package ru.application.homemedkit.dialogs

import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import ru.application.homemedkit.R

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DatePicker(onSelect: (Long) -> Unit, onDismiss: () -> Unit) {
    val state = rememberDatePickerState()

    DatePickerDialog(
        onDismissRequest = onDismiss,
        dismissButton = { TextButton(onDismiss) { Text(stringResource(R.string.text_cancel)) } },
        confirmButton = {
            TextButton(
                enabled = state.selectedDateMillis != null,
                onClick = { state.selectedDateMillis?.let { onSelect(it) } },
                content = { Text(stringResource(R.string.text_save)) })
        }
    ) {
        androidx.compose.material3.DatePicker(
            state = state,
            showModeToggle = false
        )
    }
}