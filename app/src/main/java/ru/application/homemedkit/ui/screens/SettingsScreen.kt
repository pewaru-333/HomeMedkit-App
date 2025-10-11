package ru.application.homemedkit.ui.screens

import android.Manifest
import android.content.Intent
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteDatabase.OPEN_READONLY
import android.database.sqlite.SQLiteDatabase.openDatabase
import android.net.Uri
import android.os.Build
import android.os.Build.VERSION
import android.provider.Settings
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts.CreateDocument
import androidx.activity.result.contract.ActivityResultContracts.OpenDocument
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement.SpaceBetween
import androidx.compose.foundation.layout.Arrangement.spacedBy
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Alignment.Companion.CenterVertically
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import kotlinx.coroutines.launch
import me.zhanghai.compose.preference.ListPreference
import me.zhanghai.compose.preference.Preference
import me.zhanghai.compose.preference.ProvidePreferenceLocals
import me.zhanghai.compose.preference.SwitchPreference
import me.zhanghai.compose.preference.preference
import me.zhanghai.compose.preference.preferenceCategory
import me.zhanghai.compose.preference.switchPreference
import ru.application.homemedkit.R
import ru.application.homemedkit.data.dto.Kit
import ru.application.homemedkit.dialogs.dragHandle
import ru.application.homemedkit.dialogs.draggableItems
import ru.application.homemedkit.dialogs.rememberDraggableListState
import ru.application.homemedkit.models.viewModels.SettingsViewModel
import ru.application.homemedkit.receivers.AlarmSetter
import ru.application.homemedkit.ui.elements.NavigationIcon
import ru.application.homemedkit.ui.elements.VectorIcon
import ru.application.homemedkit.ui.navigation.LocalBarVisibility
import ru.application.homemedkit.ui.theme.isDynamicColorAvailable
import ru.application.homemedkit.utils.BLANK
import ru.application.homemedkit.utils.KEY_APP_SYSTEM
import ru.application.homemedkit.utils.KEY_APP_VIEW
import ru.application.homemedkit.utils.KEY_BASIC_SETTINGS
import ru.application.homemedkit.utils.KEY_CLEAR_CACHE
import ru.application.homemedkit.utils.KEY_CONFIRM_EXIT
import ru.application.homemedkit.utils.KEY_DOWNLOAD
import ru.application.homemedkit.utils.KEY_DYNAMIC_COLOR
import ru.application.homemedkit.utils.KEY_EXP_IMP
import ru.application.homemedkit.utils.KEY_FIXING
import ru.application.homemedkit.utils.KEY_KITS
import ru.application.homemedkit.utils.KEY_PERMISSIONS
import ru.application.homemedkit.utils.KEY_USE_ALARM_CLOCK
import ru.application.homemedkit.utils.di.AlarmManager
import ru.application.homemedkit.utils.di.Database
import ru.application.homemedkit.utils.di.Preferences
import ru.application.homemedkit.utils.enums.Page
import ru.application.homemedkit.utils.enums.Sorting
import ru.application.homemedkit.utils.enums.Theme
import ru.application.homemedkit.utils.extensions.canScheduleExactAlarms
import ru.application.homemedkit.utils.extensions.drawHorizontalDivider
import ru.application.homemedkit.utils.extensions.getDisplayRegionName
import ru.application.homemedkit.utils.extensions.getLanguageList
import ru.application.homemedkit.utils.extensions.restartApplication
import ru.application.homemedkit.utils.extensions.showToast
import ru.application.homemedkit.utils.permissions.PermissionState
import ru.application.homemedkit.utils.permissions.rememberPermissionState
import java.io.File
import java.util.Locale

@Composable
fun SettingsScreen() {
    val context = LocalContext.current
    val barVisibility = LocalBarVisibility.current

    val underlineColor = MaterialTheme.colorScheme.outlineVariant

    val model = viewModel<SettingsViewModel>()
    val state by model.state.collectAsStateWithLifecycle()
    val kits by model.kits.collectAsStateWithLifecycle()

    val startPage by model.startPage.collectAsStateWithLifecycle()
    val sorting by model.sortingType.collectAsStateWithLifecycle()
    val checkExpiration by model.checkExpiration.collectAsStateWithLifecycle()
    val theme by model.theme.collectAsStateWithLifecycle()

    LaunchedEffect(state.showKits, state.showPermissions) {
        if (state.showKits || state.showPermissions) barVisibility.hide()
        else barVisibility.show()
    }

    ProvidePreferenceLocals {
        LazyColumn {
            preferenceCategory(
                key = KEY_BASIC_SETTINGS,
                title = { Text(stringResource(R.string.preference_basic_settings)) }
            )

            preference(
                key = KEY_KITS,
                title = { Text(stringResource(R.string.preference_kits_group)) },
                summary = { Text(stringResource(R.string.text_tap_to_view)) },
                onClick = model::toggleKits
            )

            item {
                ListPreference(
                    value = startPage,
                    onValueChange = model::changeStartPage,
                    values = Page.entries,
                    title = { Text(stringResource(R.string.preference_start_page)) },
                    summary = { Text(stringResource(startPage.title)) },
                    valueToText = { AnnotatedString(context.getString(it.title)) }
                )
            }

            item {
                ListPreference(
                    value = sorting,
                    onValueChange = model::changeSortingType,
                    values = Sorting.entries,
                    title = { Text(stringResource(R.string.preference_sorting_type)) },
                    summary = { Text(stringResource(sorting.title)) },
                    valueToText = { AnnotatedString(context.getString(it.title)) }
                )
            }

            switchPreference(
                key = KEY_CONFIRM_EXIT,
                defaultValue = true,
                title = { Text(stringResource(R.string.preference_confirm_exit)) },
                summary = { Text(stringResource(if (it) R.string.text_on else R.string.text_off)) }
            )

            switchPreference(
                key = KEY_DOWNLOAD,
                defaultValue = true,
                title = { Text(stringResource(R.string.preference_download_images)) },
                summary = { Text(stringResource(if (it) R.string.text_on else R.string.text_off)) }
            )

            item {
                SwitchPreference(
                    value = checkExpiration,
                    onValueChange = model::changeExpirationCheck,
                    title = { Text(stringResource(R.string.preference_check_expiration_date)) },
                    summary = { Text(stringResource(if (checkExpiration) R.string.text_daily_at else R.string.text_off)) }
                )
            }

            switchPreference(
                key = KEY_USE_ALARM_CLOCK,
                defaultValue = false,
                enabled = { context.canScheduleExactAlarms() },
                title = { Text(stringResource(R.string.preference_use_alarm_clock)) },
                summary = {
                    Text(
                        text = stringResource(
                            when {
                                context.canScheduleExactAlarms() -> if (it) R.string.text_on else R.string.text_off
                                else -> R.string.text_explain_disabled
                            }
                        )
                    )
                }
            )

            preferenceCategory(
                key = KEY_APP_VIEW,
                title = { Text(stringResource(R.string.preference_app_view)) },
                modifier = Modifier.drawHorizontalDivider(
                    color = underlineColor,
                    start = { Offset(0f, 0f) },
                    end = { Offset(size.width, 0f) }
                )
            )

            item {
                if (VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU) {
                    var value by remember { mutableStateOf(Preferences.getLanguage(context)) }

                    ListPreference(
                        value = value,
                        onValueChange = { value = it; Preferences.setLocale(context, it) },
                        values = context.getLanguageList(),
                        title = { Text(stringResource(R.string.preference_language)) },
                        summary = { Text(Locale.forLanguageTag(value).getDisplayRegionName()) },
                        valueToText = { AnnotatedString(Locale.forLanguageTag(it).getDisplayRegionName()) }
                    )
                } else Preference(
                    title = { Text(stringResource(R.string.preference_language)) },
                    onClick = {
                        context.startActivity(
                            Intent(Settings.ACTION_APP_LOCALE_SETTINGS).apply {
                                data = Uri.fromParts("package", context.packageName, null)
                            }
                        )
                    }
                )
            }

            item {
                ListPreference(
                    value = theme,
                    onValueChange = Preferences::setTheme,
                    values = Theme.entries,
                    title = { Text(stringResource(R.string.preference_app_theme)) },
                    summary = { Text(stringResource(theme.title)) },
                    valueToText = { AnnotatedString(context.getString(it.title)) }
                )
            }

            switchPreference(
                key = KEY_DYNAMIC_COLOR,
                defaultValue = false,
                title = { Text(stringResource(R.string.preference_dynamic_color)) },
                enabled = { isDynamicColorAvailable() }
            )

            preferenceCategory(
                key = KEY_APP_SYSTEM,
                title = { Text(stringResource(R.string.preference_system)) },
                modifier = Modifier.drawHorizontalDivider(
                    color = underlineColor,
                    start = { Offset(0f, 0f) },
                    end = { Offset(size.width, 0f) }
                )
            )

            preference(
                key = KEY_PERMISSIONS,
                title = { Text(stringResource(R.string.preference_permissions)) },
                summary = { Text(stringResource(R.string.text_tap_to_view)) },
                onClick = model::togglePermissions,
            )

            preference(
                key = KEY_EXP_IMP,
                title = { Text(stringResource(R.string.preference_import_export)) },
                onClick = model::toggleExport
            )

            preference(
                key = KEY_FIXING,
                title = { Text(stringResource(R.string.preference_fixing_notifications)) },
                onClick = model::toggleFixing
            )

            preference(
                key = KEY_CLEAR_CACHE,
                title = { Text(stringResource(R.string.preference_clear_app_cache)) },
                onClick = model::toggleClearing
            )
        }
    }
    
    KitsManager(
        isVisible = state.showKits,
        kits = kits,
        onSave = model::saveKitsPosition,
        onUpsert = model::upsertKit,
        onDelete = model::deleteKit,
        onBack = model::toggleKits
    )

    AnimatedVisibility(
        visible = state.showPermissions,
        enter = scaleIn(),
        exit = scaleOut()
    ) {
        BackHandler(state.showPermissions, model::togglePermissions)
        PermissionsScreen(model::togglePermissions)
    }

    when {
        state.showExport -> ExportImport(model::toggleExport)
        state.showFixing -> DialogFixing(model::toggleFixing)
        state.showClearing -> DialogClearing(model::clearCache, model::toggleClearing)
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun KitsManager(
    isVisible: Boolean,
    kits: List<Kit>,
    onSave: (List<Kit>) -> Unit,
    onUpsert: (Kit) -> Unit,
    onDelete: (Kit) -> Unit,
    onBack: () -> Unit
) {
    var list by remember { mutableStateOf(emptyList<Kit>()) }
    var kit by remember { mutableStateOf(Kit(position = list.size.toLong())) }

    var ordering by rememberSaveable { mutableStateOf(false) }
    var show by rememberSaveable { mutableStateOf(false) }

    LaunchedEffect(kits) {
        list = kits
        kit = Kit(position = list.size.toLong())
    }

    val draggableState = rememberDraggableListState { fromIndex, toIndex ->
        list = list.toMutableList().apply { add(toIndex, removeAt(fromIndex)) }
    }

    BackHandler(isVisible, onBack)
    AnimatedVisibility(
        visible = isVisible,
        enter = slideInHorizontally(initialOffsetX = { it }),
        exit = slideOutHorizontally(targetOffsetX = { it })
    ) {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = { Text(stringResource(R.string.preference_kits_group)) },
                    navigationIcon = { NavigationIcon(onBack) },
                    actions = {
                        if (ordering) {
                            IconButton(
                                content = { VectorIcon(R.drawable.vector_confirm) },
                                onClick = {
                                    onSave(list)
                                    ordering = false
                                }
                            )
                        } else {
                            IconButton(
                                onClick = { ordering = true },
                                content = { VectorIcon(R.drawable.vector_sort) }
                            )
                        }
                    }
                )
            },
            floatingActionButton = {
                if (!ordering) {
                    ExtendedFloatingActionButton(
                        text = { Text(stringResource(R.string.text_add)) },
                        icon = { VectorIcon(R.drawable.vector_add) },
                        onClick = { show = true },
                    )
                }
            }
        ) { values ->
            LazyColumn(Modifier.fillMaxSize(), draggableState.listState, values) {
                draggableItems(draggableState, list, Kit::kitId) { item, _ ->
                    ListItem(
                        headlineContent = { Text(item.title) },
                        leadingContent = ordering.let {
                            {
                                if (!it) {
                                    IconButton(
                                        content = { VectorIcon(R.drawable.vector_edit) },
                                        onClick = {
                                            kit = Kit(item.kitId, item.title, item.position)
                                            show = true
                                        }
                                    )
                                }
                            }
                        },
                        trailingContent = {
                            if (ordering) {
                                VectorIcon(
                                    icon = R.drawable.vector_menu,
                                    modifier = Modifier.dragHandle(draggableState, item.kitId)
                                )
                            } else {
                                IconButton(
                                    onClick = { onDelete(item) },
                                    content = { VectorIcon(R.drawable.vector_delete) }
                                )
                            }
                        }
                    )

                    if (item.position > 0L) {
                        HorizontalDivider()
                    }
                }
            }
        }

        if (show) {
            AlertDialog(
                title = { Text(stringResource(R.string.text_kit_title)) },
                onDismissRequest = { show = false },
                confirmButton = {
                    TextButton(
                        enabled = kit.title.isNotBlank(),
                        content = { Text(stringResource(R.string.text_save)) },
                        onClick = {
                            onUpsert(kit)
                            show = false
                        }
                    )
                },
                dismissButton = {
                    TextButton(
                        content = { Text(stringResource(R.string.text_cancel)) },
                        onClick = {
                            show = false
                            kit = Kit(position = list.size.toLong())
                        }
                    )
                },
                text = {
                    OutlinedTextField(
                        value = kit.title,
                        onValueChange = { kit = kit.copy(title = it) },
                        keyboardOptions = KeyboardOptions(KeyboardCapitalization.Sentences)
                    )
                }
            )
        }
    }
}

@Composable
fun PermissionsScreen(navigateUp: () -> Unit, exitFirstLaunch: () -> Unit = navigateUp) {
    val scheduleExactAlarms = rememberPermissionState(Manifest.permission.SCHEDULE_EXACT_ALARM)
    val postNotifications = rememberPermissionState(Manifest.permission.POST_NOTIFICATIONS)
    val fullScreenIntent = rememberPermissionState(Manifest.permission.USE_FULL_SCREEN_INTENT)
    val ignoreBattery = rememberPermissionState(Manifest.permission.REQUEST_IGNORE_BATTERY_OPTIMIZATIONS)

    Column(
        verticalArrangement = SpaceBetween,
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(12.dp)
            .verticalScroll(rememberScrollState())
    ) {
        Column(Modifier, spacedBy(8.dp), Alignment.CenterHorizontally) {
            Image(painterResource(R.drawable.vector_bell), null, Modifier.size(64.dp))
            Text(
                text = stringResource(R.string.text_pay_attention),
                style = MaterialTheme.typography.titleLarge
            )
            Text(
                text = stringResource(R.string.text_explain_request_permissions),
                textAlign = TextAlign.Center,
                style = MaterialTheme.typography.bodyMedium
            )
        }
        Column {
            if (VERSION.SDK_INT >= Build.VERSION_CODES.S) ListItem(
                headlineContent = { Text(stringResource(R.string.text_permission_title_reminders)) },
                trailingContent = { ButtonGrant(scheduleExactAlarms) },
                supportingContent = {
                    Text(
                        text = stringResource(R.string.text_explain_reminders),
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            )
            if (VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) ListItem(
                headlineContent = { Text(stringResource(R.string.text_permission_title_notifications)) },
                trailingContent = { ButtonGrant(postNotifications) },
                supportingContent = {
                    Text(
                        text = stringResource(R.string.text_explain_notifications),
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            )
            if (VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) ListItem(
                headlineContent = { Text(stringResource(R.string.text_permission_title_full_screen)) },
                trailingContent = { ButtonGrant(fullScreenIntent) },
                supportingContent = {
                    Text(
                        text = stringResource(R.string.text_explain_full_screen_intent),
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            )
            ListItem(
                headlineContent = { Text(stringResource(R.string.text_permission_title_ignore_battery)) },
                trailingContent = { ButtonGrant(ignoreBattery) },
                supportingContent = {
                    Text(
                        text = stringResource(R.string.text_expain_ignore_battery),
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            )
        }
        Row(Modifier.fillMaxWidth(), SpaceBetween, CenterVertically) {
            TextButton(navigateUp) { Text(stringResource(R.string.text_exit)) }
            Button(
                onClick = exitFirstLaunch,
                enabled = listOf(scheduleExactAlarms, postNotifications).all(PermissionState::isGranted),
                content = { Text(stringResource(R.string.text_save)) }
            )
        }
    }
}

@Composable
private fun ButtonGrant(permission: PermissionState, modifier: Modifier = Modifier) =
    TextButton(permission::launchRequest, modifier, !permission.isGranted) {
        Text(stringResource(if (permission.isGranted) R.string.text_permission_granted else R.string.text_permission_grant))
    }

@Composable
private fun ExportImport(onDismiss: () -> Unit) {
    val scope = rememberCoroutineScope()
    val context = LocalContext.current

    val queryC = "SELECT 1 FROM sqlite_master WHERE type = 'table' and name = 'room_master_table'"
    val queryG = "SELECT * FROM room_master_table"
    val name = "exported.sqlite3"
    val mimes = arrayOf("application/vnd.sqlite3", "application/x-sqlite3", "application/octet-stream")

    fun hasTable(db: SQLiteDatabase): Boolean = db.rawQuery(queryC, null)
        .use { return if (!it.moveToFirst()) false else it.getInt(0) > 0 }

    fun getHash(db: SQLiteDatabase): String = db.rawQuery(queryG, null)
        .use { return if (it.moveToNext()) it.getString(1) else BLANK }

    val exporter = rememberLauncherForActivityResult(CreateDocument(mimes[0])) { uri ->
        val path = context.getDatabasePath(Database.openHelper.databaseName)
        Database.close()

        uri?.let { uriN ->
            context.contentResolver.openOutputStream(uriN).use { output ->
                output?.let { path.inputStream().copyTo(it) }
            }
            context.restartApplication {
               putBoolean(KEY_EXP_IMP, true)
            }
        }
    }

    val importer = rememberLauncherForActivityResult(OpenDocument()) { uri ->
        val path = context.getDatabasePath(Database.openHelper.databaseName)

        uri?.let { uriN ->
            val cursor = Database.openHelper.readableDatabase.query(queryG)
            val currentHash = if (cursor.moveToNext()) cursor.getString(1) else BLANK
            val tempFile = File.createTempFile("temp", ".sqlite", context.cacheDir)

            Database.close()
            context.contentResolver.openInputStream(uriN).use { input ->
                tempFile.outputStream().use { output -> input?.copyTo(output) }
            }

            try {
                val newDB = openDatabase(tempFile.path, null, OPEN_READONLY)
                val newHash = if (hasTable(newDB)) getHash(newDB) else BLANK
                newDB.close()

                if (currentHash == newHash) {
                    scope.launch {
                        AlarmSetter.getInstance(context).cancelAll()
                    }

                    context.contentResolver.openInputStream(uriN).use { input ->
                        path.outputStream().use { output -> input?.copyTo(output) }
                    }

                    context.restartApplication {
                        putBoolean(KEY_EXP_IMP, true)
                    }
                } else context.showToast(R.string.text_error)
            } catch (_: Throwable) {
                context.showToast(R.string.text_error)
            } finally {
                tempFile.delete()
            }
        }
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        confirmButton = { TextButton({ importer.launch(mimes) }) { Text(stringResource(R.string.text_import)) } },
        dismissButton = { TextButton({ exporter.launch(name) }) { Text(stringResource(R.string.text_export)) } },
        title = { Text(stringResource(R.string.text_attention)) },
        text = {
            Text(
                text = stringResource(R.string.text_export_import_description),
                style = MaterialTheme.typography.bodyLarge
            )
        }
    )
}

@Composable
private fun DialogFixing(back: () -> Unit) {
    val scope = rememberCoroutineScope()
    val context = LocalContext.current

    fun onFix() {
        scope.launch {
            launch { AlarmManager.resetAll() }.join()
            context.restartApplication()
        }
    }

    AlertDialog(
        onDismissRequest = back,
        dismissButton = { TextButton(back) { Text(stringResource(R.string.text_cancel)) } },
        confirmButton = { TextButton(::onFix) { Text(stringResource(R.string.text_confirm)) } },
        title = { Text(stringResource(R.string.text_attention)) },
        text = {
            Text(
                text = stringResource(R.string.text_fix_notifications),
                style = MaterialTheme.typography.bodyLarge
            )
        }
    )
}

@Composable
private fun DialogClearing(onClear: (File, File) -> Unit, onDismiss: () -> Unit) {
    val context = LocalContext.current

    AlertDialog(
        onDismissRequest = onDismiss,
        dismissButton = { TextButton(onDismiss) { Text(stringResource(R.string.text_cancel)) } },
        title = { Text(stringResource(R.string.text_attention)) },
        text = {
            Text(
                text = stringResource(R.string.text_clear_app_cache_description),
                style = MaterialTheme.typography.bodyLarge
            )
        },
        confirmButton = {
            TextButton(
                content = { Text(stringResource(R.string.text_confirm)) },
                onClick = {
                    onClear(context.cacheDir, context.filesDir)
                    context.showToast(R.string.text_success)
                }
            )
        }
    )
}