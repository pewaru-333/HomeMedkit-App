package ru.application.homemedkit.databaseController

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

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