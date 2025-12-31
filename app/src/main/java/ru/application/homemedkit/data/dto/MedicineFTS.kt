package ru.application.homemedkit.data.dto

import androidx.room.Entity
import androidx.room.Fts4
import androidx.room.FtsOptions

@Entity(tableName = "medicines_fts")
@Fts4(
    tokenizer = FtsOptions.TOKENIZER_UNICODE61,
    contentEntity = Medicine::class,
    prefix = [1, 2, 3]
)
data class MedicineFTS(
    val productName: String,
    val nameAlias: String,
    val prodFormNormName: String,
    val structure: String,
    val phKinetics: String,
    val comment: String
)
