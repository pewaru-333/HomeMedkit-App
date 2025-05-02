package ru.application.homemedkit.data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Upsert
import kotlinx.coroutines.flow.Flow
import ru.application.homemedkit.data.dto.Kit
import ru.application.homemedkit.data.dto.MedicineKit

@Dao
interface KitDAO : BaseDAO<Kit> {
    // ============================== Queries ==============================
    @Query("SELECT * FROM kits ORDER BY position ASC, kitId ASC")
    fun getFlow(): Flow<List<Kit>>

    @Query("SELECT * FROM kits WHERE kitId IN (:kitIds)")
    suspend fun getKitList(kitIds: List<Long>): List<Kit>

    @Query("DELETE FROM medicines_kits WHERE medicineId = :medicineId")
    suspend fun deleteAll(medicineId: Long)

    // ============================== Insert ==============================
    @Insert
    suspend fun pinKit(kits: Iterable<MedicineKit>)

    @Upsert
    suspend fun updatePositions(kits: Iterable<Kit>)
}