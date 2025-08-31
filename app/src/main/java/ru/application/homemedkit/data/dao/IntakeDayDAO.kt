package ru.application.homemedkit.data.dao

import androidx.room.Dao
import androidx.room.Query
import ru.application.homemedkit.data.dto.IntakeDay

@Dao
interface IntakeDayDAO : BaseDAO<IntakeDay> {
    @Query("DELETE FROM intake_days WHERE intakeId = :intakeId")
    suspend fun deleteByIntakeId(intakeId: Long)
}