package ru.application.homemedkit.data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Transaction
import kotlinx.coroutines.flow.Flow
import ru.application.homemedkit.data.dto.Alarm
import ru.application.homemedkit.data.dto.Intake
import ru.application.homemedkit.data.dto.IntakeTime
import ru.application.homemedkit.data.model.IntakeFull
import ru.application.homemedkit.data.model.IntakeList

@Dao
interface IntakeDAO : BaseDAO<Intake> {
    // ============================== Queries ==============================
    @Transaction
    @Query(
        """
        SELECT intakes.intakeId, intakes.medicineId, intakes.interval, intakes.finalDate, 
        medicines.productName, medicines.nameAlias 
        FROM intakes 
        JOIN medicines ON medicines.id = intakes.medicineId
        """
    )
    fun getFlow(): Flow<List<IntakeList>>

    @Query("SELECT * FROM intakes WHERE intakeId = :intakeId")
    fun getById(intakeId: Long): IntakeFull?

    @Query("SELECT * FROM alarms WHERE intakeId = :intakeId")
    fun getAlarms(intakeId: Long): List<Alarm>

    // ============================== Insert ==============================
    @Insert
    suspend fun addIntakeTime(intakeTime: IntakeTime): Long

    // ============================== Delete ==============================
    @Query("DELETE FROM intake_time WHERE intakeId = :intakeId")
    suspend fun deleteIntakeTime(intakeId: Long)
}