package ru.application.homemedkit.databaseController.repositories

import ru.application.homemedkit.databaseController.Intake
import ru.application.homemedkit.databaseController.IntakeDAO

class IntakeRepository(private val intakeDAO: IntakeDAO) {
    fun getByPK(intakeId: Long): Intake = intakeDAO.getByPK(intakeId)
}