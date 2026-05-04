package com.poweralarm.feature.settings

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Slider
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.poweralarm.core.settings.SettingDescriptor

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    descriptors: List<SettingDescriptor<*>>,
    onChange: (id: String, value: Any) -> Unit,
) {
    var query by remember { mutableStateOf("") }
    var openGroup by remember { mutableStateOf<String?>(null) }

    val grouped = remember(descriptors) {
        descriptors.groupBy { it.groupPath.substringBefore('.') }.toSortedMap()
    }
    val results = remember(descriptors, query) {
        if (query.isBlank()) emptyList()
        else descriptors.filter {
            it.id.contains(query, ignoreCase = true) ||
                it.label.contains(query, ignoreCase = true) ||
                it.helpText.contains(query, ignoreCase = true) ||
                it.groupPath.contains(query, ignoreCase = true)
        }
    }

    Scaffold(
        topBar = { TopAppBar(title = { Text("Settings") }) },
    ) { padding ->
        Column(
            modifier = Modifier.fillMaxSize().padding(padding).padding(horizontal = 12.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            OutlinedTextField(
                value = query,
                onValueChange = { query = it },
                placeholder = { Text("Search 165+ settings…") },
                leadingIcon = { Icon(Icons.Outlined.Search, null) },
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
            )

            when {
                query.isNotBlank() -> SearchResults(results, onChange)
                openGroup == null -> GroupList(grouped) { openGroup = it }
                else -> GroupDetail(
                    group = openGroup!!,
                    descriptors = grouped[openGroup].orEmpty(),
                    onBack = { openGroup = null },
                    onChange = onChange,
                )
            }
        }
    }
}

@Composable
private fun GroupList(grouped: Map<String, List<SettingDescriptor<*>>>, onOpen: (String) -> Unit) {
    LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxSize()) {
        items(grouped.entries.toList(), key = { it.key }) { (group, list) ->
            Card(
                modifier = Modifier.fillMaxWidth().clickable { onOpen(group) },
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                shape = RoundedCornerShape(20.dp),
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            group.replaceFirstChar { it.uppercase() },
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.SemiBold,
                        )
                        Text(
                            "${list.size} setting${if (list.size == 1) "" else "s"}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                    Icon(Icons.Filled.ChevronRight, null)
                }
            }
        }
    }
}

@Composable
private fun GroupDetail(
    group: String,
    descriptors: List<SettingDescriptor<*>>,
    onBack: () -> Unit,
    onChange: (String, Any) -> Unit,
) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxSize()) {
        Box(
            modifier = Modifier.fillMaxWidth().clickable { onBack() }.padding(vertical = 4.dp),
        ) { Text("← Back to all settings", color = MaterialTheme.colorScheme.primary) }
        Text(group.replaceFirstChar { it.uppercase() }, style = MaterialTheme.typography.headlineSmall)
        LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp), modifier = Modifier.fillMaxSize()) {
            items(descriptors, key = { it.id }) { d -> DescriptorRow(d, onChange) }
        }
    }
}

@Composable
private fun SearchResults(results: List<SettingDescriptor<*>>, onChange: (String, Any) -> Unit) {
    if (results.isEmpty()) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text("No matches", color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        return
    }
    LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp), modifier = Modifier.fillMaxSize()) {
        items(results, key = { it.id }) { d -> DescriptorRow(d, onChange) }
    }
}

@Composable
private fun DescriptorRow(descriptor: SettingDescriptor<*>, onChange: (String, Any) -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
    ) {
        Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Text(descriptor.label, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold)
            Text(
                descriptor.groupPath,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            DescriptorEditor(descriptor, onChange)
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
private fun DescriptorEditor(descriptor: SettingDescriptor<*>, onChange: (String, Any) -> Unit) {
    when (descriptor) {
        is SettingDescriptor.BoolSetting -> {
            var v by remember(descriptor.id) { mutableStateOf(descriptor.default) }
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text("Enabled", modifier = Modifier.weight(1f))
                Switch(checked = v, onCheckedChange = { v = it; onChange(descriptor.id, it) })
            }
        }
        is SettingDescriptor.IntSetting -> {
            var v by remember(descriptor.id) { mutableStateOf(descriptor.default.toFloat()) }
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(v.toInt().toString(), style = MaterialTheme.typography.labelLarge, modifier = Modifier.size(48.dp))
                Slider(
                    value = v,
                    onValueChange = { v = it; onChange(descriptor.id, it.toInt()) },
                    valueRange = descriptor.range.first.toFloat()..descriptor.range.last.toFloat(),
                    modifier = Modifier.weight(1f),
                )
            }
        }
        is SettingDescriptor.FloatSetting -> {
            var v by remember(descriptor.id) { mutableStateOf(descriptor.default) }
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text("%.2f".format(v), modifier = Modifier.size(56.dp))
                Slider(
                    value = v,
                    onValueChange = { v = it; onChange(descriptor.id, it) },
                    valueRange = descriptor.rangeStart..descriptor.rangeEndInclusive,
                    modifier = Modifier.weight(1f),
                )
            }
        }
        is SettingDescriptor.ColorSetting -> {
            var v by remember(descriptor.id) { mutableStateOf(descriptor.default) }
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(28.dp)
                        .clip(RoundedCornerShape(6.dp))
                        .background(parseHex(v)),
                )
                OutlinedTextField(
                    value = v,
                    onValueChange = { v = it; if (HEX.matches(it)) onChange(descriptor.id, it) },
                    singleLine = true,
                    modifier = Modifier.weight(1f).padding(start = 8.dp),
                )
            }
        }
        is SettingDescriptor.EnumSetting -> {
            var v by remember(descriptor.id) { mutableStateOf(descriptor.default) }
            FlowRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                descriptor.choices.forEach { choice ->
                    FilterChip(
                        selected = v == choice,
                        onClick = { v = choice; onChange(descriptor.id, choice) },
                        label = { Text(choice) },
                    )
                }
            }
        }
        is SettingDescriptor.StringSetting,
        is SettingDescriptor.JsonSetting,
        -> {
            var v by remember(descriptor.id) { mutableStateOf(descriptor.default.toString()) }
            OutlinedTextField(
                value = v,
                onValueChange = { v = it; onChange(descriptor.id, it) },
                modifier = Modifier.fillMaxWidth(),
                singleLine = descriptor is SettingDescriptor.StringSetting,
            )
        }
    }
}

private val HEX = Regex("^#([0-9A-Fa-f]{6}|[0-9A-Fa-f]{8})$")

private fun parseHex(hex: String): Color {
    if (!HEX.matches(hex)) return Color.Gray
    val raw = hex.removePrefix("#")
    val argb = when (raw.length) {
        6 -> 0xFF_00_00_00.toInt() or raw.toInt(16)
        8 -> raw.toLong(16).toInt()
        else -> return Color.Gray
    }
    return Color(argb)
}
