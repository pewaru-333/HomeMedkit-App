package ru.application.homemedkit.receivers

import android.app.PendingIntent
import android.app.PendingIntent.FLAG_IMMUTABLE
import android.app.PendingIntent.FLAG_UPDATE_CURRENT
import android.app.PendingIntent.getActivity
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationCompat.BigTextStyle
import androidx.core.app.NotificationCompat.Builder
import androidx.core.app.NotificationCompat.CATEGORY_REMINDER
import androidx.core.app.NotificationCompat.VISIBILITY_PUBLIC
import androidx.core.app.NotificationManagerCompat
import ru.application.homemedkit.IntakeDialogActivity
import ru.application.homemedkit.R
import ru.application.homemedkit.data.MedicineDatabase
import ru.application.homemedkit.utils.ALARM_ID
import ru.application.homemedkit.utils.BLANK
import ru.application.homemedkit.utils.CHANNEL_ID_INTAKES
import ru.application.homemedkit.utils.ID
import ru.application.homemedkit.utils.IS_ENOUGH_IN_STOCK
import ru.application.homemedkit.utils.TAKEN_ID
import ru.application.homemedkit.utils.TYPE
import ru.application.homemedkit.utils.decimalFormat
import ru.application.homemedkit.utils.extensions.goAsync
import ru.application.homemedkit.utils.extensions.safeNotify

class AlarmReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) = goAsync {
        val database = MedicineDatabase.getInstance(context)

        val takenId = intent.getLongExtra(ALARM_ID, 0L)

        val taken = database.takenDAO().getById(takenId) ?: return@goAsync
        val intake = database.intakeDAO().getById(taken.intakeId) ?: return@goAsync
        val medicine = database.medicineDAO().getById(intake.medicineId) ?: return@goAsync

        val flag = medicine.prodAmount >= taken.amount

        val action = Intent(context, ActionReceiver::class.java).apply {
            putExtra(TAKEN_ID, takenId)
            putExtra(ID, intake.medicineId)
            putExtra(BLANK, taken.amount)
        }

        val pendingA = createPending(context, action, takenId)
        val pendingB = createPending(context, action.setAction(TYPE), takenId)

        val confirm = createAction(context, pendingB, R.string.intake_text_taken)
        val decline = createAction(context, pendingA, R.string.intake_text_not_taken)

        AlarmSetter.getInstance(context).setPreAlarm(intake.intakeId)

        if (!taken.notified) {
            with(NotificationManagerCompat.from(context)) {
                safeNotify(
                    context = context,
                    code = takenId.toInt(),
                    notification = commonBuilder(
                        context = context,
                        title = R.string.text_do_intake,
                        text = context.getString(
                            if (flag) R.string.text_intake_time else R.string.text_intake_amount_not_enough,
                            medicine.nameAlias.ifEmpty(medicine::productName),
                            decimalFormat(taken.amount),
                            context.getString(medicine.doseType.title),
                            decimalFormat(medicine.prodAmount - taken.amount)
                        ),
                        flag = flag,
                        actions = listOf(confirm, decline)
                    ).apply {
                        if (flag) {
                            addAction(confirm)
                            addAction(decline)

                            if (intake.cancellable) setTimeoutAfter(600000L)
                            if (intake.fullScreen) setFullScreenIntent(
                                getActivity(
                                    context,
                                    takenId.toInt(),
                                    Intent(context, IntakeDialogActivity::class.java).apply {
                                        flags = Intent.FLAG_ACTIVITY_NEW_TASK
                                        putExtra(ID, intake.medicineId)
                                        putExtra(TAKEN_ID, takenId)
                                        putExtra(BLANK, taken.amount)
                                    },
                                    FLAG_IMMUTABLE or FLAG_UPDATE_CURRENT
                                ), true
                            )
                        }
                    }
                        .setDeleteIntent(pendingA)
                        .setSilent(intake.noSound)
                        .setExtras(
                            Bundle().apply {
                                putLong(TAKEN_ID, takenId)
                                putLong(ID, intake.medicineId)
                                putDouble(BLANK, taken.amount)
                                putBoolean(IS_ENOUGH_IN_STOCK, flag)
                            }
                        )
                        .build()
                )

                if (activeNotifications.size > 1) {
                    val flag = activeNotifications
                        .filter { it.packageName == context.packageName }
                        .filter { it.notification.extras.containsKey(IS_ENOUGH_IN_STOCK) }
                        .all { it.notification.extras.getBoolean(IS_ENOUGH_IN_STOCK) }

                    if (flag) {
                        val action = Intent(context, ActionGroupReceiver::class.java)

                        val pendingA = createPending(context, action)
                        val pendingB = createPending(context, action.setAction(TYPE))

                        val confirm = createAction(context, pendingB, R.string.text_action_intake_all_accept)
                        val decline = createAction(context, pendingA, R.string.text_action_intake_all_decline)

                        safeNotify(
                            context = context,
                            code = Int.MAX_VALUE,
                            notification = commonBuilder(
                                context = context,
                                title = R.string.text_do_intake_all,
                                text = context.getString(R.string.text_intake_sure_to_intake_all),
                                flag = true,
                                actions = listOf(confirm, decline)
                            )
                                .addAction(confirm)
                                .addAction(decline)
                                .build()
                        )
                    } else {
                        cancel(Int.MAX_VALUE)
                    }
                }
            }
        }
    }

    private fun createPending(context: Context, action: Intent, code: Number) = PendingIntent
        .getBroadcast(context, code.toInt(), action, FLAG_IMMUTABLE or FLAG_UPDATE_CURRENT)

    private fun createPending(context: Context, action: Intent) =
        createPending(context, action, Int.MAX_VALUE)

    private fun createAction(context: Context, pending: PendingIntent, icon: Int, title: Int) =
        NotificationCompat.Action.Builder(icon, context.getString(title), pending).build()

    private fun createAction(context: Context, pending: PendingIntent, title: Int) =
        createAction(context, pending, R.drawable.vector_time, title)

    private fun commonBuilder(
        context: Context,
        title: Int,
        text: String,
        flag: Boolean,
        actions: List<NotificationCompat.Action>
    ) = Builder(context, CHANNEL_ID_INTAKES)
        .setAutoCancel(false)
        .setCategory(CATEGORY_REMINDER)
        .setContentTitle(context.getString(title))
        .setSmallIcon(R.drawable.ic_launcher_notification)
        .setStyle(BigTextStyle().bigText(text))
        .setVisibility(VISIBILITY_PUBLIC)
        .extend(
            NotificationCompat.WearableExtender().apply {
                setContentIntentAvailableOffline(false)
                if (flag) {
                    addActions(actions)
                }
            }
        )
}