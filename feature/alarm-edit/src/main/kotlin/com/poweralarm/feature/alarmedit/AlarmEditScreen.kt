package com.poweralarm.feature.alarmedit

import androidx.compose.animation.AnimatedVisibility
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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.Save
import androidx.compose.material.icons.outlined.Alarm
import androidx.compose.material.icons.outlined.Block
import androidx.compose.material.icons.outlined.Bolt
import androidx.compose.material.icons.outlined.Cast
import androidx.compose.material.icons.outlined.Cloud
import androidx.compose.material.icons.outlined.Lock
import androidx.compose.material.icons.outlined.MusicNote
import androidx.compose.material.icons.outlined.Psychology
import androidx.compose.material.icons.outlined.Snooze
import androidx.compose.material.icons.outlined.SwapHoriz
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Slider
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
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
import com.poweralarm.core.domain.model.AudioPlan
import com.poweralarm.core.domain.model.AudioSource
import com.poweralarm.core.domain.model.Condition
import com.poweralarm.core.domain.model.DismissalRequirement
import com.poweralarm.core.domain.model.Recurrence
import com.poweralarm.core.domain.model.RingerLayoutPolicy
import com.poweralarm.core.domain.model.SnoozePolicy

private enum class EditSection(val label: String, val icon: ImageVector) {
    TIME("Time & recurrence", Icons.Outlined.Alarm),
    AUDIO("Audio", Icons.Outlined.MusicNote),
    SNOOZE("Snooze", Icons.Outlined.Snooze),
    DISMISSAL("Dismissal", Icons.Outlined.Psychology),
    SKIP("Skip conditions", Icons.Outlined.Block),
    ADVANCE("Advance conditions", Icons.Outlined.Cloud),
    RINGER("Ringer layout", Icons.Outlined.SwapHoriz),
    AUTOMATION("Automation", Icons.Outlined.Bolt),
    SECURITY("Security", Icons.Outlined.Lock),
    CAST("Cast & sync", Icons.Outlined.Cast),
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun AlarmEditScreen(
    initial: Alarm,
    onSave: (Alarm) -> Unit,
    onCancel: () -> Unit = {},
) {
    var hour by remember { mutableIntStateOf(initial.hour) }
    var minute by remember { mutableIntStateOf(initial.minute) }
    var label by remember { mutableStateOf(initial.label) }
    var enabled by remember { mutableStateOf(initial.enabled) }
    var recurrence by remember { mutableStateOf(initial.recurrence) }
    var weeklyDays by remember {
        mutableStateOf((initial.recurrence as? Recurrence.Weekly)?.daysOfWeek ?: setOf(1, 2, 3, 4, 5))
    }
    var requirements by remember { mutableStateOf(initial.dismissalRequirements) }
    var conditions by remember { mutableStateOf(initial.conditions) }
    var audioPlan by remember { mutableStateOf(initial.audioPlan) }
    var snoozePolicy by remember { mutableStateOf(initial.snoozePolicy) }
    var ringerLayout by remember { mutableStateOf(initial.ringerLayout) }
    var expandedSection by remember { mutableStateOf<EditSection?>(EditSection.TIME) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(if (initial.id == 0L) "New alarm" else "Edit alarm") },
                navigationIcon = {
                    IconButton(onClick = onCancel) { Icon(Icons.Filled.Close, "Cancel") }
                },
            )
        },
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = {
                    onSave(
                        initial.copy(
                            hour = hour,
                            minute = minute,
                            label = label,
                            enabled = enabled,
                            recurrence = if (recurrence is Recurrence.Weekly) Recurrence.Weekly(weeklyDays) else recurrence,
                            conditions = conditions,
                            dismissalRequirements = requirements,
                            audioPlan = audioPlan,
                            snoozePolicy = snoozePolicy,
                            ringerLayout = ringerLayout,
                        ),
                    )
                },
                icon = { Icon(Icons.Filled.Save, null) },
                text = { Text("Save") },
            )
        },
    ) { padding ->
        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(padding),
            contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 12.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            item("anchors") {
                AnchorChipRow(current = expandedSection, onSelect = { expandedSection = it })
            }
            EditSection.entries.forEach { section ->
                item(section.name) {
                    Section(
                        section = section,
                        expanded = expandedSection == section,
                        onToggle = { expandedSection = if (expandedSection == section) null else section },
                    ) {
                        when (section) {
                            EditSection.TIME -> TimeSection(
                                hour, minute, label, enabled, recurrence, weeklyDays,
                                onHour = { hour = it },
                                onMinute = { minute = it },
                                onLabel = { label = it },
                                onEnabled = { enabled = it },
                                onRecurrence = { recurrence = it },
                                onWeeklyDays = { weeklyDays = it },
                            )
                            EditSection.AUDIO -> AudioSection(audioPlan) { audioPlan = it }
                            EditSection.SNOOZE -> SnoozeSection(snoozePolicy) { snoozePolicy = it }
                            EditSection.DISMISSAL -> DismissalSection(requirements) { requirements = it }
                            EditSection.SKIP -> SkipSection(conditions) { conditions = it }
                            EditSection.ADVANCE -> AdvanceSection(conditions) { conditions = it }
                            EditSection.RINGER -> RingerSection(ringerLayout) { ringerLayout = it }
                            EditSection.AUTOMATION -> SectionStub("Tasker intents · DND · Network · Smart light/plug. Configure in Settings → automation.")
                            EditSection.SECURITY -> SectionStub("Edit lock · Distress code · SOS contacts. Configure in Settings → security.")
                            EditSection.CAST -> SectionStub("Cast targets · LAN scope · Cloud sync. Configure in Settings → cast / sync.")
                        }
                    }
                }
            }
            item("spacer") { Box(modifier = Modifier.size(80.dp)) }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun AnchorChipRow(current: EditSection?, onSelect: (EditSection) -> Unit) {
    LazyRow(
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        contentPadding = androidx.compose.foundation.layout.PaddingValues(vertical = 4.dp),
    ) {
        EditSection.entries.forEach { section ->
            item(section.name) {
                FilterChip(
                    selected = current == section,
                    onClick = { onSelect(section) },
                    leadingIcon = { Icon(section.icon, null, modifier = Modifier.size(16.dp)) },
                    label = { Text(section.label) },
                )
            }
        }
    }
}

@Composable
private fun Section(
    section: EditSection,
    expanded: Boolean,
    onToggle: () -> Unit,
    content: @Composable () -> Unit,
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
        shape = RoundedCornerShape(20.dp),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable { onToggle() }
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Icon(section.icon, null, modifier = Modifier.size(20.dp))
            Text(
                section.label,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                modifier = Modifier.weight(1f).padding(start = 12.dp),
            )
            Icon(if (expanded) Icons.Filled.ExpandLess else Icons.Filled.ExpandMore, null)
        }
        AnimatedVisibility(visible = expanded) {
            Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)) {
                HorizontalDivider(modifier = Modifier.padding(bottom = 12.dp))
                content()
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun TimeSection(
    hour: Int,
    minute: Int,
    label: String,
    enabled: Boolean,
    recurrence: Recurrence,
    weeklyDays: Set<Int>,
    onHour: (Int) -> Unit,
    onMinute: (Int) -> Unit,
    onLabel: (String) -> Unit,
    onEnabled: (Boolean) -> Unit,
    onRecurrence: (Recurrence) -> Unit,
    onWeeklyDays: (Set<Int>) -> Unit,
) {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            OutlinedTextField(
                value = "%02d".format(hour),
                onValueChange = { it.toIntOrNull()?.takeIf { v -> v in 0..23 }?.let(onHour) },
                label = { Text("Hour") },
                modifier = Modifier.weight(1f),
            )
            OutlinedTextField(
                value = "%02d".format(minute),
                onValueChange = { it.toIntOrNull()?.takeIf { v -> v in 0..59 }?.let(onMinute) },
                label = { Text("Minute") },
                modifier = Modifier.weight(1f),
            )
        }
        OutlinedTextField(value = label, onValueChange = onLabel, label = { Text("Label") }, modifier = Modifier.fillMaxWidth())
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text("Enabled", modifier = Modifier.weight(1f))
            Switch(checked = enabled, onCheckedChange = onEnabled)
        }
        Text("Recurrence", style = MaterialTheme.typography.titleSmall)
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            listOf(
                "Once" to Recurrence.Once,
                "Daily" to Recurrence.Daily,
                "Weekly" to Recurrence.Weekly(weeklyDays),
            ).forEach { (lbl, value) ->
                FilterChip(
                    selected = recurrence::class == value::class,
                    onClick = { onRecurrence(value) },
                    label = { Text(lbl) },
                )
            }
        }
        if (recurrence is Recurrence.Weekly) {
            Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                listOf(1 to "M", 2 to "T", 3 to "W", 4 to "T", 5 to "F", 6 to "S", 7 to "S").forEach { (i, l) ->
                    val sel = i in weeklyDays
                    FilterChip(
                        selected = sel,
                        onClick = { onWeeklyDays(if (sel) weeklyDays - i else weeklyDays + i) },
                        label = { Text(l) },
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun AudioSection(plan: AudioPlan, onChange: (AudioPlan) -> Unit) {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Text("Source", style = MaterialTheme.typography.titleSmall)
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            listOf("System", "Local", "URL", "Spotify", "Drive").forEach { name ->
                FilterChip(
                    selected = sourceName(plan.source) == name,
                    onClick = { onChange(plan.copy(source = sourceFor(name))) },
                    label = { Text(name) },
                )
            }
        }
        SliderRow("Pre-ramp (min)", plan.volumeRampMin.toFloat(), 0f..30f) { onChange(plan.copy(volumeRampMin = it.toInt())) }
        SliderRow("Start volume %", plan.volumeStartPct.toFloat(), 0f..100f) { onChange(plan.copy(volumeStartPct = it.toInt())) }
        SliderRow("End volume %", plan.volumeEndPct.toFloat(), 0f..100f) { onChange(plan.copy(volumeEndPct = it.toInt())) }
        SliderRow("Fade-out (sec)", plan.fadeOutSec.toFloat(), 0f..60f) { onChange(plan.copy(fadeOutSec = it.toInt())) }
    }
}

@Composable
private fun SnoozeSection(policy: SnoozePolicy, onChange: (SnoozePolicy) -> Unit) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        SliderRow("Base (min)", policy.baseMin.toFloat(), 1f..30f) { onChange(policy.copy(baseMin = it.toInt())) }
        SliderRow("Decrement (min)", policy.decrementMin.toFloat(), 0f..10f) { onChange(policy.copy(decrementMin = it.toInt())) }
        SliderRow("Floor (min)", policy.floorMin.toFloat(), 1f..15f) { onChange(policy.copy(floorMin = it.toInt())) }
        SliderRow("Max snoozes", policy.maxCount.toFloat(), 0f..20f) { onChange(policy.copy(maxCount = it.toInt())) }
    }
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
private fun DismissalSection(reqs: List<DismissalRequirement>, onChange: (List<DismissalRequirement>) -> Unit) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text("Required to dismiss the alarm")
        val options = listOf(
            "Tap" to DismissalRequirement.TapButton,
            "Cognitive" to DismissalRequirement.Cognitive(listOf("probability"), "medium", 1),
            "Voice" to DismissalRequirement.Voice("", 0.85f),
            "NFC" to DismissalRequirement.Nfc(""),
            "QR" to DismissalRequirement.Qr(""),
            "Steps" to DismissalRequirement.Steps(30, 5),
            "Selfie" to DismissalRequirement.EyesOpenSelfie(0.7f, 5),
            "Distress" to DismissalRequirement.Distress(""),
        )
        FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            options.forEach { (lbl, req) ->
                val sel = reqs.any { it::class == req::class }
                FilterChip(
                    selected = sel,
                    onClick = {
                        onChange(if (sel) reqs.filterNot { it::class == req::class } else reqs + req)
                    },
                    label = { Text(lbl) },
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
private fun SkipSection(conds: List<Condition>, onChange: (List<Condition>) -> Unit) {
    val skipKinds = listOf(
        "Holiday" to Condition.HolidaySkip("GB", "nager"),
        "Geofence" to Condition.Geofence(0f, 0f, 200, "disable"),
        "ICS skip" to Condition.IcsSkip(emptyList(), ""),
    )
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            skipKinds.forEach { (lbl, cond) ->
                val sel = conds.any { it::class == cond::class }
                FilterChip(
                    selected = sel,
                    onClick = { onChange(if (sel) conds.filterNot { it::class == cond::class } else conds + cond) },
                    label = { Text(lbl) },
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
private fun AdvanceSection(conds: List<Condition>, onChange: (List<Condition>) -> Unit) {
    val advanceKinds = listOf(
        "Weather" to Condition.WeatherAdvance("openweather", 10, 15, 1f, 1f),
        "Traffic" to Condition.TrafficAdvance("google", 0f, 0f, 0f, 0f, 30),
        "AQI" to Condition.AqiAdvance("openaq", 35f, 15),
        "TfL" to Condition.TflDisruption(emptyList(), emptyList(), 20),
        "Calendar" to Condition.CalendarShift(emptyList(), 120, 30),
        "Bedtime" to Condition.BedtimePenalty("23:00", 30, 15),
        "Battery" to Condition.LowBatteryFailsafe(15, true, 30),
    )
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            advanceKinds.forEach { (lbl, cond) ->
                val sel = conds.any { it::class == cond::class }
                FilterChip(
                    selected = sel,
                    onClick = { onChange(if (sel) conds.filterNot { it::class == cond::class } else conds + cond) },
                    label = { Text(lbl) },
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun RingerSection(layout: RingerLayoutPolicy, onChange: (RingerLayoutPolicy) -> Unit) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text("Cognitive-load randomized layout", modifier = Modifier.weight(1f))
            Switch(
                checked = layout.cognitiveLoadRandomized,
                onCheckedChange = { onChange(layout.copy(cognitiveLoadRandomized = it)) },
            )
        }
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text("Accessibility (static layout)", modifier = Modifier.weight(1f))
            Switch(
                checked = layout.accessibilityMode,
                onCheckedChange = { onChange(layout.copy(accessibilityMode = it)) },
            )
        }
        Text("Dismiss button size")
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            listOf("small", "large", "random").forEach { sz ->
                FilterChip(
                    selected = layout.dismissSize == sz,
                    onClick = { onChange(layout.copy(dismissSize = sz)) },
                    label = { Text(sz) },
                )
            }
        }
    }
}

@Composable
private fun SectionStub(text: String) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(MaterialTheme.colorScheme.surface)
            .padding(12.dp),
    ) { Text(text, style = MaterialTheme.typography.bodyMedium) }
}

@Composable
private fun SliderRow(label: String, value: Float, range: ClosedFloatingPointRange<Float>, onChange: (Float) -> Unit) {
    Column {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(label, modifier = Modifier.weight(1f))
            Text(value.toInt().toString(), style = MaterialTheme.typography.labelLarge)
        }
        Slider(value = value, onValueChange = onChange, valueRange = range)
    }
}

private fun sourceName(s: AudioSource): String = when (s) {
    is AudioSource.SystemDefault -> "System"
    is AudioSource.Local -> "Local"
    is AudioSource.Url -> "URL"
    is AudioSource.SpotifyTrack, is AudioSource.SpotifyPlaylist, is AudioSource.SpotifyPodcast -> "Spotify"
    is AudioSource.DriveFile -> "Drive"
}

private fun sourceFor(name: String): AudioSource = when (name) {
    "Local" -> AudioSource.Local("")
    "URL" -> AudioSource.Url("")
    "Spotify" -> AudioSource.SpotifyTrack("")
    "Drive" -> AudioSource.DriveFile("", "audio/mpeg")
    else -> AudioSource.SystemDefault
}
