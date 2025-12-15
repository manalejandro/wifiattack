package com.manalejandro.wifiattack.data.model

/**
 * Represents information about a detected WiFi network.
 *
 * @property ssid The network SSID (name)
 * @property bssid The network BSSID (MAC address)
 * @property rssi Signal strength in dBm
 * @property frequency Frequency in MHz
 * @property channel WiFi channel number
 * @property capabilities Security capabilities string
 * @property timestamp Time when this network was detected
 */
data class WifiNetworkInfo(
    val ssid: String,
    val bssid: String,
    val rssi: Int,
    val frequency: Int,
    val channel: Int,
    val capabilities: String,
    val timestamp: Long = System.currentTimeMillis()
) {
    /**
     * Returns the WiFi band (2.4GHz, 5GHz, or 6GHz)
     */
    val band: WifiBand
        get() = when {
            frequency in 2400..2500 -> WifiBand.BAND_2_4GHz
            frequency in 5150..5875 -> WifiBand.BAND_5GHz
            frequency in 5925..7125 -> WifiBand.BAND_6GHz
            else -> WifiBand.UNKNOWN
        }

    /**
     * Returns signal strength as a percentage (0-100)
     */
    val signalStrengthPercent: Int
        get() = when {
            rssi >= -50 -> 100
            rssi >= -60 -> 80
            rssi >= -70 -> 60
            rssi >= -80 -> 40
            rssi >= -90 -> 20
            else -> 0
        }

    companion object {
        /**
         * Converts frequency to channel number
         */
        fun frequencyToChannel(frequency: Int): Int {
            return when {
                frequency in 2412..2484 -> (frequency - 2412) / 5 + 1
                frequency in 5170..5825 -> (frequency - 5170) / 5 + 34
                frequency in 5955..7115 -> (frequency - 5955) / 5 + 1
                else -> 0
            }
        }
    }
}

/**
 * Enum representing WiFi frequency bands
 */
enum class WifiBand(val displayName: String) {
    BAND_2_4GHz("2.4 GHz"),
    BAND_5GHz("5 GHz"),
    BAND_6GHz("6 GHz"),
    UNKNOWN("Unknown")
}

