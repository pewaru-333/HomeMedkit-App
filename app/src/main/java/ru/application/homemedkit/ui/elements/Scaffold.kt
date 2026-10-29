@file:OptIn(ExperimentalFoundationStyleApi::class)

package ru.application.homemedkit.ui.elements

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.style.ExperimentalFoundationStyleApi
import androidx.compose.foundation.style.contentPadding
import androidx.compose.foundation.style.externalPadding
import androidx.compose.foundation.style.styleable
import androidx.compose.foundation.text.input.clearText
import androidx.compose.foundation.text.input.rememberTextFieldState
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.flow.collectLatest
import ru.application.homemedkit.R
import ru.application.homemedkit.utils.BLANK

@Composable
fun ScaffoldSearchBar(
    search: String,
    onSearch: (String) -> Unit,
    modifier: Modifier = Modifier,
    onClear: () -> Unit = { onSearch(BLANK) },
    navigationIcon: @Composable (() -> Unit)? = null,
    actions: @Composable (RowScope.() -> Unit)? = null,
    menuRow: @Composable (() -> Unit)? = null,
    floatingActionButton: (@Composable BoxScope.() -> Unit)? = null,
    content: @Composable () -> Unit
) {
    val searchBarState = rememberSearchBarState()
    val textFieldState = rememberTextFieldState(search)
    val currentOnSearch by rememberUpdatedState(onSearch)

    LaunchedEffect(textFieldState) {
        snapshotFlow { textFieldState.text.toString() }.collect {
            currentOnSearch(it)
        }
    }

    SideEffect(search) {
        if (search.isEmpty() && textFieldState.text.isNotEmpty()) {
            textFieldState.clearText()
        }
    }

    Box(modifier.fillMaxSize()) {
        Column(Modifier.fillMaxSize()) {
            InteractiveAppBarWithSearch(
                state = searchBarState,
                navigationIcon = navigationIcon,
                actions = actions,
                inputField = {
                    SearchBarDefaults.InputField(
                        textFieldState = textFieldState,
                        searchBarState = searchBarState,
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

            menuRow?.invoke()

            Box(
                content = { content() },
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
            )
        }

        floatingActionButton?.invoke(this)
    }
}

@Composable
private fun InteractiveAppBarWithSearch(
    state: SearchBarState,
    inputField: @Composable () -> Unit,
    modifier: Modifier = Modifier,
    navigationIcon: @Composable (() -> Unit)? = null,
    actions: @Composable (RowScope.() -> Unit)? = null,
    shape: Shape = SearchBarDefaults.inputFieldShape,
    windowInsets: WindowInsets = SearchBarDefaults.windowInsets,
) {
    val colors = SearchBarDefaults.colors()

    LaunchedEffect(state) {
        snapshotFlow { state.targetValue }
            .collectLatest { if (it != SearchBarValue.Collapsed) state.animateToCollapsed() }
    }

    Surface(
        color = MaterialTheme.colorScheme.surface,
        modifier = modifier
            .fillMaxWidth()
            .windowInsetsPadding(windowInsets)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.styleable {
                contentPadding(4.dp, 8.dp)
            }
        ) {
            navigationIcon?.let {
                Box(
                    content = { it() },
                    modifier = Modifier.styleable {
                        externalPadding(
                            start = 4.dp,
                            top = 0.dp,
                            end = 8.dp,
                            bottom = 0.dp
                        )
                    }
                )
            }

            Box(Modifier.weight(1f)) {
                Surface(
                    shape = shape,
                    color = colors.containerColor,
                    contentColor = contentColorFor(colors.containerColor),
                    tonalElevation = SearchBarDefaults.TonalElevation,
                    shadowElevation = SearchBarDefaults.ShadowElevation,
                    content = inputField,
                    modifier = Modifier
                        .align(Alignment.Center)
                        .widthIn(360.dp, 720.dp)
                        .fillMaxWidth()
                )
            }

            actions?.let {
                Box(
                    modifier = Modifier.styleable {
                        externalPadding(
                            start = 8.dp,
                            top = 0.dp,
                            end = 4.dp,
                            bottom = 0.dp
                        )
                    }
                ) {
                    Row(
                        horizontalArrangement = Arrangement.End,
                        verticalAlignment = Alignment.CenterVertically,
                        content = it,
                    )
                }
            }
        }
    }
}