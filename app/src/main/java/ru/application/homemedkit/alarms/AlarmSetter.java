package ru.application.homemedkit.alarms;

import static android.app.AlarmManager.RTC_WAKEUP;
import static android.app.PendingIntent.FLAG_IMMUTABLE;
import static android.app.PendingIntent.FLAG_UPDATE_CURRENT;
import static android.app.PendingIntent.getBroadcast;
import static android.content.Context.ALARM_SERVICE;
import static android.content.Intent.FLAG_ACTIVITY_CLEAR_TASK;
import static android.content.Intent.FLAG_ACTIVITY_NEW_TASK;
import static android.content.Intent.FLAG_RECEIVER_FOREGROUND;
import static ru.application.homemedkit.helpers.ConstantsHelper.EXP_CODE;
import static ru.application.homemedkit.helpers.DateHelper.expirationCheckTime;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;

import androidx.core.app.AlarmManagerCompat;

import ru.application.homemedkit.databaseController.Alarm;
import ru.application.homemedkit.databaseController.Intake;
import ru.application.homemedkit.databaseController.MedicineDatabase;
import ru.application.homemedkit.helpers.ConstantsHelper;

public class AlarmSetter {

    private final Context context;

    public AlarmSetter(Context context) {
        this.context = context;
    }

    public void setAlarm(long intakeId, long trigger, String interval, String finish) {
        AlarmManager manager = (AlarmManager) context.getSystemService(ALARM_SERVICE);
        MedicineDatabase database = MedicineDatabase.getInstance(context);

        long alarmId = database.alarmDAO().add(new Alarm(intakeId, trigger));

        Intent intent = getIntent(alarmId, interval, finish);

        PendingIntent pending = getBroadcast(context, (int) alarmId, intent,
                FLAG_UPDATE_CURRENT | FLAG_IMMUTABLE);
        AlarmManagerCompat.setExactAndAllowWhileIdle(manager, RTC_WAKEUP, trigger, pending);
    }

    private Intent getIntent(long alarmId, String interval, String finish) {
        Intent intent = new Intent(context, AlarmReceiver.class);
        intent.setFlags(FLAG_ACTIVITY_NEW_TASK | FLAG_ACTIVITY_CLEAR_TASK);
        intent.addFlags(FLAG_RECEIVER_FOREGROUND);

        intent.putExtra(ConstantsHelper.ALARM_ID, alarmId);
        intent.putExtra(ConstantsHelper.INTERVAL, interval);
        intent.putExtra(ConstantsHelper.FINISH, finish);

        return intent;
    }

    public void setAlarm(long intakeId, long[] triggers, String interval, String finish) {
        AlarmManager manager = (AlarmManager) context.getSystemService(ALARM_SERVICE);
        MedicineDatabase database = MedicineDatabase.getInstance(context);

        for (long trigger : triggers) {
            long alarmId = database.alarmDAO().add(new Alarm(intakeId, trigger));

            Intent intent = getIntent(alarmId, interval, finish);

            PendingIntent pending = getBroadcast(context, (int) alarmId, intent,
                    FLAG_UPDATE_CURRENT | FLAG_IMMUTABLE);
            AlarmManagerCompat.setExactAndAllowWhileIdle(manager, RTC_WAKEUP, trigger, pending);
        }
    }

    public void setAlarm(long alarmId, long trigger) {
        AlarmManager manager = (AlarmManager) context.getSystemService(ALARM_SERVICE);

        MedicineDatabase database = MedicineDatabase.getInstance(context);
        Alarm alarm = database.alarmDAO().getByPK(alarmId);
        Intake intake = database.intakeDAO().getByPK(alarm.intakeId);

        Intent intent = getIntent(alarmId, intake.interval, intake.finalDate);

        PendingIntent pending = getBroadcast(context, (int) alarmId, intent,
                FLAG_UPDATE_CURRENT | FLAG_IMMUTABLE);
        AlarmManagerCompat.setExactAndAllowWhileIdle(manager, RTC_WAKEUP, trigger, pending);

        database.alarmDAO().reset(alarmId, trigger);
    }

    public void removeAlarm(Context context, long alarmId, Intent intent) {
        AlarmManager manager = (AlarmManager) context.getSystemService(ALARM_SERVICE);

        MedicineDatabase database = MedicineDatabase.getInstance(context);
        Alarm alarm = database.alarmDAO().getByPK(alarmId);

        PendingIntent pending = getBroadcast(context, (int) alarmId, intent,
                FLAG_UPDATE_CURRENT | FLAG_IMMUTABLE);

        manager.cancel(pending);

        database.alarmDAO().delete(alarm);
    }

    public void checkExpiration() {
        AlarmManager manager = (AlarmManager) context.getSystemService(ALARM_SERVICE);

        Intent intent = new Intent(context, ExpirationReceiver.class);
        intent.setFlags(FLAG_ACTIVITY_NEW_TASK | FLAG_ACTIVITY_CLEAR_TASK);
        intent.addFlags(FLAG_RECEIVER_FOREGROUND);

        PendingIntent pending = getBroadcast(context, EXP_CODE, intent,
                FLAG_UPDATE_CURRENT | FLAG_IMMUTABLE);

        AlarmManagerCompat.setExactAndAllowWhileIdle(manager, RTC_WAKEUP, expirationCheckTime(), pending);
    }
}

