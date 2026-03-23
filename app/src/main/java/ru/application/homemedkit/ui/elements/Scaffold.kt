@file:OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterial3ExpressiveApi::class)

package ru.application.homemedkit.ui.elements

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.text.input.clearText
import androidx.compose.foundation.text.input.rememberTextFieldState
import androidx.compose.material3.AppBarWithSearch
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.SearchBarDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.rememberSearchBarState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import ru.application.homemedkit.R
import ru.application.homemedkit.utils.BLANK
import ru.application.homemedkit.utils.extensions.collectLatestChanged

@Composable
fun ScaffoldSearchBar(
    search: String,
    onSearch: (String) -> Unit,
    onClear: () -> Unit = { onSearch(BLANK) },
    navigationIcon: @Composable (() -> Unit)? = null,
    actions: @Composable (RowScope.() -> Unit)? = null,
    floatingActionButton: @Composable BoxScope.() -> Unit = {},
    content: @Composable () -> Unit,
) {
    val searchBarState = rememberSearchBarState()
    val textFieldState = rememberTextFieldState(search)

    LaunchedEffect(textFieldState) {
        snapshotFlow { textFieldState.text.toString() }.collectLatestChanged(onSearch)
    }

    LaunchedEffect(search) {
        if (search.isEmpty()) {
            textFieldState.clearText()
        }
    }

    Box(Modifier.fillMaxSize()) {
        Column(Modifier.fillMaxSize()) {
            AppBarWithSearch(
                state = searchBarState,
                navigationIcon = navigationIcon,
                actions = actions,
                inputField = {
                    SearchBarDefaults.InputField(
                        searchBarState = searchBarState,
                        textFieldState = textFieldState,
                        onSearch = { },
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

            Box(
                content = { content() },
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
            )
        }

        floatingActionButton()
    }
}