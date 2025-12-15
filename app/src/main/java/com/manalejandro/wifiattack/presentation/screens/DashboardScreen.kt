package com.manalejandro.wifiattack.presentation.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.manalejandro.wifiattack.data.model.AttackEvent
import com.manalejandro.wifiattack.data.model.ChannelStats
import java.text.SimpleDateFormat
import java.util.*

/**
 * Main dashboard screen showing overview of WiFi monitoring status.
 */
@Composable
fun DashboardScreen(
    isScanning: Boolean,
    networksCount: Int,
    activeAttacksCount: Int,
    highestThreatLevel: String,
    channelStats: List<ChannelStats>,
    recentAttacks: List<AttackEvent>,
    lastScanTime: Long,
    onToggleScanning: () -> Unit,
    onNavigateToChannels: () -> Unit,
    onNavigateToAttacks: () -> Unit,
    modifier: Modifier = Modifier
) {
    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Status Card
        item {
            StatusCard(
                isScanning = isScanning,
                networksCount = networksCount,
                lastScanTime = lastScanTime,
                onToggleScanning = onToggleScanning
            )
        }

        // Threat Overview Card
        item {
            ThreatOverviewCard(
                activeAttacksCount = activeAttacksCount,
                highestThreatLevel = highestThreatLevel,
                onViewAttacks = onNavigateToAttacks
            )
        }

        // Channel Summary Card
        item {
            ChannelSummaryCard(
                channelStats = channelStats,
                onViewDetails = onNavigateToChannels
            )
        }

        // Recent Attacks Section
        if (recentAttacks.isNotEmpty()) {
            item {
                Text(
                    text = "Recent Attacks",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
            }

            items(recentAttacks.take(3)) { attack ->
                AttackEventCard(attack = attack)
            }
        }
    }
}

@Composable
private fun StatusCard(
    isScanning: Boolean,
    networksCount: Int,
    lastScanTime: Long,
    onToggleScanning: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = if (isScanning)
                MaterialTheme.colorScheme.primaryContainer
            else
                MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = if (isScanning) "Monitoring Active" else "Monitoring Stopped",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "$networksCount networks detected",
                        style = MaterialTheme.typography.bodyMedium
                    )
                    if (lastScanTime > 0) {
                        Text(
                            text = "Last scan: ${formatTime(lastScanTime)}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                FilledIconToggleButton(
                    checked = isScanning,
                    onCheckedChange = { onToggleScanning() }
                ) {
                    Icon(
                        imageVector = if (isScanning) Icons.Default.Close else Icons.Default.PlayArrow,
                        contentDescription = if (isScanning) "Stop" else "Start"
                    )
                }
            }

            if (isScanning) {
                Spacer(modifier = Modifier.height(8.dp))
                LinearProgressIndicator(
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
    }
}

@Composable
private fun ThreatOverviewCard(
    activeAttacksCount: Int,
    highestThreatLevel: String,
    onViewAttacks: () -> Unit,
    modifier: Modifier = Modifier
) {
    val threatColor = when (highestThreatLevel) {
        "Critical" -> Color(0xFFF44336)
        "High" -> Color(0xFFFF5722)
        "Medium" -> Color(0xFFFF9800)
        "Low" -> Color(0xFF8BC34A)
        else -> Color(0xFF4CAF50)
    }

    Card(
        modifier = modifier.fillMaxWidth(),
        onClick = onViewAttacks
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .clip(CircleShape)
                        .background(threatColor.copy(alpha = 0.2f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Lock,
                        contentDescription = null,
                        tint = threatColor,
                        modifier = Modifier.size(28.dp)
                    )
                }

                Column {
                    Text(
                        text = "Threat Level: $highestThreatLevel",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "$activeAttacksCount active attack(s) detected",
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }

            Icon(
                imageVector = Icons.Default.KeyboardArrowRight,
                contentDescription = "View details"
            )
        }
    }
}

@Composable
private fun ChannelSummaryCard(
    channelStats: List<ChannelStats>,
    onViewDetails: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        onClick = onViewDetails
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Channel Activity",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Icon(
                    imageVector = Icons.Default.KeyboardArrowRight,
                    contentDescription = "View details"
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            if (channelStats.isEmpty()) {
                Text(
                    text = "No channel data available. Start monitoring to see activity.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            } else {
                // Show top 5 channels with activity
                val topChannels = channelStats
                    .sortedByDescending { it.suspiciousActivityScore }
                    .take(5)

                topChannels.forEach { stat ->
                    ChannelActivityBar(stat = stat)
                    Spacer(modifier = Modifier.height(8.dp))
                }
            }
        }
    }
}

@Composable
private fun ChannelActivityBar(
    stat: ChannelStats,
    modifier: Modifier = Modifier
) {
    val threatColor = Color(stat.threatLevel.colorValue)

    Column(modifier = modifier) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = "Ch ${stat.channel} (${stat.band.displayName})",
                style = MaterialTheme.typography.bodySmall
            )
            Text(
                text = "${stat.networksCount} networks",
                style = MaterialTheme.typography.bodySmall
            )
        }

        Spacer(modifier = Modifier.height(4.dp))

        LinearProgressIndicator(
            progress = { stat.suspiciousActivityScore / 100f },
            modifier = Modifier
                .fillMaxWidth()
                .height(8.dp)
                .clip(RoundedCornerShape(4.dp)),
            color = threatColor,
            trackColor = MaterialTheme.colorScheme.surfaceVariant
        )
    }
}

@Composable
fun AttackEventCard(
    attack: AttackEvent,
    modifier: Modifier = Modifier
) {
    val threatColor = when {
        attack.confidence >= 80 -> Color(0xFFF44336)
        attack.confidence >= 60 -> Color(0xFFFF5722)
        attack.confidence >= 40 -> Color(0xFFFF9800)
        else -> Color(0xFF8BC34A)
    }

    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = threatColor.copy(alpha = 0.1f)
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Default.Warning,
                contentDescription = null,
                tint = threatColor,
                modifier = Modifier.size(32.dp)
            )

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = attack.attackType.displayName,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = attack.targetSsid ?: attack.targetBssid ?: "Unknown target",
                    style = MaterialTheme.typography.bodySmall
                )
                Text(
                    text = "Channel ${attack.channel} • ${attack.confidence}% confidence",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Text(
                text = formatTime(attack.timestamp),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

private fun formatTime(timestamp: Long): String {
    val sdf = SimpleDateFormat("HH:mm:ss", Locale.getDefault())
    return sdf.format(Date(timestamp))
}

