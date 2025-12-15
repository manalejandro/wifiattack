package com.manalejandro.wifiattack.presentation

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.manalejandro.wifiattack.data.model.AttackEvent
import com.manalejandro.wifiattack.data.model.ChannelStats
import com.manalejandro.wifiattack.data.model.WifiNetworkInfo
import com.manalejandro.wifiattack.service.DirectionSensorManager
import com.manalejandro.wifiattack.service.WifiScannerService
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

/**
 * Main ViewModel for the WiFi Attack Detector application.
 * Manages WiFi scanning, attack detection, and signal direction tracking.
 */
class WifiAttackViewModel(application: Application) : AndroidViewModel(application) {

    private val wifiScanner = WifiScannerService(application)
    private val directionSensor = DirectionSensorManager(application)

    // WiFi Scanner states
    val networks: StateFlow<List<WifiNetworkInfo>> = wifiScanner.networks
    val channelStats: StateFlow<List<ChannelStats>> = wifiScanner.channelStats
    val attacks: StateFlow<List<AttackEvent>> = wifiScanner.attacks
    val isScanning: StateFlow<Boolean> = wifiScanner.isScanning
    val lastScanTime: StateFlow<Long> = wifiScanner.lastScanTime

    // Direction sensor states
    val azimuth: StateFlow<Float> = directionSensor.azimuth
    val signalDirection: StateFlow<Float?> = directionSensor.signalDirection
    val signalStrengthAtDirection: StateFlow<Map<Float, Int>> = directionSensor.signalStrengthAtDirection
    val isSensorAvailable: StateFlow<Boolean> = directionSensor.isAvailable

    // UI State
    private val _selectedTab = MutableStateFlow(0)
    val selectedTab: StateFlow<Int> = _selectedTab.asStateFlow()

    private val _selectedNetwork = MutableStateFlow<WifiNetworkInfo?>(null)
    val selectedNetwork: StateFlow<WifiNetworkInfo?> = _selectedNetwork.asStateFlow()

    private val _isTrackingDirection = MutableStateFlow(false)
    val isTrackingDirection: StateFlow<Boolean> = _isTrackingDirection.asStateFlow()

    private val _permissionsGranted = MutableStateFlow(false)
    val permissionsGranted: StateFlow<Boolean> = _permissionsGranted.asStateFlow()

    // Computed states
    val activeAttacksCount: StateFlow<Int> = attacks
        .combine(MutableStateFlow(Unit)) { attacks, _ ->
            attacks.count { it.isActive }
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

    val highestThreatLevel: StateFlow<String> = channelStats
        .combine(MutableStateFlow(Unit)) { stats, _ ->
            stats.maxByOrNull { it.suspiciousActivityScore }?.threatLevel?.displayName ?: "None"
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), "None")

    /**
     * Starts WiFi scanning and sensor monitoring.
     */
    fun startMonitoring() {
        if (!_permissionsGranted.value) return

        wifiScanner.startScanning()
        directionSensor.startListening()
    }

    /**
     * Stops all monitoring activities.
     */
    fun stopMonitoring() {
        wifiScanner.stopScanning()
        directionSensor.stopListening()
        _isTrackingDirection.value = false
    }

    /**
     * Toggles the scanning state.
     */
    fun toggleScanning() {
        if (isScanning.value) {
            stopMonitoring()
        } else {
            startMonitoring()
        }
    }

    /**
     * Sets the permissions granted state.
     */
    fun setPermissionsGranted(granted: Boolean) {
        _permissionsGranted.value = granted
    }

    /**
     * Selects a network for detailed view or tracking.
     */
    fun selectNetwork(network: WifiNetworkInfo?) {
        _selectedNetwork.value = network
    }

    /**
     * Starts tracking the direction of a specific network's signal.
     */
    fun startDirectionTracking(network: WifiNetworkInfo) {
        _selectedNetwork.value = network
        _isTrackingDirection.value = true
        directionSensor.clearReadings()

        // Start recording signal readings for this network
        viewModelScope.launch {
            networks.collect { currentNetworks ->
                if (_isTrackingDirection.value) {
                    val trackedNetwork = currentNetworks.find { it.bssid == network.bssid }
                    trackedNetwork?.let {
                        directionSensor.recordSignalReading(it.rssi, it.bssid)
                    }
                }
            }
        }
    }

    /**
     * Stops direction tracking.
     */
    fun stopDirectionTracking() {
        _isTrackingDirection.value = false
        _selectedNetwork.value = null
    }

    /**
     * Calculates the estimated direction of the selected network.
     */
    fun getEstimatedDirection(): Float? {
        val network = _selectedNetwork.value ?: return null
        return directionSensor.calculateSignalDirection(network.bssid)
    }

    /**
     * Gets the current compass direction as a cardinal direction.
     */
    fun getCardinalDirection(): String = directionSensor.getCardinalDirection()

    /**
     * Changes the selected tab.
     */
    fun selectTab(index: Int) {
        _selectedTab.value = index
    }

    /**
     * Clears all attack history.
     */
    fun clearAttackHistory() {
        wifiScanner.clearData()
        directionSensor.clearReadings()
    }

    /**
     * Returns WiFi enabled status.
     */
    fun isWifiEnabled(): Boolean = wifiScanner.isWifiEnabled()

    /**
     * Cleans up resources when the ViewModel is cleared.
     */
    override fun onCleared() {
        super.onCleared()
        stopMonitoring()
    }
}

