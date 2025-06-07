package ru.application.homemedkit.utils.enums

import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import ru.application.homemedkit.R
import ru.application.homemedkit.utils.BLANK


enum class DrugType(
    val value: String,
    val ruValue: String,
    @StringRes val title: Int,
    @DrawableRes val icon: Int,
    val doseType: DoseType
) {
    TABLETS(
        value = "vector_type_tablets",
        ruValue = "ТАБЛЕТКИ",
        title = R.string.type_tablets,
        icon = R.drawable.vector_type_tablets,
        doseType = DoseType.PIECES
    ),
    CAPSULES(
        value = "vector_type_capsule",
        ruValue = "КАПСУЛЫ",
        title = R.string.type_capsules,
        icon = R.drawable.vector_type_capsule,
        doseType = DoseType.PIECES
    ),
    PILLS(
        value = "vector_type_pills",
        ruValue = "ПИЛЮЛИ",
        title = R.string.type_pills,
        icon = R.drawable.vector_type_pills,
        doseType = DoseType.PIECES
    ),
    DRAGEE(
        value = "vector_type_dragee",
        ruValue = "ДРАЖЕ",
        title = R.string.type_dragee,
        icon = R.drawable.vector_type_dragee,
        doseType = DoseType.PIECES
    ),
    GRANULES(
        value = "vector_type_granules",
        ruValue = "ГРАНУЛЫ",
        title = R.string.type_granules,
        icon = R.drawable.vector_type_granules,
        doseType = DoseType.PIECES
    ),
    POWDER(
        value = "vector_type_powder",
        ruValue = "ПОРОШОК",
        title = R.string.type_powder,
        icon = R.drawable.vector_type_powder,
        doseType = DoseType.GRAMS
    ),
    SACHETS(
        value = "vector_type_sachets",
        ruValue = "ПАКЕТИК",
        title = R.string.type_sachet,
        icon = R.drawable.vector_type_sachet,
        doseType = DoseType.PIECES
    ),
    SOLUTION(
        value = "vector_type_solution",
        ruValue = "РАСТВОР",
        title = R.string.type_solution,
        icon = R.drawable.vector_type_solution,
        doseType = DoseType.MILLILITERS
    ),
    TINCTURE(
        value = "vector_type_tincture",
        ruValue = "НАСТОЙКА",
        title = R.string.type_tincture,
        icon = R.drawable.vector_type_tincture,
        doseType = DoseType.MILLILITERS
    ),
    DECOCTION(
        value = "vector_type_decoction",
        ruValue = "ОТВАР",
        title = R.string.type_decoction,
        icon = R.drawable.vector_type_decoction,
        doseType = DoseType.MILLILITERS
    ),
    EXTRACT(
        value = "vector_type_extract",
        ruValue = "ЭКСТРАКТ",
        title = R.string.type_extract,
        icon = R.drawable.vector_type_extract,
        doseType = DoseType.MILLILITERS
    ),
    MIXTURE(
        value = "vector_type_mixture",
        ruValue = "МИКСТУРА",
        title = R.string.type_mixture,
        icon = R.drawable.vector_type_mixture,
        doseType = DoseType.MILLILITERS
    ),
    SYRUP(
        value = "vector_type_syrup",
        ruValue = "СИРОП",
        title = R.string.type_syrup,
        icon = R.drawable.vector_type_syrup,
        doseType = DoseType.MILLILITERS
    ),
    EMULSION(
        value = "vector_type_emulsion",
        ruValue = "ЭМУЛЬСИЯ",
        title = R.string.type_emulsion,
        icon = R.drawable.vector_type_emulsion,
        doseType = DoseType.MILLILITERS
    ),
    SUSPENSION(
        value = "vector_type_suspension",
        ruValue = "СУСПЕНЗИЯ",
        title = R.string.type_suspension,
        icon = R.drawable.vector_type_suspension,
        doseType = DoseType.MILLILITERS
    ),
    MIX(
        value = "vector_type_mix",
        ruValue = "СМЕСЬ",
        title = R.string.type_mix,
        icon = R.drawable.vector_type_mix,
        doseType = DoseType.MILLIGRAMS
    ),
    OINTMENT(
        value = "vector_type_ointment",
        ruValue = "МАЗЬ",
        title = R.string.type_ointment,
        icon = R.drawable.vector_type_ointment,
        doseType = DoseType.GRAMS
    ),
    GEL(
        value = "vector_type_gel",
        ruValue = "ГЕЛЬ",
        title = R.string.type_gel,
        icon = R.drawable.vector_type_gel,
        doseType = DoseType.GRAMS
    ),
    PASTE(
        value = "vector_type_paste",
        ruValue = "ПАСТА",
        title = R.string.type_paste,
        icon = R.drawable.vector_type_paste,
        doseType = DoseType.GRAMS
    ),
    SUPPOSITORY(
        value = "vector_type_suppository",
        ruValue = "СВЕЧИ",
        title = R.string.type_suppository,
        icon = R.drawable.vector_type_suppository,
        doseType = DoseType.PIECES
    ),
    AEROSOL(
        value = "vector_type_aerosol",
        ruValue = "АЭРОЗОЛЬ",
        title = R.string.type_aerosol,
        icon = R.drawable.vector_type_aerosol,
        doseType = DoseType.MILLIGRAMS
    ),
    SPRAY(
        value = "vector_type_nasal_spray",
        ruValue = "СПРЕЙ",
        title = R.string.type_spray,
        icon = R.drawable.vector_type_nasal_spray,
        doseType = DoseType.MILLILITERS
    ),
    DROPS(
        value = "vector_type_drops",
        ruValue = "КАПЛИ",
        title = R.string.type_drops,
        icon = R.drawable.vector_type_drops,
        doseType = DoseType.MILLILITERS
    ),
    PATCH(
        value = "vector_type_patch",
        ruValue = "ПЛАСТЫРЬ",
        title = R.string.type_patch,
        icon = R.drawable.vector_type_patch,
        doseType = DoseType.PIECES
    ),
    BANDAGE(
        value = "vector_type_bandage",
        ruValue = "БИНТ",
        title = R.string.type_bandage,
        icon = R.drawable.vector_type_bandage,
        doseType = DoseType.PIECES
    ),
    NAPKINS(
        value = "vector_type_napkins",
        ruValue = "САЛФЕТКИ",
        title = R.string.type_napkins,
        icon = R.drawable.vector_type_napkins,
        doseType = DoseType.PIECES
    );

    companion object {
        fun getIcon(value: String) = entries.find { it.value == value }?.icon

        fun setIcon(value: String) =
            entries.find { value.contains(it.ruValue.dropLast(1), true) }?.value ?: BLANK

        fun getDoseType(value: String) = entries.find {
            value.contains(it.ruValue.dropLast(1), true)
        }?.doseType ?: DoseType.UNKNOWN
    }
}