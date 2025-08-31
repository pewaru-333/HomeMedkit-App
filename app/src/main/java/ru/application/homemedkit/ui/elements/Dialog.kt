package ru.application.homemedkit.ui.elements

import androidx.annotation.StringRes
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.selection.toggleable
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Checkbox
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.dp
import ru.application.homemedkit.R
import ru.application.homemedkit.data.dto.Kit

@Composable
fun DialogKits(
    kits: List<Kit>,
    isChecked: (Kit) -> Boolean,
    onPick: (Kit) -> Unit,
    onDismiss: () -> Unit,
    onClear: () -> Unit,
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        dismissButton = { TextButton(onClear) { Text(stringResource(R.string.text_clear)) } },
        confirmButton = { TextButton(onDismiss) { Text(stringResource(R.string.text_save)) } },
        title = { Text(stringResource(R.string.preference_kits_group)) },
        text = {
            if (kits.isEmpty()) {
                Text(
                    text = stringResource(R.string.text_kit_list_is_empty),
                    modifier = Modifier.padding(horizontal = 12.dp),
                    style = MaterialTheme.typography.bodyLarge
                )
            } else LazyColumn {
                items(kits, Kit::kitId) { kit ->
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(56.dp)
                            .toggleable(
                                role = Role.Checkbox,
                                value = isChecked(kit),
                                onValueChange = { onPick(kit) }
                            )
                    ) {
                        Checkbox(isChecked(kit), null)
                        Text(
                            text = kit.title,
                            modifier = Modifier.padding(start = 16.dp),
                            style = MaterialTheme.typography.bodyLarge
                        )
                    }
                }
            }
        }
    )
}

@Composable
fun DialogDelete(@StringRes text: Int, onCancel: () -> Unit, onConfirm: () -> Unit) = AlertDialog(
    onDismissRequest = onCancel,
    confirmButton = { TextButton(onConfirm) { Text(stringResource(R.string.text_delete)) } },
    dismissButton = { TextButton(onCancel) { Text(stringResource(R.string.text_cancel)) } },
    text = {
        Text(
            text = stringResource(text),
            style = MaterialTheme.typography.bodyLarge
        )
    }
)