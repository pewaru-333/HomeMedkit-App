package ru.application.homemedkit.models.viewModels

import android.net.Uri
import androidx.core.net.toUri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.work.WorkInfo
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.stateIn
import ru.application.homemedkit.ui.navigation.Screen
import ru.application.homemedkit.ui.navigation.utils.DeepLinkMatcher
import ru.application.homemedkit.ui.navigation.utils.DeepLinkPattern
import ru.application.homemedkit.ui.navigation.utils.DeepLinkRequest
import ru.application.homemedkit.ui.navigation.utils.KeyDecoder
import ru.application.homemedkit.utils.DEEP_LINK_FULL_SCREEN
import ru.application.homemedkit.utils.REDIRECT_URI_YANDEX
import ru.application.homemedkit.utils.WORK_AUTO_SYNC
import ru.application.homemedkit.utils.di.Preferences
import ru.application.homemedkit.utils.di.WorkManager
import ru.application.homemedkit.worker.WorkerManager

class MainViewModel : ViewModel() {
    private val _snackbarEvent = Channel<WorkInfo.State>()
    val snackbarEvent = _snackbarEvent.receiveAsFlow()

    val syncWorkState = WorkManager
        .getWorkInfosForUniqueWorkFlow(WORK_AUTO_SYNC)
        .onStart { if (Preferences.isAutoSyncEnabled) WorkerManager.startAutoSyncWork() }
        .map { it.firstOrNull()?.state }
        .onEach { if (Preferences.isAutoSyncEnabled && it != null) _snackbarEvent.send(it) }
        .stateIn(viewModelScope, SharingStarted.Lazily, null)

    fun getDeepLink(data: Uri?): Screen {
        if (data == null) {
            return Preferences.startPage.route
        }

        val deepLinkPatterns = listOf(
            DeepLinkPattern(Screen.Auth.serializer(), REDIRECT_URI_YANDEX.toUri()),
            DeepLinkPattern(Screen.IntakeFullScreen.serializer(), DEEP_LINK_FULL_SCREEN.toUri())
        )

        val request = DeepLinkRequest(data)
        val match = deepLinkPatterns.firstNotNullOfOrNull { pattern ->
            DeepLinkMatcher(request, pattern).match()
        }

        return if (match != null) {
            KeyDecoder(match.args).decodeSerializableValue(match.serializer)
        } else {
            Preferences.startPage.route
        }
    }
}