# Contributing to WiFi Attack Detector

First off, thank you for considering contributing to WiFi Attack Detector! It's people like you that make this project better for everyone.

## Code of Conduct

By participating in this project, you are expected to uphold our Code of Conduct:

- Be respectful and inclusive
- Be patient and welcoming
- Be constructive in your feedback
- Focus on what is best for the community

## How Can I Contribute?

### Reporting Bugs

Before creating bug reports, please check existing issues to avoid duplicates. When creating a bug report, include:

- **Clear and descriptive title**
- **Steps to reproduce** the behavior
- **Expected behavior** vs **actual behavior**
- **Device information** (Android version, device model)
- **Screenshots** if applicable
- **Logs** if available

Use the bug report template:

```markdown
## Bug Description
[A clear description of the bug]

## Steps to Reproduce
1. Go to '...'
2. Click on '...'
3. See error

## Expected Behavior
[What you expected to happen]

## Actual Behavior
[What actually happened]

## Environment
- Android Version: [e.g., Android 14]
- Device: [e.g., Pixel 7]
- App Version: [e.g., 1.0.0]

## Additional Context
[Any other relevant information]
```

### Suggesting Enhancements

Enhancement suggestions are welcome! Please provide:

- **Clear and descriptive title**
- **Detailed description** of the proposed feature
- **Use case** - why would this be useful?
- **Possible implementation** (optional)

### Pull Requests

1. **Fork** the repository
2. **Create a branch** from `main`:
   ```bash
   git checkout -b feature/your-feature-name
   ```
3. **Make your changes** following our coding standards
4. **Test your changes** thoroughly
5. **Commit** with clear messages:
   ```bash
   git commit -m "Add: Brief description of your changes"
   ```
6. **Push** to your fork:
   ```bash
   git push origin feature/your-feature-name
   ```
7. **Create a Pull Request** against `main`

## Development Setup

### Prerequisites

- Android Studio Hedgehog or newer
- JDK 11 or higher
- Android SDK with API level 24+ installed

### Building the Project

```bash
# Clone your fork
git clone https://github.com/manalejandro/WifiAttack.git
cd WifiAttack

# Build debug APK
./gradlew assembleDebug

# Run tests
./gradlew test

# Run lint checks
./gradlew lint
```

### Project Structure

```
app/src/main/java/com/manalejandro/wifiattack/
├── MainActivity.kt              # Entry point
├── data/model/                  # Data classes
├── service/                     # Background services
├── presentation/                # ViewModels and UI
└── ui/theme/                    # Material 3 theming
```

## Coding Standards

### Kotlin Style Guide

Follow the [Kotlin Coding Conventions](https://kotlinlang.org/docs/coding-conventions.html):

- Use 4 spaces for indentation
- Maximum line length: 120 characters
- Use meaningful variable and function names
- Add KDoc comments for public APIs

### Compose Best Practices

- Keep composables small and focused
- Use `remember` and `derivedStateOf` appropriately
- Follow unidirectional data flow
- Use proper state hoisting

### Example Code Style

```kotlin
/**
 * Displays a WiFi network card with signal strength indicator.
 *
 * @param network The network information to display
 * @param onSelect Callback when the network is selected
 * @param modifier Modifier for this composable
 */
@Composable
fun NetworkCard(
    network: WifiNetworkInfo,
    onSelect: (WifiNetworkInfo) -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        onClick = { onSelect(network) }
    ) {
        // Card content
    }
}
```

### Commit Message Format

Use conventional commits:

- `Add:` New feature
- `Fix:` Bug fix
- `Update:` Update existing feature
- `Refactor:` Code refactoring
- `Docs:` Documentation changes
- `Test:` Adding or updating tests
- `Chore:` Maintenance tasks

Example:
```
Add: Signal direction tracking with compass sensor

- Implement DirectionSensorManager for compass readings
- Add CompassView composable with visual indicator
- Track signal strength at different orientations
```

## Testing

### Running Tests

```bash
# Unit tests
./gradlew test

# Instrumented tests
./gradlew connectedAndroidTest
```

### Writing Tests

- Write unit tests for ViewModels and business logic
- Write UI tests for critical user flows
- Aim for meaningful test coverage

## Review Process

1. All PRs require at least one review
2. CI checks must pass
3. No merge conflicts
4. Code follows our standards

## Questions?

Feel free to open an issue with the "question" label or reach out to the maintainers.

---

Thank you for contributing! 🎉

