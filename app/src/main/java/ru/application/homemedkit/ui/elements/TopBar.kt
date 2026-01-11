@file:OptIn(ExperimentalMaterial3ExpressiveApi::class)

package ru.application.homemedkit.ui.elements

import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.text.input.clearText
import androidx.compose.foundation.text.input.rememberTextFieldState
import androidx.compose.material3.AppBarWithSearch
import androidx.compose.material3.DropdownMenuGroup
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.DropdownMenuPopup
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.FilledIconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MenuDefaults
import androidx.compose.material3.OutlinedIconButton
import androidx.compose.material3.SearchBarDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.rememberSearchBarState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import ru.application.homemedkit.R
import ru.application.homemedkit.utils.BLANK
import ru.application.homemedkit.utils.extensions.collectLatestChanged

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SearchAppBar(
    search: String,
    onSearch: (String) -> Unit,
    onClear: () -> Unit = { onSearch(BLANK) },
    actions: @Composable (RowScope.() -> Unit) = {},
) {
    val searchState = rememberSearchBarState()
    val textFieldState = rememberTextFieldState(search)

    LaunchedEffect(textFieldState) {
        snapshotFlow { textFieldState.text.toString() }.collectLatestChanged(onSearch)
    }

    LaunchedEffect(search) {
        if (search.isEmpty()) {
            textFieldState.clearText()
        }
    }

    AppBarWithSearch(
        state = searchState,
        actions = actions,
        inputField = {
            SearchBarDefaults.InputField(
                textFieldState = textFieldState,
                searchBarState = searchState,
                onSearch = onSearch,
                leadingIcon = { VectorIcon(R.drawable.vector_search) },
                placeholder = { Text(stringResource(R.string.text_enter_product_name)) },
                trailingIcon = {
                    if (search.isNotEmpty()) {
                        IconButton(onClear) {
                            VectorIcon(R.drawable.vector_clear)
                        }
                    }
                }
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
    onReloadImages: (() -> Unit)? = null,
    onDuplicate: (() -> Unit)? = null,
    onNavigate: (() -> Unit)? = null
) {
    @Composable
    fun LocalDropDownItem(
        @StringRes text: Int,
        @DrawableRes icon: Int,
        shape: Shape,
        onClick: () -> Unit
    ) = DropdownMenuItem(
        onClick = onClick,
        shape = shape,
        trailingIcon = { VectorIcon(icon) },
        text = {
            Text(
                text = stringResource(text),
                modifier = Modifier.widthIn(112.dp, 280.dp)
            )
        }
    )

    if (isDefault) {
        var expanded by remember { mutableStateOf(false) }

        if (onNavigate != null) {
            FilledIconButton(
                onClick = onNavigate,
                shapes = IconButtonDefaults.shapes(),
                content = { VectorIcon(R.drawable.vector_notification) }
            )
        }

        IconButton(
            onClick = { expanded = !expanded },
            content = { VectorIcon(R.drawable.vector_dropdown_more) }
        )

        DropdownMenuPopup(
            expanded = expanded,
            onDismissRequest = { expanded = false }
        ) {
            DropdownMenuGroup(MenuDefaults.groupShape(0, 1)) {
                if (onDuplicate != null) {
                    LocalDropDownItem(
                        text = R.string.text_to_duplicate,
                        icon = R.drawable.vector_duplicate,
                        shape = MenuDefaults.leadingItemShape,
                        onClick = onDuplicate
                    )
                }

                LocalDropDownItem(
                    text = R.string.text_edit,
                    icon = R.drawable.vector_edit,
                    shape = if (onDuplicate == null) MenuDefaults.leadingItemShape else MenuDefaults.middleItemShape,
                    onClick = setModifiable
                )

                if (onReloadImages != null) {
                    LocalDropDownItem(
                        text = R.string.text_download_photos,
                        icon = R.drawable.vector_download,
                        shape = MenuDefaults.middleItemShape,
                        onClick = {
                            onReloadImages()
                            expanded = false
                        }
                    )
                }

                LocalDropDownItem(
                    text = R.string.text_delete,
                    icon = R.drawable.vector_delete,
                    shape = MenuDefaults.trailingItemShape,
                    onClick = {
                        onShowDialog()
                        expanded = false
                    }
                )
            }
        }
    } else {
        OutlinedIconButton(onSave) { VectorIcon(R.drawable.vector_confirm) }
    }
}