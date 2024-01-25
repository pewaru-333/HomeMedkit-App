package ru.application.homemedkit.fragments

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.outlined.Clear
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.ViewCompositionStrategy
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.fragment.app.Fragment
import ru.application.homemedkit.R
import ru.application.homemedkit.activities.MedicineActivity
import ru.application.homemedkit.databaseController.Medicine
import ru.application.homemedkit.databaseController.MedicineDatabase
import ru.application.homemedkit.helpers.ConstantsHelper.BLANK
import ru.application.homemedkit.helpers.ConstantsHelper.ID
import ru.application.homemedkit.helpers.DateHelper
import ru.application.homemedkit.helpers.FiltersHelper
import ru.application.homemedkit.helpers.SettingsHelper
import ru.application.homemedkit.helpers.SortingHelper
import ru.application.homemedkit.helpers.StringHelper
import ru.application.homemedkit.ui.theme.AppTheme

class FragmentMedicines : Fragment() {

    private lateinit var preferences: SettingsHelper
    private lateinit var database: MedicineDatabase
    private lateinit var medicines: List<Medicine>
    private lateinit var types: Array<String>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        preferences = SettingsHelper(context)
        database = MedicineDatabase.getInstance(context)
        medicines = database.medicineDAO().all
        types = resources.getStringArray(R.array.sorting_types)
    }

    @OptIn(ExperimentalMaterial3Api::class)
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        return ComposeView(requireContext()).apply {
            setViewCompositionStrategy(ViewCompositionStrategy.DisposeOnViewTreeLifecycleDestroyed)
            setContent {
                AppTheme {
                    val sheetState = rememberModalBottomSheetState()
                    val sorting = SortingHelper(requireActivity()).getSorting()

                    var showBottomSheet by remember { mutableStateOf(false) }
                    var comparator by remember { mutableStateOf(sorting) }
                    var text by rememberSaveable { mutableStateOf(BLANK) }

                    Column {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.Center,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            OutlinedTextField(
                                value = text,
                                onValueChange = { text = it },
                                modifier = Modifier
                                    .height(64.dp)
                                    .padding(start = 16.dp, top = 4.dp)
                                    .weight(1f),
                                label = {
                                    Text(
                                        text = getString(R.string.text_enter_product_name),
                                        overflow = TextOverflow.Visible,
                                        softWrap = false
                                    )
                                },
                                leadingIcon = { Icon(Icons.Default.Search, null) },
                                trailingIcon = {
                                    if (text.isNotEmpty())
                                        IconButton(onClick = { text = BLANK })
                                        { Icon(Icons.Outlined.Clear, null) }
                                },
                                singleLine = true
                            )

                            IconButton(
                                onClick = { showBottomSheet = true },
                                modifier = Modifier.padding(top = 4.dp, start = 4.dp, end = 4.dp)
                            ) { Icon(painterResource(id = R.drawable.vector_sort), null) }
                        }

                        val filtered =
                            FiltersHelper(requireActivity()).medicines(text).sortedWith(comparator)
                        LazyColumn {
                            items(filtered.size) { index -> MedicineCard(filtered[index]) }
                        }

                        if (showBottomSheet) {
                            ModalBottomSheet(
                                onDismissRequest = { showBottomSheet = false },
                                sheetState = sheetState
                            ) {
                                Text(
                                    text = resources.getString(R.string.text_sort_type),
                                    modifier = Modifier.fillMaxWidth(),
                                    textAlign = TextAlign.Center,
                                    style = MaterialTheme.typography.headlineMedium
                                )
                                Column(
                                    modifier = Modifier
                                        .selectableGroup()
                                        .padding(16.dp, 16.dp, 16.dp, 64.dp),
                                    verticalArrangement = Arrangement.spacedBy(12.dp)
                                ) {
                                    ButtonSort(
                                        text = getString(R.string.sorting_a_z),
                                        sort = {
                                            comparator = SortingHelper.inName
                                            preferences.sortingOrder = types[0]
                                            filtered.sortedWith(comparator)
                                        }
                                    )
                                    ButtonSort(
                                        text = getString(R.string.sorting_z_a),
                                        sort = {
                                            comparator = SortingHelper.reName
                                            preferences.sortingOrder = types[1]
                                            filtered.sortedWith(comparator)
                                        }
                                    )
                                    ButtonSort(
                                        text = getString(R.string.sorting_from_oldest),
                                        sort = {
                                            comparator = SortingHelper.inDate
                                            preferences.sortingOrder = types[2]
                                            filtered.sortedWith(comparator)
                                        }
                                    )
                                    ButtonSort(
                                        text = getString(R.string.sorting_from_newest),
                                        sort = {
                                            comparator = SortingHelper.reDate
                                            preferences.sortingOrder = types[3]
                                            filtered.sortedWith(comparator)
                                        }
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    @Composable
    fun MedicineCard(medicine: Medicine) {
        val shortName = StringHelper.shortName(medicine.productName)
        val formName = StringHelper.formName(medicine.prodFormNormName)
        val expDate = DateHelper.inCard(medicine.expDate)

        ElevatedCard(
            modifier = Modifier
                .fillMaxWidth()
                .wrapContentHeight()
                .padding(16.dp)
                .clickable {
                    val intent = Intent(context, MedicineActivity::class.java)
                    intent.putExtra(ID, medicine.id)
                    startActivity(intent)
                },
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer)
        ) {
            Column(
                modifier = Modifier.padding(start = 36.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = shortName,
                    modifier = Modifier.padding(top = 16.dp),
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

    @Composable
    fun ButtonSort(text: String, sort: () -> Unit) {
        OutlinedButton(
            onClick = { sort() },
            modifier = Modifier
                .height(64.dp)
                .fillMaxWidth(),
            shape = RectangleShape
        ) {
            Text(
                modifier = Modifier.fillMaxWidth(),
                text = text,
                color = MaterialTheme.colorScheme.onSurface,
                fontSize = 20.sp,
                textAlign = TextAlign.Start
            )
        }
    }
}