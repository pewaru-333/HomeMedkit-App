package ru.application.homemedkit.data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Transaction
import kotlinx.coroutines.flow.Flow
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
        SELECT intakeId, medicineId, productName, nameAlias, finalDate
        FROM intakes
        JOIN medicines ON medicines.id = intakes.medicineId
        WHERE (
            :searchQuery = ''
            OR LOWER(productName) LIKE '%' || LOWER(:searchQuery) || '%'
            OR LOWER(nameAlias) LIKE '%' || LOWER(:searchQuery) || '%'
          )
        """
    )
    fun getFlow(searchQuery: String): Flow<List<IntakeList>>

    @Transaction
    @Query("SELECT * FROM intakes WHERE intakeId = :intakeId")
    suspend fun getById(intakeId: Long): IntakeFull?

    // ============================== Insert ==============================
    @Insert
    suspend fun addIntakeTime(intakeTime: IntakeTime): Long

    // ============================== Delete ==============================
    @Query("DELETE FROM intake_time WHERE intakeId = :intakeId")
    suspend fun deleteIntakeTime(intakeId: Long)
}