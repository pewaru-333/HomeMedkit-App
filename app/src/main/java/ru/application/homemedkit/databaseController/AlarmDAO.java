package ru.application.homemedkit.databaseController;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;

import java.util.List;

@Dao
public interface AlarmDAO {

    // ============================== Queries ==============================
    @Query("SELECT * FROM alarms")
    List<Alarm> getAll();

    @Query("SELECT * FROM alarms WHERE alarmId=:alarmId")
    Alarm getByPK(long alarmId);

    @Query("SELECT * FROM alarms WHERE intakeId=:intakeId")
    List<Alarm> getByIntakeId(long intakeId);

    @Query("UPDATE alarms SET `trigger`=:trigger WHERE alarmId=:alarmId")
    void reset(long alarmId, long trigger);

    // ============================== Insert ==============================
    @Insert
    long add(Alarm alarm);

    // ============================== Delete ==============================
    @Delete
    void delete(Alarm alarm);
}
