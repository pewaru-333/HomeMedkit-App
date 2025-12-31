package ru.application.homemedkit.utils

import androidx.compose.foundation.interaction.Interaction
import androidx.compose.foundation.interaction.MutableInteractionSource
import kotlinx.coroutines.flow.emptyFlow

object EmptyInteractionSource : MutableInteractionSource {
    override suspend fun emit(interaction: Interaction) = Unit

    override fun tryEmit(interaction: Interaction) = true

    override val interactions = emptyFlow<Interaction>()
}