package ru.application.homemedkit.data.model

data class IntakePast(
    override val epochDay: Long,
    override val date: String,
    override val intakes: List<TakenModel>
) : IntakeListScheme<TakenModel>