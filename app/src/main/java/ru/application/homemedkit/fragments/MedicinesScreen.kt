package ru.application.homemedkit.fragments

import android.content.Context
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.outlined.Clear
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.ramcosta.composedestinations.annotation.Destination
import com.ramcosta.composedestinations.annotation.RootGraph
import com.ramcosta.composedestinations.generated.destinations.MedicineScreenDestination
import com.ramcosta.composedestinations.navigation.DestinationsNavigator
import ru.application.homemedkit.R
import ru.application.homemedkit.databaseController.Medicine
import ru.application.homemedkit.helpers.BLANK
import ru.application.homemedkit.helpers.DateHelper
import ru.application.homemedkit.helpers.FiltersHelper
import ru.application.homemedkit.helpers.Preferences
import ru.application.homemedkit.helpers.SortingHelper
import ru.application.homemedkit.helpers.formName
import ru.application.homemedkit.helpers.shortName

@OptIn(ExperimentalMaterial3Api::class)
@Destination<RootGraph>
@Composable
fun MedicinesScreen(navigator: DestinationsNavigator, context: Context = LocalContext.current) {
    var comparator by remember {
        mutableStateOf(SortingHelper(Preferences(context).getSortingOrder()).getSorting())
    }
    var text by rememberSaveable { mutableStateOf(BLANK) }
    var showMenu by rememberSaveable { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    TextField(
                        value = text,
                        onValueChange = { text = it },
                        modifier = Modifier.fillMaxWidth(),
                        label = { Text(context.getString(R.string.text_enter_product_name)) },
                        leadingIcon = { Icon(Icons.Default.Search, null) },
                        trailingIcon = {
                            if (text.isNotEmpty())
                                IconButton({ text = BLANK })
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
                    IconButton({ showMenu = true }) {
                        Icon(
                            painter = painterResource(R.drawable.vector_sort),
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onSurface
                        )
                    }

                    DropdownMenu(showMenu, { showMenu = false }) {
                        DropdownMenuItem(
                            text = { Text(context.getString(R.string.sorting_a_z)) },
                            onClick = { comparator = SortingHelper.inName }
                        )
                        DropdownMenuItem(
                            text = { Text(context.getString(R.string.sorting_z_a)) },
                            onClick = { comparator = SortingHelper.reName }
                        )
                        DropdownMenuItem(
                            text = { Text(context.getString(R.string.sorting_from_oldest)) },
                            onClick = { comparator = SortingHelper.inDate }
                        )
                        DropdownMenuItem(
                            text = { Text(context.getString(R.string.sorting_from_newest)) },
                            onClick = { comparator = SortingHelper.reDate }
                        )
                    }
                }
            )
        }
    ) { paddingValues ->
        val filtered = FiltersHelper(context).medicines(text).sortedWith(comparator)

        LazyColumn(
            modifier = Modifier.padding(horizontal = 16.dp),
            contentPadding = paddingValues,
            verticalArrangement = Arrangement.spacedBy(16.dp),
        )
        { items(filtered.size) { MedicineCard(filtered[it], navigator) } }
    }
}

@Composable
private fun MedicineCard(medicine: Medicine, navigator: DestinationsNavigator) {
    val shortName = shortName(medicine.productName)
    val formName = formName(medicine.prodFormNormName)
    val expDate = DateHelper.inCard(medicine.expDate)

    ElevatedCard(
        modifier = Modifier
            .fillMaxWidth()
            .wrapContentHeight()
            .padding(vertical = 8.dp)
            .clickable { navigator.navigate(MedicineScreenDestination(id = medicine.id)) },
        colors = CardDefaults.cardColors(
            containerColor = when {
                medicine.expDate < System.currentTimeMillis() -> MaterialTheme.colorScheme.errorContainer
                else -> MaterialTheme.colorScheme.secondaryContainer
            }
        )
    ) {
        Column(
            modifier = Modifier.padding(start = 36.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                text = shortName,
                modifier = Modifier.padding(top = 16.dp, end = 8.dp),
                color = MaterialTheme.colorScheme.onSecondaryContainer,
                fontWeight = FontWeight.Bold,
                style = MaterialTheme.typography.headlineSmall
            )
            Text(
                text = formName,
                color = MaterialTheme.colorScheme.onSecondaryContainer,
                style = MaterialTheme.typography.bodyLarge
            )
            Text(
                text = expDate,
                modifier = Modifier.padding(bottom = 8.dp),
                color = MaterialTheme.colorScheme.onSecondaryContainer,
                style = MaterialTheme.typography.bodyMedium
            )
        }
    }
}