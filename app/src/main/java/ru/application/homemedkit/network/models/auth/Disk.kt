package ru.application.homemedkit.network.models.auth

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class Disk(
    @SerialName("trash_size")
    val trashSize: Long,
    @SerialName("total_space")
    val totalSpace: Long,
    @SerialName("used_space")
    val usedSpace: Long
)
