package ru.application.homemedkit.data.model

import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.snapshots.SnapshotStateList
import ru.application.homemedkit.data.dto.IntakeTaken

data class IntakePast(
    val date: Long = 0L,
    val intakes: SnapshotStateList<IntakeTaken> = mutableStateListOf()
)