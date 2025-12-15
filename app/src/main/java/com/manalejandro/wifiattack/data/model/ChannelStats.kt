package com.manalejandro.wifiattack.data.model

/**
 * Represents statistics for a specific WiFi channel.
 *
 * @property channel The channel number
 * @property band The frequency band
 * @property networksCount Number of networks on this channel
 * @property averageRssi Average signal strength on this channel
 * @property suspiciousActivityScore Score indicating potential attack activity (0-100)
 * @property deauthPacketCount Estimated deauthentication packet count based on anomalies
 * @property lastUpdateTime Last time this channel was updated
 */
data class ChannelStats(
    val channel: Int,
    val band: WifiBand,
    val networksCount: Int = 0,
    val averageRssi: Int = -100,
    val suspiciousActivityScore: Int = 0,
    val deauthPacketCount: Int = 0,
    val lastUpdateTime: Long = System.currentTimeMillis()
) {
    /**
     * Returns the threat level based on suspicious activity score
     */
    val threatLevel: ThreatLevel
        get() = when {
            suspiciousActivityScore >= 80 -> ThreatLevel.CRITICAL
            suspiciousActivityScore >= 60 -> ThreatLevel.HIGH
            suspiciousActivityScore >= 40 -> ThreatLevel.MEDIUM
            suspiciousActivityScore >= 20 -> ThreatLevel.LOW
            else -> ThreatLevel.NONE
        }

    /**
     * Indicates if this channel has suspicious activity
     */
    val hasSuspiciousActivity: Boolean
        get() = suspiciousActivityScore >= 40
}

/**
 * Enum representing threat levels
 */
enum class ThreatLevel(val displayName: String, val colorValue: Long) {
    NONE("None", 0xFF4CAF50),       // Green
    LOW("Low", 0xFF8BC34A),          // Light Green
    MEDIUM("Medium", 0xFFFF9800),    // Orange
    HIGH("High", 0xFFFF5722),        // Deep Orange
    CRITICAL("Critical", 0xFFF44336) // Red
}

