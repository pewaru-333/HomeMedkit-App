package ru.application.homemedkit.helpers

import com.ramcosta.composedestinations.generated.destinations.HomeScreenDestination
import com.ramcosta.composedestinations.generated.destinations.IntakesScreenDestination
import com.ramcosta.composedestinations.generated.destinations.MedicinesScreenDestination
import com.ramcosta.composedestinations.generated.destinations.SettingsScreenDestination
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

// ============================== Strings ==============================
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
val LANGUAGES = listOf("System", "ru", "en-US", "de", "it", "es", "pt-BR", "pl", "tr", "vi", "ko", "zh-TW")
val MENUS = listOf(HomeScreenDestination.route, MedicinesScreenDestination.route,
    IntakesScreenDestination.route, SettingsScreenDestination.route)
val SORTING = listOf("A-z", "z-A", "old-new", "new-old")
val THEMES = listOf("System", "Light", "Dark")

val ICONS_MED: Map<String, Int> = mapOf(
    "vector_type_tablets" to vector_type_tablets,
    "vector_type_capsule" to vector_type_capsule,
    "vector_type_pills" to vector_type_pills,
    "vector_type_dragee" to vector_type_dragee,
    "vector_type_granules" to vector_type_granules,
    "vector_type_powder" to vector_type_powder,
    "vector_type_solution" to vector_type_solution,
    "vector_type_tincture" to vector_type_tincture,
    "vector_type_decoction" to vector_type_decoction,
    "vector_type_extract" to vector_type_extract,
    "vector_type_mixture" to vector_type_mixture,
    "vector_type_syrup" to vector_type_syrup,
    "vector_type_emulsion" to vector_type_emulsion,
    "vector_type_suspension" to vector_type_suspension,
    "vector_type_mix" to vector_type_mix,
    "vector_type_ointment" to vector_type_ointment,
    "vector_type_gel" to vector_type_gel,
    "vector_type_paste" to vector_type_paste,
    "vector_type_suppository" to vector_type_suppository,
    "vector_type_aerosol" to vector_type_aerosol,
    "vector_type_nasal_spray" to vector_type_nasal_spray,
    "vector_type_drops" to vector_type_drops
)