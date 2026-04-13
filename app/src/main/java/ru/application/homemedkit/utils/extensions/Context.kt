package ru.application.homemedkit.utils.extensions

import android.app.AlarmManager
import android.content.ComponentName
import android.content.Context
import android.content.Context.*
import android.content.Intent
import android.content.res.XmlResourceParser
import android.media.AudioAttributes
import android.media.RingtoneManager
import android.os.Build
import android.os.Bundle
import android.os.PowerManager
import android.os.VibrationEffect
import android.os.Vibrator
import android.widget.Toast
import androidx.annotation.StringRes
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
}.sortedBy { Locale.forLanguageTag(it).getLocalizedName() }

fun Context.restartApplication(extras: Bundle.() -> Unit = {}) {
    packageManager.getLaunchIntentForPackage(packageName)?.component?.let {
        startActivity(Intent.makeMainActivity(it).putExtras(Bundle().apply(extras)))
    }

    Runtime.getRuntime().exit(0)
}

fun Context.openAutoStartSettings() : Boolean {
    val manufacturer = Build.MANUFACTURER.lowercase()

    val components = when {
        "xiaomi" in manufacturer || "poco" in manufacturer || "redmi" in manufacturer -> listOf(
            ComponentName(
                "com.miui.securitycenter",
                "com.miui.permcenter.autostart.AutoStartManagementActivity"
            ),
            ComponentName("com.xiaomi.mipicks", "com.xiaomi.mipicks.ui.AppPicksTabActivity")
        )
        "huawei" in manufacturer || "honor" in manufacturer -> listOf(
            ComponentName(
                "com.huawei.systemmanager",
                "com.huawei.systemmanager.startupmgr.ui.StartupNormalAppListActivity"
            ),
            ComponentName(
                "com.huawei.systemmanager",
                "com.huawei.systemmanager.optimize.process.ProtectActivity"
            )
        )
        "oppo" in manufacturer -> listOf(
            ComponentName(
                "com.coloros.safecenter",
                "com.coloros.safecenter.permission.startup.FakeActivity"
            ),
            ComponentName(
                "com.oppo.safe",
                "com.oppo.safe.permission.startup.StartupAppListActivity"
            )
        )
        "vivo" in manufacturer || "iqoo" in manufacturer -> listOf(
            ComponentName(
                "com.iqoo.secure",
                "com.iqoo.secure.ui.phoneoptimize.AddWhiteListActivity"
            ),
            ComponentName(
                "com.vivo.permissionmanager",
                "com.vivo.permissionmanager.activity.BgStartUpManagerActivity"
            )
        )
        "samsung" in manufacturer -> listOf(
            ComponentName(
                "com.samsung.android.lool",
                "com.samsung.android.sm.ui.battery.BatteryActivity"
            )
        )
        "oneplus" in manufacturer -> listOf(
            ComponentName(
                "com.oneplus.security",
                "com.oneplus.security.chainlaunch.view.ChainLaunchAppListActivity"
            )
        )
        "meizu" in manufacturer -> listOf(
            ComponentName("com.meizu.safe", "com.meizu.safe.security.SHOW_APPSEC")
        )

        else -> emptyList()
    }

    for (component in components) {
        try {
            val intent = Intent().apply {
                this.component = component
                flags = Intent.FLAG_ACTIVITY_NEW_TASK

                if (component.packageName == "com.meizu.safe") {
                    addCategory(Intent.CATEGORY_DEFAULT)
                    putExtra("packageName", packageName)
                }
            }

            if (intent.resolveActivity(packageManager) != null) {
                startActivity(intent)
                return true
            }
        } catch (_: Exception) {
        }
    }

    return false
}

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

fun Context.vibrate(time: Long) = getSystemService(Vibrator::class.java)
    .vibrate(VibrationEffect.createOneShot(time, VibrationEffect.DEFAULT_AMPLITUDE))

fun Context.showToast(@StringRes message: Int) = Toast.makeText(this, message, Toast.LENGTH_LONG).show()