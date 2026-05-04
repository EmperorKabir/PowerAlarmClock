package com.poweralarm.feature.themes

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.CutCornerShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
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
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.poweralarm.core.ui.theme.HexColor
import com.poweralarm.core.ui.theme.ThemeRole

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ThemeEditor(
    initial: Map<ThemeRole, String>,
    typographyFamily: String,
    onColorChanged: (ThemeRole, String) -> Unit,
    onTypographyChanged: (String) -> Unit,
) {
    var typography by remember { mutableStateOf(typographyFamily) }
    var current by remember { mutableStateOf(initial) }

    Scaffold(topBar = { TopAppBar(title = { Text("Theme") }) }) { padding ->
        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(padding).padding(horizontal = 12.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            item("preview") {
                Card(shape = RoundedCornerShape(20.dp)) {
                    Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        Text(
                            "Live launcher mask preview",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.SemiBold,
                        )
                        Text(
                            "Same icon rendered in every adaptive-icon mask shape",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceEvenly,
                        ) {
                            IconPreview(RoundedCornerShape(0.dp), "Square", current)
                            IconPreview(RoundedCornerShape(20.dp), "Squircle", current)
                            IconPreview(CircleShape, "Circle", current)
                            IconPreview(CutCornerShape(8.dp), "Cut", current)
                        }
                    }
                }
            }
            ThemeRole.values().forEach { role ->
                item(role.name) {
                    ColorRow(role, current[role] ?: "#000000") { hex ->
                        current = current.toMutableMap().apply { put(role, hex) }
                        onColorChanged(role, hex)
                    }
                }
            }
            item("typography") {
                Card(shape = RoundedCornerShape(16.dp)) {
                    Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text(
                            "Typography family",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.SemiBold,
                        )
                        OutlinedTextField(
                            value = typography,
                            onValueChange = { typography = it; onTypographyChanged(it) },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth(),
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun ColorRow(role: ThemeRole, hex: String, onChange: (String) -> Unit) {
    var value by remember(role) { mutableStateOf(hex) }
    val color = HexColor.parse(value)
    Card(shape = RoundedCornerShape(16.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(color),
            )
            Column(modifier = Modifier.weight(1f).padding(start = 12.dp)) {
                Text(role.name, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold)
                Text(
                    role.descriptorId,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            OutlinedTextField(
                value = value,
                onValueChange = {
                    value = it
                    if (HexColor.isValid(it)) onChange(it)
                },
                singleLine = true,
                modifier = Modifier.size(width = 140.dp, height = 56.dp),
            )
        }
    }
}

@Composable
private fun IconPreview(shape: Shape, label: String, colors: Map<ThemeRole, String>) {
    val bg = HexColor.parse(colors[ThemeRole.PRIMARY] ?: "#00C2B8")
    val accent = HexColor.parse(colors[ThemeRole.TERTIARY] ?: "#26A69A")
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Box(
            modifier = Modifier.size(56.dp).clip(shape).background(bg),
            contentAlignment = Alignment.Center,
        ) {
            Box(modifier = Modifier.size(36.dp).clip(CircleShape).background(accent))
        }
        Text(label, style = MaterialTheme.typography.labelSmall)
    }
}
