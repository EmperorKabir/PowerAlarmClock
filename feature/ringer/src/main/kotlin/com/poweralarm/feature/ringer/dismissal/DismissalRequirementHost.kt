package com.poweralarm.feature.ringer.dismissal

import androidx.compose.foundation.layout.Column
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier

@Composable
fun DismissalRequirementHost(
    requirements: List<String>,
    modifier: Modifier = Modifier,
    onComplete: () -> Unit,
) {
    var index by remember(requirements) { mutableIntStateOf(0) }
    Column(modifier) {
        if (requirements.isEmpty() || index >= requirements.size) {
            onComplete()
            return@Column
        }
        when (val current = requirements[index]) {
            "tap" -> TapButtonRequirement { index++ }
            "cognitive" -> CognitiveRequirement { index++ }
            "voice" -> VoiceRequirement { index++ }
            "nfc" -> NfcRequirement { index++ }
            "qr" -> QrRequirement { index++ }
            "steps" -> StepsRequirement { index++ }
            "selfie" -> EyesOpenSelfieRequirement { index++ }
            "motion" -> MotionSustainedRequirement { index++ }
            "distress" -> DistressRequirement { index++ }
            else -> Text("Unknown requirement: $current")
        }
    }
}
