package ru.application.homemedkit.helpers

import ru.application.homemedkit.R

// ============================== Numbers ==============================
const val BOUND = 200
const val EXP_CODE = 81000

// ============================== Strings ==============================
const val ADDING = "adding"
const val ALARM_ID = "alarmId"
const val BLANK = ""
const val CATEGORY = "drugs"
const val CIS = "cis"
const val CHECK_EXP_DATE = "check_exp_date"
const val DUPLICATE = "duplicate"
const val ID = "id"
const val INTAKE_ID = "intakeId"
const val MEDICINE_ID = "medicine_id"
const val NEW_INTAKE = "newIntake"
const val NEW_MEDICINE = "newMedicine"
const val SEMICOLON = ","
const val SETTINGS_CHANGED = "settingsChanged"
const val SOUND_GROUP = "Sound group"
const val WHITESPACE = " "

// ============================== Collections ==============================
val INTERVALS: List<String> = mutableListOf("daily", "weekly", "custom")
val PERIODS: List<String> = mutableListOf("week", "month", "other", "indefinite")
val SNACKS = listOf(
    R.string.text_medicine_duplicate,
    R.string.text_not_medicine_code, R.string.text_code_not_found,
    R.string.text_unknown_error, R.string.text_unknown_error, R.string.text_connection_error
)

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