package ru.application.homemedkit.data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import kotlinx.coroutines.flow.Flow
import ru.application.homemedkit.data.dto.Kit
import ru.application.homemedkit.data.dto.MedicineKit

@Dao
interface KitDAO : BaseDAO<Kit> {
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

    @Query("DELETE FROM medicines_kits WHERE medicineId = :medicineId")
    fun deleteAll(medicineId: Long)

    // ============================== Insert ==============================
    @Insert
    fun pinKit(vararg kit: MedicineKit)
}