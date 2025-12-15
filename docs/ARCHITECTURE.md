# Architecture Overview

## Application Architecture

WiFi Attack Detector follows the MVVM (Model-View-ViewModel) architecture pattern with clean separation of concerns.

```
┌─────────────────────────────────────────────────────────────────┐
│                         Presentation Layer                       │
│  ┌─────────────────┐     ┌──────────────────────────────────┐   │
│  │  MainActivity   │────▶│      Composable Screens          │   │
│  └────────┬────────┘     │  ┌──────────┐  ┌──────────────┐  │   │
│           │              │  │Dashboard │  │ChannelStats  │  │   │
│           ▼              │  └──────────┘  └──────────────┘  │   │
│  ┌─────────────────┐     │  ┌──────────┐  ┌──────────────┐  │   │
│  │ WifiAttack      │────▶│  │ Attacks  │  │  Direction   │  │   │
│  │   ViewModel     │     │  └──────────┘  └──────────────┘  │   │
│  └────────┬────────┘     └──────────────────────────────────┘   │
│           │                                                      │
└───────────┼──────────────────────────────────────────────────────┘
            │
            ▼
┌───────────────────────────────────────────────────────────────────┐
│                         Service Layer                             │
│  ┌─────────────────────────┐    ┌─────────────────────────────┐  │
│  │   WifiScannerService    │    │  DirectionSensorManager     │  │
│  │  ┌───────────────────┐  │    │  ┌───────────────────────┐  │  │
│  │  │ WiFi Scanning     │  │    │  │ Accelerometer         │  │  │
│  │  │ Attack Detection  │  │    │  │ Magnetometer          │  │  │
│  │  │ Channel Analysis  │  │    │  │ Direction Calculation │  │  │
│  │  └───────────────────┘  │    │  └───────────────────────┘  │  │
│  └─────────────────────────┘    └─────────────────────────────┘  │
└───────────────────────────────────────────────────────────────────┘
            │
            ▼
┌───────────────────────────────────────────────────────────────────┐
│                         Data Layer                                │
│  ┌──────────────────┐  ┌───────────────┐  ┌──────────────────┐   │
│  │  WifiNetworkInfo │  │  ChannelStats │  │   AttackEvent    │   │
│  └──────────────────┘  └───────────────┘  └──────────────────┘   │
└───────────────────────────────────────────────────────────────────┘
```

## Component Details

### Presentation Layer

#### MainActivity
- Entry point of the application
- Handles permission requests
- Sets up Jetpack Compose content
- Provides ViewModel to composables

#### WifiAttackViewModel
- Central ViewModel managing all application state
- Exposes StateFlows for UI consumption
- Coordinates between WiFi scanning and direction tracking
- Handles user actions and navigation

#### Screens
- **DashboardScreen**: Overview with status, threat level, and quick stats
- **ChannelStatsScreen**: Detailed per-channel analysis with filtering
- **AttacksScreen**: Attack history with categorization
- **DirectionScreen**: Compass-based signal direction tracker

### Service Layer

#### WifiScannerService
- Manages WiFi scanning using `WifiManager`
- Registers `BroadcastReceiver` for scan results
- Analyzes scan results for anomalies
- Calculates channel statistics
- Detects potential attacks using heuristics

```kotlin
// Key Detection Heuristics
- Network count changes (beacon flood)
- RSSI fluctuations (jamming)
- Duplicate SSIDs (evil twin)
- Hidden network patterns
```

#### DirectionSensorManager
- Uses `SensorManager` for compass data
- Combines accelerometer and magnetometer readings
- Calculates device orientation (azimuth, pitch, roll)
- Records signal strength at different orientations
- Estimates signal source direction

### Data Layer

#### WifiNetworkInfo
```kotlin
data class WifiNetworkInfo(
    val ssid: String,
    val bssid: String,
    val rssi: Int,
    val frequency: Int,
    val channel: Int,
    val capabilities: String,
    val timestamp: Long
)
```

#### ChannelStats
```kotlin
data class ChannelStats(
    val channel: Int,
    val band: WifiBand,
    val networksCount: Int,
    val averageRssi: Int,
    val suspiciousActivityScore: Int,
    val deauthPacketCount: Int,
    val lastUpdateTime: Long
)
```

#### AttackEvent
```kotlin
data class AttackEvent(
    val id: String,
    val attackType: AttackType,
    val targetBssid: String?,
    val targetSsid: String?,
    val channel: Int,
    val estimatedDirection: Float?,
    val signalStrength: Int,
    val confidence: Int,
    val timestamp: Long,
    val isActive: Boolean
)
```

## Data Flow

```
┌──────────────┐     ┌───────────────────┐     ┌──────────────────┐
│ WifiManager  │────▶│ WifiScannerService│────▶│ StateFlow<List>  │
│ (Android OS) │     │ (Analysis Engine) │     │ (Reactive State) │
└──────────────┘     └───────────────────┘     └────────┬─────────┘
                                                        │
                                                        ▼
┌──────────────┐     ┌───────────────────┐     ┌──────────────────┐
│ SensorManager│────▶│DirectionSensor    │────▶│ StateFlow<Float> │
│ (Android OS) │     │      Manager      │     │   (Azimuth)      │
└──────────────┘     └───────────────────┘     └────────┬─────────┘
                                                        │
                                                        ▼
                     ┌───────────────────┐     ┌──────────────────┐
                     │ WifiAttackViewModel│◀───│ Collect as State │
                     └─────────┬─────────┘     └──────────────────┘
                               │
                               ▼
                     ┌───────────────────┐
                     │ Composable UI     │
                     │ (Recomposition)   │
                     └───────────────────┘
```

## Attack Detection Algorithm

### Suspicious Activity Score Calculation

```
Score = 0

IF network_count_variance >= 5:
    Score += 30
ELSE IF network_count_variance >= 3:
    Score += 15

FOR each network:
    IF rssi_variance >= 20:
        Score += 20
    ELSE IF rssi_variance >= 10:
        Score += 10

FOR each ssid_group:
    IF same_ssid_count > 1:
        Score += same_ssid_count * 10

hidden_count = COUNT(hidden_networks)
IF hidden_count > 2:
    Score += hidden_count * 5

RETURN CLAMP(Score, 0, 100)
```

### Attack Type Classification

| Condition | Attack Type |
|-----------|-------------|
| deauth_packets > 50 | Deauthentication |
| same_ssid_networks > 2 | Evil Twin |
| network_count > 20 | Beacon Flood |
| suspicious_score >= 70 | Unknown |

## Direction Tracking Algorithm

1. **Collect Readings**: Record RSSI at each device orientation
2. **Bucket Grouping**: Group readings into 10° buckets
3. **Average Calculation**: Calculate average RSSI per bucket
4. **Direction Estimation**: Find bucket with highest average RSSI
5. **Smoothing**: Apply moving average for stability

```
direction_buckets = GROUP_BY(readings, direction / 10 * 10)
bucket_averages = MAP(direction_buckets, AVERAGE(rssi))
estimated_direction = MAX_KEY(bucket_averages)
```

## Threading Model

```
Main Thread (UI)
    │
    ├── Compose Recomposition
    ├── User Input Handling
    │
    ▼
ViewModel Scope (viewModelScope)
    │
    ├── State Flow Collection
    ├── Coordination Logic
    │
    ▼
IO Dispatcher (Dispatchers.IO)
    │
    ├── WiFi Scanning Loop
    ├── Broadcast Receiver Callbacks
    │
    ▼
Default Dispatcher
    │
    └── Sensor Callbacks (SensorManager)
```

## Future Improvements

1. **Root Detection Mode**: Access `/proc/net/wireless` for actual error counts
2. **Machine Learning**: Train model on attack patterns
3. **Persistent Storage**: Room database for historical analysis
4. **Background Service**: Foreground service for continuous monitoring
5. **Notifications**: Alert system for detected attacks
6. **Export Functionality**: Export logs for external analysis

