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
import android.webkit.MimeTypeMap
import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts.CreateDocument
import androidx.activity.result.contract.ActivityResultContracts.OpenDocument
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement.SpaceBetween
import androidx.compose.foundation.layout.Arrangement.spacedBy
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Alignment.Companion.CenterVertically
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import kotlinx.coroutines.launch
import me.zhanghai.compose.preference.ListPreference
import me.zhanghai.compose.preference.Preference
import me.zhanghai.compose.preference.ProvidePreferenceLocals
import me.zhanghai.compose.preference.SwitchPreference
import me.zhanghai.compose.preference.listPreference
import me.zhanghai.compose.preference.preference
import me.zhanghai.compose.preference.preferenceCategory
import me.zhanghai.compose.preference.switchPreference
import ru.application.homemedkit.HomeMeds.Companion.database
import ru.application.homemedkit.R
import ru.application.homemedkit.R.string.preference_app_theme
import ru.application.homemedkit.R.string.preference_app_view
import ru.application.homemedkit.R.string.preference_basic_settings
import ru.application.homemedkit.R.string.preference_check_expiration_date
import ru.application.homemedkit.R.string.preference_clear_app_cache
import ru.application.homemedkit.R.string.preference_confirm_exit
import ru.application.homemedkit.R.string.preference_download_images
import ru.application.homemedkit.R.string.preference_dynamic_color
import ru.application.homemedkit.R.string.preference_fixing_notifications
import ru.application.homemedkit.R.string.preference_import_export
import ru.application.homemedkit.R.string.preference_kits_group
import ru.application.homemedkit.R.string.preference_language
import ru.application.homemedkit.R.string.preference_permissions
import ru.application.homemedkit.R.string.preference_sorting_type
import ru.application.homemedkit.R.string.preference_system
import ru.application.homemedkit.R.string.text_add
import ru.application.homemedkit.R.string.text_attention
import ru.application.homemedkit.R.string.text_cancel
import ru.application.homemedkit.R.string.text_clear_app_cache_description
import ru.application.homemedkit.R.string.text_confirm
import ru.application.homemedkit.R.string.text_daily_at
import ru.application.homemedkit.R.string.text_exit
import ru.application.homemedkit.R.string.text_expain_ignore_battery
import ru.application.homemedkit.R.string.text_explain_full_screen_intent
import ru.application.homemedkit.R.string.text_explain_notifications
import ru.application.homemedkit.R.string.text_explain_reminders
import ru.application.homemedkit.R.string.text_explain_request_permissions
import ru.application.homemedkit.R.string.text_export
import ru.application.homemedkit.R.string.text_export_import_description
import ru.application.homemedkit.R.string.text_fix_notifications
import ru.application.homemedkit.R.string.text_import
import ru.application.homemedkit.R.string.text_kit_title
import ru.application.homemedkit.R.string.text_off
import ru.application.homemedkit.R.string.text_on
import ru.application.homemedkit.R.string.text_pay_attention
import ru.application.homemedkit.R.string.text_permission_grant
import ru.application.homemedkit.R.string.text_permission_granted
import ru.application.homemedkit.R.string.text_permission_title_full_screen
import ru.application.homemedkit.R.string.text_permission_title_ignore_battery
import ru.application.homemedkit.R.string.text_permission_title_notifications
import ru.application.homemedkit.R.string.text_permission_title_reminders
import ru.application.homemedkit.R.string.text_save
import ru.application.homemedkit.R.string.text_success
import ru.application.homemedkit.R.string.text_tap_to_view
import ru.application.homemedkit.data.dto.Kit
import ru.application.homemedkit.helpers.BLANK
import ru.application.homemedkit.helpers.KEY_APP_SYSTEM
import ru.application.homemedkit.helpers.KEY_APP_THEME
import ru.application.homemedkit.helpers.KEY_APP_VIEW
import ru.application.homemedkit.helpers.KEY_BASIC_SETTINGS
import ru.application.homemedkit.helpers.KEY_CLEAR_CACHE
import ru.application.homemedkit.helpers.KEY_CONFIRM_EXIT
import ru.application.homemedkit.helpers.KEY_DOWNLOAD
import ru.application.homemedkit.helpers.KEY_DYNAMIC_COLOR
import ru.application.homemedkit.helpers.KEY_EXP_IMP
import ru.application.homemedkit.helpers.KEY_FIXING
import ru.application.homemedkit.helpers.KEY_KITS
import ru.application.homemedkit.helpers.KEY_ORDER
import ru.application.homemedkit.helpers.KEY_PERMISSIONS
import ru.application.homemedkit.helpers.Preferences
import ru.application.homemedkit.helpers.SORTING
import ru.application.homemedkit.helpers.Sorting
import ru.application.homemedkit.helpers.THEMES
import ru.application.homemedkit.helpers.Themes
import ru.application.homemedkit.helpers.extensions.getDisplayRegionName
import ru.application.homemedkit.helpers.extensions.getLanguageList
import ru.application.homemedkit.helpers.extensions.showToast
import ru.application.homemedkit.helpers.permissions.PermissionState
import ru.application.homemedkit.helpers.permissions.rememberPermissionState
import ru.application.homemedkit.receivers.AlarmSetter
import ru.application.homemedkit.ui.theme.isDynamicColorAvailable
import java.io.File
import java.util.Locale

@Composable
fun SettingsScreen(
    backClick: () -> Unit,
    toKitsManager: () -> Unit,
    toPermissionsScreen: () -> Unit
) {
    val context = LocalContext.current

    val sorting = Sorting.entries.map { stringResource(it.title) }
    val themes = Themes.entries.map { stringResource(it.title) }

    var showExport by rememberSaveable { mutableStateOf(false) }
    var showFixing by rememberSaveable { mutableStateOf(false) }
    var showClearing by rememberSaveable { mutableStateOf(false) }

    BackHandler(onBack = backClick)
    ProvidePreferenceLocals {
        LazyColumn {
            preferenceCategory(
                key = KEY_BASIC_SETTINGS,
                title = { Text(stringResource(preference_basic_settings)) }
            )

            preference(
                key = KEY_KITS,
                title = { Text(stringResource(preference_kits_group)) },
                onClick = toKitsManager,
                summary = { Text(stringResource(text_tap_to_view)) }
            )

            listPreference(
                key = KEY_ORDER,
                defaultValue = SORTING[0],
                values = SORTING,
                title = { Text(stringResource(preference_sorting_type)) },
                summary = { Text(localize(it, SORTING, sorting)) },
                valueToText = { localize(it, SORTING, sorting) }
            )

            switchPreference(
                key = KEY_CONFIRM_EXIT,
                defaultValue = true,
                title = { Text(stringResource(preference_confirm_exit)) },
                summary = { Text(stringResource(if (it) text_on else text_off)) }
            )

            switchPreference(
                key = KEY_DOWNLOAD,
                defaultValue = true,
                title = { Text(stringResource(preference_download_images)) },
                summary = { Text(stringResource(if (it) text_on else text_off)) }
            )

            item {
                var value by remember { mutableStateOf(Preferences.getCheckExpDate()) }

                SwitchPreference(
                    value = value,
                    onValueChange = { value = it; Preferences.setCheckExpDate(context, it) },
                    title = { Text(stringResource(preference_check_expiration_date)) },
                    summary = { Text(stringResource(if (value) text_daily_at else text_off)) }
                )
            }

            preferenceCategory(
                key = KEY_APP_VIEW,
                title = { Text(stringResource(preference_app_view)) },
                modifier = Modifier.drawBehind {
                    drawLine(Color.LightGray, Offset(0f, 0f), Offset(size.width, 0f), 2f)
                }
            )

            item {
                if (VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU) {
                    var value by remember { mutableStateOf(Preferences.getLanguage(context)) }

                    ListPreference(
                        value = value,
                        onValueChange = { value = it; Preferences.setLocale(context, it) },
                        values = context.getLanguageList(),
                        title = { Text(stringResource(preference_language)) },
                        summary = { Text(Locale.forLanguageTag(value).getDisplayRegionName()) },
                        valueToText = { AnnotatedString(Locale.forLanguageTag(it).getDisplayRegionName()) }
                    )
                } else Preference(
                    title = { Text(stringResource(preference_language)) },
                    onClick = {
                        context.startActivity(
                            Intent(Settings.ACTION_APP_LOCALE_SETTINGS).apply {
                                data = Uri.fromParts("package", context.packageName, null)
                            }
                        )
                    }
                )
            }

            listPreference(
                key = KEY_APP_THEME,
                defaultValue = THEMES[0],
                values = THEMES,
                title = { Text(stringResource(preference_app_theme)) },
                summary = { Text(localize(it, THEMES, themes)) },
                valueToText = { localize(it, THEMES, themes) }
            )

            switchPreference(
                key = KEY_DYNAMIC_COLOR,
                defaultValue = false,
                title = { Text(stringResource(preference_dynamic_color)) },
                enabled = { isDynamicColorAvailable() }
            )

            preferenceCategory(
                key = KEY_APP_SYSTEM,
                title = { Text(stringResource(preference_system)) },
                modifier = Modifier.drawBehind {
                    drawLine(Color.LightGray, Offset(0f, 0f), Offset(size.width, 0f), 2f)
                }
            )

            preference(
                key = KEY_PERMISSIONS,
                title = { Text(stringResource(preference_permissions)) },
                onClick = toPermissionsScreen,
                summary = { Text(stringResource(text_tap_to_view)) }
            )

            preference(
                key = KEY_EXP_IMP,
                title = { Text(stringResource(preference_import_export)) },
                onClick = { showExport = true }
            )

            preference(
                key = KEY_FIXING,
                title = { Text(stringResource(preference_fixing_notifications)) },
                onClick = { showFixing = true }
            )

            preference(
                key = KEY_CLEAR_CACHE,
                title = { Text(stringResource(preference_clear_app_cache)) },
                onClick = { showClearing = true }
            )
        }
    }

    if (showExport) ExportImport { showExport = false }
    if (showFixing) DialogFixing { showFixing = false }
    if (showClearing) DialogClearing { showClearing = false }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun KitsManager(back: () -> Unit) {
    val scope = rememberCoroutineScope()
    val dao = database.kitDAO()
    val kits by dao.getFlow().collectAsStateWithLifecycle(emptyList())
    var gKitId by rememberSaveable { mutableLongStateOf(0L) }
    var show by rememberSaveable { mutableStateOf(false) }
    var text by rememberSaveable { mutableStateOf(BLANK) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(preference_kits_group)) },
                navigationIcon = {
                    IconButton(back) {
                        Icon(Icons.AutoMirrored.Outlined.ArrowBack, null)
                    }
                }
            )
        },
        floatingActionButton = {
            ExtendedFloatingActionButton(
                text = { Text(stringResource(text_add)) },
                icon = { Icon(Icons.Outlined.Add, null) },
                onClick = { show = true },
            )
        }
    ) { values ->
        LazyColumn(Modifier.padding(values)) {
            items(kits) { (kitId, title) ->
                ListItem(
                    headlineContent = { Text(title) },
                    leadingContent = {
                        IconButton(
                            onClick = { gKitId = kitId; text = title; show = true }
                        ) {
                            Icon(Icons.Outlined.Edit, null)
                        }
                    },
                    trailingContent = {
                        IconButton(
                            onClick = { scope.launch { dao.delete(Kit(kitId)) } }
                        ) {
                            Icon(Icons.Outlined.Delete, null)
                        }
                    }
                )
                HorizontalDivider()
            }
        }
    }

    if (show) AlertDialog(
        onDismissRequest = { show = false },
        confirmButton = {
            TextButton(
                enabled = text.isNotEmpty(),
                onClick = {
                    val kit = Kit(
                        kitId = gKitId,
                        title = text
                    )

                    text = BLANK
                    show = false
                    gKitId = 0L

                    scope.launch { dao.upsert(kit) }
                },
            ) { Text(stringResource(text_save)) }
        },
        dismissButton = {
            TextButton({ show = false; text = BLANK; gKitId = 0L }) {
                Text(stringResource(text_cancel))
            }
        },
        title = { Text(stringResource(text_kit_title)) },
        text = {
            OutlinedTextField(
                value = text,
                onValueChange = { text = it },
                keyboardOptions = KeyboardOptions(KeyboardCapitalization.Sentences)
            )
        }
    )
}

@Composable
fun PermissionsScreen(navigateUp: () -> Unit, exitFirstLaunch: () -> Unit) {
    val scheduleExactAlarms = rememberPermissionState(Manifest.permission.SCHEDULE_EXACT_ALARM)
    val postNotifications = rememberPermissionState(Manifest.permission.POST_NOTIFICATIONS)
    val fullScreenIntent = rememberPermissionState(Manifest.permission.USE_FULL_SCREEN_INTENT)
    val ignoreBattery = rememberPermissionState(Manifest.permission.REQUEST_IGNORE_BATTERY_OPTIMIZATIONS)

    Column(
        verticalArrangement = SpaceBetween,
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .fillMaxSize()
            .padding(12.dp)
            .verticalScroll(rememberScrollState())
    ) {
        Column(Modifier, spacedBy(8.dp), Alignment.CenterHorizontally) {
            Image(painterResource(R.drawable.vector_bell), null, Modifier.size(64.dp))
            Text(
                text = stringResource(text_pay_attention),
                style = MaterialTheme.typography.titleLarge
            )
            Text(
                text = stringResource(text_explain_request_permissions),
                textAlign = TextAlign.Center,
                style = MaterialTheme.typography.bodyMedium
            )
        }
        Column {
            if (VERSION.SDK_INT >= Build.VERSION_CODES.S) ListItem(
                headlineContent = { Text(stringResource(text_permission_title_reminders)) },
                trailingContent = { ButtonGrant(scheduleExactAlarms) },
                supportingContent = {
                    Text(
                        text = stringResource(text_explain_reminders),
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            )
            if (VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) ListItem(
                headlineContent = { Text(stringResource(text_permission_title_notifications)) },
                trailingContent = { ButtonGrant(postNotifications) },
                supportingContent = {
                    Text(
                        text = stringResource(text_explain_notifications),
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            )
            if (VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) ListItem(
                headlineContent = { Text(stringResource(text_permission_title_full_screen)) },
                trailingContent = { ButtonGrant(fullScreenIntent) },
                supportingContent = {
                    Text(
                        text = stringResource(text_explain_full_screen_intent),
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            )
            ListItem(
                headlineContent = { Text(stringResource(text_permission_title_ignore_battery)) },
                trailingContent = { ButtonGrant(ignoreBattery) },
                supportingContent = {
                    Text(
                        text = stringResource(text_expain_ignore_battery),
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            )
        }
        Row(Modifier.fillMaxWidth(), SpaceBetween, CenterVertically) {
            TextButton(navigateUp) { Text(stringResource(text_exit)) }
            Button(
                onClick = exitFirstLaunch,
                enabled = listOf(scheduleExactAlarms, postNotifications).all(PermissionState::isGranted)
            ) {
                Text(stringResource(text_save))
            }
        }
    }
}

@Composable
private fun ButtonGrant(permission: PermissionState, modifier: Modifier = Modifier) =
    TextButton(permission::launchRequest, modifier, !permission.isGranted) {
        Text(stringResource(if (permission.isGranted) text_permission_granted else text_permission_grant))
    }

@Composable
private fun ExportImport(onDismiss: () -> Unit) {
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
        val path = context.getDatabasePath(database.openHelper.databaseName)
        database.close()

        uri?.let { uriN ->
            context.contentResolver.openOutputStream(uriN).use { output ->
                output?.let { path.inputStream().copyTo(it) }
            }
            context.startActivity(
                Intent.makeRestartActivityTask(
                    context.packageManager.getLaunchIntentForPackage(context.packageName)!!.component
                ).putExtra(KEY_EXP_IMP, true)
            )
            Runtime.getRuntime().exit(0)
        }
    }

    val importer = rememberLauncherForActivityResult(OpenDocument()) { uri ->
        val path = context.getDatabasePath(database.openHelper.databaseName)

        uri?.let { uriN ->
            val cursor = database.openHelper.readableDatabase.query(queryG)
            val currentHash = if (cursor.moveToNext()) cursor.getString(1) else BLANK
            val tempFile = File.createTempFile("temp", ".sqlite", context.cacheDir)

            database.close()
            context.contentResolver.openInputStream(uriN).use { input ->
                tempFile.outputStream().use { output -> input?.copyTo(output) }
            }

            try {
                val newDB = openDatabase(tempFile.path, null, OPEN_READONLY)
                val newHash = if (hasTable(newDB)) getHash(newDB) else BLANK
                newDB.close()

                if (currentHash == newHash) {
                    AlarmSetter(context).cancelAll()

                    context.contentResolver.openInputStream(uriN).use { input ->
                        path.outputStream().use { output -> input?.copyTo(output) }
                    }

                    context.startActivity(
                        Intent.makeRestartActivityTask(
                            context.packageManager.getLaunchIntentForPackage(context.packageName)!!.component
                        ).putExtra(KEY_EXP_IMP, true)
                    )
                    Runtime.getRuntime().exit(0)
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
        confirmButton = { TextButton({ importer.launch(mimes) }) { Text(stringResource(text_import)) } },
        dismissButton = { TextButton({ exporter.launch(name) }) { Text(stringResource(text_export)) } },
        title = { Text(stringResource(text_attention)) },
        text = {
            Text(
                text = stringResource(text_export_import_description),
                style = MaterialTheme.typography.bodyLarge
            )
        }
    )
}

@Composable
private fun DialogFixing(back: () -> Unit) {
    val context = LocalContext.current

    AlertDialog(
        onDismissRequest = back,
        dismissButton = { TextButton(back) { Text(stringResource(text_cancel)) } },
        title = { Text(stringResource(text_attention)) },
        text = {
            Text(
                text = stringResource(text_fix_notifications),
                style = MaterialTheme.typography.bodyLarge
            )
        },
        confirmButton = {
            TextButton(
                onClick = {
                    AlarmSetter(context).resetAll()
                    context.startActivity(
                        Intent.makeRestartActivityTask(
                            context.packageManager.getLaunchIntentForPackage(context.packageName)!!.component
                        )
                    )
                    Runtime.getRuntime().exit(0)
                }
            ) {
                Text(stringResource(text_confirm))
            }
        }
    )
}

@Composable
private fun DialogClearing(onDismiss: () -> Unit) {
    val context = LocalContext.current
    val images = database.medicineDAO().getAllImages()

    AlertDialog(
        onDismissRequest = onDismiss,
        dismissButton = { TextButton(onDismiss) { Text(stringResource(text_cancel)) } },
        title = { Text(stringResource(text_attention)) },
        text = {
            Text(
                text = stringResource(text_clear_app_cache_description),
                style = MaterialTheme.typography.bodyLarge
            )
        },
        confirmButton = {
            TextButton(
                onClick = {
                    context.cacheDir.deleteRecursively()
                    context.filesDir.listFiles()?.forEach { file ->
                        MimeTypeMap.getSingleton().getMimeTypeFromExtension(file.extension)?.let {
                            if (file.name !in images && it.startsWith("image/"))
                                file.deleteRecursively()
                        }
                    }

                    onDismiss()
                    Toast.makeText(context, text_success, Toast.LENGTH_SHORT).show()
                }
            ) {
                Text(stringResource(text_confirm))
            }
        }
    )
}

private fun localize(name: String, values: List<String>, array: List<String>) =
    when (val index = values.indexOf(name)) {
        -1 -> AnnotatedString(BLANK)
        else -> AnnotatedString(array[index])
    }