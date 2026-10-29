package ru.application.homemedkit.data.dao

import androidx.room.*
import androidx.sqlite.db.SupportSQLiteQuery
import kotlinx.coroutines.flow.Flow
import ru.application.homemedkit.data.dto.Image
import ru.application.homemedkit.data.dto.Medicine
import ru.application.homemedkit.data.dto.MedicineFTS
import ru.application.homemedkit.data.dto.MedicineKit
import ru.application.homemedkit.data.model.MedicineFull
import ru.application.homemedkit.data.model.MedicineMain

@Dao
interface MedicineDAO : BaseDAO<Medicine> {
    // ============================== Queries ==============================
    @Query("SELECT * FROM medicines")
    suspend fun getAll(): List<Medicine>

    @Query("SELECT * FROM medicines WHERE expDate < :expDate AND prodAmount > 0")
    suspend fun getExpiredSoon(expDate: Long): List<Medicine>

    @Transaction
    @RawQuery(observedEntities = [Medicine::class, MedicineFTS::class, Image::class, MedicineKit::class])
    fun getFlow(query: SupportSQLiteQuery): Flow<List<MedicineMain>>

    @Query(
        """
    SELECT id FROM medicines
    WHERE (length(:cis) = 13 AND cis = :cis COLLATE BINARY)
       OR (length(:cis) = 31 AND substr(cis, instr(cis, '01'), 31) = :cis COLLATE BINARY)
    ORDER BY id
    LIMIT 1
    """
    )
    suspend fun findDuplicate(cis: String): Long?

    @Transaction
    @Query("SELECT * FROM medicines WHERE id = :id")
    suspend fun getById(id: Long): MedicineFull?

    @Query("SELECT DISTINCT image FROM images")
    suspend fun getAllImageNames(): List<String>

    @Query("SELECT image FROM images WHERE medicineId = :medicineId AND position = 0")
    suspend fun getMedicineImage(medicineId: Long): String?

    @Query("SELECT COUNT(*) FROM images WHERE image = :fileName")
    suspend fun getImageCount(fileName: String): Int

    @Query("UPDATE medicines SET prodAmount = prodAmount - :amount WHERE id = :id")
    suspend fun intakeMedicine(id: Long, amount: Double)

    @Query("UPDATE medicines SET prodAmount = prodAmount + :amount WHERE id = :id")
    suspend fun untakeMedicine(id: Long, amount: Double)

    // ============================== Insert ==============================
    @Transaction
    suspend fun insertScanned(medicine: Medicine, images: List<Image>): Long {
        val id = insert(medicine)

        insertImages(
            images = images.map {
                it.copy(medicineId = id)
            }
        )

        return id
    }

    @Insert
    suspend fun insertImages(images: List<Image>)

    @Upsert
    suspend fun upsertAll(medicines: Iterable<Medicine>)

    // ============================== Update ==============================
    @Transaction
    suspend fun updateImages(images: List<Image>) {
        images.firstOrNull()?.let { deleteImages(it.medicineId) }
        insertImages(images)
    }

    @Transaction
    suspend fun syncMedicines(medicines: Iterable<Medicine>) {
        upsertAll(medicines)

        val ids = medicines.map { it.id }
        deleteMissing(ids)
    }

    // ============================== Delete ==============================
    @Query("DELETE FROM images WHERE medicineId = :medicineId")
    suspend fun deleteImages(medicineId: Long)

    @Query("DELETE FROM medicines WHERE id NOT IN (:ids)")
    suspend fun deleteMissing(ids: List<Long>)
}