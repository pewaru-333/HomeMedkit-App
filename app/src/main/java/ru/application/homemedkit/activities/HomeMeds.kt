package ru.application.homemedkit.activities

import android.app.Application
import ru.application.homemedkit.databaseController.MedicineDatabase
import ru.application.homemedkit.helpers.Preferences

class HomeMeds : Application() {

    companion object {
        lateinit var database: MedicineDatabase
    }

    override fun onCreate() {
        super.onCreate()

        Preferences.getInstance(this)
        database = MedicineDatabase.getInstance(this)
    }
}