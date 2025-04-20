package ru.application.homemedkit.helpers.enums

import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import ru.application.homemedkit.R
import ru.application.homemedkit.helpers.BLANK


enum class Types(
    val value: String,
    val ruValue: String,
    @StringRes val title: Int,
    @DrawableRes val icon: Int,
    val doseType: DoseTypes
) {
    TABLETS(
        value = "vector_type_tablets",
        ruValue = "ТАБЛЕТКИ",
        title = R.string.type_tablets,
        icon = R.drawable.vector_type_tablets,
        doseType = DoseTypes.PIECES
    ),
    CAPSULES(
        value = "vector_type_capsule",
        ruValue = "КАПСУЛЫ",
        title = R.string.type_capsules,
        icon = R.drawable.vector_type_capsule,
        doseType = DoseTypes.PIECES
    ),
    PILLS(
        value = "vector_type_pills",
        ruValue = "ПИЛЮЛИ",
        title = R.string.type_pills,
        icon = R.drawable.vector_type_pills,
        doseType = DoseTypes.PIECES
    ),
    DRAGEE(
        value = "vector_type_dragee",
        ruValue = "ДРАЖЕ",
        title = R.string.type_dragee,
        icon = R.drawable.vector_type_dragee,
        doseType = DoseTypes.PIECES
    ),
    GRANULES(
        value = "vector_type_granules",
        ruValue = "ГРАНУЛЫ",
        title = R.string.type_granules,
        icon = R.drawable.vector_type_granules,
        doseType = DoseTypes.PIECES
    ),
    POWDER(
        value = "vector_type_powder",
        ruValue = "ПОРОШОК",
        title = R.string.type_powder,
        icon = R.drawable.vector_type_powder,
        doseType = DoseTypes.GRAMS
    ),
    SACHETS(
        value = "vector_type_sachets",
        ruValue = "ПАКЕТИК",
        title = R.string.type_sachet,
        icon = R.drawable.vector_type_sachet,
        doseType = DoseTypes.PIECES
    ),
    SOLUTION(
        value = "vector_type_solution",
        ruValue = "РАСТВОР",
        title = R.string.type_solution,
        icon = R.drawable.vector_type_solution,
        doseType = DoseTypes.MILLILITERS
    ),
    TINCTURE(
        value = "vector_type_tincture",
        ruValue = "НАСТОЙКА",
        title = R.string.type_tincture,
        icon = R.drawable.vector_type_tincture,
        doseType = DoseTypes.MILLILITERS
    ),
    DECOCTION(
        value = "vector_type_decoction",
        ruValue = "ОТВАР",
        title = R.string.type_decoction,
        icon = R.drawable.vector_type_decoction,
        doseType = DoseTypes.MILLILITERS
    ),
    EXTRACT(
        value = "vector_type_extract",
        ruValue = "ЭКСТРАКТ",
        title = R.string.type_extract,
        icon = R.drawable.vector_type_extract,
        doseType = DoseTypes.MILLILITERS
    ),
    MIXTURE(
        value = "vector_type_mixture",
        ruValue = "МИКСТУРА",
        title = R.string.type_mixture,
        icon = R.drawable.vector_type_mixture,
        doseType = DoseTypes.MILLILITERS
    ),
    SYRUP(
        value = "vector_type_syrup",
        ruValue = "СИРОП",
        title = R.string.type_syrup,
        icon = R.drawable.vector_type_syrup,
        doseType = DoseTypes.MILLILITERS
    ),
    EMULSION(
        value = "vector_type_emulsion",
        ruValue = "ЭМУЛЬСИЯ",
        title = R.string.type_emulsion,
        icon = R.drawable.vector_type_emulsion,
        doseType = DoseTypes.MILLILITERS
    ),
    SUSPENSION(
        value = "vector_type_suspension",
        ruValue = "СУСПЕНЗИЯ",
        title = R.string.type_suspension,
        icon = R.drawable.vector_type_suspension,
        doseType = DoseTypes.MILLILITERS
    ),
    MIX(
        value = "vector_type_mix",
        ruValue = "СМЕСЬ",
        title = R.string.type_mix,
        icon = R.drawable.vector_type_mix,
        doseType = DoseTypes.MILLIGRAMS
    ),
    OINTMENT(
        value = "vector_type_ointment",
        ruValue = "МАЗЬ",
        title = R.string.type_ointment,
        icon = R.drawable.vector_type_ointment,
        doseType = DoseTypes.GRAMS
    ),
    GEL(
        value = "vector_type_gel",
        ruValue = "ГЕЛЬ",
        title = R.string.type_gel,
        icon = R.drawable.vector_type_gel,
        doseType = DoseTypes.GRAMS
    ),
    PASTE(
        value = "vector_type_paste",
        ruValue = "ПАСТА",
        title = R.string.type_paste,
        icon = R.drawable.vector_type_paste,
        doseType = DoseTypes.GRAMS
    ),
    SUPPOSITORY(
        value = "vector_type_suppository",
        ruValue = "СВЕЧИ",
        title = R.string.type_suppository,
        icon = R.drawable.vector_type_suppository,
        doseType = DoseTypes.PIECES
    ),
    AEROSOL(
        value = "vector_type_aerosol",
        ruValue = "АЭРОЗОЛЬ",
        title = R.string.type_aerosol,
        icon = R.drawable.vector_type_aerosol,
        doseType = DoseTypes.MILLIGRAMS
    ),
    SPRAY(
        value = "vector_type_nasal_spray",
        ruValue = "СПРЕЙ",
        title = R.string.type_spray,
        icon = R.drawable.vector_type_nasal_spray,
        doseType = DoseTypes.MILLILITERS
    ),
    DROPS(
        value = "vector_type_drops",
        ruValue = "КАПЛИ",
        title = R.string.type_drops,
        icon = R.drawable.vector_type_drops,
        doseType = DoseTypes.MILLILITERS
    ),
    PATCH(
        value = "vector_type_patch",
        ruValue = "ПЛАСТЫРЬ",
        title = R.string.type_patch,
        icon = R.drawable.vector_type_patch,
        doseType = DoseTypes.PIECES
    ),
    BANDAGE(
        value = "vector_type_bandage",
        ruValue = "БИНТ",
        title = R.string.type_bandage,
        icon = R.drawable.vector_type_bandage,
        doseType = DoseTypes.PIECES
    ),
    NAPKINS(
        value = "vector_type_napkins",
        ruValue = "САЛФЕТКИ",
        title = R.string.type_napkins,
        icon = R.drawable.vector_type_napkins,
        doseType = DoseTypes.PIECES
    );

    companion object {
        fun getIcon(value: String) = entries.find { it.value == value }?.icon

        fun setIcon(value: String) =
            entries.find { value.contains(it.ruValue.dropLast(1), true) }?.value ?: BLANK

        fun getDoseType(value: String) = entries.find {
            value.contains(it.ruValue.dropLast(1), true)
        }?.doseType ?: DoseTypes.UNKNOWN
    }
}