package com.manalejandro.wifiattack.presentation.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Place
import androidx.compose.material.icons.filled.Search
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
import com.manalejandro.wifiattack.data.model.AttackType
import java.text.SimpleDateFormat
import java.util.*

/**
 * Screen displaying attack history and details.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AttacksScreen(
    attacks: List<AttackEvent>,
    onClearHistory: () -> Unit,
    modifier: Modifier = Modifier
) {
    var showClearDialog by remember { mutableStateOf(false) }
    var selectedAttackType by remember { mutableStateOf<AttackType?>(null) }

    val filteredAttacks = attacks
        .filter { selectedAttackType == null || it.attackType == selectedAttackType }
        .sortedByDescending { it.timestamp }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        // Header
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Attack History",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )

            if (attacks.isNotEmpty()) {
                IconButton(onClick = { showClearDialog = true }) {
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = "Clear history"
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Statistics Card
        if (attacks.isNotEmpty()) {
            AttackStatisticsCard(attacks = attacks)
            Spacer(modifier = Modifier.height(16.dp))
        }

        // Filter Chips
        if (attacks.isNotEmpty()) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                FilterChip(
                    selected = selectedAttackType == null,
                    onClick = { selectedAttackType = null },
                    label = { Text("All") }
                )

                AttackType.entries.take(3).forEach { type ->
                    val count = attacks.count { it.attackType == type }
                    if (count > 0) {
                        FilterChip(
                            selected = selectedAttackType == type,
                            onClick = {
                                selectedAttackType = if (selectedAttackType == type) null else type
                            },
                            label = { Text("${type.displayName} ($count)") }
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
        }

        // Attacks List
        if (filteredAttacks.isEmpty()) {
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
                        imageVector = Icons.Default.Lock,
                        contentDescription = null,
                        modifier = Modifier.size(64.dp),
                        tint = Color(0xFF4CAF50)
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = if (attacks.isEmpty()) "No attacks detected" else "No attacks match filter",
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    if (attacks.isEmpty()) {
                        Text(
                            text = "Your network appears to be safe",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(filteredAttacks) { attack ->
                    AttackDetailCard(attack = attack)
                }
            }
        }
    }

    // Clear Confirmation Dialog
    if (showClearDialog) {
        AlertDialog(
            onDismissRequest = { showClearDialog = false },
            icon = { Icon(Icons.Default.Warning, contentDescription = null) },
            title = { Text("Clear Attack History") },
            text = { Text("Are you sure you want to clear all attack history? This action cannot be undone.") },
            confirmButton = {
                TextButton(
                    onClick = {
                        onClearHistory()
                        showClearDialog = false
                    }
                ) {
                    Text("Clear")
                }
            },
            dismissButton = {
                TextButton(onClick = { showClearDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }
}

@Composable
private fun AttackStatisticsCard(
    attacks: List<AttackEvent>,
    modifier: Modifier = Modifier
) {
    val totalAttacks = attacks.size
    val activeAttacks = attacks.count { it.isActive }
    val criticalAttacks = attacks.count { it.confidence >= 80 }
    val uniqueChannels = attacks.map { it.channel }.distinct().size

    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.3f)
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = "Attack Summary",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                StatisticItem(
                    value = totalAttacks.toString(),
                    label = "Total",
                    color = MaterialTheme.colorScheme.onSurface
                )
                StatisticItem(
                    value = activeAttacks.toString(),
                    label = "Active",
                    color = Color(0xFFFF9800)
                )
                StatisticItem(
                    value = criticalAttacks.toString(),
                    label = "Critical",
                    color = Color(0xFFF44336)
                )
                StatisticItem(
                    value = uniqueChannels.toString(),
                    label = "Channels",
                    color = MaterialTheme.colorScheme.primary
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Attack Type Breakdown
            val attackTypeCounts = attacks.groupBy { it.attackType }
                .mapValues { it.value.size }
                .entries
                .sortedByDescending { it.value }

            Text(
                text = "Attack Types:",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(4.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                attackTypeCounts.take(3).forEach { (type, count) ->
                    AssistChip(
                        onClick = { },
                        label = { Text("${type.displayName}: $count") },
                        modifier = Modifier.height(24.dp)
                    )
                }
            }
        }
    }
}

@Composable
private fun StatisticItem(
    value: String,
    label: String,
    color: Color,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = value,
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold,
            color = color
        )
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
private fun AttackDetailCard(
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
        modifier = modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            // Header Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = when (attack.attackType) {
                            AttackType.DEAUTH -> Icons.Default.Close
                            AttackType.EVIL_TWIN -> Icons.Default.Warning
                            AttackType.BEACON_FLOOD -> Icons.Default.Info
                            AttackType.PROBE_FLOOD -> Icons.Default.Search
                            else -> Icons.Default.Warning
                        },
                        contentDescription = null,
                        tint = threatColor,
                        modifier = Modifier.size(32.dp)
                    )

                    Column {
                        Text(
                            text = attack.attackType.displayName,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = attack.attackType.description,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                // Confidence Badge
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(4.dp))
                        .background(threatColor.copy(alpha = 0.2f))
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = "${attack.confidence}%",
                        style = MaterialTheme.typography.labelMedium,
                        color = threatColor,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            HorizontalDivider()

            Spacer(modifier = Modifier.height(12.dp))

            // Details Grid
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                DetailItem(
                    label = "Target",
                    value = attack.targetSsid ?: attack.targetBssid?.take(17) ?: "Unknown"
                )
                DetailItem(
                    label = "Channel",
                    value = attack.channel.toString()
                )
                DetailItem(
                    label = "Signal",
                    value = "${attack.signalStrength} dBm"
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Timestamp
                Row(
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Info,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = formatDateTime(attack.timestamp),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                // Direction if available
                attack.estimatedDirection?.let { direction ->
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Place,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp),
                            tint = MaterialTheme.colorScheme.primary
                        )
                        Text(
                            text = "${direction.toInt()}°",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }

                // Status Badge
                if (attack.isActive) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(4.dp))
                            .background(Color(0xFFFF9800).copy(alpha = 0.2f))
                            .padding(horizontal = 8.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = "ACTIVE",
                            style = MaterialTheme.typography.labelSmall,
                            color = Color(0xFFFF9800),
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun DetailItem(
    label: String,
    value: String,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Medium
        )
    }
}

private fun formatDateTime(timestamp: Long): String {
    val sdf = SimpleDateFormat("MMM dd, HH:mm:ss", Locale.getDefault())
    return sdf.format(Date(timestamp))
}

