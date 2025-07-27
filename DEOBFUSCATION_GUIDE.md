# Deobfuscation File Guide for FlipQuotes

## Overview
This app now uses R8 minification to reduce app size and obfuscate code. When R8 is enabled, it generates mapping files that are essential for deobfuscating crash reports in Google Play Console.

## What Changed
- **Minification Enabled**: `minifyEnabled true` in the release build type
- **Resource Shrinking**: `shrinkResources true` to remove unused resources
- **ProGuard Rules**: Updated `proguard-rules.pro` with comprehensive rules for:
  - Jetpack Compose components
  - Gson serialization (for Quote data class)
  - OkHttp networking
  - Kotlin coroutines
  - Android lifecycle components

## Mapping File Location
When you build a release APK or AAB, R8 automatically generates mapping files at:
```
app/build/outputs/mapping/release/mapping.txt
```

## How to Generate Mapping Files
1. Build a release version:
   ```bash
   ./gradlew assembleRelease
   ```
   or for App Bundle:
   ```bash
   ./gradlew bundleRelease
   ```

2. The mapping file will be generated at `app/build/outputs/mapping/release/mapping.txt`

## Uploading to Google Play Console
1. When uploading your AAB/APK to Play Console
2. Go to the "App content" section
3. Upload the `mapping.txt` file in the "Deobfuscation files" section
4. This enables readable crash reports and ANR analysis

## Important ProGuard Rules
The app includes specific rules for:
- **Quote Data Class**: Preserved for Gson JSON serialization
- **Compose UI**: Keeps @Stable and @Immutable classes
- **Networking**: Preserves OkHttp and coroutines functionality
- **Debug Info**: Maintains line numbers for crash reports

## Benefits
- **Reduced App Size**: R8 removes unused code and resources
- **Code Obfuscation**: Makes reverse engineering more difficult
- **Better Crash Reports**: Mapping files allow readable stack traces
- **Performance**: Optimized bytecode for better runtime performance

## Testing
After enabling minification, test the app thoroughly to ensure:
- Quotes load correctly from network and cache
- All UI screens function properly
- Settings and preferences work
- No crashes due to over-aggressive obfuscation