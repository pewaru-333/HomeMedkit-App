package ru.application.homemedkit.helpers

import ru.application.homemedkit.databaseController.Medicine
import java.util.Comparator.comparing


class SortingHelper(private val sorting: String) {
    companion object {
        val inName: java.util.Comparator<Medicine> = comparing(Medicine::productName)
        val reName: java.util.Comparator<Medicine> = comparing(Medicine::productName).reversed()
        val inDate: java.util.Comparator<Medicine> = comparing(Medicine::expDate)
        val reDate: java.util.Comparator<Medicine> = comparing(Medicine::expDate).reversed()
    }

    fun getSorting(): Comparator<Medicine> = when (sorting) {
        SORTING[0] -> inName
        SORTING[1] -> reName
        SORTING[2] -> inDate
        else -> reDate
    }
}