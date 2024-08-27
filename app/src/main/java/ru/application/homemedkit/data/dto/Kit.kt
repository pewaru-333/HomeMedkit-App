package ru.application.homemedkit.data.dto

import androidx.room.Entity
import androidx.room.PrimaryKey
import ru.application.homemedkit.helpers.BLANK

@Entity(tableName = "kits")
data class Kit(
    @PrimaryKey(autoGenerate = true)
    val kitId: Long = 0L,
    val title: String = BLANK
)
