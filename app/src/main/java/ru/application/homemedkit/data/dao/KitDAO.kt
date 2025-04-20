package ru.application.homemedkit.data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy.Companion.REPLACE
import androidx.room.Query
import androidx.room.Transaction
import kotlinx.coroutines.flow.Flow
import ru.application.homemedkit.data.dto.Kit
import ru.application.homemedkit.data.dto.MedicineKit
import ru.application.homemedkit.data.model.KitMedicines
import ru.application.homemedkit.data.model.KitModel

@Dao
interface KitDAO : BaseDAO<Kit> {
    // ============================== Queries ==============================
    @Query("SELECT * FROM kits")
    fun getAllKits(): Flow<List<KitMedicines>>

    @Query("SELECT * FROM kits")
    fun getFlow(): Flow<List<Kit>>

    @Transaction
    @Query(
        """
            SELECT kits.kitId, title, medicineId, position FROM kits
            LEFT JOIN medicines_kits ON kits.kitId = medicines_kits.kitId
        """
    )
    fun getMedicineKits(): Flow<List<KitModel>>

    @Query("DELETE FROM medicines_kits WHERE medicineId = :medicineId")
    fun deleteAll(medicineId: Long)

    // ============================== Insert ==============================
    @Insert
    fun pinKit(kits: Iterable<MedicineKit>)

    @Insert(onConflict = REPLACE)
    fun updatePositions(kits: Iterable<Kit>)
}