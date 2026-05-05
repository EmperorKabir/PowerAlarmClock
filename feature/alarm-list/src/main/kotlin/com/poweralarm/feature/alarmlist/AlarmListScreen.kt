package com.poweralarm.feature.alarmlist

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.outlined.AccessAlarm
import androidx.compose.material.icons.outlined.AcUnit
import androidx.compose.material.icons.outlined.Air
import androidx.compose.material.icons.outlined.Bedtime
import androidx.compose.material.icons.outlined.CalendarMonth
import androidx.compose.material.icons.outlined.Cloud
import androidx.compose.material.icons.outlined.Commute
import androidx.compose.material.icons.outlined.Fingerprint
import androidx.compose.material.icons.outlined.LocationOn
import androidx.compose.material.icons.outlined.Mic
import androidx.compose.material.icons.outlined.Nfc
import androidx.compose.material.icons.outlined.Psychology
import androidx.compose.material.icons.outlined.QrCodeScanner
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material.icons.outlined.WbSunny
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.poweralarm.core.domain.model.Alarm
import com.poweralarm.core.domain.model.Condition
import com.poweralarm.core.domain.model.DismissalRequirement
import com.poweralarm.core.domain.model.Recurrence
import com.poweralarm.core.domain.model.TimezoneMode
import java.time.LocalTime
import java.time.ZoneId
import java.time.ZonedDateTime

enum class ListFilter(val label: String) {
    ALL("All"),
    TODAY("Today"),
    TOMORROW("Tomorrow"),
    RECURRING("Recurring"),
    SUSPENDED("Suspended"),
}

@OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)
@Composable
fun AlarmListScreen(
    alarms: List<Alarm>,
    onToggle: (Long, Boolean) -> Unit,
    onClick: (Long) -> Unit,
    onAdd: () -> Unit,
    onSearch: () -> Unit = {},
) {
    var filter by remember { mutableStateOf(ListFilter.ALL) }
    val filtered = remember(alarms, filter) { applyFilter(alarms, filter) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Power Alarm Clock", style = MaterialTheme.typography.titleLarge) },
                actions = {
                    IconButton(onClick = onSearch) { Icon(Icons.Outlined.Search, "Search alarms") }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface,
                ),
            )
        },
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = onAdd,
                icon = { Icon(Icons.Filled.Add, "Add alarm") },
                text = { Text("New alarm") },
                containerColor = MaterialTheme.colorScheme.primary,
            )
        },
    ) { padding ->
        Column(
            modifier = Modifier.fillMaxSize().padding(padding),
        ) {
            FilterChipRow(
                current = filter,
                onSelect = { filter = it },
                modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
            )

            if (filtered.isEmpty()) {
                EmptyAlarmsState(onAdd = onAdd)
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 12.dp, vertical = 8.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                ) {
                    items(filtered, key = { it.id }) { alarm ->
                        AlarmCard(alarm = alarm, onToggle = onToggle, onClick = onClick)
                    }
                }
            }
        }
    }
}

@OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)
@Composable
private fun FilterChipRow(
    current: ListFilter,
    onSelect: (ListFilter) -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        ListFilter.entries.forEach { f ->
            FilterChip(
                selected = current == f,
                onClick = { onSelect(f) },
                label = { Text(f.label) },
            )
        }
    }
}

@Composable
private fun AlarmCard(alarm: Alarm, onToggle: (Long, Boolean) -> Unit, onClick: (Long) -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick(alarm.id) },
        colors = CardDefaults.cardColors(
            containerColor = if (alarm.enabled) MaterialTheme.colorScheme.surfaceVariant
            else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f),
        ),
        shape = RoundedCornerShape(20.dp),
    ) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "%02d:%02d".format(alarm.hour, alarm.minute),
                        style = MaterialTheme.typography.displayMedium,
                        fontWeight = FontWeight.Bold,
                        color = if (alarm.enabled) MaterialTheme.colorScheme.onSurface
                        else MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                    Text(
                        text = alarm.label.ifBlank { "Untitled alarm" },
                        style = MaterialTheme.typography.bodyMedium,
                    )
                    Text(
                        text = recurrenceLabel(alarm.recurrence),
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.primary,
                    )
                    timezoneLabel(alarm)?.let { tz ->
                        Text(
                            text = tz,
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.tertiary,
                        )
                    }
                }
                Switch(checked = alarm.enabled, onCheckedChange = { onToggle(alarm.id, it) })
            }
            ConditionStrip(alarm)
        }
    }
}

private fun timezoneLabel(alarm: Alarm): String? {
    if (alarm.timezoneMode !is TimezoneMode.Fixed || alarm.timezoneId.isBlank()) return null
    val device = ZoneId.systemDefault()
    val pinned = runCatching { ZoneId.of(alarm.timezoneId) }.getOrNull() ?: return null
    if (pinned == device) return null
    val now = ZonedDateTime.now()
    val deviceTime = LocalTime.of(alarm.hour, alarm.minute)
        .atDate(now.toLocalDate())
        .atZone(pinned)
        .withZoneSameInstant(device)
        .toLocalTime()
    return "🌍 ${"%02d:%02d".format(alarm.hour, alarm.minute)} ${pinned.id} · here ${"%02d:%02d".format(deviceTime.hour, deviceTime.minute)}"
}

@Composable
private fun ConditionStrip(alarm: Alarm) {
    val icons = mutableListOf<Pair<ImageVector, String>>()
    alarm.conditions.forEach { c ->
        when (c) {
            is Condition.HolidaySkip -> icons += Icons.Outlined.CalendarMonth to "Holiday skip"
            is Condition.WeatherAdvance -> icons += Icons.Outlined.Cloud to "Weather advance"
            is Condition.AqiAdvance -> icons += Icons.Outlined.Air to "AQI advance"
            is Condition.TrafficAdvance -> icons += Icons.Outlined.Commute to "Traffic"
            is Condition.TflDisruption -> icons += Icons.Outlined.Commute to "TfL"
            is Condition.Geofence -> icons += Icons.Outlined.LocationOn to "Geofence"
            is Condition.BedtimePenalty -> icons += Icons.Outlined.Bedtime to "Bedtime penalty"
            is Condition.LowBatteryFailsafe -> icons += Icons.Outlined.AcUnit to "Battery failsafe"
            is Condition.CalendarShift -> icons += Icons.Outlined.CalendarMonth to "Calendar shift"
            else -> Unit
        }
    }
    alarm.dismissalRequirements.forEach { d ->
        when (d) {
            is DismissalRequirement.Cognitive -> icons += Icons.Outlined.Psychology to "Cognitive"
            is DismissalRequirement.Voice -> icons += Icons.Outlined.Mic to "Voice"
            is DismissalRequirement.Nfc -> icons += Icons.Outlined.Nfc to "NFC"
            is DismissalRequirement.Qr -> icons += Icons.Outlined.QrCodeScanner to "QR"
            is DismissalRequirement.Steps -> icons += Icons.Outlined.AccessAlarm to "Steps"
            is DismissalRequirement.EyesOpenSelfie -> icons += Icons.Outlined.WbSunny to "Selfie"
            is DismissalRequirement.Distress -> icons += Icons.Outlined.Fingerprint to "Distress"
            else -> Unit
        }
    }
    if (icons.isEmpty()) return
    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
        icons.take(8).forEach { (iv, desc) ->
            Box(
                modifier = Modifier
                    .size(28.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.primaryContainer),
                contentAlignment = Alignment.Center,
            ) {
                Icon(iv, contentDescription = desc, modifier = Modifier.size(16.dp))
            }
        }
    }
}

private fun recurrenceLabel(r: Recurrence): String = when (r) {
    is Recurrence.Once -> "Once"
    is Recurrence.Daily -> "Every day"
    is Recurrence.Weekly -> r.daysOfWeek.sorted().joinToString(" · ") { dayLabel(it) }
    is Recurrence.SolarAnchored -> "Sunrise ${if (r.offsetMin >= 0) "+" else ""}${r.offsetMin}m"
    is Recurrence.Adhan -> "Adhan · ${r.prayer}"
    is Recurrence.Polyphasic -> "Polyphasic · ${r.templateId}"
    is Recurrence.ShiftPattern -> "Shift rotation"
    is Recurrence.Chained -> "Chained #${r.parentId}"
    is Recurrence.Cron -> "Cron · ${r.expr}"
}

private fun dayLabel(dow: Int): String = when (dow) {
    1 -> "Mon"; 2 -> "Tue"; 3 -> "Wed"; 4 -> "Thu"; 5 -> "Fri"; 6 -> "Sat"; 7 -> "Sun"; else -> "?"
}

@Composable
private fun EmptyAlarmsState(onAdd: () -> Unit) {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(12.dp),
            modifier = Modifier.padding(32.dp),
        ) {
            Icon(
                Icons.Outlined.AccessAlarm,
                contentDescription = null,
                modifier = Modifier.size(96.dp),
                tint = MaterialTheme.colorScheme.primary,
            )
            Text("No alarms yet", style = MaterialTheme.typography.headlineSmall)
            Text(
                "Add your first alarm with the button below.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            ExtendedFloatingActionButton(
                onClick = onAdd,
                icon = { Icon(Icons.Filled.Add, null) },
                text = { Text("Add your first alarm") },
            )
        }
    }
}

private fun applyFilter(alarms: List<Alarm>, filter: ListFilter): List<Alarm> = when (filter) {
    ListFilter.ALL -> alarms
    ListFilter.TODAY -> alarms.filter { it.recurrence is Recurrence.Daily || it.recurrence is Recurrence.Weekly }
    ListFilter.TOMORROW -> alarms
    ListFilter.RECURRING -> alarms.filter { it.recurrence !is Recurrence.Once }
    ListFilter.SUSPENDED -> alarms.filter { !it.enabled }
}
