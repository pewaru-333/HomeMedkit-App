package ru.application.homemedkit.databaseController

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update

@Dao
interface MedicineDAO{
    // ============================== Queries ==============================
    @Query("SELECT * FROM medicines")
    fun getAll(): List<Medicine>

    @Query("SELECT productName FROM medicines WHERE id=:medicineId")
    fun getProductName(medicineId: Long): String

    @Query("SELECT prodAmount FROM medicines WHERE id=:medicineId")
    fun getProdAmount(medicineId: Long): Double

    @Query("SELECT id FROM medicines where cis=:cis")
    fun getIDbyCis(cis: String?): Long

    @Query("SELECT * FROM medicines WHERE id = :id ")
    fun getByPK(id: Long): Medicine?

    @Query("SELECT cis from medicines")
    fun getAllCIS(): List<String>

    @Query("UPDATE medicines SET prodAmount = prodAmount-:amount WHERE id = :id")
    fun intakeMedicine(id: Long, amount: Double)

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