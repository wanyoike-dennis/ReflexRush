# Reflex Rush production release checklist

This checklist is for preparing the first public release of Reflex Rush. Keep all release work on `feature/reflex-rush-game` until the release has been reviewed and verified.

## Current release configuration

- Application ID: `com.inventsolutions.reflexrush`
- Display name: `Reflex Rush`
- Minimum Android version: API 24
- Target Android version: API 37
- Initial version: `1.0` (version code `1`)
- Release code and resource optimization: enabled through the Android Gradle Plugin 9.3+ optimization DSL
- CI release APK: unsigned unless all four local signing environment variables are supplied
- Signing keys and passwords must never be committed to Git or uploaded to the repository

Do not increment the version code until a release is actually being prepared for distribution. Every subsequent Play Store update must use a higher version code.

## Create a private release/upload keystore (Windows)

Create the keystore on the release owner's computer. For Google Play App Signing, this can be an upload key; Google Play manages the app-signing key. If distributing APKs directly, preserve the same private signing key for every update.

1. Open a terminal where the JDK `keytool` command is available.
2. Run:

   ```powershell
   keytool -genkeypair -v -keystore "$env:USERPROFILE\reflex-rush-upload.jks" -keyalg RSA -keysize 2048 -validity 10000 -alias reflex-rush-upload
   ```

3. Use a strong unique password and store it in a reputable password manager.
4. Keep at least two secure backups of the keystore. Losing the key can prevent future updates for direct APK distribution; for Play App Signing, losing the upload key requires an upload-key reset.
5. Never send the keystore or passwords in chat, email, screenshots, source control, or CI logs.

The repository ignores `*.jks` and `*.keystore` files, but keep the key outside the project directory regardless.

## Configure local release signing

Before running a signed release build, set these environment variables in the same Windows user/session used by Android Studio or Gradle:

- `REFLEX_RUSH_RELEASE_STORE_FILE`: absolute path to the private `.jks` file
- `REFLEX_RUSH_RELEASE_STORE_PASSWORD`: keystore password
- `REFLEX_RUSH_RELEASE_KEY_ALIAS`: `reflex-rush-upload` (or the alias selected during key creation)
- `REFLEX_RUSH_RELEASE_KEY_PASSWORD`: key password

All four values must be set. If none are set, CI can still build an unsigned release APK for build verification. If only some are set, Gradle stops with a configuration error rather than silently producing an unexpected artifact.

Never put passwords directly in this file or in a tracked Gradle properties file.

## Required verification before publishing

### Automated

- [ ] `testDebugUnitTest` passes.
- [ ] `assembleDebug` passes.
- [ ] `assembleRelease` passes with optimization enabled.
- [ ] A locally signed release build passes after the private signing environment is configured.
- [ ] The signed APK/AAB signature is verified with Android build tools.

### Physical-device checks

- [ ] Install the release build cleanly on a supported device.
- [ ] Upgrade from the previous installed build without losing local progress.
- [ ] Open the app offline and complete rounds in Classic, Time Attack, Survival, and Precision.
- [ ] Verify scoring, danger penalties, missed-target rules, lives, combos, powerups, timers, and replay.
- [ ] Verify audio switches independently control sound effects and music, and preferences survive app restart.
- [ ] Verify pause/resume stops and resumes gameplay/audio correctly.
- [ ] Verify high scores, statistics, daily missions, achievements, XP, and mode records persist after force-stop and restart.
- [ ] Verify backup/device-transfer behaviour on supported Android versions; do not assume a backup has run merely because the XML rules exist.
- [ ] Check layouts, touch targets, system-bar insets, and performance on a small and a larger screen.
- [ ] Check that the release build launches and plays without crashes or missing resources.

### Distribution

- [ ] Decide whether the first distribution channel is Google Play or direct APK distribution.
- [ ] Prepare an accurate store description and screenshots.
- [ ] Publish a privacy policy that accurately describes the app's local data handling and any future data collection.
- [ ] Complete the applicable Play Console declarations, content rating, app access, and testing requirements.
- [ ] Confirm the final version name and a unique, incremented version code.
- [ ] Archive the exact signed artifact and its checksum securely.

## Release rule

Do not publish solely because GitHub Actions is green. Publish only after the signed release artifact has been installed and the physical-device checklist has been completed. Record any intentionally deferred item and its reason before approval.
