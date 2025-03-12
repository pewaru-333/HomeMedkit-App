package ru.application.homemedkit.helpers

import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import ru.application.homemedkit.R
import ru.application.homemedkit.R.drawable.vector_medicine
import ru.application.homemedkit.R.drawable.vector_settings
import ru.application.homemedkit.R.drawable.vector_time
import ru.application.homemedkit.R.drawable.vector_type_aerosol
import ru.application.homemedkit.R.drawable.vector_type_bandage
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
import ru.application.homemedkit.R.drawable.vector_type_napkins
import ru.application.homemedkit.R.drawable.vector_type_nasal_spray
import ru.application.homemedkit.R.drawable.vector_type_ointment
import ru.application.homemedkit.R.drawable.vector_type_paste
import ru.application.homemedkit.R.drawable.vector_type_patch
import ru.application.homemedkit.R.drawable.vector_type_pills
import ru.application.homemedkit.R.drawable.vector_type_powder
import ru.application.homemedkit.R.drawable.vector_type_sachet
import ru.application.homemedkit.R.drawable.vector_type_solution
import ru.application.homemedkit.R.drawable.vector_type_suppository
import ru.application.homemedkit.R.drawable.vector_type_suspension
import ru.application.homemedkit.R.drawable.vector_type_syrup
import ru.application.homemedkit.R.drawable.vector_type_tablets
import ru.application.homemedkit.R.drawable.vector_type_tincture
import ru.application.homemedkit.R.drawable.vector_type_unknown
import ru.application.homemedkit.R.string.blank
import ru.application.homemedkit.R.string.bottom_bar_intakes
import ru.application.homemedkit.R.string.bottom_bar_medicines
import ru.application.homemedkit.R.string.bottom_bar_settings
import ru.application.homemedkit.R.string.dose_ed
import ru.application.homemedkit.R.string.dose_g
import ru.application.homemedkit.R.string.dose_l
import ru.application.homemedkit.R.string.dose_mg
import ru.application.homemedkit.R.string.dose_ml
import ru.application.homemedkit.R.string.dose_pcs
import ru.application.homemedkit.R.string.dose_ratio
import ru.application.homemedkit.R.string.dose_sach
import ru.application.homemedkit.R.string.intake_extra_cancellable
import ru.application.homemedkit.R.string.intake_extra_desc_cancellable
import ru.application.homemedkit.R.string.intake_extra_desc_prealarm
import ru.application.homemedkit.R.string.intake_extra_fullscreen
import ru.application.homemedkit.R.string.intake_extra_no_sound
import ru.application.homemedkit.R.string.intake_extra_prealarm
import ru.application.homemedkit.R.string.intake_interval_daily
import ru.application.homemedkit.R.string.intake_interval_other
import ru.application.homemedkit.R.string.intake_interval_weekly
import ru.application.homemedkit.R.string.intake_period_indef
import ru.application.homemedkit.R.string.intake_period_other
import ru.application.homemedkit.R.string.intake_period_pick
import ru.application.homemedkit.R.string.intake_text_food_after
import ru.application.homemedkit.R.string.intake_text_food_before
import ru.application.homemedkit.R.string.intake_text_food_during
import ru.application.homemedkit.R.string.sorting_a_z
import ru.application.homemedkit.R.string.sorting_from_newest
import ru.application.homemedkit.R.string.sorting_from_oldest
import ru.application.homemedkit.R.string.sorting_z_a
import ru.application.homemedkit.R.string.theme_dark
import ru.application.homemedkit.R.string.theme_light
import ru.application.homemedkit.R.string.theme_system
import ru.application.homemedkit.R.string.type_aerosol
import ru.application.homemedkit.R.string.type_bandage
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
import ru.application.homemedkit.R.string.type_napkins
import ru.application.homemedkit.R.string.type_ointment
import ru.application.homemedkit.R.string.type_paste
import ru.application.homemedkit.R.string.type_patch
import ru.application.homemedkit.R.string.type_pills
import ru.application.homemedkit.R.string.type_powder
import ru.application.homemedkit.R.string.type_sachet
import ru.application.homemedkit.R.string.type_solution
import ru.application.homemedkit.R.string.type_spray
import ru.application.homemedkit.R.string.type_suppository
import ru.application.homemedkit.R.string.type_suspension
import ru.application.homemedkit.R.string.type_syrup
import ru.application.homemedkit.R.string.type_tablets
import ru.application.homemedkit.R.string.type_tincture
import ru.application.homemedkit.data.dto.Medicine
import ru.application.homemedkit.helpers.DoseTypes.GRAMS
import ru.application.homemedkit.helpers.DoseTypes.MILLIGRAMS
import ru.application.homemedkit.helpers.DoseTypes.MILLILITERS
import ru.application.homemedkit.helpers.DoseTypes.PIECES
import ru.application.homemedkit.helpers.DoseTypes.entries
import ru.application.homemedkit.helpers.Intervals.entries
import ru.application.homemedkit.helpers.Periods.entries
import ru.application.homemedkit.helpers.Types.entries
import ru.application.homemedkit.ui.navigation.Screen
import ru.application.homemedkit.ui.navigation.Screen.Intakes
import ru.application.homemedkit.ui.navigation.Screen.Medicines
import ru.application.homemedkit.ui.navigation.Screen.Settings

// ============================================ Strings ============================================
const val ALARM_ID = "alarmId"
const val BLANK = ""
const val CHANNEL_ID_EXP = "channel_expiration"
const val CHANNEL_ID_INTAKES = "channel_intakes"
const val CHANNEL_ID_PRE = "channel_prealarm"
const val CIS = "cis"
const val DATABASE_NAME = "medicines"
const val ID = "id"
const val KEY_APP_SYSTEM = "app_system"
const val KEY_APP_THEME = "app_theme"
const val KEY_APP_VIEW = "app_view"
const val KEY_BASIC_SETTINGS = "app_basic_settings"
const val KEY_CHECK_EXP_DATE = "check_exp_date"
const val KEY_CLEAR_CACHE = "clear_app_cache"
const val KEY_CONFIRM_EXIT = "confirm_exit"
const val KEY_DOWNLOAD = "download_images"
const val KEY_DYNAMIC_COLOR = "dynamic_color"
const val KEY_EXP_IMP = "export_import"
const val KEY_FIRST_LAUNCH_INTAKE = "first_launch_intake"
const val KEY_FIXING = "fixing_alarms"
const val KEY_KITS = "kits_group"
const val KEY_LANGUAGE = "language"
const val KEY_ORDER = "sorting_order"
const val KEY_PERMISSIONS = "give_permissions"
const val TAKEN_ID = "takenId"
const val TYPE = "vector_type"

// ========================================== Collections ==========================================
val SORTING = Sorting.entries.map(Sorting::value)
val THEMES = Themes.entries.map(Themes::value)

// ============================================= Enums =============================================
enum class AlarmType {
    ALL, ALARM, PREALARM
}

enum class DoseTypes(val value: String, @StringRes val title: Int) {
    UNITS("ed", dose_ed),
    PIECES("pcs", dose_pcs),
    SACHETS("sach", dose_sach),
    GRAMS("g", dose_g),
    MILLIGRAMS("mg", dose_mg),
    LITERS("l", dose_l),
    MILLILITERS("ml", dose_ml),
    RATIO("ratio", dose_ratio);

    companion object {
        fun getTitle(value: String?) = entries.find { it.value == value }?.title ?: blank
    }
}

enum class FoodTypes(val value: Int, @StringRes val title: Int) {
    BEFORE(0, intake_text_food_before),
    DURING(1, intake_text_food_during),
    AFTER(2, intake_text_food_after)
}

enum class IntakeExtras(@StringRes val title: Int, @StringRes val description: Int) {
    CANCELLABLE(intake_extra_cancellable, intake_extra_desc_cancellable),
    FULLSCREEN(intake_extra_fullscreen, R.string.intake_extra_desc_fullscreen),
    NO_SOUND(intake_extra_no_sound, R.string.intake_extra_desc_no_sound),
    PREALARM(intake_extra_prealarm, intake_extra_desc_prealarm)
}

enum class Intervals(val days: Int, @StringRes val title: Int) {
    DAILY(1, intake_interval_daily),
    WEEKLY(7, intake_interval_weekly),
    CUSTOM(10, intake_interval_other);

    companion object {
        fun getValue(days: Int) = entries.find { it.days == days } ?: CUSTOM
        fun getTitle(days: String) = try {
            when (days.toInt()) {
                1 -> DAILY.title
                7 -> WEEKLY.title
                else -> CUSTOM.title
            }
        } catch (_: NumberFormatException) {
            blank
        }
    }
}

enum class Menu(val route: Screen, @StringRes val title: Int, @DrawableRes val icon: Int) {
    MEDICINES(Medicines, bottom_bar_medicines, vector_medicine),
    INTAKES(Intakes, bottom_bar_intakes, vector_time),
    SETTINGS(Settings, bottom_bar_settings, vector_settings)
}

enum class Periods(val days: Int, @StringRes val title: Int) {
    PICK(-1, intake_period_pick),
    OTHER(21, intake_period_other),
    INDEFINITE(38500, intake_period_indef);

    companion object {
        fun getValue(days: Int) = entries.find { it.days == days } ?: OTHER
    }
}

enum class SchemaTypes(@StringRes val title: Int, val interval: Intervals) {
    INDEFINITELY(R.string.intake_schema_type_indefinitely, Intervals.DAILY),
    BY_DAYS(R.string.intake_schema_type_day_picker, Intervals.WEEKLY),
    PERSONAL(R.string.intake_schema_type_personal, Intervals.CUSTOM)
}

enum class Sorting(val value: String, @StringRes val title: Int, val type: Comparator<Medicine>) {
    IN_NAME("A-z", sorting_a_z, compareBy { it.nameAlias.ifEmpty(it::productName) }),
    RE_NAME("z-A", sorting_z_a, compareByDescending { it.nameAlias.ifEmpty(it::productName) }),
    IN_DATE("old-new", sorting_from_oldest, compareBy(Medicine::expDate)),
    RE_DATE("new-old", sorting_from_newest, compareByDescending(Medicine::expDate))
}

enum class Themes(val value: String, @StringRes val title: Int) {
    SYSTEM("System", theme_system),
    LIGHT("Light", theme_light),
    DARK("Dark", theme_dark)
}

enum class Types(
    val value: String,
    val ruValue: String,
    @StringRes val title: Int,
    @DrawableRes val icon: Int,
    val doseType: DoseTypes
) {
    TABLETS("vector_type_tablets", "ТАБЛЕТКИ", type_tablets, vector_type_tablets, PIECES),
    CAPSULES("vector_type_capsule","КАПСУЛЫ", type_capsules, vector_type_capsule, PIECES),
    PILLS("vector_type_pills","ПИЛЮЛИ", type_pills, vector_type_pills, PIECES),
    DRAGEE("vector_type_dragee","ДРАЖЕ", type_dragee, vector_type_dragee, PIECES),
    GRANULES("vector_type_granules","ГРАНУЛЫ", type_granules, vector_type_granules, PIECES),
    POWDER("vector_type_powder","ПОРОШОК", type_powder, vector_type_powder, GRAMS),
    SACHETS("vector_type_sachets","ПАКЕТИК", type_sachet, vector_type_sachet, PIECES),
    SOLUTION("vector_type_solution","РАСТВОР", type_solution, vector_type_solution, MILLILITERS),
    TINCTURE("vector_type_tincture","НАСТОЙКА", type_tincture, vector_type_tincture, MILLILITERS),
    DECOCTION("vector_type_decoction","ОТВАР", type_decoction, vector_type_decoction, MILLILITERS),
    EXTRACT("vector_type_extract","ЭКСТРАКТ", type_extract, vector_type_extract, MILLILITERS),
    MIXTURE("vector_type_mixture","МИКСТУРА", type_mixture, vector_type_mixture, MILLILITERS),
    SYRUP("vector_type_syrup","СИРОП", type_syrup, vector_type_syrup, MILLILITERS),
    EMULSION("vector_type_emulsion","ЭМУЛЬСИЯ", type_emulsion, vector_type_emulsion, MILLILITERS),
    SUSPENSION("vector_type_suspension","СУСПЕНЗИЯ", type_suspension, vector_type_suspension, MILLILITERS),
    MIX("vector_type_mix","СМЕСЬ", type_mix, vector_type_mix, MILLIGRAMS),
    OINTMENT("vector_type_ointment","МАЗЬ", type_ointment, vector_type_ointment, GRAMS),
    GEL("vector_type_gel","ГЕЛЬ", type_gel, vector_type_gel, GRAMS),
    PASTE("vector_type_paste","ПАСТА", type_paste, vector_type_paste, GRAMS),
    SUPPOSITORY("vector_type_suppository","СВЕЧИ", type_suppository, vector_type_suppository, PIECES),
    AEROSOL("vector_type_aerosol","АЭРОЗОЛЬ", type_aerosol, vector_type_aerosol, MILLIGRAMS),
    SPRAY("vector_type_nasal_spray","СПРЕЙ", type_spray, vector_type_nasal_spray, MILLILITERS),
    DROPS("vector_type_drops","КАПЛИ", type_drops, vector_type_drops, MILLILITERS),
    PATCH("vector_type_patch","ПЛАСТЫРЬ", type_patch, vector_type_patch, PIECES),
    BANDAGE("vector_type_bandage","БИНТ", type_bandage, vector_type_bandage, PIECES),
    NAPKINS("vector_type_napkins","САЛФЕТКИ", type_napkins, vector_type_napkins, PIECES);

    companion object {
        fun getIcon(value: String) = entries.find { it.value == value }?.icon ?: vector_type_unknown
        fun setIcon(value: String) = entries.find { value.contains(it.ruValue.dropLast(1), true) }?.value ?: BLANK
        fun getDoseType(value: String) = entries.find {
            value.contains(it.ruValue.dropLast(1), true)
        }?.doseType?.value ?: BLANK
    }
}