package ru.application.homemedkit

import android.app.Application
import ru.application.homemedkit.R.string.channel_exp_desc
import ru.application.homemedkit.R.string.channel_intakes_desc
import ru.application.homemedkit.R.string.channel_pre_desc
import ru.application.homemedkit.data.MedicineDatabase
import ru.application.homemedkit.helpers.CHANNEL_ID_EXP
import ru.application.homemedkit.helpers.CHANNEL_ID_INTAKES
import ru.application.homemedkit.helpers.CHANNEL_ID_PRE
import ru.application.homemedkit.helpers.Preferences
import ru.application.homemedkit.helpers.createNotificationChannel

class HomeMeds : Application() {

    companion object {
        lateinit var database: MedicineDatabase
    }

    override fun onCreate() {
        super.onCreate()

        Preferences.getInstance(this)
        database = MedicineDatabase.getInstance(this)
        mapOf(
            CHANNEL_ID_INTAKES to channel_intakes_desc,
            CHANNEL_ID_PRE to channel_pre_desc,
            CHANNEL_ID_EXP to channel_exp_desc
        ).forEach { (t, u) -> createNotificationChannel(this, t, u) }
    }
}