package ru.application.homemedkit.alarms;

import static android.content.Intent.ACTION_BOOT_COMPLETED;
import static android.content.Intent.ACTION_LOCKED_BOOT_COMPLETED;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import java.util.Arrays;
import java.util.List;

import ru.application.homemedkit.databaseController.Alarm;
import ru.application.homemedkit.databaseController.MedicineDatabase;

public class BootReceiver extends BroadcastReceiver {
    private static final String REBOOT_COMPLETED = "android.intent.action.QUICKBOOT_POWERON";
    private static final List<String> ACTIONS = Arrays.asList(ACTION_LOCKED_BOOT_COMPLETED, ACTION_BOOT_COMPLETED,
            REBOOT_COMPLETED);

    @Override
    public void onReceive(Context context, Intent intent) {
        MedicineDatabase database = MedicineDatabase.getInstance(context);
        List<Alarm> alarms = database.alarmDAO().getAll();
        AlarmSetter alarmSetter = new AlarmSetter(context);

        if (ACTIONS.contains(intent.getAction()) && alarms.size() > 0) {
            alarms.forEach(alarm -> alarmSetter.setAlarm(alarm.alarmId, alarm.trigger));
        }
    }
}
