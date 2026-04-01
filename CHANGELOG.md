# Changelog

All notable changes to this project will be documented in this file.

The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.0.0/),
and this project adheres to [Semantic Versioning](https://semver.org/spec/v2.0.0.html).

## [1.1.0] - 2026-04-01

### Added
- **Workout History**: New history feature to track and view past workouts
  - `WorkoutHistoryActivity` - Activity to display workout history
  - `WorkoutHistoryAdapter` - RecyclerView adapter for history list
  - `WorkoutHistoryRepository` - Repository for managing workout history data
  - `WorkoutHistoryDatabase` - Room database for persistent history storage
  - `WorkoutHistoryDao` - Data Access Object for history queries
  - `WorkoutHistoryEntry` - Data model for history entries
- **Foreground Service**: `TimerForegroundService` - Service to keep timer running in background
- **MVVM Architecture**: `WorkoutTimerViewModel` - ViewModel for managing timer state
- **ViewBinding**: Migrated from `findViewById` to ViewBinding for type-safe view access
- **Landscape Layout Support**: Added landscape layouts for main and settings activities
- **Hungarian Language Support**: Full Hungarian translation (`values-hu/strings.xml`)
- **Locale Helper**: `LocaleHelper` for runtime language switching
- **Warm-up & Cool-down Phases**: Optional warm-up and cool-down phases with configurable durations
- **Sound & Vibration Settings**: Toggle sound and vibration feedback
- **Workout Counter**: Track total completed workouts
- **Unit Tests**: 
  - `WorkoutStateTest` - Tests for workout state logic
  - `WorkoutHistoryEntryTest` - Tests for history entry model
  - `WorkoutPreferencesTest` - Tests for preferences storage
  - `WorkoutHistoryDaoTest` - Tests for database DAO
- **UI Tests**:
  - `MainActivityTest` - Espresso tests for main activity
  - `SettingsActivityTest` - Espresso tests for settings activity
- **Custom UI Components**:
  - `button_primary.xml` - Primary button style
  - `button_secondary.xml` - Secondary button style
  - `card_background.xml` - Card background drawable
- **Data Extraction & Backup Rules**: XML configuration for data backup

### Changed
- **Architecture**: Refactored from simple Activity-based timer to MVVM with Foreground Service
  - Timer logic moved from `MainActivity` to `TimerForegroundService`
  - State management via `WorkoutTimerViewModel` with Kotlin Coroutines/Flow
- **MainActivity**: Complete rewrite using ViewBinding and ViewModel observation
- **SettingsActivity**: Enhanced with new options (warm-up, cool-down, sound, vibration, language)
- **WorkoutPreferences**: Extended with new settings:
  - `totalWorkoutCount` - Total completed workouts counter
  - `isSoundEnabled` / `isVibrationEnabled` - Feedback toggles
  - `language` - Selected language preference
  - `isWarmupEnabled` / `isCooldownEnabled` - Phase toggles
  - `warmupDuration` / `cooldownDuration` - Phase durations
- **WorkoutState Model**: New sealed class/model for timer phases (`PhaseType`)
- **Timer Events**: New `TimerEvent` sealed class for timer state changes
- **Edge-to-Edge Display**: Enabled edge-to-edge UI with proper window insets handling

### Fixed
- Timer continues running when app is in background (via Foreground Service)
- Proper lifecycle handling for timer state
- Memory leaks from ViewBinding (using nullable binding pattern)

### Technical
- Updated Gradle and build configuration
- Added KSP (Kotlin Symbol Processing) for Room database
- Added ProGuard rules for release builds
