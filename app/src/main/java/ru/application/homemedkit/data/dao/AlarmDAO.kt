package ru.application.homemedkit.data.dao

import androidx.room.Dao
import androidx.room.Query
import ru.application.homemedkit.data.dto.Alarm

@Dao
interface AlarmDAO : BaseDAO<Alarm> {
    @Query("SELECT * FROM alarms")
    fun getAll(): List<Alarm>

    @Query("SELECT * FROM alarms WHERE alarmId = :alarmId")
    fun getById(alarmId: Long): Alarm?

    @Query("SELECT * FROM alarms WHERE intakeId = :intakeId")
    fun getByIntake(intakeId: Long): List<Alarm>

    @Query("UPDATE alarms SET `trigger` = :trigger WHERE alarmId = :alarmId")
    fun reset(alarmId: Long, trigger: Long)
}