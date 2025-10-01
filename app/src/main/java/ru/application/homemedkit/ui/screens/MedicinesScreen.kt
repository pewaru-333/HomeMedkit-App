package ru.application.homemedkit.ui.screens

import android.app.Activity
import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.Crossfade
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.selection.selectable
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledTonalIconToggleButton
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SmallFloatingActionButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import ru.application.homemedkit.R
import ru.application.homemedkit.R.string.text_exit_app
import ru.application.homemedkit.R.string.text_no
import ru.application.homemedkit.R.string.text_yes
import ru.application.homemedkit.data.model.MedicineList
import ru.application.homemedkit.models.viewModels.MedicinesViewModel
import ru.application.homemedkit.ui.elements.BoxWithEmptyListText
import ru.application.homemedkit.ui.elements.DialogKits
import ru.application.homemedkit.ui.elements.MedicineImage
import ru.application.homemedkit.ui.elements.SearchAppBar
import ru.application.homemedkit.ui.elements.TextDate
import ru.application.homemedkit.ui.elements.VectorIcon
import ru.application.homemedkit.ui.navigation.Screen
import ru.application.homemedkit.utils.di.Preferences
import ru.application.homemedkit.utils.enums.MedicineTab
import ru.application.homemedkit.utils.enums.Sorting

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MedicinesScreen(onNavigate: (Screen) -> Unit) {
    val activity = LocalContext.current as Activity

    val model = viewModel<MedicinesViewModel>()
    val state by model.state.collectAsStateWithLifecycle()
    val medicines by model.medicines.collectAsStateWithLifecycle()
    val grouped by model.grouped.collectAsStateWithLifecycle()
    val kits by model.kits.collectAsStateWithLifecycle()

    val listStates = MedicineTab.entries.map { rememberLazyListState() }

    LaunchedEffect(state.search, state.sorting, state.kits) {
        listStates[state.tab.ordinal].scrollToItem(0)
    }

    BackHandler { model.showExit(true) }
    Scaffold(
        topBar = {
            SearchAppBar(
                search = state.search,
                onSearch = model::setSearch,
                onClear = model::clearSearch,
                actions = {
                    IconButton(model::showSort) { VectorIcon(R.drawable.vector_sort) }
                    DropdownMenu(state.showSort, model::showSort) {
                        Sorting.entries.forEach { entry ->
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(56.dp)
                                    .padding(horizontal = 16.dp)
                                    .selectable(
                                        role = Role.RadioButton,
                                        selected = entry == state.sorting,
                                        onClick = { model.setSorting(entry) }
                                    )
                            ) {
                                RadioButton(entry == state.sorting, null)
                                Text(stringResource(entry.title), Modifier.padding(start = 16.dp))
                            }
                        }
                    }

                    IconButton(model::showFilter) {
                        BadgedBox(
                            badge = { if (state.kits.isNotEmpty()) Badge() },
                            content = { VectorIcon(R.drawable.vector_filter) }
                        )
                    }

                    FilledTonalIconToggleButton(
                        checked = true,
                        onCheckedChange = { model.toggleView() },
                        content = {
                            VectorIcon(
                                icon = when (state.tab) {
                                    MedicineTab.LIST -> R.drawable.vector_group
                                    MedicineTab.GROUPS -> R.drawable.vector_list
                                }
                            )
                        }
                    )
                }
            )
        },
        floatingActionButton = {
            AnimatedVisibility(
                visible = listStates.all { !it.isScrollInProgress },
                enter = slideInVertically(initialOffsetY = { it * 2 }),
                exit = slideOutVertically(targetOffsetY = { it * 2 })
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    AnimatedVisibility(
                        visible = state.showAdding,
                        enter = fadeIn() + slideInVertically() + scaleIn(),
                        exit = fadeOut() + slideOutVertically() + scaleOut()
                    ) {
                        Column(
                            horizontalAlignment = Alignment.End,
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            SmallFloatingActionButton(
                                onClick = { onNavigate(Screen.Scanner) },
                                containerColor = MaterialTheme.colorScheme.secondaryContainer,
                                content = { VectorIcon(R.drawable.vector_scanner) }
                            )

                            SmallFloatingActionButton(
                                onClick = { onNavigate(Screen.Medicine()) },
                                containerColor = MaterialTheme.colorScheme.secondaryContainer,
                                content = { VectorIcon(R.drawable.vector_edit) }
                            )
                        }
                    }

                    FloatingActionButton(model::showAdding) {
                        val rotation by animateFloatAsState(if (state.showAdding) 45f else 0f)

                        VectorIcon(
                            icon = R.drawable.vector_add,
                            modifier = Modifier.rotate(rotation)
                        )
                    }
                }
            }
        }
    ) { values ->
        Crossfade(state.tab) { tab ->
            when (tab) {
                MedicineTab.LIST -> medicines.let { list ->
                    if (list.isNotEmpty()) {
                        LazyColumn(Modifier.fillMaxSize(), listStates[0], values) {
                            items(list, MedicineList::id) {
                                MedicineItem(
                                    medicine = it,
                                    modifier = Modifier.animateItem(),
                                    onClick = { onNavigate(Screen.Medicine(it)) }
                                )

                                if (it != list.lastOrNull()) {
                                    HorizontalDivider()
                                }
                            }
                        }
                    } else {
                        BoxWithEmptyListText(
                            text = R.string.text_no_data_found,
                            modifier = Modifier
                                .padding(values)
                                .fillMaxSize()
                        )
                    }
                }

                MedicineTab.GROUPS -> grouped.let { list ->
                    if (list.isNotEmpty()) {
                        LazyColumn(Modifier.fillMaxSize(), listStates[1], values) {
                            list.forEach { group ->
                                item {
                                    TextDate(group.kit.title.asString())
                                }

                                items(
                                    items = group.medicines,
                                    key = { "${group.kit.id}_${it.id}" }
                                ) {
                                    MedicineItem(
                                        medicine = it,
                                        modifier = Modifier.animateItem(),
                                        onClick = { onNavigate(Screen.Medicine(it)) }
                                    )

                                    if (it != group.medicines.lastOrNull()) {
                                        HorizontalDivider()
                                    }
                                }
                            }
                        }
                    } else {
                        BoxWithEmptyListText(
                            text = R.string.text_no_data_group_found,
                            modifier = Modifier
                                .padding(values)
                                .fillMaxSize()
                        )
                    }
                }
            }
        }
    }

    when {
        state.showFilter -> DialogKits(
            kits = kits,
            isChecked = { it in state.kits },
            onPick = model::pickFilter,
            onDismiss = model::showFilter,
            onClear = model::clearFilter
        )

        state.showExit -> if (!Preferences.confirmExit) activity.finishAndRemoveTask()
        else DialogExit(model::showExit, activity::finishAndRemoveTask)
    }
}

@Composable
private fun MedicineItem(medicine: MedicineList, modifier: Modifier, onClick: (Long) -> Unit) =
    ListItem(
        modifier = modifier.clickable { onClick(medicine.id) },
        leadingContent = { MedicineImage(medicine.image, Modifier.size(56.dp)) },
        headlineContent = {
            Text(
                text = medicine.title,
                overflow = TextOverflow.Ellipsis,
                maxLines = 1
            )
        },
        overlineContent = {
            Text(
                text = medicine.formName,
                style = MaterialTheme.typography.labelMedium
            )
        },
        supportingContent = {
            Row(Modifier.fillMaxWidth(), Arrangement.SpaceBetween) {
                Text(medicine.expDateS)
                Text("${medicine.prodAmount} ${stringResource(medicine.doseType)}")
            }
        },
        colors = ListItemDefaults.colors(
            containerColor = when {
                medicine.isExpired -> MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.6f)
                !medicine.inStock -> MaterialTheme.colorScheme.scrim.copy(alpha = 0.15f)
                else -> MaterialTheme.colorScheme.surfaceContainerLow
            }
        )
    )

@Composable
private fun DialogExit(onDismiss: () -> Unit, onExit: () -> Unit) =
    AlertDialog(
        onDismissRequest = onDismiss,
        confirmButton = { TextButton(onExit) { Text(stringResource(text_yes)) } },
        dismissButton = { TextButton(onDismiss) { Text(stringResource(text_no)) } },
        text = {
            Text(
                text = stringResource(text_exit_app),
                style = MaterialTheme.typography.bodyLarge
            )
        }
    )