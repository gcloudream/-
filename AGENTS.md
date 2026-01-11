# Repository Guidelines

## Project Structure & Module Organization
- `silemore-app-legacy-android/` is the Android app (Gradle, Kotlin DSL).
  - `app/src/main/java/` Kotlin source, `app/src/main/res/` Android resources.
  - `app/src/test/java/` JVM unit tests, `app/src/androidTest/java/` instrumentation tests.
- `silemore-app/` is legacy Flutter and not used for current Android-only work.
- `silemore-backend/` is the Java backend (Maven).
  - `src/main/java/` Java sources; add `src/test/java/` when backend tests exist.
- `docs/` contains requirements, API design, and planning documents.

## Build, Test, and Development Commands
Run commands from the repository root unless noted.
- Android build (from `silemore-app-legacy-android/`): `./gradlew :app:assembleDebug` (builds a debug APK).
- Android unit tests (from `silemore-app-legacy-android/`): `./gradlew :app:testDebugUnitTest`.
- Android instrumentation tests (from `silemore-app-legacy-android/`): `./gradlew :app:connectedDebugAndroidTest` (device/emulator required).
- Backend compile: `mvn -f silemore-backend/pom.xml compile`.
- Backend tests: `mvn -f silemore-backend/pom.xml test`.
- Backend package: `mvn -f silemore-backend/pom.xml package`.

## Coding Style & Naming Conventions
- Kotlin/Compose: follow Android Studio defaults; file names `UpperCamelCase.kt`, composables `PascalCase`.
- Android resources: `snake_case` (e.g., `activity_main.xml`, `ic_launcher.png`).
- Java: standard Java conventions; packages lowercase, classes `UpperCamelCase`.
- No formatter/linter config is present; keep style consistent with existing files.

## Testing Guidelines
- Android uses JUnit for unit tests and AndroidX Test/Espresso for instrumentation.
- Name tests with `*Test` suffix (see `ExampleUnitTest` and `ExampleInstrumentedTest`).
- Keep UI tests focused and deterministic; avoid relying on network calls.
- Backend currently has no test framework configured; add JUnit if backend tests are introduced.

## Commit & Pull Request Guidelines
- This workspace has no git history; use clear, imperative commit messages (e.g., "Add login screen").
- PRs should include a short summary, tests run, and screenshots for UI changes.
- Link related issues or specs from `docs/` when applicable.

## Configuration & Secrets
- `silemore-app-legacy-android/local.properties` is machine-specific; keep SDK paths local and do not add secrets.
- Store credentials outside source (environment variables or local config not committed).
