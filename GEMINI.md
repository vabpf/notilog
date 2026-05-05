# Notilog Project Instructions

Notilog is an Android application designed to log and manage system notifications, providing a searchable history and category-based organization.

## Architecture
- **Pattern**: MVVM (Model-View-ViewModel) with Clean Architecture principles.
- **Dependency Injection**: Hilt for DI.
- **Local Storage**: Room for notification and blacklist persistence.
- **UI**: Jetpack Compose with Material3.
- **Background Work**: WorkManager for cleanup and backup tasks.

## Tech Stack
- **Language**: Kotlin
- **Build System**: Gradle (Kotlin DSL)
- **Minimum SDK**: 26 (Android 8.0)
- **Target SDK**: 34 (Android 14)

## Development Guidelines

### UI & Icons
- **Standard Icons Only**: Use icons from `androidx.compose.material.icons.filled` that are part of the standard set. Avoid extended icons (e.g., `Block`, `History`, `GridView`) as they are not currently in the dependencies.
  - Use `Icons.Default.Refresh` instead of `History`.
  - Use `Icons.Default.List` instead of `Category` or `GridView`.
  - Use `Icons.Default.Warning` instead of `Block`.
  - Use `Icons.Default.Info` instead of `ContentCopy`.
- **Material3 APIs**: Use `SwipeToDismiss` and `rememberDismissState` for swipe-to-dismiss functionality. `SwipeToDismissBox` is not available in the current project version.
- **AutoMirrored Icons**: Avoid `Icons.AutoMirrored` as it is not supported in the current dependency version. Use standard `Icons.Default` instead.

### Backend & Background Tasks
- **WorkManager**: `NotilogApplication` must implement `Configuration.Provider` by overriding `getWorkManagerConfiguration()` as a **function**, not a property.
- **Concurrency**: Use Coroutines and Flows for asynchronous operations. Always ensure `kotlinx.coroutines.flow.map` is explicitly imported when using the `map` operator on Flows.

### Data Layer
- **DAOs**: Use Flow for observational queries to ensure UI stays in sync with the database.
- **Repositories**: Keep business logic in repositories; ViewModels should only manage UI state and delegate to repositories.

## Performance & Best Practices
- **Compose Stability**: Ensure UI state is stable; use `collectAsStateWithLifecycle` in a real environment (though `collectAsState` is used here for simplicity).
- **Resource Management**: Properly scope CoroutineScopes in `NotilogApplication` and ViewModels.
