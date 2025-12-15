package com.manalejandro.wifiattack.presentation.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.List
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.manalejandro.wifiattack.data.model.ChannelStats
import com.manalejandro.wifiattack.data.model.WifiBand

/**
 * Screen displaying detailed channel statistics and error packet counts.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChannelStatsScreen(
    channelStats: List<ChannelStats>,
    isScanning: Boolean,
    modifier: Modifier = Modifier
) {
    var selectedBand by remember { mutableStateOf<WifiBand?>(null) }
    var sortByThreat by remember { mutableStateOf(true) }

    val filteredStats = channelStats
        .filter { selectedBand == null || it.band == selectedBand }
        .let { stats ->
            if (sortByThreat) {
                stats.sortedByDescending { it.suspiciousActivityScore }
            } else {
                stats.sortedBy { it.channel }
            }
        }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        // Header with filters
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Channel Statistics",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )

            if (isScanning) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(16.dp),
                        strokeWidth = 2.dp
                    )
                    Text(
                        text = "Live",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Band Filter Chips
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            FilterChip(
                selected = selectedBand == null,
                onClick = { selectedBand = null },
                label = { Text("All") }
            )
            FilterChip(
                selected = selectedBand == WifiBand.BAND_2_4GHz,
                onClick = { selectedBand = WifiBand.BAND_2_4GHz },
                label = { Text("2.4 GHz") }
            )
            FilterChip(
                selected = selectedBand == WifiBand.BAND_5GHz,
                onClick = { selectedBand = WifiBand.BAND_5GHz },
                label = { Text("5 GHz") }
            )
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Sort Toggle
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.End,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Sort by: ",
                style = MaterialTheme.typography.bodySmall
            )
            TextButton(onClick = { sortByThreat = !sortByThreat }) {
                Text(if (sortByThreat) "Threat Level" else "Channel Number")
                Icon(
                    imageVector = if (sortByThreat) Icons.Default.Warning else Icons.Default.List,
                    contentDescription = null,
                    modifier = Modifier.size(16.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Stats Summary
        if (filteredStats.isNotEmpty()) {
            StatsSummaryCard(stats = filteredStats)
            Spacer(modifier = Modifier.height(16.dp))
        }

        // Channel List
        if (filteredStats.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Icon(
                        imageVector = Icons.Default.Refresh,
                        contentDescription = null,
                        modifier = Modifier.size(64.dp),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = "No channel data available",
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = "Start monitoring to see channel statistics",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(filteredStats) { stat ->
                    ChannelDetailCard(stat = stat)
                }
            }
        }
    }
}

@Composable
private fun StatsSummaryCard(
    stats: List<ChannelStats>,
    modifier: Modifier = Modifier
) {
    val totalNetworks = stats.sumOf { it.networksCount }
    val totalDeauthPackets = stats.sumOf { it.deauthPacketCount }
    val suspiciousChannels = stats.count { it.hasSuspiciousActivity }

    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.secondaryContainer
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            StatItem(
                value = stats.size.toString(),
                label = "Channels",
                icon = Icons.Default.Build
            )
            StatItem(
                value = totalNetworks.toString(),
                label = "Networks",
                icon = Icons.Default.Check
            )
            StatItem(
                value = totalDeauthPackets.toString(),
                label = "Error Pkts",
                icon = Icons.Default.Info
            )
            StatItem(
                value = suspiciousChannels.toString(),
                label = "Suspicious",
                icon = Icons.Default.Warning
            )
        }
    }
}

@Composable
private fun StatItem(
    value: String,
    label: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            modifier = Modifier.size(24.dp),
            tint = MaterialTheme.colorScheme.onSecondaryContainer
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = value,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold
        )
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall
        )
    }
}

@Composable
private fun ChannelDetailCard(
    stat: ChannelStats,
    modifier: Modifier = Modifier
) {
    val threatColor = Color(stat.threatLevel.colorValue)

    Card(
        modifier = modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = "Channel ${stat.channel}",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    AssistChip(
                        onClick = { },
                        label = { Text(stat.band.displayName) },
                        modifier = Modifier.height(24.dp)
                    )
                }

                // Threat Level Badge
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(4.dp))
                        .background(threatColor.copy(alpha = 0.2f))
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = stat.threatLevel.displayName,
                        style = MaterialTheme.typography.labelSmall,
                        color = threatColor,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Stats Grid
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text(
                        text = "Networks",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = "${stat.networksCount}",
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.Bold
                    )
                }
                Column {
                    Text(
                        text = "Avg RSSI",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = "${stat.averageRssi} dBm",
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.Bold
                    )
                }
                Column {
                    Text(
                        text = "Error Packets",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = "${stat.deauthPacketCount}",
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.Bold,
                        color = if (stat.deauthPacketCount > 20) threatColor else Color.Unspecified
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Suspicious Activity Bar
            Column {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "Suspicious Activity",
                        style = MaterialTheme.typography.bodySmall
                    )
                    Text(
                        text = "${stat.suspiciousActivityScore}%",
                        style = MaterialTheme.typography.bodySmall,
                        fontWeight = FontWeight.Bold,
                        color = threatColor
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
    }
}

