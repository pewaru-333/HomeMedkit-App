package ru.application.homemedkit.data.dao

import androidx.room.Dao
import androidx.room.Query
import ru.application.homemedkit.data.dto.IntakeDay
import java.time.DayOfWeek

@Dao
interface IntakeDayDAO : BaseDAO<IntakeDay> {
    @Query("SELECT day FROM intake_days WHERE intakeId = :intakeId")
    fun getByIntakeId(intakeId: Long): List<DayOfWeek>

    @Query("DELETE FROM intake_days WHERE intakeId = :intakeId")
    fun deleteByIntakeId(intakeId: Long)
}