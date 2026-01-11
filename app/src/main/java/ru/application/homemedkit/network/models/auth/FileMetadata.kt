package ru.application.homemedkit.network.models.auth

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import java.time.OffsetDateTime

@Serializable
data class FileMetadata(
    val name: String?,
    val md5: String?,
    val created: String,
    val modified: String,
    @SerialName("_embedded")
    val embedded: ResourceList?
) {
    @Serializable
    data class ResourceList(
        val items: List<FileMetadata>
    )

    data class FileMetadataSimple(
        val name: String,
        val md5: String,
        val created: Long,
        val modified: Long
    )

    fun mapper() = FileMetadataSimple(
        name = name.orEmpty(),
        md5 = md5.orEmpty(),
        created = OffsetDateTime.parse(created).toInstant().toEpochMilli(),
        modified = OffsetDateTime.parse(modified).toInstant().toEpochMilli()
    )
}
