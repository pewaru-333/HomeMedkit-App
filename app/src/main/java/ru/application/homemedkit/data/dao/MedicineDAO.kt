package ru.application.homemedkit.data.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import kotlinx.coroutines.flow.Flow
import ru.application.homemedkit.data.dto.Medicine

@Dao
interface MedicineDAO {
    // ============================== Queries ==============================
    @Query("SELECT * FROM medicines")
    fun getAll(): List<Medicine>

    @Transaction
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

    @Query("SELECT image FROM medicines")
    fun getAllImages(): List<String>

    @Query("UPDATE medicines SET prodAmount = prodAmount - :amount WHERE id = :id")
    fun intakeMedicine(id: Long, amount: Double)

    @Query("UPDATE medicines SET prodAmount = prodAmount + :amount WHERE id = :id")
    fun untakeMedicine(id: Long, amount: Double)

    // ============================== Insert ==============================
    @Insert
    suspend fun add(medicine: Medicine): Long

    // ============================== Update ==============================
    @Update
    suspend fun update(medicine: Medicine)

    // ============================== Delete ==============================
    @Delete
    suspend fun delete(medicine: Medicine)
}