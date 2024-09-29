package ru.application.homemedkit.helpers

import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import com.ramcosta.composedestinations.generated.destinations.HomeScreenDestination
import com.ramcosta.composedestinations.generated.destinations.IntakesScreenDestination
import com.ramcosta.composedestinations.generated.destinations.MedicinesScreenDestination
import com.ramcosta.composedestinations.generated.destinations.SettingsScreenDestination
import com.ramcosta.composedestinations.spec.Direction
import ru.application.homemedkit.R.drawable.vector_after_food
import ru.application.homemedkit.R.drawable.vector_before_food
import ru.application.homemedkit.R.drawable.vector_home
import ru.application.homemedkit.R.drawable.vector_in_food
import ru.application.homemedkit.R.drawable.vector_medicine
import ru.application.homemedkit.R.drawable.vector_settings
import ru.application.homemedkit.R.drawable.vector_time
import ru.application.homemedkit.R.drawable.vector_type_aerosol
import ru.application.homemedkit.R.drawable.vector_type_capsule
import ru.application.homemedkit.R.drawable.vector_type_decoction
import ru.application.homemedkit.R.drawable.vector_type_dragee
import ru.application.homemedkit.R.drawable.vector_type_drops
import ru.application.homemedkit.R.drawable.vector_type_emulsion
import ru.application.homemedkit.R.drawable.vector_type_extract
import ru.application.homemedkit.R.drawable.vector_type_gel
import ru.application.homemedkit.R.drawable.vector_type_granules
import ru.application.homemedkit.R.drawable.vector_type_mix
import ru.application.homemedkit.R.drawable.vector_type_mixture
import ru.application.homemedkit.R.drawable.vector_type_nasal_spray
import ru.application.homemedkit.R.drawable.vector_type_ointment
import ru.application.homemedkit.R.drawable.vector_type_paste
import ru.application.homemedkit.R.drawable.vector_type_pills
import ru.application.homemedkit.R.drawable.vector_type_powder
import ru.application.homemedkit.R.drawable.vector_type_solution
import ru.application.homemedkit.R.drawable.vector_type_suppository
import ru.application.homemedkit.R.drawable.vector_type_suspension
import ru.application.homemedkit.R.drawable.vector_type_syrup
import ru.application.homemedkit.R.drawable.vector_type_tablets
import ru.application.homemedkit.R.drawable.vector_type_tincture
import ru.application.homemedkit.R.string.blank
import ru.application.homemedkit.R.string.bottom_bar_intakes
import ru.application.homemedkit.R.string.bottom_bar_main
import ru.application.homemedkit.R.string.bottom_bar_medicines
import ru.application.homemedkit.R.string.bottom_bar_settings
import ru.application.homemedkit.R.string.dose_ed
import ru.application.homemedkit.R.string.dose_g
import ru.application.homemedkit.R.string.dose_l
import ru.application.homemedkit.R.string.dose_mg
import ru.application.homemedkit.R.string.dose_ml
import ru.application.homemedkit.R.string.dose_pcs
import ru.application.homemedkit.R.string.dose_ratio
import ru.application.homemedkit.R.string.intake_interval_daily
import ru.application.homemedkit.R.string.intake_interval_other
import ru.application.homemedkit.R.string.intake_interval_weekly
import ru.application.homemedkit.R.string.intake_period_indef
import ru.application.homemedkit.R.string.intake_period_other
import ru.application.homemedkit.R.string.intake_period_pick
import ru.application.homemedkit.R.string.intake_text_food_after
import ru.application.homemedkit.R.string.intake_text_food_before
import ru.application.homemedkit.R.string.intake_text_food_during
import ru.application.homemedkit.R.string.lang_cs
import ru.application.homemedkit.R.string.lang_de
import ru.application.homemedkit.R.string.lang_en
import ru.application.homemedkit.R.string.lang_es
import ru.application.homemedkit.R.string.lang_it
import ru.application.homemedkit.R.string.lang_ko
import ru.application.homemedkit.R.string.lang_pl
import ru.application.homemedkit.R.string.lang_pt_BR
import ru.application.homemedkit.R.string.lang_ru
import ru.application.homemedkit.R.string.lang_system
import ru.application.homemedkit.R.string.lang_tr
import ru.application.homemedkit.R.string.lang_vi
import ru.application.homemedkit.R.string.lang_zh_TW
import ru.application.homemedkit.R.string.sorting_a_z
import ru.application.homemedkit.R.string.sorting_from_newest
import ru.application.homemedkit.R.string.sorting_from_oldest
import ru.application.homemedkit.R.string.sorting_z_a
import ru.application.homemedkit.R.string.theme_dark
import ru.application.homemedkit.R.string.theme_light
import ru.application.homemedkit.R.string.theme_system
import ru.application.homemedkit.R.string.type_aerosol
import ru.application.homemedkit.R.string.type_capsules
import ru.application.homemedkit.R.string.type_decoction
import ru.application.homemedkit.R.string.type_dragee
import ru.application.homemedkit.R.string.type_drops
import ru.application.homemedkit.R.string.type_emulsion
import ru.application.homemedkit.R.string.type_extract
import ru.application.homemedkit.R.string.type_gel
import ru.application.homemedkit.R.string.type_granules
import ru.application.homemedkit.R.string.type_mix
import ru.application.homemedkit.R.string.type_mixture
import ru.application.homemedkit.R.string.type_ointment
import ru.application.homemedkit.R.string.type_paste
import ru.application.homemedkit.R.string.type_pills
import ru.application.homemedkit.R.string.type_powder
import ru.application.homemedkit.R.string.type_solution
import ru.application.homemedkit.R.string.type_spray
import ru.application.homemedkit.R.string.type_suppository
import ru.application.homemedkit.R.string.type_suspension
import ru.application.homemedkit.R.string.type_syrup
import ru.application.homemedkit.R.string.type_tablets
import ru.application.homemedkit.R.string.type_tincture
import ru.application.homemedkit.data.dto.Medicine
import java.util.Comparator.comparing

// ============================================ Strings ============================================
const val ALARM_ID = "alarmId"
const val BLANK = ""
const val CATEGORY = "drugs"
const val CIS = "cis"
const val CHANNEL_ID = "intake_notifications"
const val CHECK_EXP_DATE = "check_exp_date"
const val ID = "id"
const val KEY_APP_THEME = "app_theme"
const val KEY_APP_SYSTEM = "app_system"
const val KEY_APP_VIEW = "app_view"
const val KEY_MED_COMPACT_VIEW = "med_comp_view"
const val KEY_DYNAMIC_COLOR = "dynamic_color"
const val KEY_DOWNLOAD = "download_images"
const val KEY_EXP_IMP = "export_import"
const val KEY_FRAGMENT = "default_start_fragment"
const val KEY_KITS = "kits_group"
const val KEY_LANGUAGE = "language"
const val KEY_LAST_KIT = "last_kit"
const val KEY_LIGHT_PERIOD = "light_period_picker"
const val KEY_ORDER = "sorting_order"
const val SOUND_GROUP = "Sound group"
const val TAKEN_ID = "takenId"
const val TYPE = "vector_type"

// ========================================== Collections ==========================================
val LANGUAGES = Languages.entries.map(Languages::value)
val MENUS = Menu.entries.map { it.route.route }
val SORTING = Sorting.entries.map(Sorting::value)
val THEMES = Themes.entries.map(Themes::value)

// ============================================= Enums =============================================
enum class DoseTypes(val value: String, @StringRes val title: Int) {
    Units("ed", dose_ed),
    Pieces("pcs", dose_pcs),
    Grams("g", dose_g),
    Milligrams("mg", dose_mg),
    Liters("l", dose_l),
    Milliliters("ml", dose_ml),
    Ratio("ratio", dose_ratio);

    companion object {
        fun getValue(value: String) = DoseTypes.entries.find { it.value == value }
        fun getTitle(value: String?) = DoseTypes.entries.find { it.value == value }?.title ?: blank
    }
}

enum class FoodTypes(val value: Int, @StringRes val title: Int, @DrawableRes val icon: Int) {
    Before(0, intake_text_food_before, vector_before_food),
    During(1, intake_text_food_during, vector_in_food),
    After(2, intake_text_food_after, vector_after_food)
}

enum class Intervals(val days: Int, @StringRes val title: Int) {
    Daily(1, intake_interval_daily),
    Weekly(7, intake_interval_weekly),
    Custom(10, intake_interval_other);

    companion object {
        fun getValue(days: Int) = Intervals.entries.find { it.days == days } ?: Custom
        fun getTitle(days: String) = try {
            when (days.toInt()) {
                1 -> Daily.title
                7 -> Weekly.title
                else -> Custom.title
            }
        } catch (e: NumberFormatException) {
            blank
        }
    }
}

enum class Languages(val value: String, @StringRes val title: Int) {
    System("System", lang_system),
    Russian("ru", lang_ru),
    English("en-US", lang_en),
    German("de", lang_de),
    Italian("it", lang_it),
    Spanish("es", lang_es),
    PortugalB("pt-BR", lang_pt_BR),
    Czech("cs", lang_cs),
    Polish("pl", lang_pl),
    Turkish("tr", lang_tr),
    Vietnamese("vi", lang_vi),
    Korean("ko", lang_ko),
    ChineseTW("zh-TW", lang_zh_TW),
}

enum class Menu(val route: Direction, @StringRes val title: Int, @DrawableRes val icon: Int) {
    Home(HomeScreenDestination, bottom_bar_main, vector_home),
    Medicines(MedicinesScreenDestination, bottom_bar_medicines, vector_medicine),
    Intakes(IntakesScreenDestination, bottom_bar_intakes, vector_time),
    Settings(SettingsScreenDestination, bottom_bar_settings, vector_settings)
}

enum class Periods(val days: Int, @StringRes val title: Int) {
    Pick(-1, intake_period_pick),
    Other(21, intake_period_other),
    Indefinite(38500, intake_period_indef);

    companion object {
        fun getValue(days: Int) = Periods.entries.find { it.days == days } ?: Other
    }
}

enum class Sorting(val value: String, @StringRes val title: Int, val type: Comparator<Medicine>) {
    InName("A-z", sorting_a_z, comparing(Medicine::productName)),
    ReName("z-A", sorting_z_a, comparing(Medicine::productName).reversed()),
    InDate("old-new", sorting_from_oldest, comparing(Medicine::expDate)),
    ReDate("new-old", sorting_from_newest, comparing(Medicine::expDate).reversed())
}

enum class Themes(val value: String, @StringRes val title: Int) {
    System("System", theme_system),
    Light("Light", theme_light),
    Dark("Dark", theme_dark)
}

enum class Types(val value: String, @StringRes val title: Int, @DrawableRes val icon: Int) {
    Tablets("vector_type_tablets", type_tablets, vector_type_tablets),
    Capsules("vector_type_capsule", type_capsules, vector_type_capsule),
    Pills("vector_type_pills", type_pills, vector_type_pills),
    Dragee("vector_type_dragee", type_dragee, vector_type_dragee),
    Granules("vector_type_granules", type_granules, vector_type_granules),
    Powder("vector_type_powder", type_powder, vector_type_powder),
    Solution("vector_type_solution", type_solution, vector_type_solution),
    Tincture("vector_type_tincture", type_tincture, vector_type_tincture),
    Decoction("vector_type_decoction", type_decoction, vector_type_decoction),
    Extract("vector_type_extract", type_extract, vector_type_extract),
    Mixture("vector_type_mixture", type_mixture, vector_type_mixture),
    Syrup("vector_type_syrup", type_syrup, vector_type_syrup),
    Emulsion("vector_type_emulsion", type_emulsion, vector_type_emulsion),
    Suspension("vector_type_suspension", type_suspension, vector_type_suspension),
    Mix("vector_type_mix", type_mix, vector_type_mix),
    Ointment("vector_type_ointment", type_ointment, vector_type_ointment),
    Gel("vector_type_gel", type_gel, vector_type_gel),
    Paste("vector_type_paste", type_paste, vector_type_paste),
    Suppository("vector_type_suppository", type_suppository, vector_type_suppository),
    Aerosol("vector_type_aerosol", type_aerosol, vector_type_aerosol),
    Spray("vector_type_nasal_spray", type_spray, vector_type_nasal_spray),
    Drops("vector_type_drops", type_drops, vector_type_drops)
}