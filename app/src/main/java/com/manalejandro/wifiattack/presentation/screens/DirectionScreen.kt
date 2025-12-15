package com.manalejandro.wifiattack.presentation.screens

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Place
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.manalejandro.wifiattack.data.model.WifiNetworkInfo
import kotlin.math.cos
import kotlin.math.sin

/**
 * Screen for tracking signal direction using device compass.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DirectionScreen(
    networks: List<WifiNetworkInfo>,
    azimuth: Float,
    signalDirection: Float?,
    signalStrengthAtDirection: Map<Float, Int>,
    isTrackingDirection: Boolean,
    selectedNetwork: WifiNetworkInfo?,
    isSensorAvailable: Boolean,
    onStartTracking: (WifiNetworkInfo) -> Unit,
    onStopTracking: () -> Unit,
    cardinalDirection: String,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        // Header
        Text(
            text = "Signal Direction Tracker",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold
        )

        Spacer(modifier = Modifier.height(8.dp))

        if (!isSensorAvailable) {
            // Sensor not available warning
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.errorContainer
                )
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Warning,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onErrorContainer
                    )
                    Text(
                        text = "Compass sensor not available on this device",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onErrorContainer
                    )
                }
            }
            return
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Compass Card
        Card(
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = if (isTrackingDirection && selectedNetwork != null) {
                        "Tracking: ${selectedNetwork.ssid}"
                    } else {
                        "Select a network to track"
                    },
                    style = MaterialTheme.typography.titleMedium
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Compass View
                CompassView(
                    azimuth = azimuth,
                    signalDirection = signalDirection,
                    signalStrengthAtDirection = signalStrengthAtDirection,
                    isTracking = isTrackingDirection,
                    modifier = Modifier.size(250.dp)
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Current Direction Display
                Row(
                    horizontalArrangement = Arrangement.spacedBy(24.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = "Device Heading",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = "${azimuth.toInt()}° $cardinalDirection",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    if (isTrackingDirection && signalDirection != null) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                text = "Signal Direction",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Text(
                                text = "${signalDirection.toInt()}°",
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                }

                if (isTrackingDirection) {
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = "Slowly rotate your device to find the strongest signal",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = TextAlign.Center
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedButton(onClick = onStopTracking) {
                        Icon(Icons.Default.Close, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Stop Tracking")
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Network Selection List
        Text(
            text = "Available Networks",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold
        )

        Spacer(modifier = Modifier.height(8.dp))

        if (networks.isEmpty()) {
            Card(
                modifier = Modifier.fillMaxWidth()
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(32.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "No networks found. Start scanning to see available networks.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = TextAlign.Center
                    )
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(
                    networks.sortedByDescending { it.rssi }
                ) { network ->
                    NetworkTrackCard(
                        network = network,
                        isSelected = selectedNetwork?.bssid == network.bssid,
                        isTracking = isTrackingDirection && selectedNetwork?.bssid == network.bssid,
                        onTrack = { onStartTracking(network) }
                    )
                }
            }
        }
    }
}

@Composable
private fun CompassView(
    azimuth: Float,
    signalDirection: Float?,
    signalStrengthAtDirection: Map<Float, Int>,
    isTracking: Boolean,
    modifier: Modifier = Modifier
) {
    val primaryColor = MaterialTheme.colorScheme.primary
    val surfaceColor = MaterialTheme.colorScheme.surfaceVariant
    val onSurfaceColor = MaterialTheme.colorScheme.onSurface
    val signalColor = Color(0xFF4CAF50)

    Box(
        modifier = modifier,
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val center = Offset(size.width / 2, size.height / 2)
            val radius = size.minDimension / 2 - 20.dp.toPx()

            // Draw compass circle
            drawCircle(
                color = surfaceColor,
                radius = radius,
                center = center
            )

            // Draw compass outline
            drawCircle(
                color = onSurfaceColor.copy(alpha = 0.3f),
                radius = radius,
                center = center,
                style = Stroke(width = 2.dp.toPx())
            )

            // Draw direction markers
            for (i in 0 until 360 step 30) {
                val angle = Math.toRadians(i.toDouble() - 90)
                val isCardinal = i % 90 == 0
                val lineLength = if (isCardinal) 20.dp.toPx() else 10.dp.toPx()

                val startRadius = radius - lineLength
                val startX = center.x + (startRadius * cos(angle)).toFloat()
                val startY = center.y + (startRadius * sin(angle)).toFloat()
                val endX = center.x + (radius * cos(angle)).toFloat()
                val endY = center.y + (radius * sin(angle)).toFloat()

                drawLine(
                    color = onSurfaceColor.copy(alpha = if (isCardinal) 0.8f else 0.4f),
                    start = Offset(startX, startY),
                    end = Offset(endX, endY),
                    strokeWidth = if (isCardinal) 3.dp.toPx() else 1.dp.toPx()
                )
            }

            // Draw signal strength at each direction if tracking
            if (isTracking && signalStrengthAtDirection.isNotEmpty()) {
                signalStrengthAtDirection.forEach { (direction, rssi) ->
                    val normalizedStrength = ((rssi + 100) / 70f).coerceIn(0f, 1f)
                    val signalRadius = radius * 0.3f + (radius * 0.5f * normalizedStrength)
                    val angle = Math.toRadians(direction.toDouble() - 90)

                    val x = center.x + (signalRadius * cos(angle)).toFloat()
                    val y = center.y + (signalRadius * sin(angle)).toFloat()

                    drawCircle(
                        color = signalColor.copy(alpha = 0.6f),
                        radius = 8.dp.toPx(),
                        center = Offset(x, y)
                    )
                }
            }

            // Draw signal direction arrow if available
            signalDirection?.let { direction ->
                val arrowAngle = Math.toRadians(direction.toDouble() - 90)
                val arrowRadius = radius * 0.7f

                val arrowX = center.x + (arrowRadius * cos(arrowAngle)).toFloat()
                val arrowY = center.y + (arrowRadius * sin(arrowAngle)).toFloat()

                drawCircle(
                    color = signalColor,
                    radius = 12.dp.toPx(),
                    center = Offset(arrowX, arrowY)
                )
            }

            // Draw device direction indicator (triangle/arrow)
            rotate(-azimuth, center) {
                val triangleSize = 15.dp.toPx()
                val topY = center.y - radius + 5.dp.toPx()

                val path = androidx.compose.ui.graphics.Path().apply {
                    moveTo(center.x, topY)
                    lineTo(center.x - triangleSize / 2, topY + triangleSize)
                    lineTo(center.x + triangleSize / 2, topY + triangleSize)
                    close()
                }

                drawPath(
                    path = path,
                    color = primaryColor
                )
            }
        }

        // Cardinal Direction Labels
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "N",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.offset(y = (-100).dp)
            )
            Text(
                text = "S",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.offset(y = 100.dp)
            )
            Text(
                text = "E",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.offset(x = 100.dp)
            )
            Text(
                text = "W",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.offset(x = (-100).dp)
            )
        }
    }
}

@Composable
private fun NetworkTrackCard(
    network: WifiNetworkInfo,
    isSelected: Boolean,
    isTracking: Boolean,
    onTrack: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = if (isSelected)
                MaterialTheme.colorScheme.primaryContainer
            else
                MaterialTheme.colorScheme.surface
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Signal Strength Indicator
                SignalStrengthIcon(rssi = network.rssi)

                Column {
                    Text(
                        text = network.ssid,
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.Medium
                    )
                    Text(
                        text = "${network.rssi} dBm • Ch ${network.channel}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            if (isTracking) {
                CircularProgressIndicator(
                    modifier = Modifier.size(24.dp),
                    strokeWidth = 2.dp
                )
            } else {
                IconButton(onClick = onTrack) {
                    Icon(
                        imageVector = Icons.Default.Place,
                        contentDescription = "Track",
                        tint = MaterialTheme.colorScheme.primary
                    )
                }
            }
        }
    }
}

@Composable
private fun SignalStrengthIcon(
    rssi: Int,
    modifier: Modifier = Modifier
) {
    val signalLevel = when {
        rssi >= -50 -> 4
        rssi >= -60 -> 3
        rssi >= -70 -> 2
        rssi >= -80 -> 1
        else -> 0
    }

    val signalColor = when (signalLevel) {
        4 -> Color(0xFF4CAF50)
        3 -> Color(0xFF8BC34A)
        2 -> Color(0xFFFF9800)
        1 -> Color(0xFFFF5722)
        else -> Color(0xFFF44336)
    }

    Icon(
        imageVector = Icons.Default.Check,
        contentDescription = "Signal strength",
        modifier = modifier.size(24.dp),
        tint = signalColor
    )
}

