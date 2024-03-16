package ru.application.homemedkit.helpers

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider

fun <viewModel : ViewModel> viewModelFactory(initializer: () -> viewModel): ViewModelProvider.Factory {
    return object : ViewModelProvider.Factory {
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return initializer() as T
        }
    }
}