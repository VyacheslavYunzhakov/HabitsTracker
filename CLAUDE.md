# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Build & Test Commands

```bash
# Build
./gradlew assembleDebug
./gradlew assembleRelease

# Unit tests
./gradlew test

# Instrumented tests (requires device/emulator)
./gradlew connectedAndroidTest

# Single module test
./gradlew :core:data:test
./gradlew :feature:home:test

# Lint
./gradlew lint
```

## Architecture Overview

The project is a Habits Tracker app built with **Kotlin Multiplatform (KMP)**. All modules are KMP modules targeting Android and iOS.

### Module Map

| Module | Type | Purpose |
|---|---|---|
| `:app` | Android app | Entry point, wires Koin modules, hosts Navigation |
| `:feature:home` | **KMP** | Home screen — Compose Multiplatform with Voyager navigation |
| `:core:domain` | **KMP** | Domain interfaces, models (`Habit`, `HabitDay`, `HabitStatus`) |
| `:core:data` | **KMP** | Data layer with Room via expect/actual for cross-platform DB factory |
| `:core:designsystem` | **KMP** | Compose Multiplatform shared UI components |
| `:shared` | **KMP** | General shared KMP module with Voyager navigation |

KMP modules target: `androidTarget`, `iosArm64`, `iosSimulatorArm64`.

### Layer Architecture

```
UI (Compose) → ViewModel → HabitInteractor (UseCase) → HabitRepository → Room DAOs
```

- **Domain layer** owns interfaces (`HabitRepository`, `HabitInteractor`) and models (`Habit`, `HabitDay`).
- **Data layer** provides implementations (`HabitRepositoryImpl`) with entity↔model mappers.
- **Feature layer** holds ViewModels and Composable screens.
- `Dispatchers.Default` is injected at the Interactor layer (not in ViewModel or Repository).

### Dependency Injection

Koin 4.x with three modules chained: `dataModule` → `domainModule` → `viewModelModule`. In KMP modules the DI modules live in `commonMain/kotlin/.../di/`. Platform-specific bindings (e.g., `androidContext()` for the Room builder) are provided in `androidMain`.

### Database

Room 2.8.4. `core/data` uses a `DatabaseFactory` with `expect`/`actual` to construct the Room database on each platform.

### KMP Patterns

- `expect`/`actual` is used for platform-specific classes (e.g., `DatabaseFactory`, `Platform`).
- `kotlinx-datetime` is used instead of `java.time` everywhere in KMP modules.
- Voyager (`cafe.adriel.voyager`) handles navigation inside KMP/shared modules.

### State Management

ViewModels expose a single `StateFlow<UiState>` where `UiState` is an `@Immutable` data class. Events are sent via `suspend` functions or `Channel`s; no event wrapper pattern.

## Key Versions

See `gradle/libs.versions.toml` for the canonical version catalog.

- Kotlin: 2.4.0
- AGP: 9.1.1
- Compose BOM: 2026.05.01
- Koin: 4.2.2
- Room: 2.8.4
- Coroutines: 1.11.0
- kotlinx-datetime: 0.8.0
- Min SDK: 26 / Target SDK: 37 / Java: 11