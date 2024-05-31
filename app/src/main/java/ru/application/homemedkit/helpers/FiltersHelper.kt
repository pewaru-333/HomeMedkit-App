package ru.application.homemedkit.helpers

import android.content.Context
import ru.application.homemedkit.databaseController.Intake
import ru.application.homemedkit.databaseController.Medicine
import ru.application.homemedkit.databaseController.MedicineDatabase
import java.util.Locale

class FiltersHelper(context: Context) {

    private val database: MedicineDatabase = MedicineDatabase.getInstance(context)

    fun medicines(text: String, kitId: Long): List<Medicine> {
        val medicines = if (kitId == 0L) database.medicineDAO().getAll()
        else database.medicineDAO().getByKitId(kitId)
        val filtered = ArrayList<Medicine>(medicines.size)

        if (text.isEmpty()) {
            filtered.addAll(medicines)
        } else {
            medicines.forEach { medicine ->
                val productName = database.medicineDAO().getProductName(medicine.id)
                if (productName.lowercase(Locale.ROOT).contains(text.lowercase(Locale.ROOT))) {
                    filtered.add(medicine)
                }
            }
        }

        return filtered
    }

    fun intakes(text: String): List<Intake> {
        val intakes = database.intakeDAO().getAll()
        val filtered = ArrayList<Intake>(intakes.size)

        if (text.isEmpty()) {
            filtered.addAll(intakes)
        } else {
            intakes.forEach { medicine ->
                val productName = database.medicineDAO().getProductName(medicine.medicineId)
                if (productName.lowercase(Locale.ROOT).contains(text.lowercase(Locale.ROOT))) {
                    filtered.add(medicine)
                }
            }
        }

        return filtered
    }
}