package com.poweralarm.feature.statistics

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

data class DismissalRow(
    val firedAtIso: String,
    val secondsToDismiss: Int,
    val snoozeCount: Int,
    val requirementsCompleted: List<String>,
)

data class StatisticsState(
    val totalAlarmsThisWeek: Int,
    val avgSecondsToDismiss: Int,
    val mostUsedRequirement: String?,
    val rows: List<DismissalRow>,
)

@Composable
fun StatisticsScreen(state: StatisticsState) {
    Column(
        modifier = Modifier.fillMaxSize().padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Text("Wake-up forensics", style = MaterialTheme.typography.headlineSmall)
        Text("Alarms this week: ${state.totalAlarmsThisWeek}")
        Text("Avg seconds to dismiss: ${state.avgSecondsToDismiss}")
        state.mostUsedRequirement?.let { Text("Most-used requirement: $it") }
        LazyColumn(verticalArrangement = Arrangement.spacedBy(4.dp)) {
            items(state.rows) { row ->
                Text("${row.firedAtIso} — ${row.secondsToDismiss}s, snoozes=${row.snoozeCount}")
            }
        }
    }
}
