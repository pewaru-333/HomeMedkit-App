package ru.application.homemedkit.receivers

import android.app.PendingIntent.FLAG_IMMUTABLE
import android.app.PendingIntent.FLAG_UPDATE_CURRENT
import android.app.PendingIntent.getActivity
import android.app.PendingIntent.getBroadcast
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationCompat.BigTextStyle
import androidx.core.app.NotificationCompat.Builder
import androidx.core.app.NotificationCompat.CATEGORY_REMINDER
import androidx.core.app.NotificationCompat.VISIBILITY_PUBLIC
import androidx.core.app.NotificationManagerCompat
import ru.application.homemedkit.IntakeDialogActivity
import ru.application.homemedkit.R.drawable.ic_launcher_foreground
import ru.application.homemedkit.R.drawable.vector_time
import ru.application.homemedkit.R.string.intake_text_not_taken
import ru.application.homemedkit.R.string.intake_text_taken
import ru.application.homemedkit.R.string.text_do_intake
import ru.application.homemedkit.R.string.text_intake_amount_not_enough
import ru.application.homemedkit.R.string.text_intake_time
import ru.application.homemedkit.data.MedicineDatabase.Companion.getInstance
import ru.application.homemedkit.helpers.ALARM_ID
import ru.application.homemedkit.helpers.AlarmType
import ru.application.homemedkit.helpers.BLANK
import ru.application.homemedkit.helpers.CHANNEL_ID_INTAKES
import ru.application.homemedkit.helpers.DoseTypes
import ru.application.homemedkit.helpers.ID
import ru.application.homemedkit.helpers.TAKEN_ID
import ru.application.homemedkit.helpers.TYPE
import ru.application.homemedkit.helpers.decimalFormat
import ru.application.homemedkit.helpers.safeNotify

class AlarmReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val database = getInstance(context)

        val alarmId = intent.getLongExtra(ALARM_ID, 0L)

        val alarm = database.alarmDAO().getById(alarmId) ?: return
        val intake = database.intakeDAO().getById(alarm.intakeId) ?: return
        val medicine = database.medicineDAO().getById(intake.medicineId) ?: return
        val taken = database.takenDAO().getByAlarmId(alarmId) ?: return

        val takenId = taken.takenId
        val flag = medicine.prodAmount >= alarm.amount

        val action = Intent(context, ActionReceiver::class.java).apply {
            putExtra(ID, intake.medicineId)
            putExtra(TAKEN_ID, takenId)
            putExtra(BLANK, alarm.amount)
        }
        val pendingA = getBroadcast(
            context,
            takenId.toInt(),
            action,
            FLAG_IMMUTABLE or FLAG_UPDATE_CURRENT
        )
        val pendingB = getBroadcast(
            context,
            takenId.toInt(),
            action.setAction(TYPE),
            FLAG_IMMUTABLE or FLAG_UPDATE_CURRENT
        )

        val confirmAction = NotificationCompat.Action.Builder(
            vector_time, context.getString(intake_text_taken), pendingB
        ).build()

        val declineAction = NotificationCompat.Action.Builder(
            vector_time, context.getString(intake_text_not_taken), pendingA
        ).build()

        if (!taken.notified) with(NotificationManagerCompat.from(context)) {
            safeNotify(
                context,
                takenId.toInt(),
                Builder(context, CHANNEL_ID_INTAKES).apply {
                    if (flag) {
                        addAction(confirmAction)
                        addAction(declineAction)

                        if (intake.cancellable) setTimeoutAfter(600000L)
                        if (intake.fullScreen) setFullScreenIntent(
                            getActivity(
                                context,
                                takenId.toInt(),
                                Intent(context, IntakeDialogActivity::class.java).apply {
                                    setFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                                    putExtra(ID, intake.medicineId)
                                    putExtra(TAKEN_ID, takenId)
                                    putExtra(BLANK, alarm.amount)
                                },
                                FLAG_IMMUTABLE or FLAG_UPDATE_CURRENT
                            ), true
                        )
                    }
                }
                    .setAutoCancel(false)
                    .setCategory(CATEGORY_REMINDER)
                    .setContentTitle(context.getString(text_do_intake))
                    .setDeleteIntent(pendingA)
                    .setSilent(intake.noSound)
                    .setSmallIcon(ic_launcher_foreground)
                    .setStyle(
                        BigTextStyle().bigText(
                            context.getString(
                                if (flag) text_intake_time else text_intake_amount_not_enough,
                                medicine.nameAlias.ifEmpty(medicine::productName),
                                decimalFormat(alarm.amount),
                                context.getString(DoseTypes.getTitle(medicine.doseType)),
                                decimalFormat(medicine.prodAmount - alarm.amount)
                            )
                        )
                    )
                    .setVisibility(VISIBILITY_PUBLIC)
                    .extend(
                        NotificationCompat.WearableExtender().apply {
                            if (flag) {
                                addAction(confirmAction)
                                addAction(declineAction)
                            }
                            setContentIntentAvailableOffline(false)
                        }
                    )
                    .build()
            )
        }

        AlarmSetter(context).resetOrDelete(alarmId, alarm.trigger, intake, AlarmType.ALARM)
    }
}