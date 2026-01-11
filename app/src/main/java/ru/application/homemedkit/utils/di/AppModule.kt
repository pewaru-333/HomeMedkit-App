package ru.application.homemedkit.utils.di

import android.content.Context
import androidx.work.WorkManager
import ru.application.homemedkit.HomeMeds.Companion.app
import ru.application.homemedkit.data.MedicineDatabase
import ru.application.homemedkit.receivers.AlarmSetter
import ru.application.homemedkit.utils.Preferences

interface AppModule {
    val database: MedicineDatabase
    val preferences: Preferences
    val alarmManager: AlarmSetter
    val workManager: WorkManager
}

class AppModuleInitializer(context: Context) : AppModule {
    override val database by lazy { MedicineDatabase.getInstance(context) }

    override val preferences by lazy { Preferences.getInstance(context) }

    override val alarmManager by lazy { AlarmSetter.getInstance(context) }

    override val workManager by lazy { WorkManager.getInstance(context) }
}

val Database: MedicineDatabase get() = app.database

val Preferences: Preferences get() = app.preferences

val AlarmManager: AlarmSetter get() = app.alarmManager

val WorkManager: WorkManager get() = app.workManager