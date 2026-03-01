package com.fraud_detector.ui

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.fraud_detector.models.ThreatLevel
import com.fraud_detector.viewmodels.DashboardViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardScreen(viewModel: DashboardViewModel = hiltViewModel()) {
    val uiState by viewModel.uiState.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(title = { Text("SmartFraud Detector") })
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(padding),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item { StatsRow(uiState.highCount, uiState.medCount, uiState.totalScanned) }
            item { ThreatGauge(score = uiState.riskScore) }
            item {
                Text("Recent Threats", style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier.padding(top = 8.dp))
            }
            items(uiState.recentThreats) { threat ->
                ThreatCard(threat)
            }
        }
    }
}

@Composable
fun ThreatCard(threat: com.fraud_detector.models.ThreatResult) {
    val color = when (threat.level) {
        ThreatLevel.HIGH -> MaterialTheme.colorScheme.error
        ThreatLevel.MEDIUM -> Color(0xFFFF9800)
        ThreatLevel.LOW -> MaterialTheme.colorScheme.primary
    }
    Card(modifier = Modifier.fillMaxWidth()) {
        Row(Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
            Box(Modifier.size(10.dp).background(color, shape = MaterialTheme.shapes.small))
            Spacer(Modifier.width(12.dp))
            Column(Modifier.weight(1f)) {
                Text(threat.sender, style = MaterialTheme.typography.bodyMedium)
                Text(threat.body.take(60) + "…", style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            Text("%.0f%%".format(threat.score * 100), color = color,
                style = MaterialTheme.typography.labelLarge)
        }
    }
}

@Composable
fun StatsRow(high: Int, med: Int, total: Int) {
    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        StatChip("High", high, MaterialTheme.colorScheme.error, Modifier.weight(1f))
        StatChip("Medium", med, Color(0xFFFF9800), Modifier.weight(1f))
        StatChip("Scanned", total, MaterialTheme.colorScheme.primary, Modifier.weight(1f))
    }
}

@Composable
fun StatChip(label: String, value: Int, color: Color, modifier: Modifier) {
    Card(modifier = modifier) {
        Column(Modifier.padding(12.dp), horizontalAlignment = Alignment.CenterHorizontally) {
            Text("$value", style = MaterialTheme.typography.headlineMedium, color = color)
            Text(label, style = MaterialTheme.typography.labelSmall)
        }
    }
}

@Composable
fun ThreatGauge(score: Float) {
    val anim by animateFloatAsState(score, animationSpec = tween(800))
    Card(Modifier.fillMaxWidth()) {
        Column(Modifier.padding(16.dp)) {
            Text("Current Risk Score", style = MaterialTheme.typography.titleSmall)
            Spacer(Modifier.height(8.dp))
            LinearProgressIndicator(
                progress = { anim },
                modifier = Modifier.fillMaxWidth().height(12.dp),
                color = when {
                    score >= 0.75f -> MaterialTheme.colorScheme.error
                    score >= 0.45f -> Color(0xFFFF9800)
                    else -> MaterialTheme.colorScheme.primary
                }
            )
            Text("%.0f%% risk".format(score * 100),
                style = MaterialTheme.typography.bodySmall,
                modifier = Modifier.padding(top = 4.dp))
        }
    }
}