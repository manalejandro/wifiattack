# Development Setup Guide

## Prerequisites

### Java Version
This project requires **Java 17-21** for building. Java 25+ is not yet supported by Gradle 8.7.

If you have multiple Java versions installed, set `JAVA_HOME` before building:

```bash
export JAVA_HOME=/usr/lib/jvm/java-21-openjdk-amd64
./gradlew assembleDebug
```

### Android SDK
- Minimum SDK: 24 (Android 7.0)
- Target SDK: 35 (Android 15)
- Compile SDK: 35

### Required SDK Components
- Android SDK Platform 35
- Android SDK Build-Tools
- Android Emulator (optional, for testing)

## Building the Project

### Debug Build
```bash
./gradlew assembleDebug
```

The APK will be located at:
`app/build/outputs/apk/debug/app-debug.apk`

### Release Build
```bash
./gradlew assembleRelease
```

Note: Release builds require signing configuration.

## Running Tests

### Unit Tests
```bash
./gradlew test
```

### Instrumented Tests
```bash
./gradlew connectedAndroidTest
```

## IDE Setup

### Android Studio
1. Open Android Studio
2. Select "Open an existing project"
3. Navigate to the project directory
4. Wait for Gradle sync to complete

### IntelliJ IDEA
1. Open IntelliJ IDEA
2. Select "Import Project"
3. Choose the `build.gradle.kts` file
4. Select "Open as Project"

## Troubleshooting

### Build Fails with Java Version Error
If you see an error related to Java version (e.g., "25.0.1"):
1. Check your Java version: `java --version`
2. If using Java 25+, switch to Java 21
3. Set `JAVA_HOME` to point to Java 21

### Gradle Sync Issues
1. Clear Gradle caches: `./gradlew clean`
2. Invalidate caches in Android Studio: File > Invalidate Caches
3. Re-sync the project

### Missing SDK Components
Install required components through:
- Android Studio SDK Manager
- Or command line: `sdkmanager "platforms;android-35"`

