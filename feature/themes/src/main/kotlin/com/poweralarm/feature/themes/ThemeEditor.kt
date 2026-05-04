package com.poweralarm.feature.themes

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import com.poweralarm.core.ui.theme.HexColor
import com.poweralarm.core.ui.theme.ThemeRole

@Composable
fun ThemeEditor(
    initial: Map<ThemeRole, String>,
    typographyFamily: String,
    onColorChanged: (ThemeRole, String) -> Unit,
    onTypographyChanged: (String) -> Unit,
) {
    var typography by remember { mutableStateOf(typographyFamily) }

    Column(
        modifier = Modifier.fillMaxWidth().padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        ThemeRole.values().forEach { role ->
            ColorRow(role = role, hex = initial[role] ?: "#000000", onChange = { onColorChanged(role, it) })
        }
        OutlinedTextField(
            value = typography,
            onValueChange = { typography = it; onTypographyChanged(it) },
            label = { Text("Typography family (e.g. Inter, JetBrains Mono)") },
        )
    }
}

@Composable
private fun ColorRow(role: ThemeRole, hex: String, onChange: (String) -> Unit) {
    var value by remember(role) { mutableStateOf(hex) }
    val color = HexColor.parse(value)
    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(
            Modifier
                .size(36.dp)
                .clip(RoundedCornerShape(8.dp))
                .background(color),
        )
        Text(role.name, modifier = Modifier.padding(horizontal = 12.dp))
        OutlinedTextField(
            value = value,
            onValueChange = {
                value = it
                if (HexColor.isValid(it)) onChange(it)
            },
            label = { Text("Hex") },
        )
    }
}
