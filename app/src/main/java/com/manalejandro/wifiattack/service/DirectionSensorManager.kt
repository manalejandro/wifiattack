package com.manalejandro.wifiattack.service

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * Manages device orientation sensors for tracking signal direction.
 * Uses accelerometer and magnetometer to determine compass heading.
 */
class DirectionSensorManager(context: Context) : SensorEventListener {

    private val sensorManager: SensorManager by lazy {
        context.getSystemService(Context.SENSOR_SERVICE) as SensorManager
    }

    private val accelerometer: Sensor? by lazy {
        sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)
    }

    private val magnetometer: Sensor? by lazy {
        sensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD)
    }

    private val _azimuth = MutableStateFlow(0f)
    val azimuth: StateFlow<Float> = _azimuth.asStateFlow()

    private val _pitch = MutableStateFlow(0f)
    val pitch: StateFlow<Float> = _pitch.asStateFlow()

    private val _roll = MutableStateFlow(0f)
    val roll: StateFlow<Float> = _roll.asStateFlow()

    private val _isAvailable = MutableStateFlow(false)
    val isAvailable: StateFlow<Boolean> = _isAvailable.asStateFlow()

    // Signal direction tracking
    private val _signalDirection = MutableStateFlow<Float?>(null)
    val signalDirection: StateFlow<Float?> = _signalDirection.asStateFlow()

    private val _signalStrengthAtDirection = MutableStateFlow<Map<Float, Int>>(emptyMap())
    val signalStrengthAtDirection: StateFlow<Map<Float, Int>> = _signalStrengthAtDirection.asStateFlow()

    private var lastAccelerometerValues: FloatArray? = null
    private var lastMagnetometerValues: FloatArray? = null

    private val rotationMatrix = FloatArray(9)
    private val orientationAngles = FloatArray(3)

    // For signal tracking
    private val directionReadings = mutableListOf<DirectionReading>()
    private val directionHistoryWindow = 30_000L // 30 seconds

    /**
     * Starts listening to orientation sensors.
     */
    fun startListening() {
        val hasAccelerometer = accelerometer != null
        val hasMagnetometer = magnetometer != null

        _isAvailable.value = hasAccelerometer && hasMagnetometer

        if (hasAccelerometer) {
            sensorManager.registerListener(
                this,
                accelerometer,
                SensorManager.SENSOR_DELAY_UI
            )
        }

        if (hasMagnetometer) {
            sensorManager.registerListener(
                this,
                magnetometer,
                SensorManager.SENSOR_DELAY_UI
            )
        }
    }

    /**
     * Stops listening to sensors.
     */
    fun stopListening() {
        sensorManager.unregisterListener(this)
        lastAccelerometerValues = null
        lastMagnetometerValues = null
    }

    override fun onSensorChanged(event: SensorEvent?) {
        event ?: return

        when (event.sensor.type) {
            Sensor.TYPE_ACCELEROMETER -> {
                lastAccelerometerValues = event.values.clone()
            }
            Sensor.TYPE_MAGNETIC_FIELD -> {
                lastMagnetometerValues = event.values.clone()
            }
        }

        updateOrientation()
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {
        // Not needed for this implementation
    }

    /**
     * Updates orientation values from sensor data.
     */
    private fun updateOrientation() {
        val accelerometerValues = lastAccelerometerValues ?: return
        val magnetometerValues = lastMagnetometerValues ?: return

        val success = SensorManager.getRotationMatrix(
            rotationMatrix,
            null,
            accelerometerValues,
            magnetometerValues
        )

        if (success) {
            SensorManager.getOrientation(rotationMatrix, orientationAngles)

            // Convert to degrees
            val azimuthDegrees = Math.toDegrees(orientationAngles[0].toDouble()).toFloat()
            val pitchDegrees = Math.toDegrees(orientationAngles[1].toDouble()).toFloat()
            val rollDegrees = Math.toDegrees(orientationAngles[2].toDouble()).toFloat()

            // Normalize azimuth to 0-360
            _azimuth.value = (azimuthDegrees + 360) % 360
            _pitch.value = pitchDegrees
            _roll.value = rollDegrees
        }
    }

    /**
     * Records a signal reading at the current direction.
     * @param rssi Signal strength in dBm
     * @param bssid BSSID of the network being tracked
     */
    fun recordSignalReading(rssi: Int, bssid: String) {
        val currentDirection = _azimuth.value
        val currentTime = System.currentTimeMillis()

        directionReadings.add(
            DirectionReading(
                direction = currentDirection,
                rssi = rssi,
                bssid = bssid,
                timestamp = currentTime
            )
        )

        // Remove old readings
        directionReadings.removeAll { currentTime - it.timestamp > directionHistoryWindow }

        // Update signal strength map
        updateSignalStrengthMap(bssid)
    }

    /**
     * Updates the signal strength map for direction analysis.
     */
    private fun updateSignalStrengthMap(bssid: String) {
        val relevantReadings = directionReadings.filter { it.bssid == bssid }

        if (relevantReadings.isEmpty()) return

        // Group readings by direction buckets (every 10 degrees)
        val directionBuckets = relevantReadings.groupBy { reading ->
            ((reading.direction / 10).toInt() * 10).toFloat()
        }.mapValues { (_, readings) ->
            readings.map { it.rssi }.average().toInt()
        }

        _signalStrengthAtDirection.value = directionBuckets

        // Find direction with strongest signal
        val strongestDirection = directionBuckets.maxByOrNull { it.value }
        _signalDirection.value = strongestDirection?.key
    }

    /**
     * Calculates the estimated direction of a signal source.
     * Uses collected readings to triangulate the strongest signal direction.
     * @param bssid The BSSID to track
     * @return Estimated direction in degrees (0-360), or null if insufficient data
     */
    fun calculateSignalDirection(bssid: String): Float? {
        val readings = directionReadings.filter { it.bssid == bssid }

        if (readings.size < 4) return null // Need multiple readings

        // Find the direction range with consistently strongest signal
        val directionBuckets = readings.groupBy { reading ->
            ((reading.direction / 30).toInt() * 30).toFloat()
        }

        val strongestBucket = directionBuckets.maxByOrNull { (_, bucketReadings) ->
            bucketReadings.map { it.rssi }.average()
        }

        return strongestBucket?.key
    }

    /**
     * Clears all recorded signal readings.
     */
    fun clearReadings() {
        directionReadings.clear()
        _signalStrengthAtDirection.value = emptyMap()
        _signalDirection.value = null
    }

    /**
     * Returns the compass heading as a cardinal direction.
     */
    fun getCardinalDirection(): String {
        val azimuth = _azimuth.value
        return when {
            azimuth >= 337.5 || azimuth < 22.5 -> "N"
            azimuth >= 22.5 && azimuth < 67.5 -> "NE"
            azimuth >= 67.5 && azimuth < 112.5 -> "E"
            azimuth >= 112.5 && azimuth < 157.5 -> "SE"
            azimuth >= 157.5 && azimuth < 202.5 -> "S"
            azimuth >= 202.5 && azimuth < 247.5 -> "SW"
            azimuth >= 247.5 && azimuth < 292.5 -> "W"
            azimuth >= 292.5 && azimuth < 337.5 -> "NW"
            else -> "N"
        }
    }
}

/**
 * Data class representing a signal reading at a specific direction.
 */
data class DirectionReading(
    val direction: Float,
    val rssi: Int,
    val bssid: String,
    val timestamp: Long
)

