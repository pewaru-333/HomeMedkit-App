package ru.application.homemedkit.data.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow
import ru.application.homemedkit.data.dto.Kit

@Dao
interface KitDAO {
    // ============================== Queries ==============================
    @Query("SELECT * FROM kits")
    fun getAll(): List<Kit>

    @Query("SELECT * FROM kits")
    fun getFlow(): Flow<List<Kit>>

    // ============================== Insert ==============================
    @Insert
    fun add(kit: Kit): Long

    // ============================== Update ==============================
    @Update
    fun update(kit: Kit)

    // ============================== Delete ==============================
    @Delete
    fun delete(kit: Kit)
}