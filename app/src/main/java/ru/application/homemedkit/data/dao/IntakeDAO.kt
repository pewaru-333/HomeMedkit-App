package ru.application.homemedkit.data.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import kotlinx.coroutines.flow.Flow
import ru.application.homemedkit.data.dto.Alarm
import ru.application.homemedkit.data.dto.Intake
import ru.application.homemedkit.data.dto.IntakeTime

@Dao
interface IntakeDAO {
    // ============================== Queries ==============================
    @Transaction
    @Query("SELECT * FROM intakes")
    fun getFlow(): Flow<List<Intake>>

    @Query("SELECT * FROM intakes WHERE intakeId = :intakeId")
    fun getById(intakeId: Long): Intake?

    @Query("SELECT * FROM alarms WHERE intakeId = :intakeId")
    fun getAlarms(intakeId: Long): List<Alarm>

    @Query("SELECT * FROM intake_time WHERE intakeId = :intakeId")
    fun getTime(intakeId: Long): List<IntakeTime>

    // ============================== Insert ==============================
    @Insert
    suspend fun add(intake: Intake): Long

    @Insert
    suspend fun addIntakeTime(intakeTime: IntakeTime): Long

    // ============================== Update ==============================
    @Update
    suspend fun update(intake: Intake)

    // ============================== Delete ==============================
    @Delete
    suspend fun delete(intake: Intake)

    @Query("DELETE FROM intake_time WHERE intakeId = :intakeId")
    suspend fun deleteIntakeTime(intakeId: Long)
}