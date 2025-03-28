package ru.application.homemedkit.data.model

data class IntakeSchedule(
    override val epochDay: Long,
    override val date: String,
    override val intakes: List<ScheduleModel>
) : IntakeListScheme<ScheduleModel>
