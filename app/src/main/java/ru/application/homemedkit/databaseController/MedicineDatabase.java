package ru.application.homemedkit.databaseController;

import android.content.Context;

import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;

@Database(entities = {Medicine.class, Intake.class, Alarm.class}, version = 1)
public abstract class MedicineDatabase extends RoomDatabase {

    private static final String DATABASE_NAME = "medicines";
    private static MedicineDatabase instance;

    public static synchronized MedicineDatabase getInstance(Context context) {
        if (instance == null) {
            instance = Room.databaseBuilder(context, MedicineDatabase.class, DATABASE_NAME)
                    .allowMainThreadQueries()
                    .build();
        }
        return instance;
    }

    public abstract MedicineDAO medicineDAO();

    public abstract IntakeDAO intakeDAO();

    public abstract AlarmDAO alarmDAO();
}
