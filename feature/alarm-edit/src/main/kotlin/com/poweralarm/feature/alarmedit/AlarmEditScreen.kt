package com.poweralarm.feature.alarmedit

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.poweralarm.core.domain.model.Alarm

@Composable
fun AlarmEditScreen(
    initial: Alarm,
    onSave: (Alarm) -> Unit,
) {
    var hour by remember { mutableIntStateOf(initial.hour) }
    var minute by remember { mutableIntStateOf(initial.minute) }
    var label by remember { mutableStateOf(initial.label) }
    var enabled by remember { mutableStateOf(initial.enabled) }

    Column(
        modifier = Modifier.fillMaxSize().padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Text("Edit alarm")
        OutlinedTextField(
            value = "%02d".format(hour),
            onValueChange = { it.toIntOrNull()?.takeIf { v -> v in 0..23 }?.let { hour = it.toInt() } },
            label = { Text("Hour") },
        )
        OutlinedTextField(
            value = "%02d".format(minute),
            onValueChange = { it.toIntOrNull()?.takeIf { v -> v in 0..59 }?.let { minute = it.toInt() } },
            label = { Text("Minute") },
        )
        OutlinedTextField(value = label, onValueChange = { label = it }, label = { Text("Label") })
        Switch(checked = enabled, onCheckedChange = { enabled = it })
        Button(onClick = {
            onSave(initial.copy(hour = hour, minute = minute, label = label, enabled = enabled))
        }) { Text("Save") }
    }
}
