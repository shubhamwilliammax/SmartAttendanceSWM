# Smart Attendance SWM

A complete Android attendance management application built with Kotlin, Jetpack Compose, and modern Android architecture.

## Features

### Attendance Methods
1. **Face Recognition** - Camera detects faces, recognized faces automatically mark attendance
2. **BLE Proximity** - Teacher device scans BLE signals of nearby students
3. **WiFi Hotspot** - Students connect to teacher hotspot; MAC address used for attendance
4. **QR Code** - Teacher generates QR; students scan to mark attendance

### Management
- **Student Database** - Name, Roll Number, MAC Address, BLE ID, Face ID
- **Routine Manager** - Upload/manage class routine for auto subject detection
- **Reports** - Export attendance as PDF or Excel

### Technical
- Offline support - All data stored locally in Room
- MVVM Architecture
- Jetpack Compose UI

## Project Structure

```
app/src/main/java/com/swm/smartattendance/
├── MainActivity.kt
├── SmartAttendanceApp.kt
├── bluetooth/          # BLE Scanner
├── database/           # Room entities, DAOs
├── face/               # Face recognition (ML Kit)
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

- Android Studio Hedgehog or newer
- minSdk 26
- targetSdk 34
- Kotlin 1.9+
- Gradle 8.2+

## Build & Run

1. Clone the repository
2. Open in **Android Studio** (Hedgehog or newer)
3. Let Android Studio sync Gradle (it will download the Gradle wrapper if needed)
4. Run on device or emulator

Or from command line (requires Gradle installed to generate wrapper first):
```bash
gradle wrapper
./gradlew assembleDebug
```

## Permissions

The app requires:
- Camera (face recognition, QR scanning)
- Bluetooth & Location (BLE scanning)
- WiFi state
- Storage (PDF/Excel export)

## Architecture

- **MVVM** - ViewModels handle business logic
- **Room** - Local database for offline storage
- **StateFlow** - Reactive UI updates
- **Compose** - Declarative UI

## Notes

- Face recognition uses ML Kit detection; for production, consider adding face embedding for accurate recognition
- WiFi hotspot client list requires manufacturer-specific APIs or root on Android
- Add launcher icons in `res/mipmap-*` for production release
