package ru.application.homemedkit.network

fun interface IProgress {
    fun onProgress(completed: Int, total: Int, percent: Int?)
}
