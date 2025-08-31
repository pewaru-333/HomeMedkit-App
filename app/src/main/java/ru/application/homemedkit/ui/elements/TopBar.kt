package ru.application.homemedkit.ui.elements

import androidx.annotation.StringRes
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Check
import androidx.compose.material.icons.outlined.Clear
import androidx.compose.material.icons.outlined.MoreVert
import androidx.compose.material.icons.outlined.Notifications
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import ru.application.homemedkit.R

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SearchAppBar(
    search: String,
    onSearch: (String) -> Unit,
    onClear: () -> Unit,
    actions: @Composable (RowScope.() -> Unit) = {},
) {
    CenterAlignedTopAppBar(
        actions = actions,
        modifier = Modifier.drawBehind {
            drawLine(Color.LightGray, Offset(0f, size.height), Offset(size.width, size.height), 4f)
        },
        title = {
            TextField(
                value = search,
                onValueChange = onSearch,
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                label = { Text(stringResource(R.string.text_enter_product_name)) },
                leadingIcon = { Icon(Icons.Outlined.Search, null) },
                trailingIcon = {
                    if (search.isNotEmpty()) {
                        IconButton(onClear) {
                            Icon(Icons.Outlined.Clear, null)
                        }
                    }
                },
                colors = TextFieldDefaults.colors(
                    focusedContainerColor = MaterialTheme.colorScheme.surface,
                    unfocusedContainerColor = MaterialTheme.colorScheme.surface,
                    focusedIndicatorColor = Color.Transparent,
                    unfocusedIndicatorColor = Color.Transparent,
                    disabledIndicatorColor = Color.Transparent,
                )
            )
        }
    )
}

@Composable
fun TopBarActions(
    isDefault: Boolean,
    setModifiable: () -> Unit,
    onSave: () -> Unit,
    onShowDialog: () -> Unit,
    onNavigate: (() -> Unit)? = null
) {
    @Composable
    fun LocalDropDownItem(@StringRes text: Int, onClick: () -> Unit) =
        DropdownMenuItem(
            onClick = onClick,
            text = {
                Text(
                    text = stringResource(text),
                    style = MaterialTheme.typography.bodyLarge
                )
            }
        )

    if (isDefault) {
        var expanded by remember { mutableStateOf(false) }

        if (onNavigate != null) {
            IconButton(onNavigate) { Icon(Icons.Outlined.Notifications, null) }
        }

        IconButton(
            onClick = { expanded = !expanded },
            content = { Icon(Icons.Outlined.MoreVert, null) }
        )

        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false }
        ) {
            LocalDropDownItem(R.string.text_edit, setModifiable)
            LocalDropDownItem(R.string.text_delete, onShowDialog)
        }
    } else {
        IconButton(onSave) { Icon(Icons.Outlined.Check, null) }
    }
}