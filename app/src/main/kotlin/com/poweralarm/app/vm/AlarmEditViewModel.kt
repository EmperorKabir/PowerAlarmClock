package com.poweralarm.app.vm

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.poweralarm.core.domain.model.Alarm
import com.poweralarm.core.domain.port.AlarmRepository
import com.poweralarm.core.scheduler.AlarmScheduler
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AlarmEditViewModel @Inject constructor(
    private val repo: AlarmRepository,
    private val scheduler: AlarmScheduler,
) : ViewModel() {

    private val _editing = MutableStateFlow<Alarm?>(null)
    val editing: StateFlow<Alarm?> = _editing.asStateFlow()

    fun startNew() {
        _editing.value = Alarm(id = 0L, hour = 7, minute = 0)
    }

    fun startEdit(id: Long) {
        viewModelScope.launch {
            _editing.value = repo.byId(id) ?: Alarm(id = id, hour = 7, minute = 0)
        }
    }

    fun cancel() { _editing.value = null }

    fun save(alarm: Alarm) {
        viewModelScope.launch {
            val id = repo.save(alarm)
            val saved = alarm.copy(id = id)
            runCatching { scheduler.schedule(saved) }
            _editing.value = null
        }
    }
}
