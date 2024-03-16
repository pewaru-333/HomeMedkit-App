package ru.application.homemedkit.activities

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarData
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.OffsetMapping
import androidx.compose.ui.text.input.TransformedText
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.lifecycle.viewmodel.compose.viewModel
import com.google.accompanist.drawablepainter.rememberDrawablePainter
import ru.application.homemedkit.R
import ru.application.homemedkit.databaseController.Medicine
import ru.application.homemedkit.databaseController.MedicineDatabase
import ru.application.homemedkit.databaseController.Technical
import ru.application.homemedkit.dialogs.MonthYear
import ru.application.homemedkit.helpers.ConstantsHelper.ADD
import ru.application.homemedkit.helpers.ConstantsHelper.BLANK
import ru.application.homemedkit.helpers.ConstantsHelper.CIS
import ru.application.homemedkit.helpers.ConstantsHelper.DUPLICATE
import ru.application.homemedkit.helpers.ConstantsHelper.ID
import ru.application.homemedkit.helpers.ConstantsHelper.MEDICINE_ID
import ru.application.homemedkit.helpers.ConstantsHelper.NEW_MEDICINE
import ru.application.homemedkit.helpers.ConstantsHelper.SNACKS
import ru.application.homemedkit.helpers.DateHelper.toExpDate
import ru.application.homemedkit.helpers.DateHelper.toTimestamp
import ru.application.homemedkit.helpers.formName
import ru.application.homemedkit.helpers.fromHTML
import ru.application.homemedkit.helpers.getIconType
import ru.application.homemedkit.helpers.viewModelFactory
import ru.application.homemedkit.ui.theme.AppTheme
import ru.application.homemedkit.viewModels.MedicineViewModel
import ru.application.homemedkit.viewModels.ResponseUiState

class MedicineActivity : ComponentActivity() {

    private lateinit var database: MedicineDatabase

    @OptIn(ExperimentalMaterial3Api::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        database = MedicineDatabase.getInstance(this)

        val primaryKey = intent.getLongExtra(ID, 0)
        val duplicate = intent.getBooleanExtra(DUPLICATE, false)
        val cis = intent.getStringExtra(CIS)

        setContent {
            val viewModel = viewModel<MedicineViewModel>(factory = viewModelFactory {
                MedicineViewModel(database, primaryKey)
            })

            val bars = List(6) { Snackbar(it) }
            val hostState = remember(::SnackbarHostState)
            var bar by remember { mutableStateOf(bars[0]) }

            if (primaryKey == 0L) {
                viewModel.setAdding(true)
                viewModel.cis = cis
            }

            viewModel.id = primaryKey

            if (ResponseUiState.Errors.entries.contains(viewModel.responseUiState) || duplicate)
                LaunchedEffect(hostState) {
                    hostState.showSnackbar(message = BLANK, duration = SnackbarDuration.Short)
                }

            AppTheme {
                Scaffold(
                    topBar = {
                        TopAppBar(
                            title = {},
                            navigationIcon = navigationIcon(),
                            actions = {
                                when {
                                    viewModel.add && primaryKey == 0L -> {
                                        IconButton(
                                            onClick = addMedicine(cis, viewModel),
                                            enabled = viewModel.productName.isNotEmpty()
                                        )
                                        { Icon(Icons.Default.Check, null) }
                                    }

                                    viewModel.add && primaryKey != 0L || viewModel.edit -> {
                                        IconButton(
                                            onClick = updateMedicine(primaryKey, viewModel),
                                            enabled = viewModel.productName.isNotEmpty()
                                        )
                                        { Icon(Icons.Default.Check, null) }
                                    }

                                    else -> {
                                        LocalFocusManager.current.clearFocus(true)
                                        var expanded by remember { mutableStateOf(false) }

                                        IconButton(onClick = {
                                            startActivity(
                                                Intent(
                                                    this@MedicineActivity,
                                                    IntakeActivity::class.java
                                                )
                                                    .putExtra(MEDICINE_ID, primaryKey)
                                                    .putExtra(ADD, true)
                                            )
                                        }) {
                                            Icon(
                                                imageVector = Icons.Default.Notifications,
                                                contentDescription = null
                                            )
                                        }

                                        IconButton({ expanded = true }) {
                                            Icon(Icons.Default.MoreVert, null)
                                        }

                                        DropdownMenu(expanded, { expanded = false }) {
                                            DropdownMenuItem(
                                                text = { Text(resources.getString(R.string.text_edit)) },
                                                onClick = {
                                                    if (viewModel.technical.verified) viewModel.setEditing(
                                                        true
                                                    )
                                                    else viewModel.setAdding(true)
                                                }
                                            )
                                            DropdownMenuItem(
                                                text = { Text(resources.getString(R.string.text_delete)) },
                                                onClick = deleteMedicine(primaryKey)
                                            )
                                        }
                                    }
                                }
                            },

                            colors = TopAppBarDefaults.topAppBarColors(
                                containerColor = MaterialTheme.colorScheme.primaryContainer,
                                navigationIconContentColor = MaterialTheme.colorScheme.onPrimaryContainer,
                                actionIconContentColor = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                        )
                    },
                    snackbarHost = { SnackbarHost(hostState, snackbar = bar) }
                ) { paddingValues ->
                    val scroll = rememberScrollState()

                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(
                                top = paddingValues
                                    .calculateTopPadding()
                                    .plus(16.dp)
                            )
                            .verticalScroll(scroll),
                        verticalArrangement = Arrangement.spacedBy(32.dp)
                    ) {
                        ProductName(viewModel)
                        ProductImage(viewModel)
                        ExpirationDate(viewModel)
                        ProductFormName(viewModel)
                        ProductNormName(viewModel)
                        PhKinetics(viewModel)
                        Comment(viewModel)
                    }
                }


                when (viewModel.responseUiState) {
                    is ResponseUiState.Default -> {}
                    is ResponseUiState.Success -> {
                        LocalContext.current.startActivity(intent.putExtra(ID, viewModel.id))
                    }

                    is ResponseUiState.Loading -> {
                        Dialog(onDismissRequest = {}) {
                            Box(Modifier.fillMaxSize(), Alignment.Center) {
                                CircularProgressIndicator()
                            }
                        }
                    }

                    ResponseUiState.Errors.WRONG_CODE_CATEGORY -> bar = bars[1]
                    ResponseUiState.Errors.WRONG_CATEGORY -> bar = bars[2]
                    ResponseUiState.Errors.CODE_NOT_FOUND -> bar = bars[3]
                    ResponseUiState.Errors.FETCH_ERROR -> bar = bars[4]
                    ResponseUiState.Errors.NO_NETWORK -> bar = bars[5]
                }

                BackHandler {
                    startActivity(
                        Intent(this, MainActivity::class.java)
                            .putExtra(NEW_MEDICINE, true)
                    )
                }
            }
        }
    }

    @Composable
    private fun navigationIcon() = @Composable {
        IconButton(onClick = {
            startActivity(
                Intent(this, MainActivity::class.java).putExtra(NEW_MEDICINE, true)
            )
        }) {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onPrimaryContainer
            )
        }
    }

    @Composable
    private fun deleteMedicine(primaryKey: Long): () -> Unit = {
        database.medicineDAO().delete(Medicine(primaryKey))

        startActivity(
            Intent(this, MainActivity::class.java)
                .putExtra(NEW_MEDICINE, true)
                .setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK)
        )
    }

    @Composable
    private fun updateMedicine(primaryKey: Long, viewModel: MedicineViewModel): () -> Unit = {
        database.medicineDAO().update(
            Medicine(
                primaryKey, viewModel.cis, viewModel.productName,
                viewModel.expDate, viewModel.prodFormNormName, viewModel.prodDNormName,
                viewModel.prodAmount.toDouble(), viewModel.phKinetics, viewModel.comment,
                Technical(viewModel.technical.scanned, viewModel.technical.verified)
            )
        )

        startActivity(Intent(intent))
    }

    @Composable
    private fun addMedicine(cis: String?, viewModel: MedicineViewModel): () -> Unit = {
        val prodAmount = if (viewModel.prodAmount.isEmpty()) -1.0
        else viewModel.prodAmount.toDouble()

        val id = database.medicineDAO().add(
            Medicine(
                cis, viewModel.productName, viewModel.expDate, viewModel.prodFormNormName,
                viewModel.prodDNormName, prodAmount, viewModel.phKinetics,
                viewModel.comment, Technical(cis != null, false)
            )
        )

        startActivity(intent.putExtra(ID, id).setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK))
    }
}

@Composable
private fun ProductName(viewModel: MedicineViewModel) {
    val source = remember(::MutableInteractionSource)

    Column(Modifier.padding(horizontal = 16.dp), Arrangement.spacedBy(4.dp)) {

        if (viewModel.add)
            Text(
                text = LocalContext.current.getString(R.string.text_medicine_product_name),
                color = MaterialTheme.colorScheme.onSurface,
                style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold)
            )

        BasicTextField(
            value = viewModel.productName,
            onValueChange = viewModel::updateProductName,
            modifier = Modifier.fillMaxWidth(),
            readOnly = !viewModel.add,
            textStyle = MaterialTheme.typography.titleLarge.copy(
                color = MaterialTheme.colorScheme.onSurface,
                fontWeight = FontWeight.Bold
            ),
            decorationBox = decorationBox(
                viewModel = viewModel,
                text = viewModel.productName,
                source = source
            )
        )

        if (!viewModel.add)
            Text(
                text = formName(viewModel.prodFormNormName),
                style = MaterialTheme.typography.titleMedium.copy(
                    color = MaterialTheme.colorScheme.onSurface
                )
            )
    }
}

@Composable
private fun ProductImage(viewModel: MedicineViewModel) {
    // var showPicker by remember { mutableStateOf(false) }

    ElevatedCard(
        modifier = Modifier
            .fillMaxWidth()
            .height(192.dp)
            .padding(horizontal = 16.dp),
        colors = CardDefaults.cardColors(MaterialTheme.colorScheme.secondaryContainer)
    ) {
        Image(
            painter = rememberDrawablePainter(
                getIconType(LocalContext.current, formName(viewModel.prodFormNormName))
            ),
            contentDescription = null,
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            // .clickable { showPicker = true },
            alignment = Alignment.Center,
            contentScale = ContentScale.Fit
        )
    }

//    if (showPicker) IconPicker { showPicker = false }
}

@Composable
private fun ExpirationDate(viewModel: MedicineViewModel) {
    var size by remember { mutableStateOf(IntSize.Zero) }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(Modifier.onGloballyPositioned { size = it.size }) {
            Text(
                text = LocalContext.current.getString(R.string.text_exp_date),
                color = MaterialTheme.colorScheme.onSurface,
                style = if (!viewModel.add) MaterialTheme.typography.titleLarge
                else MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold)
            )

            if (!viewModel.add) {
                Text(
                    text = toExpDate(viewModel.expDate),
                    color = MaterialTheme.colorScheme.onSurface,
                    style = MaterialTheme.typography.titleLarge,
                )
            } else {
                var showPicker by remember { mutableStateOf(false) }

                OutlinedTextField(
                    value = toExpDate(viewModel.expDate),
                    onValueChange = {},
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 8.dp)
                        .clickable { showPicker = true },
                    enabled = false,
                    readOnly = true,
                    leadingIcon = { Icon(Icons.Default.DateRange, null) },
                    colors = fieldColorsInverted()
                )

                if (showPicker) MonthYear(
                    onConfirm = { month, year ->
                        viewModel.updateExpDate(toTimestamp(month, year)); showPicker = false
                    },
                    onCancel = { showPicker = false })
            }
        }

        if (!viewModel.add) {
            val icon: ImageVector
            val color: Color
            val onColor: Color
            val onClick: () -> Unit

            when {
                viewModel.technical.verified -> {
                    color = MaterialTheme.colorScheme.primaryContainer
                    onColor = MaterialTheme.colorScheme.onPrimaryContainer
                    icon = Icons.Default.Check
                    onClick = {}
                }

                viewModel.technical.scanned && !viewModel.technical.verified -> {
                    color = MaterialTheme.colorScheme.errorContainer
                    onColor = MaterialTheme.colorScheme.onErrorContainer
                    icon = Icons.Default.Refresh
                    onClick = viewModel::fetchData
                }

                else -> {
                    color = MaterialTheme.colorScheme.error
                    onColor = MaterialTheme.colorScheme.onError
                    icon = Icons.Default.Info
                    onClick = {}
                }
            }

            Box(Modifier.background(color, RoundedCornerShape(16.dp))) {
                IconButton(
                    onClick = onClick,
                    modifier = Modifier.size(with(LocalDensity.current) { size.height.toDp() })
                ) { Icon(imageVector = icon, contentDescription = null, tint = onColor) }
            }
        }
    }
    HorizontalDivider(Modifier.padding(16.dp, 0.dp), 2.dp, MaterialTheme.colorScheme.outline)
}

@Composable
private fun ProductFormName(viewModel: MedicineViewModel) {
    val source = remember { MutableInteractionSource() }

    Column(modifier = Modifier.padding(horizontal = 16.dp)) {
        Text(
            text = LocalContext.current.getString(R.string.text_medicine_description),
            style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold)
        )

        BasicTextField(
            value = viewModel.prodFormNormName,
            onValueChange = viewModel::updateFormName,
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp),
            readOnly = !viewModel.add,
            textStyle = MaterialTheme.typography.bodyLarge.copy(MaterialTheme.colorScheme.onSurface),
            decorationBox = decorationBox(
                viewModel = viewModel,
                text = viewModel.prodFormNormName,
                id = R.string.placeholder_form_name,
                source = source
            )
        )
    }
}

@Composable
private fun ProductNormName(viewModel: MedicineViewModel) {
    val sourceA = remember(::MutableInteractionSource)
    val sourceB = remember(::MutableInteractionSource)

    Row(Modifier.padding(horizontal = 16.dp), Arrangement.spacedBy(16.dp)) {
        Column(Modifier.weight(0.5f), Arrangement.spacedBy(8.dp)) {
            Text(
                text = LocalContext.current.getString(R.string.text_medicine_dose),
                style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold)
            )

            BasicTextField(
                value = viewModel.prodDNormName,
                onValueChange = viewModel::updateNormName,
                modifier = Modifier.fillMaxWidth(),
                readOnly = !viewModel.add,
                textStyle = MaterialTheme.typography.bodyLarge.copy(MaterialTheme.colorScheme.onSurface),
                decorationBox = decorationBox(
                    viewModel = viewModel,
                    text = viewModel.prodDNormName,
                    id = R.string.placeholder_dose,
                    source = sourceA
                )
            )
        }

        Column(Modifier.weight(0.5f), Arrangement.spacedBy(8.dp)) {
            Text(
                text = LocalContext.current.getString(R.string.text_amount),
                style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold)
            )

            BasicTextField(
                value = viewModel.prodAmount,
                onValueChange = viewModel::updateProdAmount,
                modifier = Modifier.fillMaxWidth(),
                readOnly = !viewModel.add && !viewModel.edit,
                textStyle = MaterialTheme.typography.bodyLarge.copy(MaterialTheme.colorScheme.onSurface),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                visualTransformation = {
                    TransformedText(
                        AnnotatedString(it.text.replace('.', ',')),
                        OffsetMapping.Identity
                    )
                },
                decorationBox = decorationBox(
                    viewModel = viewModel,
                    text = viewModel.prodAmount,
                    id = R.string.placeholder_amount,
                    source = sourceB
                )
            )
        }
    }
}


@Composable
private fun PhKinetics(viewModel: MedicineViewModel) {
    val source = remember(::MutableInteractionSource)

    if (viewModel.add || viewModel.edit || viewModel.phKinetics.isNotEmpty())
        Column(modifier = Modifier.padding(horizontal = 16.dp)) {
            Text(
                text = LocalContext.current.getString(R.string.text_indications_for_use),
                style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold)
            )

            BasicTextField(
                value = fromHTML(viewModel.phKinetics),
                onValueChange = viewModel::updatePhKinetics,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                readOnly = !viewModel.add,
                textStyle = MaterialTheme.typography.bodyLarge.copy(MaterialTheme.colorScheme.onSurface),
                decorationBox = decorationBox(
                    viewModel = viewModel,
                    text = viewModel.phKinetics,
                    source = source
                )
            )
        }
}

@Composable
private fun Comment(viewModel: MedicineViewModel) {
    val source = remember(::MutableInteractionSource)

    if (viewModel.add || viewModel.edit || viewModel.comment.isNotEmpty())
        Column(modifier = Modifier.padding(16.dp, 0.dp, 16.dp, 16.dp)) {
            Text(
                text = LocalContext.current.getString(R.string.text_medicine_comment),
                style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold)
            )

            BasicTextField(
                value = viewModel.comment,
                onValueChange = viewModel::updateComment,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                readOnly = !viewModel.add && !viewModel.edit,
                textStyle = MaterialTheme.typography.bodyLarge.copy(MaterialTheme.colorScheme.onSurface),
                decorationBox = decorationBox(
                    viewModel = viewModel,
                    text = viewModel.comment,
                    source = source
                )
            )
        }
}

@Composable
@OptIn(ExperimentalMaterial3Api::class)
private fun decorationBox(
    viewModel: MedicineViewModel,
    text: String,
    id: Int = R.string.text_empty,
    source: MutableInteractionSource
): @Composable (innerTextField: @Composable () -> Unit) -> Unit {
    return {
        when {
            viewModel.add ||
                    viewModel.edit && text == viewModel.prodAmount ||
                    viewModel.edit && text == viewModel.comment ->
                OutlinedTextFieldDefaults.DecorationBox(
                    value = text,
                    innerTextField = it,
                    enabled = true,
                    singleLine = false,
                    visualTransformation = VisualTransformation.None,
                    interactionSource = source,
                    placeholder = { Text(LocalContext.current.getString(id)) },
                    container = {
                        OutlinedTextFieldDefaults.ContainerBox(
                            enabled = true,
                            isError = false,
                            interactionSource = source,
                            colors = OutlinedTextFieldDefaults.colors()
                        )
                    }
                )

            else -> OutlinedTextFieldDefaults.DecorationBox(
                value = text,
                innerTextField = it,
                enabled = true,
                singleLine = false,
                visualTransformation = VisualTransformation.None,
                interactionSource = source,
                contentPadding = PaddingValues(0.dp),
                container = {}
            )
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun IconPicker(onCancel: () -> Unit) {
    val icons = LocalContext.current.resources.obtainTypedArray(R.array.medicine_types_icons)
    val scrollState = rememberScrollState()

    Dialog(onDismissRequest = onCancel) {
        Surface(Modifier.padding(vertical = 64.dp), RoundedCornerShape(16.dp)) {
            FlowRow(
                modifier = Modifier
                    .padding(16.dp)
                    .verticalScroll(scrollState),
                horizontalArrangement = Arrangement.Center,
                verticalArrangement = Arrangement.Center,
                maxItemsInEachRow = 4
            ) {
                for (i in 0..<icons.length()) {
                    ElevatedCard(modifier = Modifier.padding(8.dp)) {
                        Image(
                            painter = rememberDrawablePainter(icons.getDrawable(i)),
                            contentDescription = null,
                            modifier = Modifier
                                .size(128.dp)
                                .padding(16.dp)
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun Snackbar(id: Int): @Composable (SnackbarData) -> Unit {
    return {
        androidx.compose.material3.Snackbar(
            modifier = Modifier.padding(8.dp),
            containerColor = MaterialTheme.colorScheme.errorContainer,
            contentColor = MaterialTheme.colorScheme.onErrorContainer,
            shape = RoundedCornerShape(4.dp)
        ) { Text(text = LocalContext.current.getString(SNACKS[id])) }
    }
}