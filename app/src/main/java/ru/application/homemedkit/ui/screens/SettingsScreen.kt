package ru.application.homemedkit.ui.screens

import android.content.Context
import android.content.Intent
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteDatabase.OPEN_READONLY
import android.database.sqlite.SQLiteDatabase.openDatabase
import android.os.Build
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts.CreateDocument
import androidx.activity.result.contract.ActivityResultContracts.OpenDocument
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
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
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import me.zhanghai.compose.preference.ListPreference
import me.zhanghai.compose.preference.ProvidePreferenceLocals
import me.zhanghai.compose.preference.SwitchPreference
import me.zhanghai.compose.preference.listPreference
import me.zhanghai.compose.preference.preference
import me.zhanghai.compose.preference.preferenceCategory
import me.zhanghai.compose.preference.switchPreference
import ru.application.homemedkit.HomeMeds.Companion.database
import ru.application.homemedkit.R.string.placeholder_kitchen
import ru.application.homemedkit.R.string.preference_app_theme
import ru.application.homemedkit.R.string.preference_app_view
import ru.application.homemedkit.R.string.preference_check_expiration_date
import ru.application.homemedkit.R.string.preference_download_images
import ru.application.homemedkit.R.string.preference_dynamic_color
import ru.application.homemedkit.R.string.preference_import_export
import ru.application.homemedkit.R.string.preference_kits_group
import ru.application.homemedkit.R.string.preference_language
import ru.application.homemedkit.R.string.preference_med_compact_view
import ru.application.homemedkit.R.string.preference_sorting_type
import ru.application.homemedkit.R.string.preference_system
import ru.application.homemedkit.R.string.text_add
import ru.application.homemedkit.R.string.text_attention
import ru.application.homemedkit.R.string.text_cancel
import ru.application.homemedkit.R.string.text_daily_at
import ru.application.homemedkit.R.string.text_export
import ru.application.homemedkit.R.string.text_export_import_description
import ru.application.homemedkit.R.string.text_import
import ru.application.homemedkit.R.string.text_new_group
import ru.application.homemedkit.R.string.text_off
import ru.application.homemedkit.R.string.text_on
import ru.application.homemedkit.R.string.text_save
import ru.application.homemedkit.R.string.text_tap_to_view
import ru.application.homemedkit.data.dto.Kit
import ru.application.homemedkit.helpers.BLANK
import ru.application.homemedkit.helpers.KEY_APP_SYSTEM
import ru.application.homemedkit.helpers.KEY_APP_THEME
import ru.application.homemedkit.helpers.KEY_APP_VIEW
import ru.application.homemedkit.helpers.KEY_DOWNLOAD
import ru.application.homemedkit.helpers.KEY_DYNAMIC_COLOR
import ru.application.homemedkit.helpers.KEY_EXP_IMP
import ru.application.homemedkit.helpers.KEY_KITS
import ru.application.homemedkit.helpers.KEY_MED_COMPACT_VIEW
import ru.application.homemedkit.helpers.KEY_ORDER
import ru.application.homemedkit.helpers.LANGUAGES
import ru.application.homemedkit.helpers.Languages
import ru.application.homemedkit.helpers.Preferences
import ru.application.homemedkit.helpers.SORTING
import ru.application.homemedkit.helpers.Sorting
import ru.application.homemedkit.helpers.THEMES
import ru.application.homemedkit.helpers.Themes
import ru.application.homemedkit.helpers.showToast
import ru.application.homemedkit.receivers.AlarmSetter
import ru.application.homemedkit.ui.theme.isDynamicColorAvailable
import java.io.File

@Composable
fun SettingsScreen(backClick: () -> Unit) {
    val context = LocalContext.current

    val sorting = Sorting.entries.map { stringResource(it.title) }
    val languages = Languages.entries.map { stringResource(it.title) }
    val themes = Themes.entries.map { stringResource(it.title) }

    var showDialog by rememberSaveable { mutableStateOf(false) }
    var showExport by rememberSaveable { mutableStateOf(false) }

    BackHandler(onBack = backClick)
    ProvidePreferenceLocals {
        LazyColumn {
            preferenceCategory(
                key = KEY_APP_VIEW,
                title = { Text(stringResource(preference_app_view)) },
            )

            preference(
                key = KEY_KITS,
                title = { Text(stringResource(preference_kits_group)) },
                onClick = { showDialog = true },
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
                key = KEY_MED_COMPACT_VIEW,
                defaultValue = false,
                title = { Text(stringResource(preference_med_compact_view)) },
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
                key = KEY_APP_SYSTEM,
                title = { Text(stringResource(preference_system)) },
                modifier = Modifier.drawBehind {
                    drawLine(Color.LightGray, Offset(0f, 0f), Offset(size.width, 0f), 2f)
                }
            )

            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU) item {
                var value by remember { mutableStateOf(Preferences.getLanguage()) }

                ListPreference(
                    value = value,
                    onValueChange = { value = it; Preferences.setLocale(context, it) },
                    values = LANGUAGES,
                    title = { Text(stringResource(preference_language)) },
                    summary = { Text(localize(value, LANGUAGES, languages)) },
                    valueToText = { localize(it, LANGUAGES, languages) }
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

            preference(
                key = KEY_EXP_IMP,
                title = { Text(stringResource(preference_import_export)) },
                onClick = { showExport = true }
            )
        }
    }

    if (showDialog) KitsManager { showDialog = false }
    if (showExport) ExportImport({ showExport = false })
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun KitsManager(onDismiss: () -> Unit) {
    val dao = database.kitDAO()
    val kits by dao.getFlow().collectAsStateWithLifecycle(emptyList())
    var show by rememberSaveable { mutableStateOf(false) }
    var text by rememberSaveable { mutableStateOf(BLANK) }

    Dialog(onDismiss, DialogProperties(usePlatformDefaultWidth = false)) {
        Surface(Modifier.fillMaxSize()) {
            Scaffold(
                topBar = {
                    TopAppBar(
                        title = { Text(stringResource(preference_kits_group)) },
                        navigationIcon = {
                            IconButton(onDismiss) {
                                Icon(Icons.AutoMirrored.Outlined.ArrowBack, null)
                            }
                        }
                    )
                },
                floatingActionButton = {
                    ExtendedFloatingActionButton(
                        { Text(stringResource(text_add)) },
                        { Icon(Icons.Outlined.Add, null) },
                        { show = true },
                    )
                }
            ) { paddingValues ->
                Column(Modifier.padding(top = paddingValues.calculateTopPadding())) {
                    kits.forEach { (kitId, title) ->
                        ListItem(
                            headlineContent = { Text(title) },
                            trailingContent = {
                                IconButton({ dao.delete(Kit(kitId)) }) {
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
                        onClick = { dao.add(Kit(title = text)); text = BLANK; show = false },
                        enabled = text.isNotEmpty()
                    ) { Text(stringResource(text_save)) }
                },
                dismissButton = {
                    TextButton({ show = false; text = BLANK }) {
                        Text(stringResource(text_cancel))
                    }
                },
                title = { Text(stringResource(text_new_group)) },
                text = {
                    OutlinedTextField(
                        value = text,
                        onValueChange = { text = it },
                        placeholder = { Text(stringResource(placeholder_kitchen)) }
                    )
                }
            )
        }
    }
}

@Composable
private fun ExportImport(onDismiss: () -> Unit, context: Context = LocalContext.current) {
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
                } else showToast(false, context)
            } catch (e: Throwable) {
                showToast(false, context)
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
        text = { Text(stringResource(text_export_import_description)) }
    )
}

private fun localize(name: String, values: List<String>, array: List<String>) =
    when (val index = values.indexOf(name)) {
        -1 -> AnnotatedString(BLANK)
        else -> AnnotatedString(array[index])
    }