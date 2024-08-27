package ru.application.homemedkit.data.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import ru.application.homemedkit.data.dto.Medicine

@Dao
interface MedicineDAO {
    // ============================== Queries ==============================
    @Query("SELECT * FROM medicines")
    fun getAll(): List<Medicine>

    @Query("SELECT productName FROM medicines WHERE id = :medicineId")
    fun getProductName(medicineId: Long): String

    @Query("SELECT prodAmount FROM medicines WHERE id = :medicineId")
    fun getProdAmount(medicineId: Long): Double

    @Query("SELECT id FROM medicines where cis = :cis")
    fun getIdbyCis(cis: String?): Long

    @Query("SELECT * FROM medicines WHERE id = :id ")
    fun getById(id: Long): Medicine?

    @Query("SELECT title FROM kits WHERE kitId = :kitId")
    fun getKitTitle(kitId: Long?): String?

    @Query("SELECT * FROM medicines WHERE kitId = :kitId")
    fun getByKitId(kitId: Long?): List<Medicine>

    @Query("SELECT cis from medicines")
    fun getAllCIS(): List<String>

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