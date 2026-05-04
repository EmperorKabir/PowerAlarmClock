package com.poweralarm.feature.profiles

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
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.outlined.PeopleAlt
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
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

data class ProfileSummary(
    val id: String,
    val name: String,
    val emoji: String,
    val memberCount: Int,
    val tint: Color,
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfilesScreen(
    profiles: List<ProfileSummary> = DEFAULT_PROFILES,
    initialActive: String = "default",
    onActivate: (String) -> Unit = {},
) {
    var active by remember { mutableStateOf(initialActive) }
    Scaffold(
        topBar = { TopAppBar(title = { Text("Profiles") }) },
    ) { padding ->
        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(padding),
            contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 12.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            items(profiles, key = { it.id }) { p ->
                ProfileCard(
                    profile = p,
                    active = active == p.id,
                    onActivate = { active = p.id; onActivate(p.id) },
                )
            }
        }
    }
}

@Composable
private fun ProfileCard(profile: ProfileSummary, active: Boolean, onActivate: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth().clickable { onActivate() },
        colors = CardDefaults.cardColors(
            containerColor = if (active) profile.tint.copy(alpha = 0.2f)
            else MaterialTheme.colorScheme.surfaceVariant,
        ),
        shape = RoundedCornerShape(20.dp),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(CircleShape)
                    .background(profile.tint),
                contentAlignment = Alignment.Center,
            ) {
                Text(profile.emoji, style = MaterialTheme.typography.headlineSmall)
            }
            Column(modifier = Modifier.weight(1f).padding(start = 16.dp)) {
                Text(profile.name, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                Text(
                    "${profile.memberCount} alarm${if (profile.memberCount == 1) "" else "s"}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            if (active) {
                Icon(Icons.Filled.Check, "Active", tint = MaterialTheme.colorScheme.primary)
            }
        }
    }
}

private val DEFAULT_PROFILES = listOf(
    ProfileSummary("default", "Default", "🌅", 0, Color(0xFF00C2B8)),
    ProfileSummary("work", "Work", "💼", 0, Color(0xFFFF7043)),
    ProfileSummary("holiday", "Holiday", "🏖️", 0, Color(0xFFFFEB3B)),
    ProfileSummary("travel", "Travel", "✈️", 0, Color(0xFF9575CD)),
)
