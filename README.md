# WiFi Attack Detector

<p align="center">
  <img src="docs/images/app_icon.png" width="120" alt="WiFi Attack Detector Logo">
</p>

<p align="center">
  <strong>Detect and track WiFi attacks in real-time using your Android device</strong>
</p>

<p align="center">
  <a href="#features">Features</a> •
  <a href="#installation">Installation</a> •
  <a href="#usage">Usage</a> •
  <a href="#how-it-works">How It Works</a> •
  <a href="#contributing">Contributing</a> •
  <a href="#license">License</a>
</p>

---

## Overview

WiFi Attack Detector is an Android application that monitors WiFi network activity to detect potential security threats. It analyzes WiFi scan results to identify suspicious patterns that may indicate various types of attacks, including deauthentication attacks, evil twin attacks, and beacon floods.

## Features

### 🔍 Real-time WiFi Monitoring
- Continuous scanning of nearby WiFi networks
- Detection of network anomalies and suspicious patterns
- Live updates with configurable scan intervals

### 📊 Channel Statistics
- Visual representation of activity per WiFi channel
- Error packet estimation based on network behavior
- Threat level indicators (None, Low, Medium, High, Critical)
- Support for 2.4GHz, 5GHz, and 6GHz bands

### ⚠️ Attack Detection
- **Deauthentication Attacks**: Detects sudden disconnection patterns
- **Evil Twin Attacks**: Identifies duplicate SSIDs on the same channel
- **Beacon Flood**: Detects excessive access point beacons
- **Probe Flood**: Identifies suspicious probe request activity

### 🧭 Signal Direction Tracking
- Uses device compass and accelerometer
- Records signal strength at different orientations
- Estimates the direction of attack sources
- Visual compass display with signal indicators

### 📱 Modern Android UI
- Material Design 3 (Material You)
- Jetpack Compose UI
- Dark/Light theme support
- Clean, intuitive interface

## Screenshots

<p align="center">
  <img src="docs/images/dashboard.png" width="200" alt="Dashboard">
  <img src="docs/images/channels.png" width="200" alt="Channel Stats">
  <img src="docs/images/attacks.png" width="200" alt="Attack History">
  <img src="docs/images/direction.png" width="200" alt="Direction Tracker">
</p>

## Requirements

- Android 7.0 (API level 24) or higher
- WiFi-enabled device
- Location permissions (required for WiFi scanning)
- Compass sensor (optional, for direction tracking)

## Installation

### From Source

1. Clone the repository:
```bash
git clone https://github.com/manalejandro/WifiAttack.git
cd WifiAttack
```

2. Open the project in Android Studio

3. Build and run on your device:
```bash
./gradlew assembleDebug
```

### From Release

Download the latest APK from the [Releases](https://github.com/yourusername/WifiAttack/releases) page.

## Usage

### Getting Started

1. **Grant Permissions**: On first launch, grant location and WiFi permissions when prompted
2. **Start Monitoring**: Tap the play button to begin WiFi scanning
3. **View Dashboard**: Monitor overall network status and threat level
4. **Explore Channels**: Navigate to "Channels" tab for detailed per-channel statistics
5. **Track Attacks**: View detected attacks in the "Attacks" tab
6. **Find Direction**: Use the "Direction" tab to locate signal sources

### Understanding Threat Levels

| Level | Color | Description |
|-------|-------|-------------|
| None | Green | No suspicious activity detected |
| Low | Light Green | Minor anomalies detected |
| Medium | Orange | Moderate suspicious activity |
| High | Deep Orange | Significant threat indicators |
| Critical | Red | Active attack likely in progress |

### Direction Tracking

1. Navigate to the "Direction" tab
2. Select a network to track from the list
3. Slowly rotate your device 360 degrees
4. The app will record signal strength at each direction
5. The strongest signal direction indicates the likely source location

## How It Works

### Detection Methods

WiFi Attack Detector uses heuristic analysis of WiFi scan results to detect potential attacks:

1. **Network Count Anomalies**: Sudden changes in the number of visible networks may indicate beacon flood attacks or deauthentication attempts

2. **RSSI Fluctuations**: Rapid changes in signal strength can indicate jamming or interference attacks

3. **Duplicate SSIDs**: Multiple access points with the same SSID on the same channel may indicate evil twin attacks

4. **Hidden Networks**: Excessive hidden networks can be indicators of attack infrastructure

### Limitations

⚠️ **Important**: This app uses publicly available Android APIs and cannot:
- Capture raw WiFi packets (requires root)
- See actual deauthentication frames
- Monitor encrypted traffic

The app provides **estimates** based on observable network behavior. For comprehensive WiFi security monitoring, consider dedicated hardware solutions.

## Architecture

```
com.manalejandro.wifiattack/
├── MainActivity.kt              # Main activity and navigation
├── data/
│   └── model/
│       ├── WifiNetworkInfo.kt   # Network data model
│       ├── ChannelStats.kt      # Channel statistics model
│       └── AttackEvent.kt       # Attack event model
├── service/
│   ├── WifiScannerService.kt    # WiFi scanning and analysis
│   └── DirectionSensorManager.kt # Compass and direction tracking
├── presentation/
│   ├── WifiAttackViewModel.kt   # Main ViewModel
│   └── screens/
│       ├── DashboardScreen.kt   # Main dashboard
│       ├── ChannelStatsScreen.kt # Channel statistics
│       ├── AttacksScreen.kt     # Attack history
│       └── DirectionScreen.kt   # Signal direction tracker
└── ui/
    └── theme/                   # Material 3 theming
```

## Tech Stack

- **Language**: Kotlin
- **UI Framework**: Jetpack Compose
- **Architecture**: MVVM
- **Minimum SDK**: 24 (Android 7.0)
- **Target SDK**: 36

### Dependencies

- AndroidX Core KTX
- Jetpack Compose (BOM 2024.09.00)
- Material 3 Components
- Lifecycle ViewModel Compose
- Navigation Compose
- Kotlinx Coroutines

## Contributing

Contributions are welcome! Please read our [Contributing Guidelines](CONTRIBUTING.md) before submitting pull requests.

### Development Setup

1. Fork the repository
2. Create a feature branch: `git checkout -b feature/your-feature`
3. Make your changes
4. Run tests: `./gradlew test`
5. Submit a pull request

## Privacy

WiFi Attack Detector:
- Does NOT collect or transmit any user data
- Does NOT require internet connection
- Only uses WiFi scanning for local analysis
- All data remains on your device

See our [Privacy Policy](PRIVACY.md) for more details.

## License

This project is licensed under the Apache License 2.0 - see the [LICENSE](LICENSE) file for details.

```
Copyright 2024 manalejandro

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

    http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
```

## Disclaimer

This application is provided for educational and security research purposes. Users are responsible for ensuring compliance with local laws and regulations regarding WiFi monitoring. The developers are not responsible for any misuse of this application.

## Acknowledgments

- [Material Design 3](https://m3.material.io/) for the design system
- [Jetpack Compose](https://developer.android.com/jetpack/compose) for the modern UI framework
- The Android security research community

---

<p align="center">
  Made with ❤️ for network security awareness
</p>

