# Smart Attendance SWM

A professional Android attendance management application built with Kotlin, Jetpack Compose, and modern Android architecture.

## Features

### Attendance Methods
1. **BLE Proximity** - Teacher device scans BLE signals of nearby students.
2. **WiFi Hotspot** - Students connect to teacher hotspot; automatic detection using `ip neigh` and ARP.
3. **QR Scanning** - Secure QR-based attendance marking.

### Management
- **Student Database** - Name, Roll Number, MAC Address, and BLE ID management.
- **Routine Manager** - Manage class routine for automatic subject detection.
- **Reports** - Professional dark-themed reports with PDF/Excel export capabilities.
- **Dashboard** - Modern, premium dark-mode dashboard with live statistics.

### Technical
- **Android 15 Ready** - Support for 16 KB page sizes and SDK 35.
- **Modern UI** - Material 3 with a "Professional Admin" aesthetic (black backgrounds, gradients, 24dp rounded corners).
- **Offline First** - All data stored locally in Room.
- **MVVM Architecture** - Stable state management using StateFlow and flatMapLatest.

## Project Structure

```
app/src/main/java/com/swm/smartattendance/
├── MainActivity.kt
├── SmartAttendanceApp.kt
├── bluetooth/          # BLE Scanner
├── database/           # Room entities, DAOs
├── model/              # Data models
├── qr/                 # QR generation & scanning
├── ui/
│   ├── navigation/
│   ├── screens/
│   └── theme/
├── viewmodel/
├── utils/
└── wifi/               # WiFi detection
```

## Requirements

- Android Studio Koala or newer
- minSdk 26
- targetSdk 35
- Kotlin 1.9+
- Gradle 8.4+

## Permissions

The app requires:
- Camera (QR scanning)
- Bluetooth & Location (BLE scanning)
- WiFi state
- Storage (PDF/Excel export)

## Architecture

- **MVVM** - ViewModels handle business logic and state.
- **Room** - Local database for offline storage.
- **StateFlow** - Reactive UI updates.
- **Compose** - Declarative UI with custom modern components.
