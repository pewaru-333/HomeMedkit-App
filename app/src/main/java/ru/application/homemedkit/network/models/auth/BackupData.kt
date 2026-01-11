package ru.application.homemedkit.network.models.auth

import kotlinx.serialization.Serializable

@Serializable
data class BackupData<T>(
    val version: Int,
    val data: T
)
