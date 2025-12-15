package com.manalejandro.wifiattack

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.List
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Place
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.lifecycle.viewmodel.compose.viewModel
import com.manalejandro.wifiattack.presentation.WifiAttackViewModel
import com.manalejandro.wifiattack.presentation.screens.AttacksScreen
import com.manalejandro.wifiattack.presentation.screens.ChannelStatsScreen
import com.manalejandro.wifiattack.presentation.screens.DashboardScreen
import com.manalejandro.wifiattack.presentation.screens.DirectionScreen
import com.manalejandro.wifiattack.ui.theme.WifiAttackTheme

/**
 * Main Activity for the WiFi Attack Detector application.
 * Handles permissions and hosts the main Compose UI.
 */
class MainActivity : ComponentActivity() {

    private val requiredPermissions = mutableListOf(
        Manifest.permission.ACCESS_FINE_LOCATION,
        Manifest.permission.ACCESS_COARSE_LOCATION,
        Manifest.permission.ACCESS_WIFI_STATE,
        Manifest.permission.CHANGE_WIFI_STATE
    ).apply {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            add(Manifest.permission.POST_NOTIFICATIONS)
        }
    }

    private var onPermissionResult: ((Boolean) -> Unit)? = null

    private val permissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        val allGranted = permissions.values.all { it }
        onPermissionResult?.invoke(allGranted)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            WifiAttackTheme {
                val viewModel: WifiAttackViewModel = viewModel()

                var permissionsChecked by remember { mutableStateOf(false) }
                var showRationale by remember { mutableStateOf(false) }

                LaunchedEffect(Unit) {
                    checkAndRequestPermissions { granted ->
                        viewModel.setPermissionsGranted(granted)
                        permissionsChecked = true
                        if (!granted) {
                            showRationale = true
                        }
                    }
                }

                if (showRationale) {
                    PermissionRationaleScreen(
                        onRequestPermission = {
                            showRationale = false
                            requestPermissions { granted ->
                                viewModel.setPermissionsGranted(granted)
                                if (!granted) {
                                    showRationale = true
                                }
                            }
                        }
                    )
                } else {
                    WifiAttackApp(viewModel = viewModel)
                }
            }
        }
    }

    private fun checkAndRequestPermissions(onResult: (Boolean) -> Unit) {
        val notGranted = requiredPermissions.filter {
            ContextCompat.checkSelfPermission(this, it) != PackageManager.PERMISSION_GRANTED
        }

        if (notGranted.isEmpty()) {
            onResult(true)
        } else {
            requestPermissions(onResult)
        }
    }

    private fun requestPermissions(onResult: (Boolean) -> Unit) {
        onPermissionResult = onResult
        permissionLauncher.launch(requiredPermissions.toTypedArray())
    }
}

@Composable
fun PermissionRationaleScreen(
    onRequestPermission: () -> Unit,
    modifier: Modifier = Modifier
) {
    Scaffold { paddingValues ->
        Column(
            modifier = modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(32.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Icon(
                imageVector = Icons.Default.Lock,
                contentDescription = null,
                modifier = Modifier.size(80.dp),
                tint = MaterialTheme.colorScheme.primary
            )

            Spacer(modifier = Modifier.height(24.dp))

            Text(
                text = "Permissions Required",
                style = MaterialTheme.typography.headlineMedium,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "WiFi Attack Detector needs the following permissions to function:",
                style = MaterialTheme.typography.bodyLarge,
                textAlign = TextAlign.Center,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(24.dp))

            PermissionItem(
                icon = Icons.Default.LocationOn,
                title = "Location Access",
                description = "Required to scan WiFi networks (Android requirement)"
            )

            PermissionItem(
                icon = Icons.Default.Settings,
                title = "WiFi Access",
                description = "Required to monitor WiFi network activity"
            )

            Spacer(modifier = Modifier.height(32.dp))

            Button(
                onClick = onRequestPermission,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Grant Permissions")
            }
        }
    }
}

@Composable
private fun PermissionItem(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    title: String,
    description: String,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        horizontalArrangement = Arrangement.spacedBy(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary
        )
        Column {
            Text(
                text = title,
                style = MaterialTheme.typography.titleSmall
            )
            Text(
                text = description,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WifiAttackApp(
    viewModel: WifiAttackViewModel,
    modifier: Modifier = Modifier
) {
    val selectedTab by viewModel.selectedTab.collectAsState()
    val isScanning by viewModel.isScanning.collectAsState()
    val networks by viewModel.networks.collectAsState()
    val channelStats by viewModel.channelStats.collectAsState()
    val attacks by viewModel.attacks.collectAsState()
    val lastScanTime by viewModel.lastScanTime.collectAsState()
    val azimuth by viewModel.azimuth.collectAsState()
    val signalDirection by viewModel.signalDirection.collectAsState()
    val signalStrengthAtDirection by viewModel.signalStrengthAtDirection.collectAsState()
    val isTrackingDirection by viewModel.isTrackingDirection.collectAsState()
    val selectedNetwork by viewModel.selectedNetwork.collectAsState()
    val isSensorAvailable by viewModel.isSensorAvailable.collectAsState()
    val activeAttacksCount by viewModel.activeAttacksCount.collectAsState()
    val highestThreatLevel by viewModel.highestThreatLevel.collectAsState()

    Scaffold(
        modifier = modifier,
        topBar = {
            TopAppBar(
                title = { Text("WiFi Attack Detector") },
                actions = {
                    IconButton(onClick = { viewModel.toggleScanning() }) {
                        Icon(
                            imageVector = if (isScanning) Icons.Default.Close else Icons.Default.PlayArrow,
                            contentDescription = if (isScanning) "Stop monitoring" else "Start monitoring"
                        )
                    }
                }
            )
        },
        bottomBar = {
            NavigationBar {
                NavigationBarItem(
                    icon = { Icon(Icons.Default.Home, contentDescription = null) },
                    label = { Text("Dashboard") },
                    selected = selectedTab == 0,
                    onClick = { viewModel.selectTab(0) }
                )
                NavigationBarItem(
                    icon = {
                        BadgedBox(
                            badge = {
                                if (channelStats.any { it.hasSuspiciousActivity }) {
                                    Badge()
                                }
                            }
                        ) {
                            Icon(Icons.Default.List, contentDescription = null)
                        }
                    },
                    label = { Text("Channels") },
                    selected = selectedTab == 1,
                    onClick = { viewModel.selectTab(1) }
                )
                NavigationBarItem(
                    icon = {
                        BadgedBox(
                            badge = {
                                if (attacks.isNotEmpty()) {
                                    Badge { Text(attacks.size.toString()) }
                                }
                            }
                        ) {
                            Icon(Icons.Default.Warning, contentDescription = null)
                        }
                    },
                    label = { Text("Attacks") },
                    selected = selectedTab == 2,
                    onClick = { viewModel.selectTab(2) }
                )
                NavigationBarItem(
                    icon = { Icon(Icons.Default.Place, contentDescription = null) },
                    label = { Text("Direction") },
                    selected = selectedTab == 3,
                    onClick = { viewModel.selectTab(3) }
                )
            }
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            when (selectedTab) {
                0 -> DashboardScreen(
                    isScanning = isScanning,
                    networksCount = networks.size,
                    activeAttacksCount = activeAttacksCount,
                    highestThreatLevel = highestThreatLevel,
                    channelStats = channelStats,
                    recentAttacks = attacks,
                    lastScanTime = lastScanTime,
                    onToggleScanning = { viewModel.toggleScanning() },
                    onNavigateToChannels = { viewModel.selectTab(1) },
                    onNavigateToAttacks = { viewModel.selectTab(2) }
                )
                1 -> ChannelStatsScreen(
                    channelStats = channelStats,
                    isScanning = isScanning
                )
                2 -> AttacksScreen(
                    attacks = attacks,
                    onClearHistory = { viewModel.clearAttackHistory() }
                )
                3 -> DirectionScreen(
                    networks = networks,
                    azimuth = azimuth,
                    signalDirection = signalDirection,
                    signalStrengthAtDirection = signalStrengthAtDirection,
                    isTrackingDirection = isTrackingDirection,
                    selectedNetwork = selectedNetwork,
                    isSensorAvailable = isSensorAvailable,
                    onStartTracking = { network -> viewModel.startDirectionTracking(network) },
                    onStopTracking = { viewModel.stopDirectionTracking() },
                    cardinalDirection = viewModel.getCardinalDirection()
                )
            }
        }
    }
}