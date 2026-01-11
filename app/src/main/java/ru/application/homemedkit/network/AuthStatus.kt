package ru.application.homemedkit.network

sealed interface AuthStatus {
    data object Error : AuthStatus
    data object Nothing : AuthStatus
    data object Loading : AuthStatus
    data object Success : AuthStatus
}