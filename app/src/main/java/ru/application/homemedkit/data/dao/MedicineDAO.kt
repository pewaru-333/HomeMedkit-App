package ru.application.homemedkit.data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Transaction
import kotlinx.coroutines.flow.Flow
import ru.application.homemedkit.data.dto.Image
import ru.application.homemedkit.data.dto.Medicine
import ru.application.homemedkit.data.model.MedicineFull
import ru.application.homemedkit.data.model.MedicineMain

@Dao
interface MedicineDAO : BaseDAO<Medicine> {
    // ============================== Queries ==============================
    @Transaction
    @Query("SELECT * FROM medicines")
    fun getAll(): List<Medicine>

    @Transaction
    @Query("SELECT * FROM medicines")
    fun getFlow(): Flow<List<MedicineMain>>

    @Query("SELECT id FROM medicines where cis = :cis")
    fun getIdByCis(cis: String): Long

    @Query("SELECT * FROM medicines WHERE id = :id ")
    fun getById(id: Long): MedicineFull?

    @Query("SELECT cis from medicines")
    fun getAllCis(): List<String>

    @Query("SELECT image FROM images")
    fun getAllImages(): List<String>

    @Query("SELECT image FROM images WHERE medicineId = :medicineId")
    fun getMedicineImages(medicineId: Long): List<String>

    @Query("UPDATE medicines SET prodAmount = prodAmount - :amount WHERE id = :id")
    fun intakeMedicine(id: Long, amount: Double)

    @Query("UPDATE medicines SET prodAmount = prodAmount + :amount WHERE id = :id")
    fun untakeMedicine(id: Long, amount: Double)

    // ============================== Insert ==============================
    @Insert
    suspend fun addImage(image: Image)

    @Insert
    suspend fun addImage(image: Iterable<Image>)

    // ============================== Update ==============================
    @Transaction
    suspend fun updateImages(images: Iterable<Image>) {
        deleteImages(images.first().medicineId)
        addImage(images)
    }

    // ============================== Delete ==============================
    @Query("DELETE FROM images WHERE medicineId = :medicineId")
    suspend fun deleteImages(medicineId: Long)
}