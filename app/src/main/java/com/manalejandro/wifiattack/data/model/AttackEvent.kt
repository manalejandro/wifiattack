package com.manalejandro.wifiattack.data.model

/**
 * Represents an attack detection event.
 *
 * @property id Unique identifier for this attack
 * @property attackType Type of attack detected
 * @property targetBssid BSSID of the target network (if known)
 * @property targetSsid SSID of the target network (if known)
 * @property channel Channel where the attack was detected
 * @property estimatedDirection Estimated direction of the attacker in degrees (0-360)
 * @property signalStrength Signal strength of the attack
 * @property confidence Confidence level of the detection (0-100)
 * @property timestamp When the attack was detected
 * @property isActive Whether the attack is still ongoing
 */
data class AttackEvent(
    val id: String = java.util.UUID.randomUUID().toString(),
    val attackType: AttackType,
    val targetBssid: String? = null,
    val targetSsid: String? = null,
    val channel: Int,
    val estimatedDirection: Float? = null,
    val signalStrength: Int,
    val confidence: Int,
    val timestamp: Long = System.currentTimeMillis(),
    val isActive: Boolean = true
) {
    /**
     * Duration since the attack was first detected
     */
    val durationMs: Long
        get() = System.currentTimeMillis() - timestamp
}

/**
 * Enum representing types of WiFi attacks
 */
enum class AttackType(val displayName: String, val description: String) {
    DEAUTH("Deauthentication", "Forcing devices to disconnect from the network"),
    DISASSOC("Disassociation", "Terminating client associations with access points"),
    EVIL_TWIN("Evil Twin", "Fake access point mimicking a legitimate network"),
    BEACON_FLOOD("Beacon Flood", "Flooding the area with fake access point beacons"),
    PROBE_FLOOD("Probe Flood", "Excessive probe requests from a single source"),
    UNKNOWN("Unknown", "Unidentified suspicious activity")
}

