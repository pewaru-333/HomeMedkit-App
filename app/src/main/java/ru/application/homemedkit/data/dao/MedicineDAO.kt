package ru.application.homemedkit.data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Transaction
import kotlinx.coroutines.flow.Flow
import ru.application.homemedkit.data.dto.Image
import ru.application.homemedkit.data.dto.Medicine

@Dao
interface MedicineDAO : BaseDAO<Medicine> {
    // ============================== Queries ==============================
    @Query("SELECT * FROM medicines")
    fun getAll(): List<Medicine>

    @Query("SELECT * FROM medicines")
    fun getFlow(): Flow<List<Medicine>>

    @Query("SELECT productName FROM medicines WHERE id = :medicineId")
    fun getProductName(medicineId: Long): String

    @Query("SELECT id FROM medicines where cis = :cis")
    fun getIdByCis(cis: String): Long

    @Query("SELECT * FROM medicines WHERE id = :id ")
    fun getById(id: Long): Medicine?

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

    // ============================== Update ==============================
    @Transaction
    suspend fun updateImages(vararg images: Image) {
        deleteImages(images.first().medicineId)
        images.forEach { addImage(it) }
    }

    // ============================== Delete ==============================
    @Query("DELETE FROM images WHERE medicineId = :medicineId")
    suspend fun deleteImages(medicineId: Long)
}