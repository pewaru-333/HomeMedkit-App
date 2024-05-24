package ru.application.homemedkit.helpers

import com.ramcosta.composedestinations.generated.destinations.HomeScreenDestination
import com.ramcosta.composedestinations.generated.destinations.IntakesScreenDestination
import com.ramcosta.composedestinations.generated.destinations.MedicinesScreenDestination
import com.ramcosta.composedestinations.generated.destinations.SettingsScreenDestination
import ru.application.homemedkit.R

// ============================== Strings ==============================
const val ALARM_ID = "alarmId"
const val BLANK = ""
const val CATEGORY = "drugs"
const val CIS = "cis"
const val CHANNEL_ID = "intake_notifications"
const val CHECK_EXP_DATE = "check_exp_date"
const val CODE_EXPORT = 2020
const val ID = "id"
const val KEY_APP_THEME = "app_theme"
const val KEY_APP_SYSTEM = "app_system"
const val KEY_APP_VIEW = "app_view"
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
const val TYPE = "vector_type"

// ============================== Collections ==============================
val LANGUAGES = listOf("System", "ru", "en-US", "es", "tr")
val MENUS = listOf(HomeScreenDestination.route, MedicinesScreenDestination.route,
    IntakesScreenDestination.route, SettingsScreenDestination.route)
val SNACKS = listOf(
    R.string.text_medicine_duplicate,
    R.string.text_not_medicine_code, R.string.text_code_not_found,
    R.string.text_unknown_error, R.string.text_unknown_error, R.string.text_connection_error
)
val SORTING = listOf("A-z", "z-A", "old-new", "new-old")
val THEMES = listOf("System", "Light", "Dark")

val ICONS_MED: Map<String, Int> = mapOf(
    "vector_type_tablets" to R.drawable.vector_type_tablets,
    "vector_type_capsule" to R.drawable.vector_type_capsule,
    "vector_type_pills" to R.drawable.vector_type_pills,
    "vector_type_dragee" to R.drawable.vector_type_dragee,
    "vector_type_granules" to R.drawable.vector_type_granules,
    "vector_type_powder" to R.drawable.vector_type_powder,
    "vector_type_solution" to R.drawable.vector_type_solution,
    "vector_type_tincture" to R.drawable.vector_type_tincture,
    "vector_type_decoction" to R.drawable.vector_type_decoction,
    "vector_type_extract" to R.drawable.vector_type_extract,
    "vector_type_mixture" to R.drawable.vector_type_mixture,
    "vector_type_syrup" to R.drawable.vector_type_syrup,
    "vector_type_emulsion" to R.drawable.vector_type_emulsion,
    "vector_type_suspension" to R.drawable.vector_type_suspension,
    "vector_type_mix" to R.drawable.vector_type_mix,
    "vector_type_ointment" to R.drawable.vector_type_ointment,
    "vector_type_gel" to R.drawable.vector_type_gel,
    "vector_type_paste" to R.drawable.vector_type_paste,
    "vector_type_suppository" to R.drawable.vector_type_suppository,
    "vector_type_aerosol" to R.drawable.vector_type_aerosol,
    "vector_type_nasal_spray" to R.drawable.vector_type_nasal_spray,
    "vector_type_drops" to R.drawable.vector_type_drops
)