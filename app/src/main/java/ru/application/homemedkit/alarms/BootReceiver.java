package ru.application.homemedkit.alarms;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import java.util.List;
import java.util.Objects;

import ru.application.homemedkit.R;
import ru.application.homemedkit.databaseController.Alarm;
import ru.application.homemedkit.databaseController.MedicineDatabase;
import ru.application.homemedkit.graphics.Toasts;

public class BootReceiver extends BroadcastReceiver {
    private static final String BOOT_COMPLETED = "android.intent.action.BOOT_COMPLETED";
    private static final String REBOOT_COMPLETED = "android.intent.action.QUICKBOOT_POWERON";
    private static final String LOCKED_REBOOT_COMPLETED = "android.intent.action.LOCKED_BOOT_COMPLETED";

    @Override
    public void onReceive(Context context, Intent intent) {
        MedicineDatabase database = MedicineDatabase.getInstance(context);
        List<Alarm> alarms = database.alarmDAO().getAll();
        AlarmSetter alarmSetter = new AlarmSetter(context);

        boolean boot = Objects.equals(intent.getAction(), BOOT_COMPLETED);
        boolean reboot = Objects.equals(intent.getAction(), REBOOT_COMPLETED);
        boolean rebootLocked = Objects.equals(intent.getAction(), LOCKED_REBOOT_COMPLETED);

        if ((boot || reboot || rebootLocked) && alarms.size() > 0) {
            for (int i = 0; i < alarms.size(); i++) {
                Alarm alarm = alarms.get(i);
                alarmSetter.setAlarm(alarm.alarmId, alarm.trigger);
            }
            new Toasts(context, R.string.text_reschedule_alarms);
        }
    }
}
