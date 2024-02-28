package ru.application.homemedkit.viewModels.factories

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import ru.application.homemedkit.databaseController.repositories.IntakeRepository
import ru.application.homemedkit.viewModels.IntakeViewModel

class IntakeViewModelFactory(private val repository: IntakeRepository, private val intakeId: Long) :
    ViewModelProvider.NewInstanceFactory() {
    override fun <T : ViewModel> create(modelClass: Class<T>): T =
        IntakeViewModel(repository, intakeId) as T
}