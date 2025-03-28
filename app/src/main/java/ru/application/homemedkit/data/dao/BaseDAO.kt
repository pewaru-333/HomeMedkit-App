package ru.application.homemedkit.data.dao

import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Update
import androidx.room.Upsert

interface BaseDAO<T> {
    @Insert
    suspend fun insert(item: T): Long

    @Insert
    suspend fun insert(items: Iterable<T>)

    @Update
    suspend fun update(item: T)

    @Upsert
    suspend fun upsert(item: T)

    @Delete
    suspend fun delete(item: T)

    @Delete
    suspend fun delete(items: Iterable<T>)
}