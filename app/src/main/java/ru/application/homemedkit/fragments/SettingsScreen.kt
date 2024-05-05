package ru.application.homemedkit.fragments

import android.content.Context
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.AnnotatedString
import com.google.android.material.color.DynamicColors
import com.ramcosta.composedestinations.annotation.Destination
import com.ramcosta.composedestinations.annotation.RootGraph
import me.zhanghai.compose.preference.ListPreference
import me.zhanghai.compose.preference.ProvidePreferenceLocals
import me.zhanghai.compose.preference.SwitchPreference
import me.zhanghai.compose.preference.listPreference
import me.zhanghai.compose.preference.preferenceCategory
import me.zhanghai.compose.preference.switchPreference
import ru.application.homemedkit.R
import ru.application.homemedkit.helpers.BLANK
import ru.application.homemedkit.helpers.CHECK_EXP_DATE
import ru.application.homemedkit.helpers.KEY_APP_SYSTEM
import ru.application.homemedkit.helpers.KEY_APP_VIEW
import ru.application.homemedkit.helpers.KEY_DOWNLOAD
import ru.application.homemedkit.helpers.KEY_FRAGMENT
import ru.application.homemedkit.helpers.KEY_LIGHT_PERIOD
import ru.application.homemedkit.helpers.KEY_ORDER
import ru.application.homemedkit.helpers.LANGUAGES
import ru.application.homemedkit.helpers.MENUS
import ru.application.homemedkit.helpers.Preferences
import ru.application.homemedkit.helpers.SORTING
import ru.application.homemedkit.helpers.THEMES

@Destination<RootGraph>
@Composable
fun SettingsScreen(context: Context = LocalContext.current, preferences: Preferences = Preferences(context)) {
    val pages = context.resources.getStringArray(R.array.fragment_pages_name)
    val sorting = context.resources.getStringArray(R.array.sorting_types_name)
    val languages = context.resources.getStringArray(R.array.languages_name)
    val themes = context.resources.getStringArray(R.array.app_themes_name)

    ProvidePreferenceLocals {
        LazyColumn {
            preferenceCategory(
                key = KEY_APP_VIEW,
                title = { Text(context.getString(R.string.preference_app_view)) },
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
                    enabled = DynamicColors.isDynamicColorAvailable()
                )
            }
        }
    }
}

private fun localize(name: String, values: List<String>, array: Array<String>): AnnotatedString =
    when (val index = values.indexOf(name)) {
        -1 -> AnnotatedString(BLANK)
        else -> AnnotatedString(array[index])
    }