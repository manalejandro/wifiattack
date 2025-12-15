package com.manalejandro.wifiattack.service

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.net.wifi.ScanResult
import android.net.wifi.WifiManager
import android.os.Build
import com.manalejandro.wifiattack.data.model.AttackEvent
import com.manalejandro.wifiattack.data.model.AttackType
import com.manalejandro.wifiattack.data.model.ChannelStats
import com.manalejandro.wifiattack.data.model.WifiBand
import com.manalejandro.wifiattack.data.model.WifiNetworkInfo
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/**
 * Service responsible for scanning WiFi networks and detecting attacks.
 * Uses WifiManager to perform scans and analyzes results for suspicious patterns.
 */
class WifiScannerService(private val context: Context) {

    private val wifiManager: WifiManager by lazy {
        context.applicationContext.getSystemService(Context.WIFI_SERVICE) as WifiManager
    }

    private val _networks = MutableStateFlow<List<WifiNetworkInfo>>(emptyList())
    val networks: StateFlow<List<WifiNetworkInfo>> = _networks.asStateFlow()

    private val _channelStats = MutableStateFlow<List<ChannelStats>>(emptyList())
    val channelStats: StateFlow<List<ChannelStats>> = _channelStats.asStateFlow()

    private val _attacks = MutableStateFlow<List<AttackEvent>>(emptyList())
    val attacks: StateFlow<List<AttackEvent>> = _attacks.asStateFlow()

    private val _isScanning = MutableStateFlow(false)
    val isScanning: StateFlow<Boolean> = _isScanning.asStateFlow()

    private val _lastScanTime = MutableStateFlow(0L)
    val lastScanTime: StateFlow<Long> = _lastScanTime.asStateFlow()

    private var scanJob: Job? = null
    private val scope = CoroutineScope(Dispatchers.IO)

    // History for anomaly detection
    private val networkHistory = mutableMapOf<String, MutableList<WifiNetworkInfo>>()
    private val channelHistory = mutableMapOf<Int, MutableList<Int>>() // channel -> network counts

    private val wifiReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            if (intent?.action == WifiManager.SCAN_RESULTS_AVAILABLE_ACTION) {
                val success = intent.getBooleanExtra(WifiManager.EXTRA_RESULTS_UPDATED, false)
                processScanResults(success)
            }
        }
    }

    /**
     * Starts continuous WiFi scanning.
     * @param intervalMs Interval between scans in milliseconds
     */
    fun startScanning(intervalMs: Long = 5000) {
        if (_isScanning.value) return

        _isScanning.value = true
        registerReceiver()

        scanJob = scope.launch {
            while (_isScanning.value) {
                performScan()
                delay(intervalMs)
            }
        }
    }

    /**
     * Stops the scanning process.
     */
    fun stopScanning() {
        _isScanning.value = false
        scanJob?.cancel()
        scanJob = null
        unregisterReceiver()
    }

    /**
     * Performs a single WiFi scan.
     */
    @Suppress("DEPRECATION")
    fun performScan() {
        if (!wifiManager.isWifiEnabled) {
            return
        }
        wifiManager.startScan()
    }

    /**
     * Registers the broadcast receiver for scan results.
     */
    private fun registerReceiver() {
        val filter = IntentFilter(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            context.registerReceiver(wifiReceiver, filter, Context.RECEIVER_NOT_EXPORTED)
        } else {
            context.registerReceiver(wifiReceiver, filter)
        }
    }

    /**
     * Unregisters the broadcast receiver.
     */
    private fun unregisterReceiver() {
        try {
            context.unregisterReceiver(wifiReceiver)
        } catch (e: IllegalArgumentException) {
            // Receiver not registered
        }
    }

    /**
     * Processes the scan results and updates states.
     */
    @Suppress("DEPRECATION")
    private fun processScanResults(success: Boolean) {
        if (!success) return

        val scanResults = try {
            wifiManager.scanResults
        } catch (e: SecurityException) {
            emptyList()
        }

        _lastScanTime.value = System.currentTimeMillis()

        val networkInfoList = scanResults.map { result ->
            WifiNetworkInfo(
                ssid = getSsid(result),
                bssid = result.BSSID ?: "",
                rssi = result.level,
                frequency = result.frequency,
                channel = WifiNetworkInfo.frequencyToChannel(result.frequency),
                capabilities = result.capabilities ?: ""
            )
        }

        _networks.value = networkInfoList

        // Update history
        updateNetworkHistory(networkInfoList)

        // Calculate channel statistics
        val stats = calculateChannelStats(networkInfoList)
        _channelStats.value = stats

        // Detect attacks
        val detectedAttacks = detectAttacks(networkInfoList, stats)
        if (detectedAttacks.isNotEmpty()) {
            val currentAttacks = _attacks.value.toMutableList()
            currentAttacks.addAll(detectedAttacks)
            // Keep only recent attacks (last 100)
            _attacks.value = currentAttacks.takeLast(100)
        }
    }

    /**
     * Gets the SSID from a scan result handling hidden networks.
     */
    @Suppress("DEPRECATION")
    private fun getSsid(result: ScanResult): String {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            result.wifiSsid?.toString()?.removeSurrounding("\"") ?: "<Hidden>"
        } else {
            result.SSID?.takeIf { it.isNotEmpty() } ?: "<Hidden>"
        }
    }

    /**
     * Updates the network history for anomaly detection.
     */
    private fun updateNetworkHistory(networks: List<WifiNetworkInfo>) {
        val currentTime = System.currentTimeMillis()
        val historyWindow = 60_000L // Keep 60 seconds of history

        networks.forEach { network ->
            val history = networkHistory.getOrPut(network.bssid) { mutableListOf() }
            history.add(network)
            // Remove old entries
            history.removeAll { currentTime - it.timestamp > historyWindow }
        }

        // Clean up networks no longer visible
        val currentBssids = networks.map { it.bssid }.toSet()
        networkHistory.keys.toList().forEach { bssid ->
            if (bssid !in currentBssids) {
                val history = networkHistory[bssid]
                if (history != null) {
                    history.removeAll { currentTime - it.timestamp > historyWindow }
                    if (history.isEmpty()) {
                        networkHistory.remove(bssid)
                    }
                }
            }
        }
    }

    /**
     * Calculates statistics for each channel.
     */
    private fun calculateChannelStats(networks: List<WifiNetworkInfo>): List<ChannelStats> {
        val channelGroups = networks.groupBy { it.channel }

        return channelGroups.map { (channel, channelNetworks) ->
            val count = channelNetworks.size
            val avgRssi = channelNetworks.map { it.rssi }.average().toInt()
            val band = channelNetworks.firstOrNull()?.band ?: WifiBand.UNKNOWN

            // Update channel history
            val history = channelHistory.getOrPut(channel) { mutableListOf() }
            history.add(count)
            if (history.size > 12) { // Keep last 12 scans (~1 minute at 5s interval)
                history.removeAt(0)
            }

            // Calculate suspicious activity score
            val suspiciousScore = calculateSuspiciousScore(channel, channelNetworks)
            val deauthCount = estimateDeauthPackets(channel, channelNetworks)

            ChannelStats(
                channel = channel,
                band = band,
                networksCount = count,
                averageRssi = avgRssi,
                suspiciousActivityScore = suspiciousScore,
                deauthPacketCount = deauthCount
            )
        }.sortedBy { it.channel }
    }

    /**
     * Calculates a suspicious activity score for a channel.
     */
    private fun calculateSuspiciousScore(channel: Int, networks: List<WifiNetworkInfo>): Int {
        var score = 0

        // Check for sudden network count changes
        val history = channelHistory[channel] ?: return 0
        if (history.size >= 2) {
            val currentCount = networks.size
            val previousCount = history[history.size - 2]
            val variance = kotlin.math.abs(currentCount - previousCount)

            // Large sudden changes indicate potential beacon flood or deauth
            if (variance >= 5) score += 30
            else if (variance >= 3) score += 15
        }

        // Check for RSSI fluctuations (potential jamming)
        networks.forEach { network ->
            val networkHist = networkHistory[network.bssid] ?: return@forEach
            if (networkHist.size >= 2) {
                val rssiVariance = networkHist.takeLast(5).map { it.rssi }
                    .zipWithNext { a, b -> kotlin.math.abs(a - b) }
                    .maxOrNull() ?: 0

                if (rssiVariance >= 20) score += 20
                else if (rssiVariance >= 10) score += 10
            }
        }

        // Check for suspicious network names (Evil Twin indicators)
        val ssidGroups = networks.groupBy { it.ssid.lowercase() }
        ssidGroups.forEach { (_, similarNetworks) ->
            if (similarNetworks.size > 1) {
                // Multiple networks with same SSID on same channel - potential evil twin
                score += similarNetworks.size * 10
            }
        }

        // Check for hidden networks (often used in attacks)
        val hiddenCount = networks.count { it.ssid == "<Hidden>" }
        if (hiddenCount > 2) score += hiddenCount * 5

        return score.coerceIn(0, 100)
    }

    /**
     * Estimates deauthentication packet count based on network behavior.
     */
    @Suppress("UNUSED_PARAMETER")
    private fun estimateDeauthPackets(channel: Int, networks: List<WifiNetworkInfo>): Int {
        var estimatedPackets = 0

        networks.forEach { network ->
            val history = networkHistory[network.bssid] ?: return@forEach
            if (history.size >= 3) {
                // Look for sudden disappearance patterns
                val rssiValues = history.takeLast(5).map { it.rssi }
                val suddenDrops = rssiValues.zipWithNext().count { (prev, curr) ->
                    prev - curr > 15 // Sudden drop in signal
                }
                estimatedPackets += suddenDrops * 10

                // Count rapid fluctuations
                val fluctuations = rssiValues.zipWithNext().count { (a, b) ->
                    kotlin.math.abs(a - b) > 10
                }
                estimatedPackets += fluctuations * 5
            }
        }

        return estimatedPackets
    }

    /**
     * Detects potential attacks based on scan results and channel stats.
     */
    private fun detectAttacks(
        networks: List<WifiNetworkInfo>,
        stats: List<ChannelStats>
    ): List<AttackEvent> {
        val attacks = mutableListOf<AttackEvent>()

        // Check each channel for attack indicators
        stats.filter { it.suspiciousActivityScore >= 50 }.forEach { channelStat ->
            val channelNetworks = networks.filter { it.channel == channelStat.channel }

            val attackType = when {
                channelStat.deauthPacketCount > 50 -> AttackType.DEAUTH
                channelNetworks.groupBy { it.ssid }.any { it.value.size > 2 } -> AttackType.EVIL_TWIN
                channelStat.networksCount > 20 -> AttackType.BEACON_FLOOD
                channelStat.suspiciousActivityScore >= 70 -> AttackType.UNKNOWN
                else -> null
            }

            attackType?.let { type ->
                val targetNetwork = channelNetworks.maxByOrNull { it.rssi }
                attacks.add(
                    AttackEvent(
                        attackType = type,
                        targetBssid = targetNetwork?.bssid,
                        targetSsid = targetNetwork?.ssid,
                        channel = channelStat.channel,
                        signalStrength = targetNetwork?.rssi ?: -100,
                        confidence = channelStat.suspiciousActivityScore
                    )
                )
            }
        }

        return attacks
    }

    /**
     * Clears all collected data and history.
     */
    fun clearData() {
        _networks.value = emptyList()
        _channelStats.value = emptyList()
        _attacks.value = emptyList()
        networkHistory.clear()
        channelHistory.clear()
    }

    /**
     * Returns the WiFi enabled state.
     */
    fun isWifiEnabled(): Boolean = wifiManager.isWifiEnabled
}

