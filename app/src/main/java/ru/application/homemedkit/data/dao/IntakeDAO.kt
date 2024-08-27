package ru.application.homemedkit.data.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import ru.application.homemedkit.data.dto.Alarm
import ru.application.homemedkit.data.dto.Intake

@Dao
interface IntakeDAO {

    // ============================== Queries ==============================
    @Query("SELECT * FROM intakes")
    fun getAll(): List<Intake>

    @Query("SELECT * FROM intakes WHERE intakeId = :intakeId")
    fun getById(intakeId: Long): Intake?

    @Query("SELECT * FROM alarms WHERE intakeId = :intakeId")
    fun getAlarms(intakeId: Long): List<Alarm>

    // ============================== Insert ==============================
    @Insert
    suspend fun add(intake: Intake): Long

    // ============================== Update ==============================
    @Update
    suspend fun update(intake: Intake)

    // ============================== Delete ==============================
    @Delete
    suspend fun delete(intake: Intake)

    @Delete
    fun remove(intake: Intake)
}