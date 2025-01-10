package ru.application.homemedkit.data.model

interface IntakeListScheme<T> {
    val date: Long
    val intakes: List<T>
}