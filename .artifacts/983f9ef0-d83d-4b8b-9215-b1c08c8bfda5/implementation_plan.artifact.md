# Update FlipQuotes to target Android 16 (API 36)

This plan outlines the steps to upgrade the FlipQuotes app to target Android 16 (API level 36), ensuring compatibility with the latest platform features and behavior changes.

## User Review Required

> [!IMPORTANT]
> The upgrade involves bumping the Android Gradle Plugin (AGP) and Kotlin versions. This may require a project sync and potentially updating Android Studio to the latest version (Meerkat or higher) to support API 36.

## Proposed Changes

### Build Configuration

#### [MODIFY] [build.gradle](file:///C:/Users/amris/AndroidStudioProjects/FlipQuotes/build.gradle)
- Upgrade Android Gradle Plugin from `8.7.3` to `9.3.2`.
- Upgrade Kotlin version from `2.1.20` to `2.4.10`.

#### [MODIFY] [app/build.gradle](file:///C:/Users/amris/AndroidStudioProjects/FlipQuotes/app/build.gradle)
- Update `compileSdk` to `36`.
- Update `targetSdk` to `36`.
- Update Kotlin Compose plugin version to `2.4.10`.
- Remove legacy `kotlinCompilerExtensionVersion` as it's now managed by the Kotlin Compose plugin.
- Update `compose-bom` to `2026.08.00`.
- Update `androidx.activity:activity-compose` to `1.13.0`.
- Update `androidx.compose.material3:material3` to `1.4.0`.

### Manifest and UI

#### [MODIFY] [AndroidManifest.xml](file:///C:/Users/amris/AndroidStudioProjects/FlipQuotes/app/src/main/AndroidManifest.xml)
- Add `android:enableOnBackInvokedCallback="true"` to the `<application>` tag to opt-in to Predictive Back animations, which are default for apps targeting API 36.

#### [MODIFY] [MainActivity.kt](file:///C:/Users/amris/AndroidStudioProjects/FlipQuotes/app/src/main/java/com/app/codebuzz/flipquotes/MainActivity.kt)
- Add `enableEdgeToEdge()` call in `onCreate` to ensure the app correctly handles edge-to-edge enforcement on all supported versions, especially since API 36 disables the opt-out mechanism.

## Verification Plan

### Automated Tests
- Run `./gradlew assembleDebug` to ensure the project builds with the new SDK and plugin versions.
- Run `./gradlew lint` to check for any new API compatibility issues.

### Manual Verification
- Deploy the app to an Android 16 emulator (if available) to verify edge-to-edge rendering and predictive back behavior.
- Verify that the UI remains consistent across different screen sizes, keeping in mind the new adaptive layout defaults for large screens (sw600dp+).
