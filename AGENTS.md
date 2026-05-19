# Notilog - Android Notification History App

## Build & Install

```bash
./gradlew assembleDebug && /mnt/d/Apps/Android/Sdk/platform-tools/adb.exe install -r app/build/outputs/apk/debug/app-debug.apk
```

## Tech Stack
- Kotlin 1.9.22 + Jetpack Compose (Material3)
- Room, Hilt, WorkManager
- Min SDK 26 / Target SDK 34 / JVM 17

## Compose Constraints
- **Icons**: Use `Icons.Default.*` only (no extended icons, no `Icons.AutoMirrored.*`)
- **SwipeToDismiss**: Use `SwipeToDismiss` + `rememberDismissState` (not `SwipeToDismissBox`)
- **Coroutines**: Explicitly import `kotlinx.coroutines.flow.map` when using `map` on Flows

## Architecture
- MVVM + Clean Architecture
- `NotilogApplication` must implement `Configuration.Provider` via `getWorkManagerConfiguration()` as a **function**
- DAOs return `Flow` for reactive queries
- Repositories hold business logic; ViewModels only manage UI state

## Style
- **Primary**: Blue (#3B82F6)
- **Typography**: PlusJakartaSans font family; ExtraBold for display, Bold/SemiBold for headings, Regular for body, Bold for labels
- **Shapes**: 8dp (small), 16dp (medium), 32dp (large)

## Project Structure
- `app/src/main/java/com/notilog/`
  - `data/local/` - Room entities, DAOs, database
  - `di/` - Hilt modules
  - `service/` - NotificationListenerService
  - `ui/` - Screens, ViewModels, theme
  - `worker/` - WorkManager jobs (Cleanup, Backup)
