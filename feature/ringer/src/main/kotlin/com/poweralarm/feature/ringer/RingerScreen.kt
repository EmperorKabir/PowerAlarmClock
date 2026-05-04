package com.poweralarm.feature.ringer

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.poweralarm.core.domain.model.RingerLayoutPolicy
import com.poweralarm.feature.ringer.dismissal.DismissalRequirementHost
import com.poweralarm.feature.ringer.layout.CognitiveLoadLayout
import kotlinx.coroutines.flow.MutableStateFlow

@Composable
fun RingerScreen(alarmId: Long, vm: RingerViewModel = remember { RingerViewModel() }) {
    val state by vm.state.collectAsState()
    val instanceKey = remember(alarmId, state.recompositionTrigger) { alarmId to state.recompositionTrigger }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background),
    ) {
        Column(
            modifier = Modifier.fillMaxSize().padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Top,
        ) {
            Text(
                text = state.label.ifBlank { "Power Alarm" },
                style = MaterialTheme.typography.displayLarge,
                fontWeight = FontWeight.Bold,
            )
            Text(
                text = state.timeLabel,
                style = MaterialTheme.typography.headlineMedium,
            )
        }

        if (state.showsRequirements) {
            DismissalRequirementHost(
                requirements = state.requirements,
                modifier = Modifier.fillMaxSize(),
                onComplete = vm::onAllRequirementsSatisfied,
            )
        }

        CognitiveLoadLayout(
            policy = state.layoutPolicy,
            instanceKey = instanceKey,
            dismiss = { mod ->
                Button(onClick = vm::onDismiss, modifier = mod.fillMaxSize()) { Text("Dismiss") }
            },
            snooze = { mod ->
                OutlinedButton(onClick = vm::onSnooze, modifier = mod.fillMaxSize()) { Text("Snooze") }
            },
        )
    }
}

class RingerViewModel {
    val state = MutableStateFlow(RingerUiState())

    fun onDismiss() { /* delegate to RingerCoordinator via DI in :app */ }
    fun onSnooze() { /* delegate */ }
    fun onAllRequirementsSatisfied() {
        state.value = state.value.copy(showsRequirements = false)
    }
}

data class RingerUiState(
    val label: String = "",
    val timeLabel: String = "",
    val layoutPolicy: RingerLayoutPolicy = RingerLayoutPolicy(),
    val requirements: List<String> = emptyList(),
    val showsRequirements: Boolean = false,
    val recompositionTrigger: Int = 0,
)
