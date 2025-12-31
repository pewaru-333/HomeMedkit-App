package ru.application.homemedkit.ui.navigation

import androidx.navigation3.runtime.NavKey

class Navigator(val state: NavigationState) {
    fun navigate(route: NavKey) {
        if (route in state.backStacks.keys) {
            state.topLevelRoute = route
        } else {
            state.backStacks[state.topLevelRoute]?.add(route)
        }
    }

    fun navigateAndClearStack(route: NavKey) {
        state.backStacks[state.topLevelRoute]?.retainAll(setOf(route))
    }

    fun goBack() {
        val currentStack = state.backStacks[state.topLevelRoute] ?: throw IllegalStateException()
        val currentRoute = currentStack.last()

        if (currentRoute == state.topLevelRoute) {
            state.topLevelRoute = state.startRoute
        } else {
            currentStack.removeLastOrNull()
        }
    }
}