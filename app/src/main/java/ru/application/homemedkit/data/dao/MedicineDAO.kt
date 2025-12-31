package ru.application.homemedkit.data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.RawQuery
import androidx.room.Transaction
import androidx.sqlite.db.SupportSQLiteQuery
import kotlinx.coroutines.flow.Flow
import ru.application.homemedkit.data.dto.Image
import ru.application.homemedkit.data.dto.Medicine
import ru.application.homemedkit.data.dto.MedicineFTS
import ru.application.homemedkit.data.model.MedicineFull
import ru.application.homemedkit.data.model.MedicineMain

@Dao
interface MedicineDAO : BaseDAO<Medicine> {
    // ============================== Queries ==============================
    @Query("SELECT * FROM medicines")
    suspend fun getAll(): List<Medicine>

    @RawQuery(observedEntities = [Medicine::class, MedicineFTS::class])
    fun getFlow(query: SupportSQLiteQuery): Flow<List<MedicineMain>>

    @Query("SELECT id FROM medicines WHERE :cis LIKE '%' || cis || '%' AND cis IS NOT NULL AND cis != '' LIMIT 1")
    suspend fun getIdByCis(cis: String): Long?

    @Transaction
    @Query("SELECT * FROM medicines WHERE id = :id")
    suspend fun getById(id: Long): MedicineFull?

    @Query("SELECT image FROM images")
    suspend fun getAllImages(): List<String>

    @Query("SELECT image FROM images WHERE medicineId = :medicineId")
    suspend fun getMedicineImages(medicineId: Long): List<String>

    @Query("UPDATE medicines SET prodAmount = prodAmount - :amount WHERE id = :id")
    suspend fun intakeMedicine(id: Long, amount: Double)

    @Query("UPDATE medicines SET prodAmount = prodAmount + :amount WHERE id = :id")
    suspend fun untakeMedicine(id: Long, amount: Double)

    // ============================== Insert ==============================
    @Insert
    suspend fun addImage(image: Iterable<Image>)

    // ============================== Update ==============================
    @Transaction
    suspend fun updateImages(images: Iterable<Image>) {
        if (images.count() > 0) {
            deleteImages(images.first().medicineId)
        }
        addImage(images)
    }

    // ============================== Delete ==============================
    @Query("DELETE FROM images WHERE medicineId = :medicineId")
    suspend fun deleteImages(medicineId: Long)
}