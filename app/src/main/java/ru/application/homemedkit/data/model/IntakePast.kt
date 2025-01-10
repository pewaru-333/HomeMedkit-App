package ru.application.homemedkit.data.model

import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.snapshots.SnapshotStateList
import ru.application.homemedkit.data.dto.IntakeTaken

data class IntakePast(
    override val date: Long = 0L,
    override val intakes: SnapshotStateList<IntakeTaken> = mutableStateListOf()
) : IntakeListScheme<IntakeTaken>