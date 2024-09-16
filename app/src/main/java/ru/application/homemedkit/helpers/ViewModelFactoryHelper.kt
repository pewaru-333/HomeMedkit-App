package ru.application.homemedkit.helpers

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider.Factory

fun <viewModel : ViewModel> viewModelFactory(initializer: () -> viewModel): Factory =
    object : Factory {
        override fun <T : ViewModel> create(modelClass: Class<T>): T = initializer() as T
    }