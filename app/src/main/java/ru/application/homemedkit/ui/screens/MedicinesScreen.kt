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
import androidx.compose.foundation.selection.toggleable
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material.icons.outlined.Clear
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.Checkbox
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledTonalIconToggleButton
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SmallFloatingActionButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import ru.application.homemedkit.R
import ru.application.homemedkit.R.drawable.vector_filter
import ru.application.homemedkit.R.drawable.vector_scanner
import ru.application.homemedkit.R.drawable.vector_sort
import ru.application.homemedkit.R.string.preference_kits_group
import ru.application.homemedkit.R.string.text_clear
import ru.application.homemedkit.R.string.text_enter_product_name
import ru.application.homemedkit.R.string.text_exit_app
import ru.application.homemedkit.R.string.text_no
import ru.application.homemedkit.R.string.text_save
import ru.application.homemedkit.R.string.text_yes
import ru.application.homemedkit.data.dto.Kit
import ru.application.homemedkit.data.model.MedicineList
import ru.application.homemedkit.models.states.MedicinesState
import ru.application.homemedkit.models.viewModels.MedicinesViewModel
import ru.application.homemedkit.ui.elements.BoxWithEmptyListText
import ru.application.homemedkit.ui.navigation.Screen
import ru.application.homemedkit.utils.Preferences
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
        listStates[state.tab.ordinal].animateScrollToItem(0)
    }

    BackHandler { model.showExit(true) }
    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    TextField(
                        value = state.search,
                        onValueChange = model::setSearch,
                        modifier = Modifier.fillMaxWidth(),
                        label = { Text(stringResource(text_enter_product_name)) },
                        leadingIcon = { Icon(Icons.Outlined.Search, null) },
                        singleLine = true,
                        trailingIcon = {
                            if (state.search.isNotEmpty())
                                IconButton(model::clearSearch)
                                { Icon(Icons.Outlined.Clear, null) }
                        },
                        colors = TextFieldDefaults.colors(
                            focusedContainerColor = MaterialTheme.colorScheme.surface,
                            unfocusedContainerColor = MaterialTheme.colorScheme.surface,
                            focusedIndicatorColor = Color.Transparent,
                            unfocusedIndicatorColor = Color.Transparent,
                            disabledIndicatorColor = Color.Transparent,
                        )
                    )
                },
                modifier = Modifier.drawBehind {
                    drawLine(Color.LightGray, Offset(0f, size.height), Offset(size.width, size.height), 4f)
                },
                actions = {
                    IconButton(model::showSort) { Icon(painterResource(vector_sort), null) }
                    DropdownMenu(state.showSort, model::showSort) {
                        Sorting.entries.forEach { entry ->
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(56.dp)
                                    .padding(horizontal = 16.dp)
                                    .selectable(
                                        selected = entry == state.sorting,
                                        onClick = { model.setSorting(entry) },
                                        role = Role.RadioButton
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
                            content = { Icon(painterResource(vector_filter), null) }
                        )
                    }

                    FilledTonalIconToggleButton(
                        checked = true,
                        onCheckedChange = { model.toggleView() },
                        content = {
                            Icon(
                                contentDescription = null,
                                painter = painterResource(
                                    when (state.tab) {
                                        MedicineTab.LIST -> R.drawable.vector_group
                                        MedicineTab.GROUPS -> R.drawable.vector_list
                                    }
                                )
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
                                containerColor = MaterialTheme.colorScheme.secondaryContainer
                            ) {
                                Icon(
                                    painter = painterResource(vector_scanner),
                                    contentDescription = null
                                )
                            }

                            SmallFloatingActionButton(
                                onClick = { onNavigate(Screen.Medicine()) },
                                containerColor = MaterialTheme.colorScheme.secondaryContainer
                            ) {
                                Icon(
                                    imageVector = Icons.Outlined.Edit,
                                    contentDescription = null
                                )
                            }
                        }
                    }

                    FloatingActionButton(model::showAdding) {
                        val rotation by animateFloatAsState(if (state.showAdding) 45f else 0f)

                        Icon(
                            imageVector = Icons.Outlined.Add,
                            contentDescription = null,
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
            state = state,
            pick = model::pickFilter,
            show = model::showFilter,
            clear = model::clearFilter
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
private fun DialogKits(
    kits: List<Kit>,
    state: MedicinesState,
    pick: (Kit) -> Unit,
    show: () -> Unit,
    clear: () -> Unit
) = AlertDialog(
    onDismissRequest = show,
    confirmButton = { TextButton(show) { Text(stringResource(text_save)) } },
    dismissButton = { TextButton(clear) { Text(stringResource(text_clear)) } },
    title = { Text(stringResource(preference_kits_group)) },
    text = {
        LazyColumn {
            items(kits, Kit::kitId) { kit ->
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp)
                        .toggleable(
                            value = kit in state.kits,
                            onValueChange = { pick(kit) },
                            role = Role.Checkbox
                        )
                ) {
                    Checkbox(kit in state.kits, null)
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

@Composable
private fun DialogExit(dismiss: () -> Unit, exit: () -> Unit) =
    AlertDialog(
        onDismissRequest = dismiss,
        confirmButton = { TextButton(exit) { Text(stringResource(text_yes)) } },
        dismissButton = { TextButton(dismiss) { Text(stringResource(text_no)) } },
        text = {
            Text(
                text = stringResource(text_exit_app),
                style = MaterialTheme.typography.bodyLarge
            )
        }
    )