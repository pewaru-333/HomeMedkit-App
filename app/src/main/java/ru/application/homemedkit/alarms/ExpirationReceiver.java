package ru.application.homemedkit.alarms;

import static android.app.PendingIntent.FLAG_IMMUTABLE;
import static android.app.PendingIntent.FLAG_UPDATE_CURRENT;
import static android.app.PendingIntent.getActivity;
import static ru.application.homemedkit.helpers.ConstantsHelper.BOUND;
import static ru.application.homemedkit.helpers.ConstantsHelper.ID;
import static ru.application.homemedkit.helpers.ConstantsHelper.SOUND_GROUP;

import android.annotation.SuppressLint;
import android.app.Notification;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.media.RingtoneManager;

import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

import java.util.List;
import java.util.Random;

import ru.application.homemedkit.R;
import ru.application.homemedkit.activities.MedicineActivity;
import ru.application.homemedkit.databaseController.Medicine;
import ru.application.homemedkit.databaseController.MedicineDAO;
import ru.application.homemedkit.databaseController.MedicineDatabase;
import ru.application.homemedkit.helpers.SettingsHelper;

public class ExpirationReceiver extends BroadcastReceiver {

    private static final long MONTH = 2419200000L;

    private static Notification expirationNotification(Context context, long medicineId) {
        MedicineDatabase database = MedicineDatabase.getInstance(context);
        String productName = database.medicineDAO().getProductName(medicineId);
        int code = new Random().nextInt(BOUND);

        Intent intent = new Intent(context, MedicineActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        intent.putExtra(ID, medicineId);

        PendingIntent pending = getActivity(context, code, intent,
                FLAG_IMMUTABLE | FLAG_UPDATE_CURRENT);


        return new NotificationCompat.Builder(context, context.getString(R.string.notification_channel_name))
                .setSmallIcon(R.drawable.vector_time)
                .setContentTitle(context.getString(R.string.text_attention))
                .setCategory(NotificationCompat.CATEGORY_ALARM)
                .setAutoCancel(true)
                .setForegroundServiceBehavior(NotificationCompat.FOREGROUND_SERVICE_IMMEDIATE)
                .setContentText(String.format(context.getString(R.string.text_expire_soon), productName))
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setDefaults(Notification.DEFAULT_ALL)
                .setGroup(SOUND_GROUP)
                .setGroupAlertBehavior(NotificationCompat.GROUP_ALERT_SUMMARY)
                .setContentIntent(pending)
                .build();
    }

    @SuppressLint("MissingPermission")
    @Override
    public void onReceive(Context context, Intent intent) {
        MedicineDatabase database = MedicineDatabase.getInstance(context);
        AlarmSetter alarmSetter = new AlarmSetter(context);

        intent.addFlags(Intent.FLAG_RECEIVER_FOREGROUND);

        MedicineDAO medicineDAO = database.medicineDAO();
        List<Medicine> medicines = medicineDAO.getAll();

        Notification notification;
        NotificationManagerCompat compat;

        boolean flag = new SettingsHelper(context).checkExpirationDate();

        if (flag && !medicines.isEmpty()) {
            for (int i = 0; i < medicines.size(); i++) {
                Medicine medicine = medicines.get(i);
                if ((medicine.expDate < System.currentTimeMillis() + MONTH) && medicine.prodAmount > 0) {

                    notification = expirationNotification(context, medicine.id);
                    compat = NotificationManagerCompat.from(context);

                    compat.notify(new Random().nextInt(BOUND), notification);
                    playSound(context);

                    alarmSetter.checkExpiration();
                }
            }
        }
    }

    private void playSound(Context context) {
        RingtoneManager.getRingtone(context,
                RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)).play();
    }
}
