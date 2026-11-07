package ru.application.homemedkit.utils.enums

import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import ru.application.homemedkit.R


enum class DrugType(
    val value: String,
    val ruValue: String,
    // Italian word stems, as used in AIFA pharmaceutical forms (e.g. "Compressa rivestita")
    val itValues: List<String>,
    @StringRes val title: Int,
    @DrawableRes val icon: Int,
    val doseType: DoseType
) {
    TABLETS(
        value = "vector_type_tablets",
        ruValue = "ТАБЛЕТКИ",
        itValues = listOf("COMPRESS", "PASTIGLI"),
        title = R.string.type_tablets,
        icon = R.drawable.vector_type_tablets,
        doseType = DoseType.PIECES
    ),
    CAPSULES(
        value = "vector_type_capsule",
        ruValue = "КАПСУЛЫ",
        itValues = listOf("CAPSUL"),
        title = R.string.type_capsules,
        icon = R.drawable.vector_type_capsule,
        doseType = DoseType.PIECES
    ),
    PILLS(
        value = "vector_type_pills",
        ruValue = "ПИЛЮЛИ",
        itValues = listOf("PILLOL"),
        title = R.string.type_pills,
        icon = R.drawable.vector_type_pills,
        doseType = DoseType.PIECES
    ),
    DRAGEE(
        value = "vector_type_dragee",
        ruValue = "ДРАЖЕ",
        itValues = listOf("CONFETT"),
        title = R.string.type_dragee,
        icon = R.drawable.vector_type_dragee,
        doseType = DoseType.PIECES
    ),
    GRANULES(
        value = "vector_type_granules",
        ruValue = "ГРАНУЛЫ",
        itValues = listOf("GRANUL"),
        title = R.string.type_granules,
        icon = R.drawable.vector_type_granules,
        doseType = DoseType.PIECES
    ),
    POWDER(
        value = "vector_type_powder",
        ruValue = "ПОРОШОК",
        itValues = listOf("POLVERE"),
        title = R.string.type_powder,
        icon = R.drawable.vector_type_powder,
        doseType = DoseType.GRAMS
    ),
    SACHETS(
        value = "vector_type_sachets",
        ruValue = "ПАКЕТИК",
        itValues = listOf("BUSTIN"),
        title = R.string.type_sachet,
        icon = R.drawable.vector_type_sachet,
        doseType = DoseType.PIECES
    ),
    SOLUTION(
        value = "vector_type_solution",
        ruValue = "РАСТВОР",
        itValues = listOf("SOLUZION"),
        title = R.string.type_solution,
        icon = R.drawable.vector_type_solution,
        doseType = DoseType.MILLILITERS
    ),
    TINCTURE(
        value = "vector_type_tincture",
        ruValue = "НАСТОЙКА",
        itValues = listOf("TINTUR"),
        title = R.string.type_tincture,
        icon = R.drawable.vector_type_tincture,
        doseType = DoseType.MILLILITERS
    ),
    DECOCTION(
        value = "vector_type_decoction",
        ruValue = "ОТВАР",
        itValues = listOf("DECOTT"),
        title = R.string.type_decoction,
        icon = R.drawable.vector_type_decoction,
        doseType = DoseType.MILLILITERS
    ),
    EXTRACT(
        value = "vector_type_extract",
        ruValue = "ЭКСТРАКТ",
        itValues = listOf("ESTRATT"),
        title = R.string.type_extract,
        icon = R.drawable.vector_type_extract,
        doseType = DoseType.MILLILITERS
    ),
    MIXTURE(
        value = "vector_type_mixture",
        ruValue = "МИКСТУРА",
        itValues = emptyList(),
        title = R.string.type_mixture,
        icon = R.drawable.vector_type_mixture,
        doseType = DoseType.MILLILITERS
    ),
    SYRUP(
        value = "vector_type_syrup",
        ruValue = "СИРОП",
        itValues = listOf("SCIROPP"),
        title = R.string.type_syrup,
        icon = R.drawable.vector_type_syrup,
        doseType = DoseType.MILLILITERS
    ),
    EMULSION(
        value = "vector_type_emulsion",
        ruValue = "ЭМУЛЬСИЯ",
        itValues = listOf("EMULSION"),
        title = R.string.type_emulsion,
        icon = R.drawable.vector_type_emulsion,
        doseType = DoseType.MILLILITERS
    ),
    SUSPENSION(
        value = "vector_type_suspension",
        ruValue = "СУСПЕНЗИЯ",
        itValues = listOf("SOSPENSION"),
        title = R.string.type_suspension,
        icon = R.drawable.vector_type_suspension,
        doseType = DoseType.MILLILITERS
    ),
    MIX(
        value = "vector_type_mix",
        ruValue = "СМЕСЬ",
        itValues = emptyList(),
        title = R.string.type_mix,
        icon = R.drawable.vector_type_mix,
        doseType = DoseType.MILLIGRAMS
    ),
    OINTMENT(
        value = "vector_type_ointment",
        ruValue = "МАЗЬ",
        itValues = listOf("UNGUENT", "CREMA"),
        title = R.string.type_ointment,
        icon = R.drawable.vector_type_ointment,
        doseType = DoseType.GRAMS
    ),
    GEL(
        value = "vector_type_gel",
        ruValue = "ГЕЛЬ",
        itValues = listOf("GEL"),
        title = R.string.type_gel,
        icon = R.drawable.vector_type_gel,
        doseType = DoseType.GRAMS
    ),
    PASTE(
        value = "vector_type_paste",
        ruValue = "ПАСТА",
        itValues = listOf("PASTA"),
        title = R.string.type_paste,
        icon = R.drawable.vector_type_paste,
        doseType = DoseType.GRAMS
    ),
    SUPPOSITORY(
        value = "vector_type_suppository",
        ruValue = "СВЕЧИ",
        itValues = listOf("SUPPOST"),
        title = R.string.type_suppository,
        icon = R.drawable.vector_type_suppository,
        doseType = DoseType.PIECES
    ),
    AEROSOL(
        value = "vector_type_aerosol",
        ruValue = "АЭРОЗОЛЬ",
        itValues = listOf("AEROSOL"),
        title = R.string.type_aerosol,
        icon = R.drawable.vector_type_aerosol,
        doseType = DoseType.MILLIGRAMS
    ),
    SPRAY(
        value = "vector_type_nasal_spray",
        ruValue = "СПРЕЙ",
        itValues = listOf("SPRAY"),
        title = R.string.type_spray,
        icon = R.drawable.vector_type_nasal_spray,
        doseType = DoseType.MILLILITERS
    ),
    DROPS(
        value = "vector_type_drops",
        ruValue = "КАПЛИ",
        itValues = listOf("GOCCE"),
        title = R.string.type_drops,
        icon = R.drawable.vector_type_drops,
        doseType = DoseType.MILLILITERS
    ),
    PATCH(
        value = "vector_type_patch",
        ruValue = "ПЛАСТЫРЬ",
        itValues = listOf("CEROTT"),
        title = R.string.type_patch,
        icon = R.drawable.vector_type_patch,
        doseType = DoseType.PIECES
    ),
    BANDAGE(
        value = "vector_type_bandage",
        ruValue = "БИНТ",
        itValues = listOf("BEND"),
        title = R.string.type_bandage,
        icon = R.drawable.vector_type_bandage,
        doseType = DoseType.PIECES
    ),
    NAPKINS(
        value = "vector_type_napkins",
        ruValue = "САЛФЕТКИ",
        itValues = listOf("SALVIETT"),
        title = R.string.type_napkins,
        icon = R.drawable.vector_type_napkins,
        doseType = DoseType.PIECES
    );

    companion object {
        val iconMap = entries.associate { it.value to it.icon }

        fun setIcon(value: String) = fromForm(value)?.value.orEmpty()

        fun getDoseType(value: String) = fromForm(value)?.doseType ?: DoseType.UNKNOWN

        private fun fromForm(value: String) = entries.find { type ->
            value.contains(type.ruValue.dropLast(1), true) || type.itValues.any { value.contains(it, true) }
        }
    }
}