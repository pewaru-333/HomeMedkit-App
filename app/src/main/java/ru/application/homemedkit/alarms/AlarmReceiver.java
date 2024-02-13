package ru.application.homemedkit.alarms;

import static android.app.PendingIntent.FLAG_IMMUTABLE;
import static android.app.PendingIntent.FLAG_UPDATE_CURRENT;
import static android.app.PendingIntent.getActivity;
import static java.util.Objects.requireNonNull;
import static ru.application.homemedkit.helpers.ConstantsHelper.ALARM_ID;
import static ru.application.homemedkit.helpers.ConstantsHelper.BOUND;
import static ru.application.homemedkit.helpers.ConstantsHelper.FINISH;
import static ru.application.homemedkit.helpers.ConstantsHelper.INTERVAL;
import static ru.application.homemedkit.helpers.ConstantsHelper.NEW_INTAKE;
import static ru.application.homemedkit.helpers.ConstantsHelper.SOUND_GROUP;
import static ru.application.homemedkit.helpers.StringHelperKt.daysInterval;
import static ru.application.homemedkit.helpers.StringHelperKt.shortName;

import android.annotation.SuppressLint;
import android.app.AlarmManager;
import android.app.Notification;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.media.RingtoneManager;

import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

import java.util.Objects;
import java.util.Random;

import ru.application.homemedkit.R;
import ru.application.homemedkit.activities.MainActivity;
import ru.application.homemedkit.databaseController.AlarmDAO;
import ru.application.homemedkit.databaseController.Intake;
import ru.application.homemedkit.databaseController.IntakeDAO;
import ru.application.homemedkit.databaseController.MedicineDAO;
import ru.application.homemedkit.databaseController.MedicineDatabase;
import ru.application.homemedkit.helpers.DateHelper;

public class AlarmReceiver extends BroadcastReceiver {

    private static final long DAY = AlarmManager.INTERVAL_DAY;
    private static final long WEEK = AlarmManager.INTERVAL_DAY * 7;

    private static Notification intakeNotification(Context context, long medicineId, boolean flag) {
        MedicineDatabase database = MedicineDatabase.getInstance(context);
        String productName = database.medicineDAO().getProductName(medicineId);
        int code = new Random().nextInt(BOUND);

        String title = context.getString(R.string.text_intake_time) + shortName(productName);
        if (!flag) title += context.getString(R.string.text_medicine_amount_not_enough);

        Intent intent = new Intent(context, MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        intent.putExtra(NEW_INTAKE, true);

        PendingIntent pending = getActivity(context, code, intent,
                FLAG_IMMUTABLE | FLAG_UPDATE_CURRENT);

        return new NotificationCompat.Builder(context, context.getString(R.string.notification_channel_name))
                .setSmallIcon(R.drawable.vector_time)
                .setContentTitle(context.getString(R.string.text_do_intake))
                .setCategory(NotificationCompat.CATEGORY_ALARM)
                .setAutoCancel(true)
                .setForegroundServiceBehavior(NotificationCompat.FOREGROUND_SERVICE_IMMEDIATE)
                .setContentText(title)
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setDefaults(Notification.DEFAULT_ALL)
                .setGroup(SOUND_GROUP)
                .setGroupAlertBehavior(NotificationCompat.GROUP_ALERT_SUMMARY)
                .setContentIntent(pending)
                .build();
    }

    @SuppressLint({"MissingPermission", "UnsafeProtectedBroadcastReceiver", "UnsafeIntentLaunch"})
    @Override
    public void onReceive(Context context, Intent intent) {
        MedicineDatabase database = MedicineDatabase.getInstance(context);
        AlarmSetter alarmSetter = new AlarmSetter(context);
        String[] intervals = context.getResources().getStringArray(R.array.interval_types);

        intent.addFlags(Intent.FLAG_RECEIVER_FOREGROUND);

        MedicineDAO medicineDAO = database.medicineDAO();
        IntakeDAO intakeDAO = database.intakeDAO();
        AlarmDAO alarmDAO = database.alarmDAO();

        Notification notification;
        NotificationManagerCompat compat;

        long alarmId = intent.getLongExtra(ALARM_ID, 0);
        String interval = intent.getStringExtra(INTERVAL);
        String finish = intent.getStringExtra(FINISH);

        long trigger = alarmDAO.getByPK(alarmId).trigger;

        if (DateHelper.lastDay(finish) > System.currentTimeMillis()) {
            long intakeId = alarmDAO.getByPK(alarmId).intakeId;
            long medicineId = intakeDAO.getByPK(intakeId).medicineId;
            double amount = intakeDAO.getByPK(intakeId).amount;

            if (medicineDAO.getByPK(medicineId).prodAmount > amount) {
                medicineDAO.intakeMedicine(medicineId, amount);

                notification = intakeNotification(context, medicineId, true);
                compat = NotificationManagerCompat.from(context);

                resetAlarm(alarmSetter, intervals, alarmId, interval, trigger);
            } else {
                notification = intakeNotification(context, medicineId, false);
                compat = NotificationManagerCompat.from(context);
                intakeDAO.delete(new Intake(intakeId));
            }
            playSound(context);
            compat.notify(new Random().nextInt(BOUND), notification);
        } else {
            alarmSetter.removeAlarm(context, alarmId, intent);
        }
    }

    private void resetAlarm(AlarmSetter alarmSetter, String[] intervals, long alarmId, String interval, long trigger) {
        if (Objects.equals(interval, intervals[0]) || Objects.equals(interval, intervals[1])) {
            alarmSetter.setAlarm(alarmId, trigger + DAY);
        } else if (Objects.equals(interval, intervals[2])) {
            alarmSetter.setAlarm(alarmId, trigger + WEEK);
        } else {
            alarmSetter.setAlarm(alarmId, trigger + daysInterval(requireNonNull(interval)) * DAY);
        }
    }

    private void playSound(Context context) {
        RingtoneManager.getRingtone(context,
                RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)).play();
    }
}
