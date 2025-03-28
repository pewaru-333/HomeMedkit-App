package ru.application.homemedkit.helpers.extensions

import android.app.AlarmManager
import android.content.Context
import android.content.Context.*
import android.content.Intent
import android.content.res.XmlResourceParser
import android.media.AudioAttributes
import android.media.RingtoneManager
import android.os.Build
import android.os.Bundle
import android.os.PowerManager
import android.widget.Toast
import androidx.annotation.StringRes
import androidx.core.app.LocaleManagerCompat
import androidx.core.app.NotificationChannelCompat
import androidx.core.app.NotificationManagerCompat
import org.xmlpull.v1.XmlPullParser
import ru.application.homemedkit.R
import java.util.Locale

fun Context.isIgnoringBatteryOptimizations() =
    (getSystemService(POWER_SERVICE) as PowerManager).isIgnoringBatteryOptimizations(packageName)

fun Context.canScheduleExactAlarms() = if (Build.VERSION.SDK_INT < Build.VERSION_CODES.S) true
else (getSystemService(ALARM_SERVICE) as AlarmManager).canScheduleExactAlarms()

fun Context.canUseFullScreenIntent() = NotificationManagerCompat.from(this).canUseFullScreenIntent()

fun Context.getLanguageList() = mutableListOf<String>().apply {
    resources.getXml(R.xml._generated_res_locale_config).use { xml ->
        while (xml.eventType != XmlResourceParser.END_DOCUMENT) {
            if (xml.eventType == XmlPullParser.START_TAG && xml.name == "locale") {
                add(xml.getAttributeValue(0))
            }
            xml.next()
        }
    }
}.sortedBy { Locale.forLanguageTag(it).getDisplayRegionName() }

fun Context.restartApplication(extras: Bundle.() -> Unit = {}) {
    packageManager.getLaunchIntentForPackage(packageName)?.component?.let {
        startActivity(Intent.makeMainActivity(it).putExtras(Bundle().apply(extras)))
    }

    Runtime.getRuntime().exit(0)
}

fun Context.getSelectedLanguage() = LocaleManagerCompat.getSystemLocales(this)
    .getFirstMatch(getLanguageList().toTypedArray())?.language ?: Locale.ENGLISH.language

fun Context.createNotificationChannel(channelId: String, @StringRes channelName: Int) =
    NotificationManagerCompat.from(this).createNotificationChannel(
        NotificationChannelCompat.Builder(channelId, NotificationManagerCompat.IMPORTANCE_MAX)
            .setName(getString(channelName))
            .setDescription(getString(R.string.channel_name))
            .setSound(
                RingtoneManager.getActualDefaultRingtoneUri(this, RingtoneManager.TYPE_NOTIFICATION),
                AudioAttributes.Builder()
                    .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                    .setUsage(AudioAttributes.USAGE_NOTIFICATION)
                    .build()
            )
            .build()
    )

fun Context.showToast(@StringRes message: Int) = Toast.makeText(this, message, Toast.LENGTH_LONG).show()