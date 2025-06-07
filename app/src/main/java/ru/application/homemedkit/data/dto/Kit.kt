package ru.application.homemedkit.data.dto

import androidx.room.Entity
import androidx.room.PrimaryKey
import ru.application.homemedkit.utils.BLANK

@Entity(tableName = "kits")
data class Kit(
    @PrimaryKey(autoGenerate = true)
    val kitId: Long = 0L,
    val title: String = BLANK,
    val position: Long = 0L
)
