# ChargeLock ![Build Status](https://img.shields.io/github/actions/workflow/status/uzumaki-ak/ChargeLock/main.yml?branch=main) ![License](https://img.shields.io/github/license/uzumaki-ak/ChargeLock) ![Platform](https://img.shields.io/badge/platform-Android-blue)

---

# 📖 Introduction

ChargeLock is a sophisticated Android application designed to enhance device security by monitoring and alerting users about various disconnection events. Utilizing a combination of hardware and system sensors—including Bluetooth, headphone jack, power connection, and proximity sensors—ChargeLock detects when critical peripherals are unplugged or when the device is moved from a face-down position. Its modular architecture ensures flexible configuration, allowing users to customize which detection methods are active, and integrates biometric authentication for secure alarm dismissal.

Built with modern Android development practices, ChargeLock leverages Kotlin, Jetpack Compose, and Android's native services to provide real-time alerts and robust protection mechanisms. Its clean architecture, combined with comprehensive permission handling and notification management, makes it a reliable tool for safeguarding your device in everyday scenarios.

---

# ✨ Features

- **Multi-Source Detection:** Monitors power cable disconnection, Bluetooth device disconnects, headphone unplug events, and proximity sensor changes.
- **Customizable Settings:** Users can enable/disable specific detection types, set debounce times, and select monitored Bluetooth devices.
- **Secure Alerts:** Triggers alarms with options for biometric authentication to dismiss.
- **Notification Management:** Foreground service notifications ensure persistent monitoring without system interference.
- **Bluetooth Device Management:** View, add, and monitor Bluetooth devices, with real-time connection status updates.
- **Extensible Architecture:** Modular codebase with clear separation of concerns for easy maintenance and feature expansion.
- **Android 12+ Compatibility:** Handles runtime permissions and modern Bluetooth APIs.
- **Notification Channels:** Configurable channels for alarms and service notifications, compliant with Android Oreo+.

---

# 🛠️ Tech Stack

| Library/Tool                      | Purpose                                              | Version / Details                                   |
|----------------------------------|------------------------------------------------------|-----------------------------------------------------|
| **Kotlin**                     | Main programming language                             | 1.8.x (assumed based on code style)                |
| **Android SDK**                | Platform API                                          | Min SDK: 23+ (assumed), Target SDK: 33+             |
| **Jetpack Compose**            | UI toolkit                                            | Version not explicitly specified, likely 1.2+     |
| **AndroidX Lifecycle & Navigation** | UI state management & navigation                   | androidx.navigation.compose, androidx.lifecycle        |
| **NotificationCompat**         | Notification handling                                 | Part of AndroidX                                    |
| **Bluetooth APIs**             | Bluetooth device detection                            | android.bluetooth.*                                |
| **DataStore Preferences**      | Persistent settings storage                           | androidx.datastore:preferences                    |
| **Coroutines**                 | Asynchronous programming                              | kotlinx-coroutines-core                          |
| **JUnit & AndroidX Test**       | Testing framework                                     | androidx.test.*                                   |
| **Hilt / Dagger** (assumed)    | Dependency injection (not explicitly shown)           | Likely used in full project                       |
| **Custom Resources**           | Strings, colors, themes                                | resources in res/values/strings.xml etc.          |

(Note: Exact versions are inferred; please refer to build.gradle files for definitive info.)

---

# 🚀 Quick Start / Installation

To get a local copy up and running, follow these steps:

```bash
git clone https://github.com/uzumaki-ak/ChargeLock (51 files.git
cd ChargeLock
```

**Prerequisites:**

- Android Studio Electric Eel or higher (recommended)
- JDK 17+
- A physical device or emulator running Android 13+

**Build & Run:**

1. Open the project in Android Studio.
2. Sync Gradle dependencies.
3. Connect your device or start an emulator.
4. Run the `app` module on your device.

---

# 📁 Project Structure

```
ChargeLock/
├── app/
│   ├── src/
│   │   ├── main/
│   │   │   ├── java/com/holdon/app/
│   │   │   │   ├── HoldOnApplication.kt        # Application class initializing notification channels
│   │   │   │   ├── MainActivity.kt               # Main UI container with Compose navigation
│   │   │   │   ├── data/
│   │   │   │   │   ├── local/
│   │   │   │   │   │   ├── PreferencesManager.kt # Handles app preferences with DataStore
│   │   │   │   │   │   ├── AlarmSoundManager.kt  # Manages alarm sound URIs
│   │   │   │   │   ├── model/
│   │   │   │   │   │   ├── AlarmType.kt          # Enum of alarm trigger types
│   │   │   │   │   │   ├── BluetoothDeviceInfo.kt # Bluetooth device data class
│   │   │   │   │   │   ├── DetectionSettings.kt    # Detection configuration data class
│   │   │   │   │   ├── repository/
│   │   │   │   │   │   ├── SettingsRepository.kt # Data layer abstraction
│   │   │   │   │   ├── receiver/
│   │   │   │   │   │   ├── AlarmDismissReceiver.kt # Handles alarm dismissal intents
│   │   │   │   │   ├── service/
│   │   │   │   │   │   ├── ProtectionService.kt   # Core background service
│   │   │   │   │   ├── ui/
│   │   │   │   │   │   ├── screens/
│   │   │   │   │   │   │   ├── MainScreen.kt       # Main UI screen
│   │   │   │   │   │   │   ├── SettingsScreen.kt   # Settings UI
│   │   │   │   │   │   ├── theme/
│   │   │   │   │   │   │   └── HoldOnTheme.kt    # UI theme definitions
│   │   │   │   │   └── AndroidManifest.xml             # App configuration & permissions
│   │   │   ├── res/
│   │   │   │   ├── values/
│   │   │   │   │   ├── strings.xml                   # String resources
│   │   │   │   │   └── colors.xml                    # Color definitions
│   │   │   │   ├── drawable/
│   │   │   │   └── AndroidManifest.xml
│   └── build.gradle.kts (or .gradle) for dependencies
└── README.md
```

*Key folders explained:*

- `java/com/holdon/app/` — Core Kotlin source files including app logic, models, services, and receivers.
- `ui/` — Jetpack Compose screens and themes.
- `data/` — Data management, local preferences, Bluetooth info, alarm sounds.
- `service/` — Background monitoring service.
- `receiver/` — Broadcast receivers for alarm dismissal and system events.
- `res/` — Resources including strings, colors, and drawables.

---

# 🔧 Configuration

**Required Environment Variables & Settings:**

- No explicit environment variables are indicated; however, ensure the following permissions are granted at runtime:
  - `WAKE_LOCK`, `FOREGROUND_SERVICE`, `POST_NOTIFICATIONS`, `BLUETOOTH_*`, `USE_BIOMETRIC`, `ACCESS_NOTIFICATION_POLICY`, `READ_EXTERNAL_STORAGE`, `RECEIVE_BOOT_COMPLETED`.

**Notification Channels:**

- Defined in `HoldOnApplication.kt` for:
  - `Constants.NOTIFICATION_CHANNEL_SERVICE_ID` — Foreground service notifications.
  - `Constants.NOTIFICATION_CHANNEL_ALARM_ID` — Alarm notifications.

**Build Variants:**

- Default build targets Android 13+.
- Ensure `minSdkVersion` and `targetSdkVersion` in `build.gradle` are aligned with dependencies and API usage.

---

# 🤝 Contributing

Contributions are welcome! Please fork the repository and submit pull requests via [GitHub](https://github.com/uzumaki-ak/ChargeLock). For issues or feature requests, open an [Issue](https://github.com/uzumaki-ak/ChargeLock/issues). Review the [Contributing Guidelines](https://github.com/uzumaki-ak/ChargeLock/blob/main/CONTRIBUTING.md) for code standards and best practices.

---

# 📄 License

This project is licensed under the MIT License. See the [LICENSE](LICENSE) file for details.

---

# 🙏 Acknowledgments

- Thanks to the Android developer community for open-source libraries and best practices.
- Special appreciation to the contributors and testers who helped refine ChargeLock.
- Icons and resources sourced from free and open repositories.

---

*This README provides a comprehensive overview based on the current code structure, dependencies, and features observed in the ChargeLock project.*