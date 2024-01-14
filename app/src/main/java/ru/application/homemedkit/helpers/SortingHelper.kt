package ru.application.homemedkit.helpers

import android.app.Activity
import ru.application.homemedkit.R
import ru.application.homemedkit.databaseController.Medicine
import java.util.Comparator.comparing


class SortingHelper(private val activity: Activity) {
    companion object {
        val inName: java.util.Comparator<Medicine> = comparing(Medicine::productName)
        val reName: java.util.Comparator<Medicine> = comparing(Medicine::productName).reversed()
        val inDate: java.util.Comparator<Medicine> = comparing(Medicine::expDate)
        val reDate: java.util.Comparator<Medicine> = comparing(Medicine::expDate).reversed()
    }

    fun getSorting(): Comparator<Medicine> {
        val preferences = SettingsHelper(activity)
        val types = activity.resources.getStringArray(R.array.sorting_types)
        val type = preferences.sortingOrder

        return when {
            type.equals(types[0]) -> inName
            type.equals(types[1]) -> reName
            type.equals(types[2]) -> inDate
            else -> reDate
        }
    }
}