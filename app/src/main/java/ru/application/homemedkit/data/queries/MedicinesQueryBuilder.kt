package ru.application.homemedkit.data.queries

import androidx.sqlite.db.SimpleSQLiteQuery
import ru.application.homemedkit.data.dto.Kit
import ru.application.homemedkit.utils.BLANK
import ru.application.homemedkit.utils.enums.Sorting

object MedicinesQueryBuilder {
    fun selectBy(search: String, order: Sorting, kits: Set<Kit>): SimpleSQLiteQuery {
        val args = mutableListOf<Any>()
        val basicQuery = StringBuilder(
            """
                SELECT m.id, m.productName, m.nameAlias, m.prodAmount, m.doseType, m.expDate, m.prodFormNormName,
                (SELECT image FROM images WHERE medicineId = m.id ORDER BY position ASC LIMIT 1) as image,
                (SELECT GROUP_CONCAT(kitId) FROM medicines_kits WHERE medicineId = m.id) as kitIdsString
                FROM medicines m
                WHERE 1=1
                """.trimIndent()
        )

        if (search.isNotBlank()) {
            basicQuery.append(" AND m.id IN (SELECT rowid FROM medicines_fts WHERE medicines_fts MATCH ?)")
            args.add("$search*")
        }

        if (kits.isNotEmpty()) {
            val kitIds = kits.map(Kit::kitId)
            val placeholders = kitIds.joinToString(",") { "?" }

            basicQuery.append(" AND m.id IN (SELECT medicineId FROM medicines_kits WHERE kitId IN ($placeholders))")
            args.addAll(kitIds)
        }

        val orderClause = when (order) {
            Sorting.IN_NAME -> "COALESCE(NULLIF(m.nameAlias, ''), m.productName) COLLATE NOCASE ASC"
            Sorting.RE_NAME -> "COALESCE(NULLIF(m.nameAlias, ''), m.productName) COLLATE NOCASE DESC"
            Sorting.IN_DATE -> "m.expDate ASC"
            Sorting.RE_DATE -> "m.expDate DESC"
        }

        basicQuery.append(" ORDER BY $orderClause")

        return SimpleSQLiteQuery(basicQuery.toString(), args.toTypedArray())
    }

    val selectAll = selectBy(BLANK, Sorting.IN_NAME, emptySet())
}