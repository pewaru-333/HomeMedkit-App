package ru.application.homemedkit.models.viewModels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update

abstract class BaseViewModel<State, Event> : ViewModel() {
    private val _state = MutableStateFlow(initState())
    val state = _state
        .onStart { loadData() }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000L), initState())

    protected val currentState: State
        get() = _state.value

    protected fun updateState(update: (State) -> State) = _state.update(update)

    protected abstract fun initState(): State
    protected abstract fun loadData()
    abstract fun onEvent(event: Event)
}