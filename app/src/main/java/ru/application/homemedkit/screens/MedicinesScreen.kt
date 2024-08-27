package ru.application.homemedkit.screens

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Clear
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
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
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.rememberAsyncImagePainter
import com.ramcosta.composedestinations.annotation.Destination
import com.ramcosta.composedestinations.annotation.RootGraph
import com.ramcosta.composedestinations.generated.destinations.MedicineScreenDestination
import com.ramcosta.composedestinations.navigation.DestinationsNavigator
import ru.application.homemedkit.R
import ru.application.homemedkit.R.string.preference_kits_group
import ru.application.homemedkit.R.string.text_all
import ru.application.homemedkit.R.string.text_cancel
import ru.application.homemedkit.R.string.text_enter_product_name
import ru.application.homemedkit.R.string.text_no_data_found
import ru.application.homemedkit.R.string.text_save
import ru.application.homemedkit.HomeMeds.Companion.database
import ru.application.homemedkit.data.dto.Medicine
import ru.application.homemedkit.helpers.BLANK
import ru.application.homemedkit.helpers.ICONS_MED
import ru.application.homemedkit.helpers.TYPE
import ru.application.homemedkit.helpers.formName
import ru.application.homemedkit.helpers.inCard
import ru.application.homemedkit.helpers.shortName
import ru.application.homemedkit.viewModels.MedicinesState
import ru.application.homemedkit.viewModels.MedicinesViewModel
import ru.application.homemedkit.viewModels.SortingItems
import java.io.File

@OptIn(ExperimentalMaterial3Api::class)
@Destination<RootGraph>
@Composable
fun MedicinesScreen(navigator: DestinationsNavigator) {
    val model = viewModel<MedicinesViewModel>()
    val state by model.state.collectAsStateWithLifecycle()
    val medicines by model.medicines.collectAsStateWithLifecycle()

    if (state.showFilter) KitsDialog(model, state)
    BackHandler{}
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
                        trailingIcon = {
                            if (state.search.isNotEmpty())
                                IconButton(model::clearSearch)
                                { Icon(Icons.Outlined.Clear, null) }
                        },
                        singleLine = true,
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
                    IconButton(model::showSort) {
                        Icon(
                            painter = painterResource(R.drawable.vector_sort),
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onSurface
                        )
                    }

                    DropdownMenu(state.showSort, model::hideSort) {
                        SortingItems.entries.forEach { entry ->
                            DropdownMenuItem(
                                { Text(stringResource(entry.text)) },
                                { model.setSorting(entry.sorting) }
                            )
                        }
                    }

                    IconButton(model::showFilter) {
                        Icon(
                            painter = painterResource(R.drawable.vector_filter),
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onSurface
                        )
                    }
                }
            )
        }
    ) { values ->
        medicines.let { list ->
            if (list.isEmpty()) Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp),
                contentAlignment = Alignment.Center
            ) { Text(stringResource(text_no_data_found), textAlign = TextAlign.Center) }
            else LazyColumn(
                state = state.listState,
                contentPadding = PaddingValues(16.dp, values.calculateTopPadding(), 16.dp, 0.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) { items(list) { MedicineCard(it, navigator) } }
        }
    }
}

@Composable
private fun MedicineCard(medicine: Medicine, navigator: DestinationsNavigator) {
    val shortName = shortName(medicine.productName)
    val formName = formName(medicine.prodFormNormName)
    val expDate = inCard(medicine.expDate)
    val kitTitle = database.medicineDAO().getKitTitle(medicine.kitId) ?: BLANK
    val icon = medicine.image.let {
        when {
            it.contains(TYPE) -> ICONS_MED[it]
            it.isEmpty() -> R.drawable.vector_type_unknown
            else -> File(LocalContext.current.filesDir, it).run {
                if (exists()) this else R.drawable.vector_type_unknown
            }
        }
    }

    ListItem(
        headlineContent = {
            Text(
                text = shortName,
                modifier = Modifier.padding(vertical = 8.dp),
                overflow = TextOverflow.Clip,
                softWrap = false,
                style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.SemiBold)
            )
        },
        modifier = Modifier
            .clickable { navigator.navigate(MedicineScreenDestination(id = medicine.id)) }
            .padding(vertical = 8.dp)
            .clip(MaterialTheme.shapes.medium),
        overlineContent = { Text(text = formName, style = MaterialTheme.typography.labelLarge) },
        supportingContent = { Text(text = expDate, fontWeight = FontWeight.SemiBold) },
        leadingContent = {
            Image(
                painter = rememberAsyncImagePainter(icon),
                contentDescription = null,
                modifier = Modifier
                    .size(64.dp)
                    .offset(y = 12.dp)
            )
        },
        trailingContent = { Text(text = kitTitle, style = MaterialTheme.typography.labelLarge) },
        colors = ListItemDefaults.colors(
            containerColor = when {
                medicine.expDate < System.currentTimeMillis() -> MaterialTheme.colorScheme.errorContainer
                else -> MaterialTheme.colorScheme.secondaryContainer
            }
        )
    )
}

@Composable
private fun KitsDialog(model: MedicinesViewModel, state: MedicinesState) = AlertDialog(
    onDismissRequest = model::hideFilter,
    confirmButton = { TextButton(model::saveFilter) { Text(stringResource(text_save)) } },
    dismissButton = { TextButton(model::hideFilter) { Text(stringResource(text_cancel)) } },
    title = { Text(stringResource(preference_kits_group)) },
    text = {
        Column(Modifier.selectableGroup()) {
            Row(
                Modifier
                    .fillMaxWidth()
                    .height(56.dp)
                    .selectable(
                        selected = state.kitId == 0L,
                        onClick = { model.setFilter(0L) },
                        role = Role.RadioButton
                    ),
                verticalAlignment = Alignment.CenterVertically
            ) {
                RadioButton(state.kitId == 0L, null)
                Text(
                    text = stringResource(text_all),
                    style = MaterialTheme.typography.bodyLarge,
                    modifier = Modifier.padding(start = 16.dp)
                )
            }

            database.kitDAO().getAll().forEach { kit ->
                Row(
                    Modifier
                        .fillMaxWidth()
                        .height(56.dp)
                        .selectable(
                            selected = state.kitId == kit.kitId,
                            onClick = { model.setFilter(kit.kitId) },
                            role = Role.RadioButton
                        ),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    RadioButton(state.kitId == kit.kitId, null)
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