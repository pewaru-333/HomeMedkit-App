package ru.application.homemedkit.receivers

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.Intent.ACTION_LOCALE_CHANGED
import ru.application.homemedkit.R.string.channel_exp_desc
import ru.application.homemedkit.R.string.channel_intakes_desc
import ru.application.homemedkit.R.string.channel_pre_desc
import ru.application.homemedkit.helpers.CHANNEL_ID_EXP
import ru.application.homemedkit.helpers.CHANNEL_ID_INTAKES
import ru.application.homemedkit.helpers.CHANNEL_ID_PRE
import ru.application.homemedkit.helpers.extensions.createNotificationChannel

class LocaleReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == ACTION_LOCALE_CHANGED) mapOf(
            CHANNEL_ID_INTAKES to channel_intakes_desc,
            CHANNEL_ID_PRE to channel_pre_desc,
            CHANNEL_ID_EXP to channel_exp_desc
        ).forEach { (id, name) -> context.createNotificationChannel(id, name) }
    }
}