package ru.application.homemedkit.utils

object MimeType {
    const val IMAGES = "image/"
    const val ZIP = "application/zip"

    object Database {
        const val DB_SQLITE_STREAM = "application/octet-stream"
        const val DB_SQLITE_VND = "application/vnd.sqlite3"
        const val DB_SQLITE_X = "application/x-sqlite3"

        val array = arrayOf(DB_SQLITE_STREAM, DB_SQLITE_VND, DB_SQLITE_X)
    }
}