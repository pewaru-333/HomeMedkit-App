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
    @Query("SELECT * FROM intakes_taken")
    fun getAll(): List<IntakeTaken>

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
    fun getById(takenId: Long): IntakeTakenFull?

    @Query("UPDATE intakes_taken SET taken = :taken, inFact = :inFact WHERE takenId = :id")
    fun setTaken(id: Long, taken: Boolean, inFact: Long)

    @Query("UPDATE intakes_taken SET notified = 1 WHERE takenId = :id")
    fun setNotified(id: Long)
}