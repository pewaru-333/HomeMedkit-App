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
                SELECT id, productName, nameAlias, prodAmount, doseType, expDate, prodFormNormName,
                CASE WHEN nameAlias = '' THEN productName ELSE nameAlias END AS sortedName 
                FROM medicines
                WHERE 1=1
                """.trimIndent()
        )

        if (search.isNotBlank()) {
            basicQuery.append(" AND id IN (SELECT rowid FROM medicines_fts WHERE medicines_fts MATCH ?)")
            args.add("$search*")
        }

        if (kits.isNotEmpty()) {
            val kitIds = kits.map(Kit::kitId)
            val kitsArgs = kitIds.joinToString(",") { "?" }

            basicQuery.append(" AND id IN (SELECT medicineId FROM medicines_kits WHERE kitId IN ($kitsArgs))")
            args.addAll(kitIds)
        }

        val orderClause = when (order) {
            Sorting.IN_NAME -> "sortedName COLLATE NOCASE ASC"
            Sorting.RE_NAME -> "sortedName COLLATE NOCASE DESC"
            Sorting.IN_DATE -> "expDate ASC"
            Sorting.RE_DATE -> "expDate DESC"
        }

        basicQuery.append(" ORDER BY $orderClause")

        return SimpleSQLiteQuery(basicQuery.toString(), args.toTypedArray())
    }

    val selectAll = selectBy(BLANK, Sorting.IN_NAME, emptySet())
}