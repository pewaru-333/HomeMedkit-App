package ru.application.homemedkit.data.dao

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Transaction
import kotlinx.coroutines.flow.Flow
import ru.application.homemedkit.data.dto.IntakeTaken
import ru.application.homemedkit.data.model.IntakeTakenFull

@Dao
interface TakenDAO : BaseDAO<IntakeTaken> {
    // ============================== Queries ==============================
    @Query(
        """
        SELECT * FROM intakes_taken
        WHERE (:search = '' OR LOWER(productName) LIKE '%' || LOWER(:search) || '%')
        ORDER BY `trigger`
        """
    )
    fun getFlow(search: String): Flow<List<IntakeTaken>>

    @Transaction
    @Query("SELECT * FROM intakes_taken WHERE takenId = :takenId")
    suspend fun getById(takenId: Long): IntakeTakenFull?

    @Query(
        """
        SELECT amount FROM intakes_taken 
        WHERE medicineId = :medicineId
        ORDER BY `trigger` DESC
        LIMIT 1
        """
    )
    suspend fun getSimilarAmount(medicineId: Long): Double?

    @Query("UPDATE intakes_taken SET taken = :taken, inFact = :inFact WHERE takenId = :id")
    suspend fun setTaken(id: Long, taken: Boolean, inFact: Long)

    @Query("UPDATE intakes_taken SET notified = 1 WHERE takenId = :id")
    suspend fun setNotified(id: Long)

    @Query("UPDATE intakes_taken SET notified = 1 WHERE notified = 0")
    suspend fun setNotified()
}