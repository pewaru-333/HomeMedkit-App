package ru.application.homemedkit.data.dao

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Transaction
import kotlinx.coroutines.flow.Flow
import ru.application.homemedkit.data.dto.IntakeTaken

@Dao
interface TakenDAO : BaseDAO<IntakeTaken> {
    // ============================== Queries ==============================
    @Transaction
    @Query("SELECT * FROM intakes_taken")
    fun getAll(): List<IntakeTaken>

    @Transaction
    @Query("SELECT * FROM intakes_taken")
    fun getFlow(): Flow<List<IntakeTaken>>

    @Query("SELECT * FROM intakes_taken WHERE takenId = :takenId")
    fun getById(takenId: Long): IntakeTaken?

    @Query("UPDATE intakes_taken SET taken = :taken, inFact = :inFact WHERE takenId = :id")
    fun setTaken(id: Long, taken: Boolean, inFact: Long)

    @Query("UPDATE intakes_taken SET notified = 1 WHERE takenId = :id")
    fun setNotified(id: Long)
}