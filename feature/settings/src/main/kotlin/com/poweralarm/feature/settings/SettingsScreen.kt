package com.poweralarm.feature.settings

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.poweralarm.core.settings.SettingDescriptor

/**
 * Generic settings UI: iterates the registry and renders one row per descriptor.
 * Adding a new feature variable means registering a descriptor — never editing this file.
 */
@Composable
fun SettingsScreen(
    descriptors: List<SettingDescriptor<*>>,
    onChange: (id: String, value: Any) -> Unit,
) {
    val grouped = remember(descriptors) { descriptors.groupBy { it.groupPath.substringBefore('.') } }

    LazyColumn(
        modifier = Modifier.fillMaxSize().padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        grouped.forEach { (group, list) ->
            item {
                Text(group, style = MaterialTheme.typography.titleMedium)
            }
            items(list) { descriptor ->
                DescriptorRow(descriptor, onChange)
            }
        }
    }
}

@Composable
private fun DescriptorRow(descriptor: SettingDescriptor<*>, onChange: (String, Any) -> Unit) {
    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        Text(descriptor.label)
        when (descriptor) {
            is SettingDescriptor.BoolSetting -> {
                var v by remember { mutableStateOf(descriptor.default) }
                Switch(checked = v, onCheckedChange = { v = it; onChange(descriptor.id, it) })
            }
            is SettingDescriptor.IntSetting -> {
                var v by remember { mutableStateOf(descriptor.default.toString()) }
                OutlinedTextField(value = v, onValueChange = {
                    v = it
                    it.toIntOrNull()?.takeIf { i -> i in descriptor.range }?.let { onChange(descriptor.id, it.toInt()) }
                })
            }
            is SettingDescriptor.FloatSetting -> {
                var v by remember { mutableStateOf(descriptor.default.toString()) }
                OutlinedTextField(value = v, onValueChange = {
                    v = it
                    it.toFloatOrNull()
                        ?.takeIf { f -> f in descriptor.rangeStart..descriptor.rangeEndInclusive }
                        ?.let { onChange(descriptor.id, it.toFloat()) }
                })
            }
            is SettingDescriptor.StringSetting,
            is SettingDescriptor.ColorSetting,
            is SettingDescriptor.JsonSetting,
            -> {
                var v by remember { mutableStateOf(descriptor.default.toString()) }
                OutlinedTextField(value = v, onValueChange = { v = it; onChange(descriptor.id, it) })
            }
            is SettingDescriptor.EnumSetting -> {
                var v by remember { mutableStateOf(descriptor.default) }
                OutlinedTextField(value = v, onValueChange = {
                    v = it
                    if (it in descriptor.choices) onChange(descriptor.id, it)
                })
                Text("Choices: ${descriptor.choices.joinToString()}", style = MaterialTheme.typography.bodySmall)
            }
        }
    }
}
