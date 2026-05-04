package com.poweralarm.feature.alarmlist

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.poweralarm.core.domain.model.Alarm

@Composable
fun AlarmListScreen(
    alarms: List<Alarm>,
    onToggle: (Long, Boolean) -> Unit,
    onClick: (Long) -> Unit,
    onAdd: () -> Unit,
) {
    Scaffold(
        floatingActionButton = {
            FloatingActionButton(onClick = onAdd) { Text("+", style = MaterialTheme.typography.headlineLarge) }
        },
    ) { padding ->
        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(padding),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            items(alarms) { alarm ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onClick(alarm.id) }
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "%02d:%02d".format(alarm.hour, alarm.minute),
                            style = MaterialTheme.typography.headlineMedium,
                        )
                        Text(alarm.label.ifBlank { "Untitled" })
                    }
                    Switch(checked = alarm.enabled, onCheckedChange = { onToggle(alarm.id, it) })
                }
            }
        }
    }
}
