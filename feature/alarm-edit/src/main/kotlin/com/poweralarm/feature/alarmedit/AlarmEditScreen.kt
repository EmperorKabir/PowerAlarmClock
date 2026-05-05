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
import androidx.compose.material.icons.outlined.Flight
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
import com.poweralarm.core.domain.model.TimezoneMode
import java.time.ZoneId

/**
 * Sections are framed as user-tasks instead of technical concept names.
 * `basic` flag controls visibility: when the user has *not* turned on Advanced mode
 * inside this alarm, only basic sections are shown.
 */
private enum class EditSection(
    val title: String,
    val tagline: String,
    val icon: ImageVector,
    val basic: Boolean,
) {
    WHEN_AND_WHERE("When", "Time, recurrence, timezone", Icons.Outlined.Alarm, basic = true),
    SOUND("Sound", "What plays — tone, Spotify, Drive, ramp", Icons.Outlined.MusicNote, basic = true),
    SNOOZE("Snooze", "How long, how many times, gestures", Icons.Outlined.Snooze, basic = true),
    DISMISSAL("How I prove I'm awake", "Tap, captcha, voice, NFC, QR, steps, selfie", Icons.Outlined.Psychology, basic = true),
    SKIP("Skip on holidays / days off", "Holiday, geofence, ICS skips", Icons.Outlined.Block, basic = false),
    ADVANCE("Wake earlier when needed", "Weather, traffic, AQI, calendar, bedtime, battery", Icons.Outlined.Cloud, basic = false),
    TRAVEL("Travel & timezones", "How this alarm behaves abroad", Icons.Outlined.Flight, basic = true),
    RINGER("Wake-up screen", "Button positions, lockout, hardware buttons", Icons.Outlined.SwapHoriz, basic = false),
    AUTOMATION("Smart triggers", "Tasker, DND, network, lights, plugs", Icons.Outlined.Bolt, basic = false),
    SECURITY("Lock-down", "Edit lock, distress, SOS", Icons.Outlined.Lock, basic = false),
    CAST("Other devices", "Cast targets, cloud sync", Icons.Outlined.Cast, basic = false),
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
    var tzMode by remember { mutableStateOf(initial.timezoneMode) }
    var tzId by remember { mutableStateOf(initial.timezoneId.ifEmpty { ZoneId.systemDefault().id }) }
    var expandedSection by remember { mutableStateOf<EditSection?>(EditSection.WHEN_AND_WHERE) }
    var showAdvanced by remember { mutableStateOf(false) }

    val visibleSections = EditSection.entries.filter { showAdvanced || it.basic }

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
                            timezoneMode = tzMode,
                            timezoneId = if (tzMode is TimezoneMode.Fixed) tzId else "",
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
                AnchorChipRow(
                    sections = visibleSections,
                    current = expandedSection,
                    onSelect = { expandedSection = it },
                )
            }
            item("advanced-toggle") {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    shape = RoundedCornerShape(16.dp),
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text("Show advanced options", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold)
                            Text(
                                "Skip rules, advance triggers, lockout, automation, casting…",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                        }
                        Switch(checked = showAdvanced, onCheckedChange = { showAdvanced = it })
                    }
                }
            }
            visibleSections.forEach { section ->
                item(section.name) {
                    Section(
                        section = section,
                        expanded = expandedSection == section,
                        onToggle = { expandedSection = if (expandedSection == section) null else section },
                    ) {
                        when (section) {
                            EditSection.WHEN_AND_WHERE -> TimeSection(
                                hour, minute, label, enabled, recurrence, weeklyDays,
                                onHour = { hour = it },
                                onMinute = { minute = it },
                                onLabel = { label = it },
                                onEnabled = { enabled = it },
                                onRecurrence = { recurrence = it },
                                onWeeklyDays = { weeklyDays = it },
                            )
                            EditSection.SOUND -> AudioSection(audioPlan) { audioPlan = it }
                            EditSection.SNOOZE -> SnoozeSection(snoozePolicy) { snoozePolicy = it }
                            EditSection.DISMISSAL -> DismissalSection(requirements) { requirements = it }
                            EditSection.SKIP -> SkipSection(conditions) { conditions = it }
                            EditSection.ADVANCE -> AdvanceSection(conditions) { conditions = it }
                            EditSection.TRAVEL -> TravelSection(tzMode, tzId, onMode = { tzMode = it }, onZone = { tzId = it })
                            EditSection.RINGER -> RingerSection(ringerLayout) { ringerLayout = it }
                            EditSection.AUTOMATION -> SectionStub("Tasker intents · Do Not Disturb · Network toggle · Smart light & plug.\n\nFine-tune in Settings → Smart triggers.")
                            EditSection.SECURITY -> SectionStub("Edit lock · Distress code · SOS contacts.\n\nFine-tune in Settings → Lock-down.")
                            EditSection.CAST -> SectionStub("Cast to speakers/TVs · Cloud backup.\n\nFine-tune in Settings → Backup & devices.")
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
private fun AnchorChipRow(sections: List<EditSection>, current: EditSection?, onSelect: (EditSection) -> Unit) {
    LazyRow(
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        contentPadding = androidx.compose.foundation.layout.PaddingValues(vertical = 4.dp),
    ) {
        sections.forEach { section ->
            item(section.name) {
                FilterChip(
                    selected = current == section,
                    onClick = { onSelect(section) },
                    leadingIcon = { Icon(section.icon, null, modifier = Modifier.size(16.dp)) },
                    label = { Text(section.title) },
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
            Column(modifier = Modifier.weight(1f).padding(start = 12.dp)) {
                Text(section.title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                Text(
                    section.tagline,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
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
        OutlinedTextField(value = label, onValueChange = onLabel, label = { Text("Label (e.g. ‘School run’)") }, modifier = Modifier.fillMaxWidth())
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text("Alarm is on", modifier = Modifier.weight(1f))
            Switch(checked = enabled, onCheckedChange = onEnabled)
        }
        Text("How often", style = MaterialTheme.typography.titleSmall)
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            listOf(
                "Just once" to Recurrence.Once,
                "Every day" to Recurrence.Daily,
                "Pick days" to Recurrence.Weekly(weeklyDays),
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
private fun TravelSection(
    mode: TimezoneMode,
    zoneId: String,
    onMode: (TimezoneMode) -> Unit,
    onZone: (String) -> Unit,
) {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            FilterChip(
                selected = mode is TimezoneMode.Device,
                onClick = { onMode(TimezoneMode.Device) },
                label = { Text("Device time") },
            )
            FilterChip(
                selected = mode is TimezoneMode.Fixed,
                onClick = { onMode(TimezoneMode.Fixed) },
                label = { Text("Pin to a zone") },
            )
        }
        Text(
            text = when (mode) {
                is TimezoneMode.Device -> "Fires at ${ZoneId.systemDefault().id} time, wherever you are. Travel = wake-up follows local clock."
                is TimezoneMode.Fixed -> "Fires at the same wall-clock time in $zoneId, even when abroad. Useful for ‘call mum at 8am UK time’."
            },
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        if (mode is TimezoneMode.Fixed) {
            OutlinedTextField(
                value = zoneId,
                onValueChange = onZone,
                label = { Text("IANA zone (e.g. Europe/London)") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
            )
            FlowRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                COMMON_ZONES.forEach { z ->
                    FilterChip(
                        selected = zoneId == z,
                        onClick = { onZone(z) },
                        label = { Text(z) },
                    )
                }
            }
        }
    }
}

private val COMMON_ZONES = listOf(
    "Europe/London", "Europe/Paris", "Europe/Berlin", "America/New_York", "America/Los_Angeles",
    "America/Chicago", "Asia/Tokyo", "Asia/Singapore", "Asia/Dubai", "Asia/Kolkata",
    "Australia/Sydney", "Pacific/Auckland",
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun AudioSection(plan: AudioPlan, onChange: (AudioPlan) -> Unit) {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Text("Sound source", style = MaterialTheme.typography.titleSmall)
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            listOf("System default", "Local file", "URL", "Spotify", "Drive").forEach { name ->
                FilterChip(
                    selected = sourceLabel(plan.source) == name,
                    onClick = { onChange(plan.copy(source = sourceFor(name))) },
                    label = { Text(name) },
                )
            }
        }
        SliderRow("Volume ramp (minutes)", plan.volumeRampMin.toFloat(), 0f..30f) { onChange(plan.copy(volumeRampMin = it.toInt())) }
        SliderRow("Start volume %", plan.volumeStartPct.toFloat(), 0f..100f) { onChange(plan.copy(volumeStartPct = it.toInt())) }
        SliderRow("Final volume %", plan.volumeEndPct.toFloat(), 0f..100f) { onChange(plan.copy(volumeEndPct = it.toInt())) }
        SliderRow("Fade-out after dismiss (sec)", plan.fadeOutSec.toFloat(), 0f..60f) { onChange(plan.copy(fadeOutSec = it.toInt())) }
    }
}

@Composable
private fun SnoozeSection(policy: SnoozePolicy, onChange: (SnoozePolicy) -> Unit) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        SliderRow("First snooze length (min)", policy.baseMin.toFloat(), 1f..30f) { onChange(policy.copy(baseMin = it.toInt())) }
        SliderRow("Get shorter each time by (min)", policy.decrementMin.toFloat(), 0f..10f) { onChange(policy.copy(decrementMin = it.toInt())) }
        SliderRow("Stop shrinking at (min)", policy.floorMin.toFloat(), 1f..15f) { onChange(policy.copy(floorMin = it.toInt())) }
        SliderRow("Max snoozes", policy.maxCount.toFloat(), 0f..20f) { onChange(policy.copy(maxCount = it.toInt())) }
    }
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
private fun DismissalSection(reqs: List<DismissalRequirement>, onChange: (List<DismissalRequirement>) -> Unit) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text("Pick one or more challenges:")
        val options = listOf(
            "Tap a button" to DismissalRequirement.TapButton,
            "Solve a problem" to DismissalRequirement.Cognitive(listOf("probability"), "medium", 1),
            "Speak a phrase" to DismissalRequirement.Voice("", 0.85f),
            "Scan an NFC tag" to DismissalRequirement.Nfc(""),
            "Scan a QR code" to DismissalRequirement.Qr(""),
            "Walk N steps" to DismissalRequirement.Steps(30, 5),
            "Eyes-open selfie" to DismissalRequirement.EyesOpenSelfie(0.7f, 5),
            "Distress code" to DismissalRequirement.Distress(""),
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
        "Public holidays" to Condition.HolidaySkip("GB", "nager"),
        "Only at home" to Condition.Geofence(0f, 0f, 200, "disable"),
        "Calendar event titles" to Condition.IcsSkip(emptyList(), ""),
    )
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text("Don't ring this alarm when:")
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
        "Bad weather" to Condition.WeatherAdvance("openweather", 10, 15, 1f, 1f),
        "Heavy traffic" to Condition.TrafficAdvance("google", 0f, 0f, 0f, 0f, 30),
        "Bad air quality" to Condition.AqiAdvance("openaq", 35f, 15),
        "Tube disruption" to Condition.TflDisruption(emptyList(), emptyList(), 20),
        "Early calendar event" to Condition.CalendarShift(emptyList(), 120, 30),
        "I broke my bedtime" to Condition.BedtimePenalty("23:00", 30, 15),
        "Phone almost dead" to Condition.LowBatteryFailsafe(15, true, 30),
    )
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text("Wake me earlier when:")
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
            Text("Randomise button positions", modifier = Modifier.weight(1f))
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

private fun sourceLabel(s: AudioSource): String = when (s) {
    is AudioSource.SystemDefault -> "System default"
    is AudioSource.Local -> "Local file"
    is AudioSource.Url -> "URL"
    is AudioSource.SpotifyTrack, is AudioSource.SpotifyPlaylist, is AudioSource.SpotifyPodcast -> "Spotify"
    is AudioSource.DriveFile -> "Drive"
}

private fun sourceFor(name: String): AudioSource = when (name) {
    "Local file" -> AudioSource.Local("")
    "URL" -> AudioSource.Url("")
    "Spotify" -> AudioSource.SpotifyTrack("")
    "Drive" -> AudioSource.DriveFile("", "audio/mpeg")
    else -> AudioSource.SystemDefault
}
