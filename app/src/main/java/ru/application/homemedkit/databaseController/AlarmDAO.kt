package ru.application.homemedkit.databaseController

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query

@Dao
interface AlarmDAO {

    // ============================== Queries ==============================
    @Query("SELECT * FROM alarms")
    fun getAll(): List<Alarm>

    @Query("SELECT * FROM alarms WHERE alarmId=:alarmId")
    fun getByPK(alarmId: Long): Alarm

    @Query("UPDATE alarms SET `trigger`=:trigger WHERE alarmId=:alarmId")
    fun reset(alarmId: Long, trigger: Long)

    // ============================== Insert ==============================
    @Insert
    fun add(alarm: Alarm): Long

    // ============================== Delete ==============================
    @Delete
    fun delete(alarm: Alarm)
}