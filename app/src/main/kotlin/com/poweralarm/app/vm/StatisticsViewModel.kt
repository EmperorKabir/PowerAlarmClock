package com.poweralarm.app.vm

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.poweralarm.core.data.db.DismissalEventDao
import com.poweralarm.feature.statistics.DismissalRow
import com.poweralarm.feature.statistics.StatisticsState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import javax.inject.Inject

@HiltViewModel
class StatisticsViewModel @Inject constructor(
    private val dao: DismissalEventDao,
) : ViewModel() {

    private val isoFmt = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")

    val state: StateFlow<StatisticsState> = dao.observeRecent(RECENT_LIMIT)
        .map { events ->
            val rows = events.map { e ->
                val seconds = e.dismissedAt?.let { ((it - e.firedAt) / SEC_TO_MS).toInt() } ?: 0
                DismissalRow(
                    firedAtIso = isoFmt.format(Instant.ofEpochMilli(e.firedAt).atZone(ZoneId.systemDefault())),
                    secondsToDismiss = seconds,
                    snoozeCount = e.snoozeCount,
                    requirementsCompleted = emptyList(),
                )
            }
            val avg = if (rows.isEmpty()) 0 else rows.sumOf { it.secondsToDismiss } / rows.size
            StatisticsState(
                totalAlarmsThisWeek = rows.size,
                avgSecondsToDismiss = avg,
                mostUsedRequirement = null,
                rows = rows,
            )
        }
        .stateIn(
            viewModelScope,
            SharingStarted.WhileSubscribed(5_000),
            StatisticsState(0, 0, null, emptyList()),
        )

    private companion object {
        const val RECENT_LIMIT = 50
        const val SEC_TO_MS = 1_000L
    }
}
