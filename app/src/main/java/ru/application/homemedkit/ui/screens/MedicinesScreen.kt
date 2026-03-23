@file:OptIn(ExperimentalMaterial3ExpressiveApi::class, ExperimentalMaterial3Api::class)

package ru.application.homemedkit.ui.screens

import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.LocalActivity
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.selection.toggleable
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.Checkbox
import androidx.compose.material3.DropdownMenuGroup
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.DropdownMenuPopup
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.FloatingActionButtonMenu
import androidx.compose.material3.FloatingActionButtonMenuItem
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.ListItem
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.ListItemShapes
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MenuDefaults
import androidx.compose.material3.SegmentedListItem
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.ToggleFloatingActionButton
import androidx.compose.material3.animateFloatingActionButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.util.fastForEach
import androidx.compose.ui.util.fastForEachIndexed
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
import ru.application.homemedkit.ui.elements.IconButton
import ru.application.homemedkit.ui.elements.MedicineImage
import ru.application.homemedkit.ui.elements.ScaffoldSearchBar
import ru.application.homemedkit.ui.elements.TextDate
import ru.application.homemedkit.ui.elements.VectorIcon
import ru.application.homemedkit.ui.navigation.Screen
import ru.application.homemedkit.utils.di.Preferences
import ru.application.homemedkit.utils.enums.MedicineListView
import ru.application.homemedkit.utils.enums.Sorting
import ru.application.homemedkit.utils.extensions.drawHorizontalDivider

@Composable
fun MedicinesScreen(model: MedicinesViewModel = viewModel(), onNavigate: (Screen) -> Unit) {
    val activity = LocalActivity.current as? ComponentActivity

    val state by model.state.collectAsStateWithLifecycle()
    val medicines by model.medicines.collectAsStateWithLifecycle()
    val grouped by model.grouped.collectAsStateWithLifecycle()
    val kits by model.kits.collectAsStateWithLifecycle()

    val pagerState = rememberPagerState(pageCount = MedicineListView.entries::size)

    val listStates = MedicineListView.entries.map { rememberLazyListState() }
    val listState = remember(state.listView.ordinal) { listStates[state.listView.ordinal] }

    val currentParams = remember(state.search, state.sorting, state.kits) {
        Triple(state.search, state.sorting, state.kits)
    }
    val oldParams = remember { mutableStateOf(currentParams) }

    LaunchedEffect(medicines) {
        if (oldParams.value != currentParams) {
            if (medicines.isNotEmpty()) {
                listState.scrollToItem(0)
            }

            oldParams.value = currentParams
        }
    }

    LaunchedEffect(state.listView) {
        pagerState.animateScrollToPage(state.listView.ordinal)
    }

    val onItemClick = remember {
        { id: Long -> onNavigate(Screen.Medicine(id)) }
    }

    val color = MaterialTheme.colorScheme.outlineVariant

    BackHandler { model.showExit(true) }
    ScaffoldSearchBar(
        search = state.search,
        onSearch = model::onSearch,
        actions = {
            IconButton(model::showSorting) { VectorIcon(R.drawable.vector_sort) }
            DropdownMenuPopup(state.showSorting, model::showSorting) {
                DropdownMenuGroup(MenuDefaults.groupShape(0, 2)) {
                    MenuDefaults.Label {
                        Text(
                            text = stringResource(R.string.preference_sorting_type),
                            style = MaterialTheme.typography.labelMedium
                        )
                    }

                    HorizontalDivider(Modifier.padding(MenuDefaults.HorizontalDividerPadding))

                    Sorting.entries.fastForEachIndexed { index, entry ->
                        DropdownMenuItem(
                            selected = entry == state.sorting,
                            onClick = { model.setSorting(entry) },
                            text = { Text(stringResource(entry.title)) },
                            shapes = MenuDefaults.itemShape(index, Sorting.entries.size)
                        )
                    }
                }

                Spacer(Modifier.height(MenuDefaults.GroupSpacing))

                DropdownMenuGroup(MenuDefaults.groupShape(1, 2)) {
                    MenuDefaults.Label {
                        Text(
                            text = stringResource(R.string.text_list_view),
                            style = MaterialTheme.typography.labelMedium
                        )
                    }

                    HorizontalDivider(Modifier.padding(MenuDefaults.HorizontalDividerPadding))

                    MedicineListView.entries.fastForEachIndexed { index, entry ->
                        DropdownMenuItem(
                            selected = state.listView == entry,
                            onClick = { model.pickView(entry)  },
                            text = { Text(stringResource(entry.title)) },
                            shapes = MenuDefaults.itemShape(index, MedicineListView.entries.size)
                        )
                    }
                }
            }

            IconButton(model::toggleFilter) {
                BadgedBox(
                    badge = { if (state.kits.isNotEmpty()) Badge() },
                    content = { VectorIcon(R.drawable.vector_filter) }
                )
            }
        },
        floatingActionButton = {
            FloatingActionButtonMenu(
                modifier = Modifier.align(Alignment.BottomEnd),
                expanded = state.showAdding,
                button = {
                    ToggleFloatingActionButton(
                        checked = state.showAdding,
                        onCheckedChange = { model.toggleAdding() },
                        modifier = Modifier
                            .animateFloatingActionButton(
                                visible = listStates.all { !it.isScrollInProgress } || state.showAdding,
                                alignment = Alignment.BottomEnd
                            )
                    ) {
                        val rotation by animateFloatAsState(if (state.showAdding) 45f else 0f)

                        VectorIcon(
                            icon = R.drawable.vector_add,
                            modifier = Modifier.rotate(rotation),
                            tint = if (state.showAdding) MaterialTheme.colorScheme.onPrimary
                            else MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    }
                }
            ) {
                FloatingActionButtonMenuItem(
                    onClick = { onNavigate(Screen.Scanner) },
                    icon = { VectorIcon(R.drawable.vector_scanner) },
                    text = { Text(stringResource(R.string.text_scan)) }
                )
                FloatingActionButtonMenuItem(
                    onClick = { onNavigate(Screen.Medicine()) },
                    icon = { VectorIcon(R.drawable.vector_edit) },
                    text = { Text(stringResource(R.string.text_add)) }
                )
            }
        }
    ) {
        HorizontalPager(pagerState, userScrollEnabled = false) { page ->
            when (MedicineListView.entries[page]) {
                MedicineListView.LIST -> if (medicines.isNotEmpty()) {
                    LazyColumn(Modifier.fillMaxSize(), listStates[0]) {
                        items(medicines, MedicineList::id) { medicine ->
                            MedicineItem(
                                medicine = medicine,
                                onClick = onItemClick,
                                modifier = Modifier
                                    .animateItem()
                                    .drawHorizontalDivider(color)
                            )
                        }
                    }
                } else {
                    BoxWithEmptyListText(R.string.text_no_data_found)
                }

                MedicineListView.GROUPS -> if (grouped.isNotEmpty()) {
                    LazyColumn(Modifier.fillMaxSize(), listStates[1]) {
                        grouped.fastForEach { group ->
                            item(group.kit.id) {
                                TextDate(group.kit.title.asString())
                            }

                            val groupSize = group.medicines.size
                            itemsIndexed(
                                items = group.medicines,
                                key = { _, item -> Pair(group.kit.id, item.id) }
                            ) { index, medicine ->
                                SegmentedMedicineItem(
                                    medicine = medicine,
                                    onClick = onItemClick,
                                    modifier = Modifier.animateItem(),
                                    shapes = ListItemDefaults.segmentedShapes(index, groupSize)
                                )
                            }
                        }
                    }
                } else {
                    BoxWithEmptyListText(R.string.text_no_data_group_found)
                }
            }
        }
    }

    when {
        state.showFilter -> DialogKits(
            kits = kits,
            isChecked = { it in state.kits },
            onPick = model::pickFilter,
            onDismiss = model::toggleFilter,
            onClear = model::clearFilter,
            itemFilterEmpty = { ItemFilterEmptyMedicines(state.hideEmpty, model::hideEmpty) }
        )

        state.showExit -> if (!Preferences.confirmExit) activity?.finishAndRemoveTask()
        else activity?.let { DialogExit(model::showExit, it::finishAndRemoveTask) }
    }
}

@Composable
private fun MedicineItem(medicine: MedicineList, modifier: Modifier, onClick: (Long) -> Unit) =
    ListItem(
        modifier = modifier,
        onClick = { onClick(medicine.id) },
        leadingContent = { MedicineImage(medicine.image, Modifier.size(56.dp)) },
        overlineContent = { Text(medicine.formName) },
        content = {
            Text(
                text = medicine.title,
                overflow = TextOverflow.Ellipsis,
                maxLines = 1
            )
        },
        supportingContent = {
            Row(Modifier.fillMaxWidth(), Arrangement.SpaceBetween) {
                Text(medicine.expDateS)
                Text(medicine.prodAmountDoseType.asString())
            }
        },
        colors = ListItemDefaults.segmentedColors(
            containerColor = containerColor(
                inStock = medicine.inStock,
                isExpired = medicine.isExpired
            )
        )
    )

@Composable
private fun SegmentedMedicineItem(
    medicine: MedicineList,
    modifier: Modifier,
    shapes: ListItemShapes,
    onClick: (Long) -> Unit
) = SegmentedListItem(
    shapes = shapes,
    modifier = modifier.padding(ListItemDefaults.SegmentedGap),
    onClick = { onClick(medicine.id) },
    leadingContent = { MedicineImage(medicine.image, Modifier.size(56.dp)) },
    content = {
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
            Text(medicine.prodAmountDoseType.asString())
        }
    },
    colors = ListItemDefaults.segmentedColors(
        containerColor = containerColor(
            inStock = medicine.inStock,
            isExpired = medicine.isExpired
        )
    )
)

@Composable
private fun ItemFilterEmptyMedicines(isChecked: Boolean, onClick: (Boolean) -> Unit) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .fillMaxWidth()
            .height(56.dp)
            .toggleable(
                role = Role.Checkbox,
                value = isChecked,
                onValueChange = onClick
            )
    ) {
        Checkbox(isChecked, null)
        Text(
            text = stringResource(R.string.text_hide_empty),
            modifier = Modifier.padding(start = 16.dp),
            style = MaterialTheme.typography.bodyLarge
        )
    }

    HorizontalDivider()
}

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

@Composable
private fun containerColor(inStock: Boolean, isExpired: Boolean) = when {
    !inStock -> MaterialTheme.colorScheme.surfaceContainer
    isExpired -> MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.9f)
    else -> MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f)
}