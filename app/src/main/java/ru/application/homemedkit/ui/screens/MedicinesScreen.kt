package ru.application.homemedkit.ui.screens

import android.app.Activity
import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.toggleable
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material.icons.outlined.Clear
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.Checkbox
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import ru.application.homemedkit.R.drawable.vector_filter
import ru.application.homemedkit.R.drawable.vector_scanner
import ru.application.homemedkit.R.drawable.vector_sort
import ru.application.homemedkit.R.string.preference_kits_group
import ru.application.homemedkit.R.string.text_clear
import ru.application.homemedkit.R.string.text_enter_product_name
import ru.application.homemedkit.R.string.text_exit_app
import ru.application.homemedkit.R.string.text_no
import ru.application.homemedkit.R.string.text_no_data_found
import ru.application.homemedkit.R.string.text_save
import ru.application.homemedkit.R.string.text_yes
import ru.application.homemedkit.data.model.KitMedicines
import ru.application.homemedkit.data.model.MedicineList
import ru.application.homemedkit.helpers.Preferences
import ru.application.homemedkit.helpers.enums.Sorting
import ru.application.homemedkit.models.states.MedicinesState
import ru.application.homemedkit.models.viewModels.MedicinesViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MedicinesScreen(navigateToScanner: () -> Unit, navigateToMedicine: (Long) -> Unit) {
    val activity = LocalContext.current as Activity

    val model = viewModel<MedicinesViewModel>()
    val state by model.state.collectAsStateWithLifecycle()
    val medicines by model.medicines.collectAsStateWithLifecycle()
    val kits by model.kits.collectAsStateWithLifecycle()
    val offset by remember { derivedStateOf { state.listState.firstVisibleItemScrollOffset } }

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
                                        selected = entry.type == state.sorting,
                                        onClick = { model.setSorting(entry.type) },
                                        role = Role.RadioButton
                                    )
                            ) {
                                RadioButton(entry.type == state.sorting, null)
                                Text(stringResource(entry.title), Modifier.padding(start = 16.dp))
                            }
                        }
                    }

                    IconButton(model::showFilter) { Icon(painterResource(vector_filter), null) }
                }
            )
        },
        floatingActionButton = {
            AnimatedVisibility(
                visible = !state.listState.isScrollInProgress && offset.dp <= 100.dp,
                enter = slideInVertically(initialOffsetY = { it * 2 }),
                exit = slideOutVertically(targetOffsetY = { it * 2 })
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    AnimatedVisibility(state.showAdding) {
                        Column {
                            ElevatedCard(
                                onClick = navigateToScanner,
                                colors = CardDefaults.cardColors(MaterialTheme.colorScheme.primaryContainer)
                            ) {
                                Icon(
                                    painter = painterResource(vector_scanner),
                                    contentDescription = null,
                                    modifier = Modifier
                                        .size(36.dp)
                                        .padding(4.dp)
                                )
                            }

                            ElevatedCard(
                                onClick = { navigateToMedicine(0L) },
                                colors = CardDefaults.cardColors(MaterialTheme.colorScheme.primaryContainer)
                            ) {
                                Icon(
                                    imageVector = Icons.Outlined.Edit,
                                    contentDescription = null,
                                    modifier = Modifier
                                        .size(36.dp)
                                        .padding(4.dp)
                                )
                            }
                        }
                    }
                    FloatingActionButton(model::showAdding) { Icon(Icons.Outlined.Add, null) }
                }
            }
        }
    ) { values ->
        medicines.let { list ->
            if (list.isNotEmpty())
                LazyColumn(Modifier, state.listState, values) {
                    items(list, MedicineList::id) {
                        MedicineItem(it, Modifier.animateItem(), navigateToMedicine)
                        HorizontalDivider()
                    }
                }
            else Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp)
            ) {
                Text(
                    text = stringResource(text_no_data_found),
                    textAlign = TextAlign.Center
                )
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
private fun MedicineItem(medicine: MedicineList, modifier: Modifier, navigateToMedicine: (Long) -> Unit) =
    ListItem(
        modifier = modifier.clickable { navigateToMedicine(medicine.id) },
        headlineContent = { Text(medicine.title) },
        leadingContent = { MedicineImage(medicine.image, Modifier.size(56.dp)) },
        overlineContent = { Text(medicine.formName) },
        supportingContent = {
            Row(Modifier.fillMaxWidth(), Arrangement.SpaceBetween) {
                Text(medicine.expDateS)
                Text("${medicine.prodAmount} ${stringResource(medicine.doseType)}")
            }
        },
        colors = ListItemDefaults.colors(
            if (medicine.expDateL >= System.currentTimeMillis()) ListItemDefaults.containerColor
            else MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.7f)
        )
    )

@Composable
private fun DialogKits(
    kits: List<KitMedicines>,
    state: MedicinesState,
    pick: (KitMedicines) -> Unit,
    show: () -> Unit,
    clear: () -> Unit
) = AlertDialog(
    onDismissRequest = show,
    confirmButton = { TextButton(show) { Text(stringResource(text_save)) } },
    dismissButton = { TextButton(clear) { Text(stringResource(text_clear)) } },
    title = { Text(stringResource(preference_kits_group)) },
    text = {
        LazyColumn {
            items(kits) { kit ->
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