package com.poweralarm.app.vm

import androidx.compose.ui.graphics.Color
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.poweralarm.core.domain.port.AlarmRepository
import com.poweralarm.core.settings.SettingsStore
import com.poweralarm.feature.profiles.ProfileSummary
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ProfilesViewModel @Inject constructor(
    private val alarmRepo: AlarmRepository,
    private val store: SettingsStore,
) : ViewModel() {

    data class State(val profiles: List<ProfileSummary>, val active: String)

    val state: StateFlow<State> = combine(
        alarmRepo.observeAll(),
        store.observe<String>("activeProfile"),
    ) { alarms, active ->
        val byProfile = alarms.groupBy { it.profileId }
        State(
            profiles = listOf(
                ProfileSummary("default", "Default", "🌅", byProfile["default"]?.size ?: 0, Color(0xFF00C2B8)),
                ProfileSummary("work", "Work", "💼", byProfile["work"]?.size ?: 0, Color(0xFFFF7043)),
                ProfileSummary("holiday", "Holiday", "🏖️", byProfile["holiday"]?.size ?: 0, Color(0xFFFFEB3B)),
                ProfileSummary("travel", "Travel", "✈️", byProfile["travel"]?.size ?: 0, Color(0xFF9575CD)),
            ),
            active = active,
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), State(emptyList(), "default"))

    fun activate(id: String) {
        viewModelScope.launch { runCatching { store.set("activeProfile", id) } }
    }
}
