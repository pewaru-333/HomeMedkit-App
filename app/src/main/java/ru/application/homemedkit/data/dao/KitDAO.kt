package ru.application.homemedkit.data.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Upsert
import kotlinx.coroutines.flow.Flow
import ru.application.homemedkit.data.dto.Kit
import ru.application.homemedkit.data.dto.MedicineKit

@Dao
interface KitDAO {
    // ============================== Queries ==============================
    @Query("SELECT * FROM kits")
    fun getAll(): List<Kit>

    @Query("SELECT * FROM kits")
    fun getFlow(): Flow<List<Kit>>

    @Query("SELECT * FROM medicines_kits")
    fun getMedicinesKits(): Flow<List<MedicineKit>>

    @Query("SELECT kitId FROM medicines_kits WHERE medicineId = :medicineId")
    fun getIdList(medicineId: Long): List<Long>

    @Query("SELECT title FROM kits WHERE kitId = :kitId")
    fun getTitle(kitId: Long): String

    @Query("SELECT title FROM kits WHERE kitId IN (SELECT kitId from medicines_kits WHERE medicineId = :medicineId)")
    fun getTitleByMedicine(medicineId: Long): List<String>

    @Query("DELETE FROM medicines_kits WHERE medicineId = :medicineId")
    fun deleteAll(medicineId: Long)

    // ============================== Insert ==============================
    @Upsert
    fun add(kit: Kit): Long

    @Insert
    fun pinKit(kit: MedicineKit): Long

    // ============================== Delete ==============================
    @Delete
    fun delete(kit: Kit)
}