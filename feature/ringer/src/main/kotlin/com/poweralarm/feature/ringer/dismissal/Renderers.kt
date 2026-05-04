package com.poweralarm.feature.ringer.dismissal

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.poweralarm.feature.ringer.cognitive.CognitiveProblemBank

/** Tap-only — trivial confirm. */
@Composable
fun TapButtonRequirement(onSatisfied: () -> Unit) {
    Button(onClick = onSatisfied, modifier = Modifier.fillMaxWidth().padding(8.dp)) { Text("I'm awake") }
}

/** Cognitive captcha. Pulls a problem from the bank, requires correct answer. */
@Composable
fun CognitiveRequirement(onSatisfied: () -> Unit) {
    val problem = remember { CognitiveProblemBank.next() }
    var answer by remember { mutableStateOf("") }
    var error by remember { mutableStateOf<String?>(null) }

    Column(Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(problem.statement)
        OutlinedTextField(value = answer, onValueChange = { answer = it; error = null }, label = { Text("Answer") })
        error?.let { Text(it) }
        Button(onClick = {
            if (problem.isCorrect(answer)) onSatisfied() else error = "Incorrect — try again."
        }) { Text("Submit") }
    }
}

@Composable fun VoiceRequirement(onSatisfied: () -> Unit) {
    PlaceholderRequirement(label = "Voice passphrase", instruction = "Say your enrolled phrase.", onSatisfied = onSatisfied)
}

@Composable fun NfcRequirement(onSatisfied: () -> Unit) {
    PlaceholderRequirement(label = "NFC tag", instruction = "Tap the registered tag.", onSatisfied = onSatisfied)
}

@Composable fun QrRequirement(onSatisfied: () -> Unit) {
    PlaceholderRequirement(label = "QR scan", instruction = "Scan the registered QR code.", onSatisfied = onSatisfied)
}

@Composable fun StepsRequirement(onSatisfied: () -> Unit) {
    PlaceholderRequirement(label = "Steps", instruction = "Walk the configured number of steps.", onSatisfied = onSatisfied)
}

@Composable fun EyesOpenSelfieRequirement(onSatisfied: () -> Unit) {
    PlaceholderRequirement(label = "Eyes-open selfie", instruction = "Look at the camera with eyes open.", onSatisfied = onSatisfied)
}

@Composable fun MotionSustainedRequirement(onSatisfied: () -> Unit) {
    PlaceholderRequirement(label = "Sustained motion", instruction = "Keep moving for the configured window.", onSatisfied = onSatisfied)
}

@Composable fun DistressRequirement(onSatisfied: () -> Unit) {
    PlaceholderRequirement(label = "Distress code", instruction = "Enter your normal or distress code.", onSatisfied = onSatisfied)
}

@Composable
private fun PlaceholderRequirement(label: String, instruction: String, onSatisfied: () -> Unit) {
    Column(Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(label, style = androidx.compose.material3.MaterialTheme.typography.titleMedium)
        Text(instruction)
        Button(onClick = onSatisfied) { Text("Confirm") }
    }
}
