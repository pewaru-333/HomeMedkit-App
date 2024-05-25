package ru.application.homemedkit.fragments

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.Intent.FLAG_ACTIVITY_CLEAR_TASK
import android.content.Intent.FLAG_ACTIVITY_NEW_TASK
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteDatabase.OPEN_READONLY
import android.database.sqlite.SQLiteDatabase.openDatabase
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts.CreateDocument
import androidx.activity.result.contract.ActivityResultContracts.OpenDocument
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
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
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
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
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.ramcosta.composedestinations.annotation.Destination
import com.ramcosta.composedestinations.annotation.RootGraph
import me.zhanghai.compose.preference.ListPreference
import me.zhanghai.compose.preference.ProvidePreferenceLocals
import me.zhanghai.compose.preference.SwitchPreference
import me.zhanghai.compose.preference.listPreference
import me.zhanghai.compose.preference.preference
import me.zhanghai.compose.preference.preferenceCategory
import me.zhanghai.compose.preference.switchPreference
import ru.application.homemedkit.R
import ru.application.homemedkit.activities.MainActivity
import ru.application.homemedkit.alarms.AlarmSetter
import ru.application.homemedkit.databaseController.Kit
import ru.application.homemedkit.databaseController.MedicineDatabase
import ru.application.homemedkit.helpers.BLANK
import ru.application.homemedkit.helpers.CHECK_EXP_DATE
import ru.application.homemedkit.helpers.KEY_APP_SYSTEM
import ru.application.homemedkit.helpers.KEY_APP_VIEW
import ru.application.homemedkit.helpers.KEY_DOWNLOAD
import ru.application.homemedkit.helpers.KEY_EXP_IMP
import ru.application.homemedkit.helpers.KEY_FRAGMENT
import ru.application.homemedkit.helpers.KEY_KITS
import ru.application.homemedkit.helpers.KEY_LIGHT_PERIOD
import ru.application.homemedkit.helpers.KEY_ORDER
import ru.application.homemedkit.helpers.LANGUAGES
import ru.application.homemedkit.helpers.MENUS
import ru.application.homemedkit.helpers.Preferences
import ru.application.homemedkit.helpers.SORTING
import ru.application.homemedkit.helpers.THEMES
import ru.application.homemedkit.ui.theme.isDynamicColorAvailable
import java.io.File

@Destination<RootGraph>
@Composable
fun SettingsScreen(context: Context = LocalContext.current, preferences: Preferences = Preferences(context)) {
    val pages = context.resources.getStringArray(R.array.fragment_pages_name)
    val sorting = context.resources.getStringArray(R.array.sorting_types_name)
    val languages = context.resources.getStringArray(R.array.languages_name)
    val themes = context.resources.getStringArray(R.array.app_themes_name)
    var showDialog by rememberSaveable { mutableStateOf(false) }
    var showExport by rememberSaveable { mutableStateOf(false) }
    
    ProvidePreferenceLocals {
        LazyColumn {
            preferenceCategory(
                key = KEY_APP_VIEW,
                title = { Text(context.getString(R.string.preference_app_view)) },
            )

            preference(
                key = KEY_KITS,
                title = { Text(context.getString(R.string.preference_kits_group)) },
                onClick = { showDialog = true },
                summary = { Text(context.getString(R.string.text_tap_to_view)) }
            )

            listPreference(
                key = KEY_FRAGMENT,
                defaultValue = MENUS[0],
                values = MENUS,
                title = { Text(context.getString(R.string.preference_start_page)) },
                summary = { Text(localize(it, MENUS, pages)) },
                valueToText = { localize(it, MENUS, pages) }
            )

            listPreference(
                key = KEY_ORDER,
                defaultValue = SORTING[0],
                values = SORTING,
                title = { Text(context.getString(R.string.preference_sorting_type)) },
                summary = { Text(localize(it, SORTING, sorting)) },
                valueToText = { localize(it, SORTING, sorting) }
            )

            switchPreference(
                key = KEY_DOWNLOAD,
                defaultValue = true,
                title = { Text(context.getString(R.string.preference_download_images)) },
                summary = { Text(context.getString(if (it) R.string.text_on else R.string.text_off)) }
            )

            switchPreference(
                key = KEY_LIGHT_PERIOD,
                defaultValue = true,
                title = { Text(context.getString(R.string.preference_easy_period_picker)) },
                summary = { Text(context.getString(if (it) R.string.text_on else R.string.text_off)) }
            )

            switchPreference(
                key = CHECK_EXP_DATE,
                defaultValue = false,
                title = { Text(context.getString(R.string.preference_check_expiration_date)) },
                summary = { Text(context.getString(if (it) R.string.text_daily_at else R.string.text_off)) }
            )

            preferenceCategory(
                key = KEY_APP_SYSTEM,
                title = { Text(context.getString(R.string.preference_system)) },
                modifier = Modifier.drawBehind {
                    drawLine(Color.LightGray, Offset(0f, 0f), Offset(size.width, 0f), 2f)
                }
            )

            item {
                var value by remember { mutableStateOf(preferences.getLanguage()) }

                ListPreference(
                    value = value,
                    onValueChange = { value = it; preferences.setLanguage(value) },
                    values = LANGUAGES,
                    title = { Text(context.getString(R.string.preference_language)) },
                    summary = { Text(localize(value, LANGUAGES, languages)) },
                    valueToText = { localize(it, LANGUAGES, languages) }
                )
            }

            item {
                var value by remember { mutableStateOf(preferences.getAppTheme()) }

                ListPreference(
                    value = value,
                    onValueChange = { value = it; preferences.setTheme(value) },
                    values = THEMES,
                    title = { Text(context.getString(R.string.preference_app_theme)) },
                    summary = { Text(localize(value, THEMES, themes)) },
                    valueToText = { localize(it, THEMES, themes) }
                )
            }

            item {
                var value by remember { mutableStateOf(preferences.getDynamicColors()) }

                SwitchPreference(
                    value = value,
                    onValueChange = { value = it; preferences.setDynamicColors(value) },
                    title = { Text(context.getString(R.string.preference_dynamic_color)) },
                    enabled = isDynamicColorAvailable()
                )
            }

            preference(
                key = KEY_EXP_IMP,
                title = { Text(context.getString(R.string.preference_import_export)) },
                onClick = { showExport = true }
            )
        }
    }

    if (showDialog) KitsManager({ showDialog = false })
    if (showExport) ExportImport({ showExport = false })
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun KitsManager(onDismiss: () -> Unit, context: Context = LocalContext.current) {
    val dao = MedicineDatabase.getInstance(context).kitDAO()
    val kits by dao.getFlow().collectAsState(emptyList())
    var show by rememberSaveable { mutableStateOf(false) }
    var text by rememberSaveable { mutableStateOf(BLANK) }

    Dialog(onDismiss, DialogProperties(usePlatformDefaultWidth = false)) {
        Surface(Modifier.fillMaxSize()) {
            Scaffold(
                topBar = {
                    TopAppBar(
                        title = { Text(context.getString(R.string.preference_kits_group)) },
                        navigationIcon = {
                            IconButton(onDismiss) {
                                Icon(Icons.AutoMirrored.Filled.ArrowBack, null)
                            }
                        }
                    )
                },
                floatingActionButton = {
                    ExtendedFloatingActionButton(
                        { Text(context.getString(R.string.text_add)) },
                        { Icon(Icons.Default.Add, null) },
                        { show = true },
                    )
                }
            ) { paddingValues ->
                Column(Modifier.padding(top = paddingValues.calculateTopPadding())) {
                    kits.forEach { kit ->
                        ListItem(
                            headlineContent = { Text(kit.title) },
                            trailingContent = {
                                IconButton({ dao.delete(Kit(kit.kitId)) }) {
                                    Icon(Icons.Default.Delete, null)
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
                    ) { Text(context.getString(R.string.text_save)) }
                },
                dismissButton = {
                    TextButton({ show = false; text = BLANK }) {
                        Text(context.getString(R.string.text_cancel))
                    }
                },
                title = { Text(context.getString(R.string.text_new_group)) },
                text = {
                    OutlinedTextField(
                        value = text,
                        onValueChange = { text = it },
                        placeholder = { Text(context.getString(R.string.placeholder_kitchen)) }
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

    fun showToast(success: Boolean) = Toast.makeText(
        context, context.getString(if (success) R.string.text_success else R.string.text_error),
        Toast.LENGTH_LONG).show()

    val exporter = rememberLauncherForActivityResult(CreateDocument(mimes[0])) { uri ->
        val current = MedicineDatabase.getInstance(context)
        val path = context.getDatabasePath(current.openHelper.databaseName).also { current.close() }

        uri?.let { uriN ->
            context.contentResolver.openOutputStream(uriN).use { output ->
                output?.let { path.inputStream().copyTo(it) }
            }.also {
                onDismiss()
                MedicineDatabase.setNull()
                showToast(true)
            }
        }
    }

    val importer = rememberLauncherForActivityResult(OpenDocument()) { uri ->
        val current = MedicineDatabase.getInstance(context)
        val path = context.getDatabasePath(current.openHelper.databaseName).also { current.close() }

        uri?.let { uriN ->
            val cursor = current.openHelper.readableDatabase.query(queryG)
            val currentHash = if (cursor.moveToNext()) cursor.getString(1) else BLANK
            val tempFile = File.createTempFile("temp", ".sqlite", context.cacheDir)

            context.contentResolver.openInputStream(uriN).use { input ->
                tempFile.outputStream().use { output -> input?.copyTo(output) }
            }

            try {
                val newDB = openDatabase(tempFile.path, null, OPEN_READONLY)
                val newHash = if (hasTable(newDB)) getHash(newDB) else BLANK

                if (currentHash == newHash) {
                    MedicineDatabase.setNull().also { AlarmSetter(context).cancelAll() }

                    context.contentResolver.openInputStream(uriN).use { input ->
                        path.outputStream().use { output -> input?.copyTo(output) }
                    }.also {
                        (context as Activity).finishAndRemoveTask()
                        context.startActivity(
                            Intent(context, MainActivity::class.java)
                                .setFlags(FLAG_ACTIVITY_CLEAR_TASK or FLAG_ACTIVITY_NEW_TASK)
                        )

                        MedicineDatabase.setNull().also { AlarmSetter(context).resetAll() }
                        showToast(true)
                    }
                } else showToast(false)
            } catch (e: Exception) {
                showToast(false)
            } finally {
                tempFile.delete()
            }
        }
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        confirmButton = { Button({ importer.launch(mimes) }) { Text(context.getString(R.string.text_import)) }},
        dismissButton = { Button({ exporter.launch(name) }) { Text(context.getString(R.string.text_export)) }},
        title = { Text(context.getString(R.string.text_attention)) },
        text = {
            Text(
                text = context.getString(R.string.text_export_import_description),
                style = MaterialTheme.typography.titleMedium
            )
        }
    )
}

private fun localize(name: String, values: List<String>, array: Array<String>): AnnotatedString =
    when (val index = values.indexOf(name)) {
        -1 -> AnnotatedString(BLANK)
        else -> AnnotatedString(array[index])
    }